package com.p2p.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * A Wrapper class for the Seller
 * For now, seller just needs to start the server and block until shutdown
 * */
public class Seller extends PeerImpl {
    private Product product;
    private int amount;
    private final int stock;
    private final Server server;
    private static final Random RANDOM = new Random();

    private static final Logger logger = Logger.getLogger(Seller.class.getName());
    public Seller(int id, String IPAddress, int port, int KNeighbors, Product product, int amount) {
        super(id, IPAddress, port, KNeighbors);
        this.product = product;
        this.amount =amount;
        this.stock = amount;
        this.server =
                ServerBuilder.forPort(port).addService(new MarketplaceSellerImpl()).executor(Executors.newFixedThreadPool(10)).build();
    }

    /**
     * Server side code for the Seller
     * The Seller has somewhat different implementation because
     * (1) only the Seller can sell - the buyRPC server code should only for the server
     * (2) The lookupRPC in the Seller will decide whether to propagate or to reply!
     * */
    private class MarketplaceSellerImpl extends MarketPlaceGrpc.MarketPlaceImplBase {
        @Override
        public void lookupRPC(BuyRequest request, StreamObserver<PeerId> streamObserver) {
            logger.info("Receive lookup request at " + Seller.this.getPort());
            // propagate the lookup or return a reply
            // if the seller is selling the product
            if (request.getProduct().equals(product.name())) {
                logger.info("Reply to lookup request");
                streamObserver.onNext(PeerId.newBuilder().setId(Seller.this.getId()).setIPAddress(Seller.this.getIPAddress()).setPort(Seller.this.getPort()).build());
            } else if (request.getHopCount() == 1) {
                // if hopCount is 1 then cannot flood further
                streamObserver.onNext(PeerId.newBuilder().setId(-1).build());
            } else {
                List<PeerId> sellerList = lookup(request.getProduct(), request.getHopCount() - 1);
                sellerList.forEach(streamObserver::onNext);
            }
            streamObserver.onCompleted();
        }

        @Override
        public synchronized void buyRPC(PeerId seller, StreamObserver<Ack> streamObserver) {
            logger.info("Receive a buy request at " + Seller.this.getPort());
            processBuy();
            streamObserver.onNext(Ack.newBuilder().setMessage("Ack Sell").build());
            streamObserver.onCompleted();
            logger.info("Finish a transaction. Current have " + amount);
        }
    }

    // this method will need to be synchronized!
    // decrement the amountSell and restock :)
    private void processBuy() {
        // decrement the count
        amount -= 1;
        if (amount == 0) {
            logger.info(product.name() + " runs out!!!! Restocking");
            // randomize and restock!
            product = Product.values()[RANDOM.nextInt(Product.values().length - 1)];
            amount = stock;
            logger.info("After randomize a new product and restock, now selling " + product.name());
        }
    }

    @Override
    public void startServer() {
        try {
            this.server.start();
            logger.info("Starting a server at " + this.getIPAddress() + " " + this.getPort());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void blockUntilShutdown() {
        try {
            this.server.awaitTermination();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {
        // args: id, port, product, amount, neighborId, neighborPort
        // test case 1 and 3
        Seller seller = new Seller(Integer.parseInt(args[0]),"localhost", Integer.parseInt(args[1]), 1,
                Product.valueOf(args[2].toUpperCase()), Integer.parseInt(args[3]));
        PeerId peer1 =
                PeerId.newBuilder().setId(Integer.parseInt(args[4])).setIPAddress("localhost").setPort(Integer.parseInt(args[5])).build();
        PeerId peer2 =
                PeerId.newBuilder().setId(Integer.parseInt(args[6])).setIPAddress("localhost").setPort(Integer.parseInt(args[7])).build();
        seller.addNeighbor(peer1);
        seller.addNeighbor(peer2);
        seller.startServer();
        seller.blockUntilShutdown();
    }
}

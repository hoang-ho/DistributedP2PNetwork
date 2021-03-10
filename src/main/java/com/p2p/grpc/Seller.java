package com.p2p.grpc;

import com.p2p.utils.Pair;
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
    private static Random RANDOM = new Random(0);


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
        public void lookupRPC(LookUpRequest request, StreamObserver<PeerId> streamObserver) {
            logger.info("Receive lookup request at " + Seller.this.getPort() + " from peer " + request.getId() + " " +
                    " for product " + request.getProduct());
            // propagate the lookup or return a reply
            // if the seller is selling the product
            if (request.getProduct().equals(product.name())) {
                logger.info("Reply to lookup request from peer " + request.getId() + " for product " + request.getProduct());
                streamObserver.onNext(PeerId.newBuilder().setId(Seller.this.getId()).setIPAddress(Seller.this.getIPAddress()).setPort(Seller.this.getPort()).build());
            } else if (request.getHopCount() == 1) {
                // if hopCount is 1 then cannot flood further
                logger.info("Invalidate lookup request from peer " + request.getId() + " for product" +
                        " " + request.getProduct());
                streamObserver.onNext(PeerId.newBuilder().setId(-1).build());
            } else {
                logger.info("Flood Lookup request from peer " + request.getId() + " for product " + request.getProduct());
                Pair<Integer, String> lookupSender = new Pair<>(request.getId(), request.getProduct());
                Seller.this.lookupSenderList.add(lookupSender);
                List<PeerId> sellerList = Seller.this.lookup(request.getProduct(), request.getHopCount() - 1);
                Seller.this.lookupSenderList.remove(lookupSender);
                sellerList.forEach(streamObserver::onNext);
                logger.info("Sent back results for lookup request from peer " + request.getId() + " for product " + request.getProduct());
            }
            streamObserver.onCompleted();
        }

        @Override
        public synchronized void buyRPC(BuyRequest buyRequest, StreamObserver<Ack> streamObserver) {
            logger.info("Receive a buy request at " + Seller.this.getPort() + " from peer " + buyRequest.getId() + " " +
                    "for product " + buyRequest.getProduct());
            if (Seller.this.product.name().equals(buyRequest.getProduct())) {
                processBuy();
                streamObserver.onNext(Ack.newBuilder().setMessage("Ack Sell").build());
                streamObserver.onCompleted();
                logger.info("Finish a transaction for peer " + buyRequest.getId() + ". Currently, having: " + amount);
            } else {
                streamObserver.onNext(Ack.newBuilder().setMessage("Out of Stock").build());
                streamObserver.onCompleted();
                logger.info("Product out of stock! Now selling " + Seller.this.product.name());
            }

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
            int val = RANDOM.nextInt(4);
            product = Product.values()[val];
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

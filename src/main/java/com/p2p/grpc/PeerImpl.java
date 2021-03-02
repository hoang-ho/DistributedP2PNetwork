package com.p2p.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class PeerImpl implements Peer{
    private int amountBuy;
    private int amountSell;
    private Product productName;
    private PeerReference neighbor;
    private final String IPAddress;
    private final int port;
    private int id;
    private final int stock;
    private final Server server;
    private static final Random RANDOM = new Random();

    private static final Logger logger = Logger.getLogger(PeerImpl.class.getName());

    public PeerImpl(int id, String IPAddress, int port, Product product, int amountSell, int amountBuy) {
        this.id = id;
        this.IPAddress = IPAddress;
        this.port = port;
        this.amountBuy = amountBuy;
        this.productName = product;
        this.amountSell = amountSell;
        this.stock = amountSell;
        this.server =
                ServerBuilder.forPort(port).addService(new MarketPlaceImpl()).executor(Executors.newFixedThreadPool(10)).build();
    }

    /**
     * Implementation lookup interface
     * This lookup will do a lookupRPC call to all its neighbors
     * The return from the lookupRPC is a PeerId, which is a reference to the seller
     * */
    @Override
    public PeerId lookup(BuyRequest request) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(this.getNeighborAddress(),
                this.getNeighborPort()).usePlaintext().build();
        // Wait for the server to start!
        MarketPlaceGrpc.MarketPlaceBlockingStub stub = MarketPlaceGrpc.newBlockingStub(channel).withWaitForReady();
        logger.info("Send a lookup request to " + this.getNeighborAddress() + " " + this.getNeighborPort());
        return stub.lookupRPC(request);
    }

    /**
     * This doesn't do anything for now
     * */
    @Override
    public PeerId reply(PeerId peerId) {
        return null;
    }

    /**
     * Nothing going on here! Only buyer can buy
     * */
    @Override
    public void buy(PeerId peerId) { }

    public void startServer() {
        try {
            server.start();
            logger.info("Starting a server at " + IPAddress + " " + port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void blockUntilShutdown() {
        try {
            server.awaitTermination();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Server side code for each RPC call
     * Both Buyer and Seller have the server.
     * Buyer will be the server for lookup and reply.
     * Seller will be the server for lookup and buy.
     * All RPC calls return an acknowledge message immediately, except for the buy RPC which blocks until the buy
     * completes
     * */
    private final class MarketPlaceImpl extends MarketPlaceGrpc.MarketPlaceImplBase {
        @Override
        public void lookupRPC(BuyRequest request, StreamObserver<PeerId> streamObserver) {
            logger.info("Receive lookup request at " + port);
            // propagate the lookup or return a reply
            streamObserver.onNext(lookupHelper(request));
            streamObserver.onCompleted();
        }

        @Override
        public void buyRPC(PeerId seller, StreamObserver<Ack> streamObserver) {
            logger.info("Receive a buy request at " + port);
            processBuy();
            streamObserver.onNext(Ack.newBuilder().setMessage("Ack Sell").build());
            streamObserver.onCompleted();
            logger.info("Finish a transaction. Current have " + amountSell);
        }
    }

    private PeerId lookupHelper(BuyRequest request) {
        // if we are the buyer and we are selling the same product
        if (request.getHopCount() > 0 && amountSell > 0 && request.getProduct().equals(productName.name())) {
            // reply
            PeerId seller = PeerId.newBuilder().setId(this.id).setIPAddress(this.IPAddress).setPort(this.port).build();
            return seller;
        }
        // No flooding for now!
//        else if (request.getHopCount() > 1) {
//            // else call the lookup to its neighbor, i.e., flooding
//            BuyRequest newRequest =
//                    BuyRequest.newBuilder().setProductName(request.getProductName()).setHopCount(request.getHopCount() - 1).build();
//            return this.lookup(newRequest);
//        }
        // this is to signify not found!!!
        return PeerId.newBuilder().setId(-1).build();
    }


    // this method will need to be synchronized!
    // decrement the amountSell and restock :)
    private void processBuy() {
        // decrement the count
        amountSell -= 1;
        if (amountSell == 0) {
            logger.info(productName.name() + " runs out!!!! Restocking");
            // randomize and restock!
            productName = Product.values()[RANDOM.nextInt(Product.values().length) - 1];
            amountSell = stock;
            logger.info("After randomize a new product and restock, now selling " + productName);
        }
    }

    public void setNeighbor(PeerReference peer) {
        this.neighbor = peer;
    }

    public void incrementBuy(Ack message) {
        if (message.getMessage().equals("Ack Sell")) {
            amountBuy += 1;
        }
    }

    public int getAmountBuy() {
        return amountBuy;
    }

    public int getId() { return id; }

    public int getPort() {
        return port;
    }

    public String getNeighborAddress() {
        return neighbor.getIPAddress();
    }

    public int getNeighborPort() {
        return neighbor.getPort();
    }

    public Product getProduct() {
        return productName;
    }
}

package com.p2p.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Buyer extends PeerImpl{
    // a logger to log what happens
    private Product product;
    private Map<Product, Integer> buyItems;
    private static final Random RANDOM = new Random(0);
    private static final Logger logger = Logger.getLogger(Buyer.class.getName());
    private List<PeerId> potentialSellers;
    private Server server;
    private int hopCount;

    public Buyer(int id, String IPAddress, int port, int KNeighbors, Product product) {
        super(id, IPAddress, port, KNeighbors);
        this.product = product;
        this.buyItems = new ConcurrentHashMap<>();
        this.potentialSellers = Collections.synchronizedList(new ArrayList<>());
        this.server =
                ServerBuilder.forPort(port).addService(new MarketPlaceBuyerImpl()).executor(Executors.newFixedThreadPool(KNeighbors + 1)).build();
    }

    /**
     * Implementation lookup interface for the buyer
     * This lookup will do a lookupRPC call to all its neighbors
     * */
    @Override
    public void lookup(String product, int hopCount) {
        LookUpRequest request =
                LookUpRequest.newBuilder().setBuyer(this.getId()).setProduct(product).setHopCount(hopCount).addPath(this.getId()).build();

        Thread[] lookupThread = new Thread[this.getNumberNeighbor()];
        int counter = 0;

        for (PeerId neighbor: this.getAllNeighbors().values()){
            lookupThread[counter] = new Thread(() -> {
                // Open a new channel for
                ManagedChannel channel = ManagedChannelBuilder.forAddress(neighbor.getIPAddress(),
                        neighbor.getPort()).usePlaintext().build();
                // Wait for the server to start!
                MarketPlaceGrpc.MarketPlaceBlockingStub stub = MarketPlaceGrpc.newBlockingStub(channel).withWaitForReady();
                logger.info("Send a lookup request to peer " + neighbor.getId() + " at port " + neighbor.getIPAddress() + " " +
                        + neighbor.getPort() + " for product " + product);

                stub.lookupRPC(request);
                channel.shutdown();
            });
            lookupThread[counter].start();
            counter++;
        }

        for (int i = 0; i < counter; i++) {
            try {
                lookupThread[i].join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Implementation for the buy
     * @param sellerId the sellerId reference. We use this to establish a direct connection to the seller
     * */
    @Override
    public synchronized void buy(PeerId sellerId) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(sellerId.getIPAddress(),
                sellerId.getPort()).usePlaintext().build();
        try {
            // Wait for the server to start!
            MarketPlaceGrpc.MarketPlaceBlockingStub stub = MarketPlaceGrpc.newBlockingStub(channel).withWaitForReady();
            logger.info( "Send a buy request to peer " + sellerId.getId());
            BuyRequest request =
                    BuyRequest.newBuilder().setId(Buyer.this.getId()).setProduct(Buyer.this.product.name()).build();
            Ack message = stub.buyRPC(request);
            if (message.getMessage().equals("Ack Sell")) {
                buyItems.put(product, buyItems.getOrDefault(product, 0) + 1);
                logger.info("Bought " + this.product.name() + " from peer " + sellerId.getId() + ". Buyer current has" +
                        " " + buyItems.get(this.product) + " " + this.product.name());

            } else {
                logger.info("Buy unsuccessful!");
            }
        } finally {
            channel.shutdown();
        }
    }

    /**
     * A class that extends PeerImpl.MarketPlaceImpl to override replyRPC
     * Need to check if this Buyer is the original sender of the lookup request
     * */
    private class MarketPlaceBuyerImpl extends PeerImpl.MarketPlaceImpl {
        @Override
        public void replyRPC(ReplyRequest request, StreamObserver<Empty> streamObserver) {
            logger.info("Receive a reply request at " + Buyer.this.getId() + " size of path " + request.getPathCount());
            streamObserver.onNext(Empty.newBuilder().build());
            streamObserver.onCompleted();
            if (request.getPathCount() == 0 ) {
                logger.info("Request Product: " + request.getProduct());
                logger.info("Buyer Product " + Buyer.this.product.name());
                if (request.getProduct().equals(Buyer.this.product.name())) {
                    logger.info("Add the seller to the list of sellers");
                    Buyer.this.potentialSellers.add(request.getSellerId());
                }
                // discard reply
            } else {
                // continue down the path
                logger.info("Continue down the path for reply");
                reverseReply(request);
            }
        }
    }

    public void startServer() {
        try {
            server.start();
            logger.info("Starting a server at " + this.getIPAddress() + " " + this.getIPAddress());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        this.startServer();

        // buyer keeps buying products forever
        // buyer keeps sending out lookup request
        while (true) {
            // buyer perform lookup
            Thread t = new Thread(() -> {
                logger.info("Currently buying " + this.product.name());
                this.lookup(this.product.name(), this.hopCount);

                try {
                    Thread.sleep(1000); // sleep a little bit
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (this.potentialSellers.size() > 0 ) {
                int index = RANDOM.nextInt(this.potentialSellers.size());
                PeerId seller = this.potentialSellers.get(index);
                this.potentialSellers.clear();
                this.buy(seller);
            }

            logger.info("Choose a new product to buy");
            this.product = Product.values()[RANDOM.nextInt(3)];
            logger.info("Now buying " + this.product.name());

        }
    }

    public void setProduct(String product) {
        this.product = Product.valueOf(product.toUpperCase());
    }

}

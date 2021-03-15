package com.p2p.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * A Wrapper class for the Seller
 * For now, seller just needs to start the server and block until shutdown
 * */
public class Seller extends PeerImpl {
    private Product product;
    private int amount;
    private int stock;
    private Server server;
    private static Random RANDOM = new Random(0);
    private Map<Integer, List<Integer>> replyPath;

    private static final Logger logger = Logger.getLogger(Seller.class.getName());
    public Seller(int id, String IPAddress, int port, int KNeighbors, Product product, int amount) {
        super(id, IPAddress, port, KNeighbors);
        this.product = product;
        this.amount =amount;
        this.stock = amount;
        this.replyPath = new ConcurrentHashMap<>();
        this.server =
                ServerBuilder.forPort(port).addService(new MarketplaceSellerImpl()).executor(Executors.newFixedThreadPool(KNeighbors + 1)).build();
    }


    /**
     * Reply request from Seller to Buyer
     * */
    @Override
    public void reply(int buyerId, PeerId seller) {
        List<Integer> path = replyPath.get(buyerId);
        int previousNodeId = path.remove(path.size() - 1);
        PeerId previousNode = this.getNeighbor(previousNodeId);
        ManagedChannel channel = ManagedChannelBuilder.forAddress(previousNode.getIPAddress(), previousNode.getPort()).usePlaintext().build();
        MarketPlaceGrpc.MarketPlaceBlockingStub stub = MarketPlaceGrpc.newBlockingStub(channel).withWaitForReady();
        logger.info("Send a reply request to peer " + previousNode.getId() + " at port " + previousNode.getIPAddress() + " " +
                    + previousNode.getPort() + " for product " + product);
        ReplyRequest replyRequest =
                ReplyRequest.newBuilder().setSellerId(seller).setProduct(this.product.name()).addAllPath(path).build();
        logger.info("Reply path size " + replyRequest.getPathCount());
        stub.replyRPC(replyRequest);
        channel.shutdown();
        logger.info("Done Reply");
        replyPath.remove(buyerId);
    }

    /**
     * Server side code for the Seller
     * The Seller has somewhat different implementation because
     * (1) only the Seller can sell - the buyRPC server code should only for the server
     * (2) The lookupRPC in the Seller will decide whether to propagate or to reply!
     * */
    private class MarketplaceSellerImpl extends PeerImpl.MarketPlaceImpl {
        /**
         * This would returns an ack empty message and check if it can reply.
         * If the Seller isn't selling the product, it call a helper function to flood the request
         * */
        @Override
        public void lookupRPC(LookUpRequest request, StreamObserver<Empty> streamObserver) {
            logger.info("Receive lookup request at " + Seller.this.getPort() + " from peer " + request.getBuyer() + " " +
                    " for product " + request.getProduct());
            streamObserver.onNext(Empty.newBuilder().build());
            streamObserver.onCompleted();

            // propagate the lookup or return a reply
            // if the seller is selling the product
            if (request.getProduct().equals(product.name())) {
                logger.info("Reply to lookup request from peer " + request.getBuyer() + " for product " + request.getProduct());
                replyPath.put(request.getBuyer(), new ArrayList<>(request.getPathList()));
                PeerId seller =
                        PeerId.newBuilder().setId(Seller.this.getId()).setIPAddress(Seller.this.getIPAddress()).setPort(Seller.this.getPort()).build();
                Seller.this.reply(request.getBuyer(), seller);
            } else if (request.getHopCount() == 1) {
                // if hopCount is 1 then cannot flood further
                logger.info("Invalidate lookup request from peer " + request.getBuyer() + " for product" +
                        " " + request.getProduct());
            } else {
                logger.info("Flood Lookup request from peer " + request.getBuyer() + " for product " + request.getProduct());
                floodLookUp(request);
            }
        }

        /**
         * THis is a synchronized method so only one thread can access it at a time
         * It first verifies again that the buy request is for the product it currently has
         * If the buy product is invalid, then it returns an error message
         * */
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
            product = Product.values()[RANDOM.nextInt(3)];
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

    public void setProduct(String product) {
        this.product = Product.valueOf(product.toUpperCase());
    }

    public void run() {
        this.startServer();
        this.blockUntilShutdown();
    }

}

package com.p2p.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
    private int stock;
    private Server server;
    private static Random RANDOM = new Random(0);
    private List<Integer> replyPath;

    private static final Logger logger = Logger.getLogger(Seller.class.getName());
    public Seller(int id, String IPAddress, int port, int KNeighbors, Product product, int amount) {
        super(id, IPAddress, port, KNeighbors);
        this.product = product;
        this.amount =amount;
        this.stock = amount;
        this.replyPath = new ArrayList<>();
        this.server =
                ServerBuilder.forPort(port).addService(new MarketplaceSellerImpl()).executor(Executors.newFixedThreadPool(2 * KNeighbors)).build();
    }

    public Seller(int id, int KNeighbors) {
        super(id, KNeighbors);
        this.replyPath = new ArrayList<>();
    }

    /**
     * Reply request from Seller to Buyer
     * */
    @Override
    public void reply(PeerId buyer, PeerId seller) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(buyer.getIPAddress(),
                    buyer.getPort()).usePlaintext().build();
        MarketPlaceGrpc.MarketPlaceBlockingStub stub = MarketPlaceGrpc.newBlockingStub(channel).withWaitForReady();
        logger.info("Send a reply request to peer " + buyer.getId() + " at port " + buyer.getIPAddress() + " " +
                    + buyer.getPort() + " for product " + product);
        ReplyRequest replyRequest =
                ReplyRequest.newBuilder().setSellerId(seller).setProduct(this.product.name()).addAllPath(replyPath).build();
        logger.info("Reply path size " + replyRequest.getPathCount());
        stub.replyRPC(replyRequest);
        channel.shutdown();
        logger.info("Done Reply");
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
            logger.info("Receive lookup request at " + Seller.this.getPort() + " from peer " + request.getFromNode() + " " +
                    " for product " + request.getProduct());
            streamObserver.onNext(Empty.newBuilder().build());
            streamObserver.onCompleted();

            // propagate the lookup or return a reply
            // if the seller is selling the product
            if (request.getProduct().equals(product.name())) {
                logger.info("Reply to lookup request from peer " + request.getFromNode() + " for product " + request.getProduct());
                synchronized (this) {
                    replyPath.addAll(request.getPathList());
                    PeerId fromNode = Seller.this.getNeighbor(replyPath.remove(replyPath.size() - 1));
                    PeerId seller =
                            PeerId.newBuilder().setId(Seller.this.getId()).setIPAddress(Seller.this.getIPAddress()).setPort(Seller.this.getPort()).build();
                    Seller.this.reply(fromNode, seller);
                    replyPath.clear();
                }

            } else if (request.getHopCount() == 1) {
                // if hopCount is 1 then cannot flood further
                logger.info("Invalidate lookup request from peer " + request.getFromNode() + " for product" +
                        " " + request.getProduct());
            } else {
                logger.info("Flood Lookup request from peer " + request.getFromNode() + " for product " + request.getProduct());
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

    public void run() throws IOException {
        FileInputStream fstream = new FileInputStream("Config.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
        String strLine;
        while ((strLine = br.readLine()) != null)   {
            // Print the content on the console
            String[] vals = strLine.split(" ");
            if (vals[0].equals("N") || vals[0].equals("K")) {
                continue;
            }
            if (Integer.parseInt(vals[0]) ==  this.getId()) {
                this.setPort(Integer.parseInt(vals[2]));
                this.setProduct(vals[4]);
                this.stock = Integer.parseInt(vals[5]);
                this.amount = stock;
                for (int i = 6; i < vals.length; i+=3) {
                    PeerId neighbor =
                            PeerId.newBuilder().setIPAddress("localhost").setId(Integer.parseInt(vals[i])).setPort(Integer.parseInt(vals[i+2])).build();
                    this.addNeighbor(neighbor);
                }
                break;
            }
        }
        this.server =
                ServerBuilder.forPort(this.getPort()).addService(new MarketplaceSellerImpl()).executor(Executors.newFixedThreadPool(2 * this.getNumberNeighbor())).build();

        this.startServer();
        this.blockUntilShutdown();
    }


    public static void main(String[] args) {
        // args: id, port, product, amount, neighborId, neighborPort
        // test case 1 and 3
        Seller seller = new Seller(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        try {
            seller.run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

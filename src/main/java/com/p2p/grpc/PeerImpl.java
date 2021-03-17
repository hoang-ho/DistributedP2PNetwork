package com.p2p.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class PeerImpl implements Peer{

    private Map<Integer, PeerId> neighbors;
    private String IPAddress;
    private int port;
    private int id;
    private Server server;
    public String lookupRPCLatency ;
    public String replyRPCLatency;
    public String buyRPCLatency;
    public String lookupResponseTime;

    private static final Logger logger = Logger.getLogger(PeerImpl.class.getName());

    public PeerImpl(int id, String IPAddress, int port, int KNeighbor) {
        this.id = id;
        this.IPAddress = IPAddress;
        this.port = port;
        this.neighbors = new HashMap<>();
        this.server =
                ServerBuilder.forPort(port).addService(new MarketPlaceImpl()).executor(Executors.newFixedThreadPool(KNeighbor + 1)).build();
        this.lookupRPCLatency = "lookupRPCLatency_" + id + ".txt";
        this.replyRPCLatency = "replyRPCLatency_" + id + ".txt";
        this.buyRPCLatency = "buyRPCLatency_" + id + ".txt";
        this.lookupResponseTime = "lookupResponseTime_" + id + ".txt";
    }

    /**
     * Lookup implemented in the Buyer
     * */
    @Override
    public void lookup(String product, int hopCount) {
    }

    /**
     * Reply implemented in Seller
     * */
    @Override
    public void reply(int buyerId, PeerId seller) {
    }

    /**
     * Buy implemented in Buyer
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
     * The Seller will extends this class to override the lookupRPC and the buyRPC
     * The Buyer will override this implementation for replyRPC
     * */
    class MarketPlaceImpl extends MarketPlaceGrpc.MarketPlaceImplBase {
        /**
         * The lookupRPC will acks to the request immediately after it receives the request
         * Then the lookupRPC will check if it can further flood the request
         * */
        @Override
        public void lookupRPC(LookUpRequest request, StreamObserver<Empty> streamObserver) {
            logger.info("Receive lookup request at " + port + " from peer " + request.getBuyer() + " for product " + request.getProduct());
            // propagate the lookup or return a reply
            streamObserver.onNext(Empty.newBuilder().build());
            streamObserver.onCompleted();
            if (request.getHopCount() == 1) {
                // cancel the request
                logger.info("Invalidate lookup request from peer " + request.getBuyer() + " for product" +
                        " " + request.getProduct());
            } else {
                // Flood the lookup request
                logger.info("Flood Lookup request from peer " + request.getBuyer() + " for product " + request.getProduct());
                floodLookUp(request);
            }
        }

        /**
         * The replyRPC will acks to the request immediately after it receives the request
         * Then the replyRPC further continue down the reverse path
         * */
        @Override
        public void replyRPC(ReplyRequest request, StreamObserver<Empty> streamObserver) {
            // Traverse the reverse path
            logger.info("Receive Reply request at " + PeerImpl.this.id + ". Size of reverse path " + request.getPathCount());
            logger.info("Continue down the path for reply");
            streamObserver.onNext(Empty.newBuilder().build());
            streamObserver.onCompleted();
            reverseReply(request);
        }
    }

    /**
     * Further flood the request to neighbors
     * Flooding avoid sending back the request to the previous sender
     * @param request the LookupRequest to further flood
     * */
    protected void floodLookUp(LookUpRequest request) {
        List<Integer> path = new ArrayList<>(request.getPathList());
        path.add(this.id);
        LookUpRequest newRequest =
                LookUpRequest.newBuilder().setBuyer(request.getBuyer()).setProduct(request.getProduct()).setHopCount(request.getHopCount() - 1).addAllPath(path).build();

        Thread[] lookupThread = new Thread[neighbors.size()];
        int counter = 0;

        for (PeerId neighbor: neighbors.values()) {
            // Ignore the neighbor whom send us the request
            if (path.contains(neighbor.getId())) {
                continue;
            }

            lookupThread[counter] = new Thread(() -> {
                // Open a new channel for
                ManagedChannel channel = ManagedChannelBuilder.forAddress(neighbor.getIPAddress(),
                        neighbor.getPort()).usePlaintext().build();
                // Wait for the server to start!
                MarketPlaceGrpc.MarketPlaceBlockingStub stub = MarketPlaceGrpc.newBlockingStub(channel).withWaitForReady();
                logger.info("Send a lookup request to peer " + neighbor.getId() + " at port " + neighbor.getIPAddress() + " " +
                        + neighbor.getPort() + " for product " + request.getProduct());
                long start = System.currentTimeMillis();
                stub.lookupRPC(newRequest);
                long finish = System.currentTimeMillis();
                writeToFile((finish - start) + " milliseconds", lookupRPCLatency);
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
     * reverseReply further send the request back to the buyer via the reverse path
     * @param request the ReplyRequest to send back to buyer
     * */
    protected void reverseReply(ReplyRequest request) {
        List<Integer> path = new ArrayList<>(request.getPathList());
        PeerId fromNode = neighbors.get(path.remove(path.size() - 1));
        PeerId seller = request.getSellerId();

        ManagedChannel channel = ManagedChannelBuilder.forAddress(fromNode.getIPAddress(),
                fromNode.getPort()).usePlaintext().build();
        MarketPlaceGrpc.MarketPlaceBlockingStub stub = MarketPlaceGrpc.newBlockingStub(channel).withWaitForReady();
        logger.info("Send a reply request to peer " + fromNode.getId() + " at " + fromNode.getIPAddress() + " " + fromNode.getPort());
        ReplyRequest replyRequest =
                ReplyRequest.newBuilder().setSellerId(seller).setProduct(request.getProduct()).addAllPath(path).build();
        long start = System.currentTimeMillis();
        stub.replyRPC(replyRequest);
        long finish = System.currentTimeMillis();
        writeToFile((finish - start) + " milliseconds", replyRPCLatency);
        channel.shutdown();
    }

    public synchronized void writeToFile(String val, String filename) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(filename, true));
            writer.println(val + "\n");
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addNeighbor(PeerId peer) {
        this.neighbors.put(peer.getId(), peer);
    }

    public PeerId getNeighbor(int id) {
        return this.neighbors.get(id);
    }

    public int getNumberNeighbor() {
        return this.neighbors.size();
    }
    public int getId() { return id; }

    public int getPort() {
        return port;
    }

    public Map<Integer, PeerId> getAllNeighbors() {
        return this.neighbors;
    }

    public void setIPAddress(String IPAddress) { this.IPAddress = IPAddress; }

    public String getIPAddress() { return IPAddress; }

    public void setPort(int port) {
        this.port = port;
    }

    public void run()  {
        this.startServer();
        this.blockUntilShutdown();
    }
}

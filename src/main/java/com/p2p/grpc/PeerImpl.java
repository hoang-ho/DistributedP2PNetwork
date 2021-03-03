package com.p2p.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class PeerImpl implements Peer{

    private PeerReference neighbor;
    private final String IPAddress;
    private final int port;
    private final int id;
    private final Server server;

    private static final Logger logger = Logger.getLogger(PeerImpl.class.getName());

    public PeerImpl(int id, String IPAddress, int port) {
        this.id = id;
        this.IPAddress = IPAddress;
        this.port = port;
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
     * The Buyer and the general peer will use this server code
     * The Seller will override this implementation
     * */
    class MarketPlaceImpl extends MarketPlaceGrpc.MarketPlaceImplBase {
        @Override
        public void lookupRPC(BuyRequest request, StreamObserver<PeerId> streamObserver) {
            logger.info("Receive lookup request at " + port);
            // propagate the lookup or return a reply
            streamObserver.onNext(lookupHelper(request));
            streamObserver.onCompleted();
        }
    }

    /**
     * For a general peer, we just need it to flood the request to other peers!
     * There is no general peer in milestone 1, so we ignore this for now!
     * Nothing for now since we only have
     * */
    private PeerId lookupHelper(BuyRequest request) {
        return null;
    }

    public void setNeighbor(PeerReference peer) {
        this.neighbor = peer;
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

    public String getIPAddress() { return IPAddress; }
}

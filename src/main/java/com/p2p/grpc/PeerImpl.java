package com.p2p.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PeerImpl implements Peer{

    private List<PeerId> neighbors;
    private final String IPAddress;
    private final int port;
    private final int id;
    private final Server server;
    private Set<Pair<Integer, String>> lookupSenderList;

    private static final Logger logger = Logger.getLogger(PeerImpl.class.getName());

    public PeerImpl(int id, String IPAddress, int port, int KNeighbor) {
        this.id = id;
        this.IPAddress = IPAddress;
        this.port = port;
        this.neighbors = new ArrayList<>();
        this.lookupSenderList = Collections.synchronizedSet(new HashSet<>());
        this.server =
                ServerBuilder.forPort(port).addService(new MarketPlaceImpl()).executor(Executors.newFixedThreadPool(KNeighbor)).build();
    }

    /**
     * Implementation lookup interface
     * This lookup will do a lookupRPC call to all its neighbors
     *
     * @return a list PeerId each of which is a reference to the seller
     * */
    @Override
    public List<PeerId> lookup(String product, int hopCount) {
        BuyRequest request = BuyRequest.newBuilder().setId(this.id).setProduct(product).setHopCount(hopCount).build();
        Thread[] lookupThread = new Thread[neighbors.size()];
        List<PeerId> sellerList = Collections.synchronizedList(new ArrayList<>());
        int counter = 0;
        // stop flooding back the to where we was before
        List<PeerId> toFlood =
                neighbors.stream().filter(e -> !lookupSenderList.contains(new Pair<>(e.getId(), product))).collect(Collectors.toList());
        for (PeerId neighbor: toFlood) {
            lookupThread[counter] = new Thread(() -> {
                ManagedChannel channel = ManagedChannelBuilder.forAddress(neighbor.getIPAddress(),
                        neighbor.getPort()).usePlaintext().build();
                // Wait for the server to start!
                MarketPlaceGrpc.MarketPlaceBlockingStub stub = MarketPlaceGrpc.newBlockingStub(channel).withWaitForReady();
                logger.info("Send a lookup request to " + neighbor.getIPAddress() + " " + neighbor.getPort());
                Iterator<PeerId> sellers = stub.lookupRPC(request);
                logger.info("Lookup request return");
                for (int i = 0; sellers.hasNext(); i++) {
                    PeerId seller = sellers.next();
                    if (seller.getId() == -1) {
                        continue;
                    }
                    sellerList.add(seller);
                }
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

        return sellerList;
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
        /**
         * Seller and no-role peer floods the request to its neighbors
         * */
        @Override
        public void lookupRPC(BuyRequest request, StreamObserver<PeerId> streamObserver) {
            logger.info("Receive lookup request at " + port);
            // propagate the lookup or return a reply
            if (request.getHopCount() == 1) {
                streamObserver.onNext(PeerId.newBuilder().setId(-1).build());
            } else {
                logger.info("Flood Lookup request");
                Pair<Integer, String> lookupSender = new Pair<>(request.getId(), request.getProduct());
                lookupSenderList.add(lookupSender);
                List<PeerId> sellerList = lookup(request.getProduct(), request.getHopCount() - 1);
                sellerList.forEach(streamObserver::onNext);
                lookupSenderList.remove(lookupSender);
                logger.info("Sent back results for lookup request");
            }
            streamObserver.onCompleted();
        }
    }

    public void addNeighbor(PeerId peer) {
        this.neighbors.add(peer);
    }

    public int getId() { return id; }

    public int getPort() {
        return port;
    }

    public String getIPAddress() { return IPAddress; }

    public static void main(String[] args) {
        // id,IP address, port and KNeigbors
        PeerImpl peer = new PeerImpl(Integer.parseInt(args[0]),"localhost", Integer.parseInt(args[1]), 1);
        PeerId neighbor1 =
                PeerId.newBuilder().setId(Integer.parseInt(args[2])).setIPAddress("localhost").setPort(Integer.parseInt(args[3])).build();
        PeerId neighbor2 =
                PeerId.newBuilder().setId(Integer.parseInt(args[4])).setIPAddress("localhost").setPort(Integer.parseInt(args[5])).build();
        peer.addNeighbor(neighbor1);
        peer.addNeighbor(neighbor2);
        peer.startServer();
        peer.blockUntilShutdown();
    }
}

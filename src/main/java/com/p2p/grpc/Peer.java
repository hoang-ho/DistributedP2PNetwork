package com.p2p.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.ArrayList;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Peer {
    private int id;
    private String IPAddress;
    private int port;
    private String product;
    private int amountSell;
    private int amountBuy;
    private int stock;
    private Peer neighbors;
    private static final Logger logger = Logger.getLogger(Peer.class.getName());

    public Peer(int id, String IPAddress, int port) {
        this.id = id;
        this.IPAddress = IPAddress;
        this.port = port;
    }

    /**
     * Run the server!
     */
    public void run(int amountSell, int amountBuy, String product) {
        this.stock = amountSell;
        this.amountSell = this.stock;
        this.amountBuy = amountBuy;
        this.product = product;
        Server server =
                ServerBuilder.forPort(port).addService(new MarketPlaceImpl()).executor(Executors.newFixedThreadPool(10)).build();

        try {
            server.start();
            logger.info("starting a server at " + IPAddress + " " + port);
            // start buying if we are buyer
            if (this.stock < 0) {
                // perform lookup
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ManagedChannel channel = ManagedChannelBuilder.forAddress(neighbors.getIPAddress(),
                                neighbors.getPort()).usePlaintext().build();
                        try {
                            MarketPlaceGrpc.MarketPlaceBlockingStub stub = MarketPlaceGrpc.newBlockingStub(channel);
                            PeerId peerId = PeerId.newBuilder().setIPAddress(IPAddress).setPort(port).build();
                            BuyRequest request =
                                    BuyRequest.newBuilder().setProductName(product).setHopCount(1).addPeer(peerId).build();
                            logger.info(port + " send a buy request to " + neighbors.IPAddress + " " + neighbors.port);
                            stub.lookup(request);
                        } finally {
                            channel.shutdown();
                        }
                    }
                });
                t.start();
            }
            server.awaitTermination();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class MarketPlaceImpl extends MarketPlaceGrpc.MarketPlaceImplBase {
        @Override
        public void lookup(BuyRequest request, StreamObserver<Ack> streamObserver) {
            logger.info("Receive lookup request at " + port);
            streamObserver.onNext(Ack.newBuilder().setMessage("Ack").build());
            streamObserver.onCompleted();
            lookupHelper(request);
        }

        @Override
        public void reply(SellerId message, StreamObserver<Ack> streamObserver) {
            logger.info("Receive reply request at " + port);
            streamObserver.onNext(Ack.newBuilder().setMessage("Ack").build());
            streamObserver.onCompleted();
            replyHelper(message);
        }

        @Override
        public void buy(PeerId seller, StreamObserver<Ack> streamObserver) {
            logger.info("Receive a buy request at " + port);
            buyHelper();
            streamObserver.onNext(Ack.newBuilder().setMessage("Ack").build());
            streamObserver.onCompleted();
        }
    }

    private void lookupHelper(BuyRequest request) {
        // if peer is a seller and is selling the product, reply to the caller
        if (amountSell > 0 && request.getProductName().equals(product)) {
            // RPC reply
            int lastIdx = request.getPeerCount() - 1;
            String IPAddress = request.getPeer(lastIdx).getIPAddress();
            int lastPort = request.getPeer(lastIdx).getPort();
            List<PeerId> peers = new ArrayList<>(request.getPeerList());
            peers.remove(lastIdx);
            SellerId message =
                    SellerId.newBuilder().setIPAddress(this.IPAddress).setPort(port).addAllPeer(peers).build();

            ManagedChannel channel = ManagedChannelBuilder.forAddress(IPAddress, lastPort).usePlaintext().build();
            try {
                MarketPlaceGrpc.MarketPlaceBlockingStub stub = MarketPlaceGrpc.newBlockingStub(channel);
                logger.info("Send reply request at " + port);
                Ack acknowledge = stub.reply(message);
            } finally {
                channel.shutdown();
            }
        }
        // else pass the message to its neighbor, i.e., flooding
    }

    private void replyHelper(SellerId message) {
        // if the message arrives to the original sender
        if (message.getPeerList().size() == 0) {
            // open up a thread to send a buy request
            String sellerIPAddress = message.getIPAddress();
            int sellerPort = message.getPort();
            PeerId peer = PeerId.newBuilder().setIPAddress(IPAddress).setPort(port).build();
            ManagedChannel channel = ManagedChannelBuilder.forAddress(sellerIPAddress, sellerPort).usePlaintext().build();
            try {
                MarketPlaceGrpc.MarketPlaceBlockingStub stub = MarketPlaceGrpc.newBlockingStub(channel);
                logger.info("Send buy request at " + port);
                Ack acknowledge = stub.buy(peer);
                // increment your amount buy this need to be synchronized
                this.amountBuy += 1;
                logger.info("Buying " + product + " succeeds.");
            } finally {
                channel.shutdown();
            }
        }
        // else open up a thread pass the message back the path
    }

    // this method will need to be synchronized!
    private void buyHelper() {
        amountSell -= 1;

    }

    public void setNeighbor(Peer neighbor) {
        neighbors = neighbor;
    }

    public String getIPAddress() {
        return IPAddress;
    }

    public int getPort() {
        return port;
    }
}

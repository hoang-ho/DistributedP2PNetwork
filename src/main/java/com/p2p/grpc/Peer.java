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
    private final String product;
    private int amountSell;
    private int amountBuy;
    private PeerReference neighbor;
    private final String IPAddress;
    private final int port;
    private int id;
    private final int stock;
    private final Server server;

    private static final Logger logger = Logger.getLogger(Peer.class.getName());

    public Peer(int id, String IPAddress, int port, String product, int amountBuy, int amountSell) {
        this.id = id;
        this.IPAddress = IPAddress;
        this.port = port;
        this.product = product;
        this.amountBuy = amountBuy;
        this.amountSell = amountSell;
        this.stock = amountSell;
        this.server =
                ServerBuilder.forPort(port).addService(new MarketPlaceImpl()).executor(Executors.newFixedThreadPool(10)).build();
    }

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
            logger.info("Finish a transaction. Current have " + amountSell);
        }
    }

    // this code helps
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
                MarketPlaceGrpc.MarketPlaceBlockingStub stub = MarketPlaceGrpc.newBlockingStub(channel).withWaitForReady();
                logger.info("Send reply request at " + port);
                Ack acknowledge = stub.reply(message);
            } finally {
                channel.shutdown();
            }
        }
        // else pass the message to its neighbor, i.e., flooding
        // Nothing for milestone 1
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
                MarketPlaceGrpc.MarketPlaceBlockingStub stub = MarketPlaceGrpc.newBlockingStub(channel).withWaitForReady();
                logger.info("Send buy request at " + port);
                Ack acknowledge = stub.buy(peer);
                // increment your amount buy this need to be synchronized
                this.amountBuy += 1;
                logger.info("Buying " + product + " succeeds. Current have " + this.amountBuy + " " + product);
            } finally {
                channel.shutdown();
            }
        }
        // else open up a thread pass the message back the path
    }

    // this method will need to be synchronized!
    // decrement the amountSell and restock :)
    private void buyHelper() {
        amountSell -= 1;
        if (amountSell == 0) {
            logger.info(product + " runs out!!!! Restocking");
            amountSell = stock;
        }
    }

    public void setNeighbor(PeerReference peer) {
        this.neighbor = peer;
    }

    public String getIPAddress() {
        return IPAddress;
    }

    public int getPort() {
        return port;
    }

    public String getNeighborAddress() {
        return neighbor.getIPAddress();
    }

    public int getNeighborPort() {
        return neighbor.getPort();
    }

    public String getProduct() {
        return product;
    }
}

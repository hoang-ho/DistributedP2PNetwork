package com.p2p.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class Buyer extends Peer{
    // a logger to log what happens
    private static final Logger logger = Logger.getLogger(Buyer.class.getName());

    public Buyer(int id, String IPAddress, int port, String product, int amount) {
        super(id, IPAddress, port, product, amount, -1);
    }

    /**
     * This is the local lookup of the buyer
     * The buyer's local lookup will do a remote lookup call to all its neighbors
     * @Return an acknowledgement string from the neighbor to verify message is received
     * */
    public String lookup(BuyRequest request) {
        // we only have one neighbors so we only need one acknowledgement message for now
        AtomicReference<String> ackMessage = new AtomicReference<>("");
        Thread t = new Thread(() -> {
            ManagedChannel channel = ManagedChannelBuilder.forAddress(super.getNeighborAddress(),
                    super.getNeighborPort()).usePlaintext().build();
            try {
                // Wait for the server to start!
                MarketPlaceGrpc.MarketPlaceBlockingStub stub = MarketPlaceGrpc.newBlockingStub(channel).withWaitForReady();
                logger.info(super.getPort() + " Send a buy request to " + super.getNeighborAddress() + " " + super.getNeighborPort());
                ackMessage.set(stub.lookup(request).getMessage());
            } finally {
                channel.shutdown();
            }
        });
        t.start();

        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return ackMessage.get();
    }

    // the main function for buyer
    // The buyer keeps sending buy messages
    public static void main(String[] args) {
        // test case 1 and 3
        Buyer buyer = new Buyer(Integer.parseInt(args[0]),"localhost", Integer.parseInt(args[1]), args[2],  0);
        PeerReference peer = new PeerReference(Integer.parseInt(args[3]), "localhost", Integer.parseInt(args[4]));
        buyer.setNeighbor(peer);
        buyer.startServer();

        // buyer keeps buying products forever :D
        // buyer keeps sending out lookup request
        PeerId peerId =
                PeerId.newBuilder().setIPAddress(buyer.getIPAddress()).setPort(buyer.getPort()).build();
        BuyRequest request =
                BuyRequest. newBuilder().setProductName(buyer.getProduct()).setHopCount(1).addPeer(peerId).build();
        while (true) {
            // buyer perform lookup
            buyer.lookup(request);
            try {
                Thread.sleep(3000); // sleep a little bit before lookup again
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}

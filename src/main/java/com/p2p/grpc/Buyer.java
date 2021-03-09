package com.p2p.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class Buyer extends PeerImpl{
    // a logger to log what happens
    private Product product;
    private int amount = 0;

    private static final Logger logger = Logger.getLogger(Buyer.class.getName());

    public Buyer(int id, String IPAddress, int port, int KNeighbors, Product product) {
        super(id, IPAddress, port, KNeighbors);
        this.product = product;
    }

    /**
     * Implementation for the buy
     * @param peerId the sellerId reference. We use this to establish a direct connection to the seller
     * */
    @Override
    public void buy(PeerId peerId) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(peerId.getIPAddress(),
                peerId.getPort()).usePlaintext().build();
        try {
            // Wait for the server to start!
            MarketPlaceGrpc.MarketPlaceBlockingStub stub = MarketPlaceGrpc.newBlockingStub(channel).withWaitForReady();
            logger.info( "Send a buy request to peer " + peerId.getId());
            Ack message = stub.buyRPC(peerId);
            if (message.getMessage().equals("Ack Sell")) {
                amount += 1;
            }
            logger.info("Bought " + this.product.name() + " from peer " + peerId.getId() + ". Buyer current has " + this.amount + " " + this.product.name());
        } finally {
            channel.shutdown();
        }
    }

    // the main function for buyer
    // The buyer keeps sending buy messages
    public static void main(String[] args) {
        // args: id port product neighborId neighborPort
        // test case 1 and 2
        Random rand = new Random();
        Buyer buyer = new Buyer(Integer.parseInt(args[0]),"localhost", Integer.parseInt(args[1]), 1,
                Product.valueOf(args[2].toUpperCase()));
        PeerId peer1 =
                PeerId.newBuilder().setId(Integer.parseInt(args[3])).setIPAddress("localhost").setPort(Integer.parseInt(args[4])).build();
        PeerId peer2 =
                PeerId.newBuilder().setId(Integer.parseInt(args[5])).setIPAddress("localhost").setPort(Integer.parseInt(args[6])).build();
        buyer.addNeighbor(peer1);
        buyer.addNeighbor(peer2);
        buyer.startServer();

        // buyer keeps buying products forever :D
        // buyer keeps sending out lookup request
        while (true) {
            // buyer perform lookup
            List<PeerId> sellerList = buyer.lookup(buyer.product.name(), 2);
            // if lookup return peerID results
            if (sellerList.size() > 0) {
                int index = rand.nextInt(sellerList.size());
                PeerId seller = sellerList.get(index);
                buyer.buy(seller);
            }
            try {
                Thread.sleep(3000); // sleep a little bit before lookup again
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

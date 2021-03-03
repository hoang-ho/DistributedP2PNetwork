package com.p2p.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.logging.Logger;

public class Buyer extends PeerImpl{
    // a logger to log what happens
    private Product product;
    private int amount = 0;

    private static final Logger logger = Logger.getLogger(Buyer.class.getName());

    public Buyer(int id, String IPAddress, int port, Product product) {
        super(id, IPAddress, port);
        this.product = product;
    }

    /**
     * Implementation for the buy.
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
        try {
            Buyer buyer = new Buyer(Integer.parseInt(args[0]),"localhost", Integer.parseInt(args[1]),
                    Product.valueOf(args[2].toUpperCase()));
            PeerReference peer = new PeerReference(Integer.parseInt(args[3]), "localhost", Integer.parseInt(args[4]));
            buyer.setNeighbor(peer);
            buyer.startServer();

            // buyer keeps buying products forever :D
            // buyer keeps sending out lookup request
            BuyRequest request =
                    BuyRequest.newBuilder().setProduct(buyer.product.name()).setHopCount(1).build();
            while (true) {
                // buyer perform lookup
                PeerId seller = buyer.lookup(request);

                // if not an invalid message, then buy!
                if (seller.getId() != -1) {
                    buyer.buy(seller);
                }
                try {
                    Thread.sleep(3000); // sleep a little bit before lookup again
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (RuntimeException e) {
            System.out.println("Gradle is shutting down this process " + e.getMessage());
        }

    }
}

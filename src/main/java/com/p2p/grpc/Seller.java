package com.p2p.grpc;

/**
 * A Wrapper class for the Seller
 * For now, seller just needs to start the server and block until shutdown
 * */
public class Seller extends Peer {
    public Seller(int id, String IPAddress, int port, String product, int amount) {
        super(id, IPAddress, port, product, 0, amount);
    }

    public static void main(String[] args) {
        // args: id, port, product, amount, neighbors id, neighbor port
        // test case 1 and 3
        Seller seller = new Seller(Integer.parseInt(args[0]),"localhost", Integer.parseInt(args[1]), args[2],
                Integer.parseInt(args[3]));
        PeerReference peer = new PeerReference(Integer.parseInt(args[4]), "localhost", Integer.parseInt(args[5]));
        seller.setNeighbor(peer);
        seller.startServer();
        seller.blockUntilShutdown();

    }
}

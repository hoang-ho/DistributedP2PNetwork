package com.p2p.grpc;

import java.util.ArrayList;

public class Runner {
    public static void main(String[] args) {
        // Initialize the peer
        Peer peer1 = new Peer(1, "localhost",8080);
        Peer peer2 = new Peer(2, "localhost",8081);
        peer1.setNeighbor(peer2);
        peer2.setNeighbor(peer1);

        // create two threads to run the peer
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                peer1.run(3, 0, "fish");
            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                peer2.run(-1, 0, "fish");
            }
        });

        t1.start();
        t2.start();

    }
}

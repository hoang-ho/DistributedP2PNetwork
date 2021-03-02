package com.p2p.grpc;

/**
 * This is the reference to the peer
 * Every peer will have access their the neighbors' PeerReference
 * */
public class PeerReference {
    private final String IPAddress;
    private final int port;
    private final int id;

    public PeerReference(int id, String IPAddress, int port) {
        this.id = id;
        this.IPAddress = IPAddress;
        this.port = port;
    }

    public int getPort(){
        return port;
    }

    public String getIPAddress() {
        return IPAddress;
    }

    public int getId() {
        return id;
    }
}

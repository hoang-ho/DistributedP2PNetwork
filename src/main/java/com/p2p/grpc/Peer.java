package com.p2p.grpc;

public interface Peer {
    /**
     * Call a RPC lookup to lookup for seller peer
     * @param request is a BuyRequest containing product name and hopcount
     * @return a PeerId object containing reference to the Seller
     * */
    PeerId lookup(BuyRequest request);

    /**
     * Right now, I haven't made use of this function yet!
     * */
    PeerId reply(PeerId peerId);

    /**
     * Perform a buy operation. Buyer send request directly to the Seller.
     * Seller decrement the stock and buyer increments its inventory
     * @param peerId reference to the Seller
     * */
    void buy(PeerId peerId);
}

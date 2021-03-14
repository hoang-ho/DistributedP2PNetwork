package com.p2p.grpc;

public interface Peer {
    /**
     * Call a RPC lookup to lookup for seller peer
     * @param product is a string value for the name of the product
     * @param hopCount is an integer value for the hopCount
     * */
    void lookup(String product, int hopCount);

    /**
     * Implemented in the Seller class
     * For the Seller to reply back to the Buyer
     * */
    void reply(int buyerId, PeerId sellerId);

    /**
     * Perform a buy operation. Buyer send request directly to the Seller.
     * Seller decrement the stock and buyer increments its inventory
     * @param peerId reference to the Seller
     * */
    void buy(PeerId peerId);
}

package com.p2p.grpc;

public interface Peer {
    /**
     * Call a RPC lookup to lookup for seller peer
     * For Byuer to lookup seller to buy from
     * Implemented in the Buyer class
     * @param product is a string value for the name of the product
     * @param hopCount is an integer value for the hopCount
     * */
    void lookup(String product, int hopCount);

    /**
     * Call a RPC reply to traverse the reverse path back to the buyer
     * For the Seller to reply back to the Buyer
     * Implemented in the Seller class
     * @param buyerId the node id of the buyer
     * @param sellerId a reference to the seller (id, IPAddress and port)
     * */
    void reply(int buyerId, PeerId sellerId);

    /**
     * Perform a buy operation. Buyer send request directly to the Seller.
     * Seller decrement the stock and buyer increments its inventory
     * Implemented in the Buyer class
     * @param peerId reference to the Seller
     * */
    void buy(PeerId peerId);
}

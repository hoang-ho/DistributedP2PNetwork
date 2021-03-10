package com.p2p.utils;

public class Pair<K, V> {
    private final K peerID;
    private final V product;
    public Pair(K peerID, V product) {
        this.peerID = peerID;
        this.product = product;
    }

    public K getPeerID() {
        return peerID;
    }
    public V getProduct() {
        return product;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Pair<K,V> other = (Pair<K,V>) obj;
        if (!this.peerID.equals(other.getPeerID())) {
            return false;
        }
        if (!this.getProduct().equals(other.getProduct())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 7 * peerID.hashCode() + 13 * product.hashCode();
    }
}

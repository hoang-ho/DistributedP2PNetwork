# Overall design

The design of the system is as follows. PeerImpl.java implements the Peer interface

```java
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
```

PeerImpl.java implements the interface:

```java
public class PeerImpl {
    ...
    /**
     * Implementation lookup interface
     * This lookup will do a lookupRPC call to all its neighbors
     * The return from the lookupRPC is a PeerId, which is a reference to the seller
     * */
    @Override
    public PeerId lookup(BuyRequest request) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(this.getNeighborAddress(),
                this.getNeighborPort()).usePlaintext().build();
        // Wait for the server to start!
        MarketPlaceGrpc.MarketPlaceBlockingStub stub = MarketPlaceGrpc.newBlockingStub(channel).withWaitForReady();
        logger.info("Send a lookup request to " + this.getNeighborAddress() + " " + this.getNeighborPort());
        return stub.lookupRPC(request);
    }

    /**
     * This doesn't do anything for now
     * */
    @Override
    public PeerId reply(PeerId peerId) {
        return null;
    }

    /**
     * Nothing going on here! Only buyer can buy
     * */
    @Override
    public void buy(PeerId peerId) { }
}
```

Buyer.java overrides the buy function implementation:

```java
public class Buyer {
    ...
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
    ...
}
```

The lookup and buy functions perform the lookRPC and buyRPC call, specified as follows:

``` 
service MarketPlace {
  rpc lookupRPC(BuyRequest) returns (PeerId);
  rpc buyRPC(PeerId) returns (Ack);
}

message Ack {
  string message = 1;
}

message BuyRequest {
  string productName = 1;
  int32 hopCount = 2;
}

message PeerId {
  int32 id = 1;
  string IPAddress = 2;
  int32 port = 3;
}
```

The lookup function send lookupRPC to its neighbor. The lookupRPC will again call the lookupHelper function to help it decide whether to propagate the lookup request or to reply back! For a general peer, its lookupRPC will just propagate, while for a Seller peer, it will either propagate or reply. 

```java
public class PeerImpl {
    ...

    /**
     * Server side code for each RPC call
     * The Buyer and the general peer will use this server code
     * The Seller will override this implementation
     * */
    class MarketPlaceImpl extends MarketPlaceGrpc.MarketPlaceImplBase {
        @Override
        public void lookupRPC(BuyRequest request, StreamObserver<PeerId> streamObserver) {
            logger.info("Receive lookup request at " + port);
            // propagate the lookup or return a reply
            streamObserver.onNext(lookupHelper(request));
            streamObserver.onCompleted();
        }
    }

    /**
     * For a general peer, we just need it to flood the request to other peers!
     * There is no general peer in milestone 1, so we ignore this for now!
     * Nothing for now since we only have
     * */
    private PeerId lookupHelper(BuyRequest request) {
        return null;
    }
}

public class Seller {
    ...
    /**
     * Server side code for the Seller
     * The Seller has somewhat different implementation because
     * (1) only the Seller can sell - the buyRPC server code should only for the server
     * (2) The lookupRPC in the Seller will decide whether to propagate or to reply!
     * */
    private class MarketplaceSellerImpl extends MarketPlaceGrpc.MarketPlaceImplBase {
        @Override
        public void lookupRPC(BuyRequest request, StreamObserver<PeerId> streamObserver) {
            logger.info("Receive lookup request at " + Seller.this.getPort());
            // propagate the lookup or return a reply
            streamObserver.onNext(lookupHelper(request));
            streamObserver.onCompleted();
        }

        @Override
        public void buyRPC(PeerId seller, StreamObserver<Ack> streamObserver) {
            logger.info("Receive a buy request at " + Seller.this.getPort());
            processBuy();
            streamObserver.onNext(Ack.newBuilder().setMessage("Ack Sell").build());
            streamObserver.onCompleted();
            logger.info("Finish a transaction. Current have " + amount);
        }
    }

    private PeerId lookupHelper(BuyRequest request) {
        // if we are the buyer and we are selling the same product
        if (request.getHopCount() > 0 && request.getProduct().equals(product.name())) {
            // reply
            return PeerId.newBuilder().setId(this.getId()).setIPAddress(this.getIPAddress()).setPort(this.getPort()).build();
        }
        // No flooding for now!
//        else if (request.getHopCount() > 1) {
//            // else call the lookup to its neighbor, i.e., flooding
//            BuyRequest newRequest =
//                    BuyRequest.newBuilder().setProductName(request.getProductName()).setHopCount(request.getHopCount() - 1).build();
//            return this.lookup(newRequest);
//        }
        // this is to signify not found!!!
        return PeerId.newBuilder().setId(-1).build();
    }

    // this method will need to be synchronized!
    // decrement the amountSell and restock :)
    private void processBuy() {
        // decrement the count
        amount -= 1;
        if (amount == 0) {
            logger.info(product.name() + " runs out!!!! Restocking");
            // randomize and restock!
            product = Product.values()[RANDOM.nextInt(Product.values().length) - 1];
            amount = stock;
            logger.info("After randomize a new product and restock, now selling " + product.name());
        }
    }
}
```

The RPC calls are synchronous, so the lookupRPC waits for a PeerId reply and we don't need to explicitly call reply function here!
For lookupRPC, it calls the local lookupHelper function, which in turn decides whether to return a PeerId or to propagate the lookup request! Since we only have one neighbor this time, so this shouldn't matter this time!

Other considered design implementation:

* Instead of having lookupRPC to be synchronous, I tried to make it asynchronous and have a replyRPC to reply the PeerId of the seller back, however, this would cause my implementation to become stateful because I need to store information about the clients' PeerId.* 
* Before I thought it was possible to change the interface, so I append a list of PeerId into the interface calls (lookup and reply), however, the instructors cleared this confusion: the interface must remain the same as specified and can be local function, we need to design our RPC such that it supports what the interface try to achieve!
* I also consider other cases to make use of the reply interface, however, I believe the reply interface can only be used ...


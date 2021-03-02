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
      logger.info(this.port + " Send a buy request to " + this.getNeighborAddress() + " " + this.getNeighborPort());
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
        this.incrementBuy(message);
        logger.info("Bought " + this.getProduct() + " from peer " + peerId.getId() + ". Buyer current has " + this.getAmountBuy() + " " + this.getProduct());
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

The implementation of these RPC calls are as follows:

```java
public class PeerImpl {
    ...
    
    /**
     * Server side code for each RPC call
     * Both Buyer and Seller have the server.
     * Buyer will be the server for lookup and reply.
     * Seller will be the server for lookup and buy.
     * All RPC calls return an acknowledge message immediately, except for the buy RPC which blocks until the buy
     * completes
     * */
    private final class MarketPlaceImpl extends MarketPlaceGrpc.MarketPlaceImplBase {
      @Override
      public void lookupRPC(BuyRequest request, StreamObserver<PeerId> streamObserver) {
        logger.info("Receive lookup request at " + port);
        // propagate the lookup or return a reply
        streamObserver.onNext(lookupHelper(request));
        streamObserver.onCompleted();
      }
  
      @Override
      public void buyRPC(PeerId seller, StreamObserver<Ack> streamObserver) {
        logger.info("Receive a buy request at " + port);
        processBuy();
        streamObserver.onNext(Ack.newBuilder().setMessage("Ack Sell").build());
        streamObserver.onCompleted();
        logger.info("Finish a transaction. Current have " + amountSell);
      }
    }
    ...

    private PeerId lookupHelper(BuyRequest request) {
        // if we are the buyer and we are selling the same product
        if (request.getHopCount() > 0 && amountSell > 0 && request.getProductName().equals(product)) {
            // reply
            PeerId seller = PeerId.newBuilder().setId(this.id).setIPAddress(this.IPAddress).setPort(this.port).build();
            return seller;
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
    
    private void processBuy() {
      amountSell -= 1;
      if (amountSell == 0) {
        logger.info(product + " runs out!!!! Restocking");
        amountSell = stock;
      }
    }

    public void incrementBuy(Ack message) {
      if (message.getMessage().equals("Ack Sell")) {
        amountBuy += 1;
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


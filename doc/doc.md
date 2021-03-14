# Overall design

The design of the system is as follows. PeerImpl.java implements the Peer interface

```java
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
    void reply(PeerId buyerId, PeerId sellerId);

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
     * Lookup implemented in the Buyer
     * */
    @Override
    public void lookup(String product, int hopCount) {
    }

    /**
     * Reply implemented in Seller
     * */
    @Override
    public void reply(PeerId buyer, PeerId seller) {
    }

    /**
     * Buy implemented in Buyer
     * */
    @Override
    public void buy(PeerId peerId) { }
}
```

Buyer.java overrides the lookup and the buy function implementation:

```java
public class Buyer {
    /**
     * Implementation lookup interface for the buyer
     * This lookup will do a lookupRPC call to all its neighbors
     * */
    @Override
    public void lookup(String product, int hopCount) {
        LookUpRequest request =
                LookUpRequest.newBuilder().setFromNode(this.getId()).setProduct(product).setHopCount(hopCount).addPath(this.getId()).build();

        Thread[] lookupThread = new Thread[this.getNumberNeighbor()];
        int counter = 0;
        // stop flooding back the to where we was before
        // filter out the neighbor who sent us the lookup request for the product

        for (PeerId neighbor: this.getAllNeighbors().values()){
            lookupThread[counter] = new Thread(() -> {
                // Open a new channel for
                ManagedChannel channel = ManagedChannelBuilder.forAddress(neighbor.getIPAddress(),
                        neighbor.getPort()).usePlaintext().build();
                // Wait for the server to start!
                MarketPlaceGrpc.MarketPlaceBlockingStub stub = MarketPlaceGrpc.newBlockingStub(channel).withWaitForReady();
                logger.info("Send a lookup request to peer " + neighbor.getId() + " at port " + neighbor.getIPAddress() + " " +
                        + neighbor.getPort() + " for product " + product);

                stub.lookupRPC(request);
                channel.shutdown();
            });
            lookupThread[counter].start();
            counter++;
        }

        for (int i = 0; i < counter; i++) {
            try {
                lookupThread[i].join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Implementation for the buy
     * @param sellerId the sellerId reference. We use this to establish a direct connection to the seller
     * */
    @Override
    public synchronized void buy(PeerId sellerId) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(sellerId.getIPAddress(),
                sellerId.getPort()).usePlaintext().build();
        try {
            // Wait for the server to start!
            MarketPlaceGrpc.MarketPlaceBlockingStub stub = MarketPlaceGrpc.newBlockingStub(channel).withWaitForReady();
            logger.info( "Send a buy request to peer " + sellerId.getId());
            Ack message =
                    stub.buyRPC(BuyRequest.newBuilder().setId(Buyer.this.getId()).setProduct(Buyer.this.product.name()).build());
            if (message.getMessage().equals("Ack Sell")) {
                buyItems.put(product, buyItems.getOrDefault(product, 0) + 1);
                logger.info("Bought " + this.product.name() + " from peer " + sellerId.getId() + ". Buyer current has" +
                        " " + buyItems.get(this.product) + " " + this.product.name());

            } else {
                logger.info("Buy unsuccessful!");
            }
        } finally {
            channel.shutdown();
        }
    }
}
```

Seller.java overrides the reply interface:

```java
public class Seller {
    /**
     * Reply request from Seller to Buyer 
     * */
    @Override
    public void reply(PeerId buyer, PeerId seller) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(buyer.getIPAddress(),
                buyer.getPort()).usePlaintext().build();
        MarketPlaceGrpc.MarketPlaceBlockingStub stub = MarketPlaceGrpc.newBlockingStub(channel).withWaitForReady();
        logger.info("Send a reply request to peer " + buyer.getId() + " at port " + buyer.getIPAddress() + " " +
                + buyer.getPort() + " for product " + product);
        ReplyRequest replyRequest =
                ReplyRequest.newBuilder().setSellerId(seller).setProduct(this.product.name()).addAllPath(replyPath).build();
        logger.info("Reply path size " + replyRequest.getPathCount());
        stub.replyRPC(replyRequest);
        channel.shutdown();
        logger.info("Done Reply");
    }
}
```

The lookup, reply buy functions perform the lookRPC, replyRPC and buyRPC call, specified as follows:

``` 
service MarketPlace {
  rpc lookupRPC(LookUpRequest) returns (Empty);
  rpc replyRPC(ReplyRequest) returns (Empty);
  rpc buyRPC(BuyRequest) returns (Ack);
}

message Ack {
  string message = 1;
}

message Empty {

}

message LookUpRequest {
  int32 fromNode = 1;
  string product = 2;
  int32 hopCount = 3;
  repeated int32 path = 4;
}

message ReplyRequest {
  PeerId sellerId = 1;
  string product = 2;
  repeated int32 path = 3;
}

message BuyRequest {
  int32 id = 1;
  string Product = 2;
}

message PeerId {
  int32 id = 1;
  string IPAddress = 2;
  int32 port = 3;
}

enum Product {
  FISH = 0;
  SALT = 1;
  BOAR = 2;
}
```

The lookup function send lookupRPC to its neighbor. The lookupRPC will again flood the lookup request to other neighbors

```java
public class PeerImpl {
    ...

    /**
     * Server side code for each RPC call
     * The Seller will extends this class to override the lookupRPC and the buyRPC
     * The Buyer will override this implementation for replyRPC
     * */
    class MarketPlaceImpl extends MarketPlaceGrpc.MarketPlaceImplBase {
        /**
         * The No-role peer floods the request to its neighbors
         * This would returns an ack empty message and call a helper function to flood the request
         * */
        @Override
        public void lookupRPC(LookUpRequest request, StreamObserver<Empty> streamObserver) {
            logger.info("Receive lookup request at " + port + " from peer " + request.getFromNode() + " for product " + request.getProduct());
            // propagate the lookup or return a reply
            streamObserver.onNext(Empty.newBuilder().build());
            streamObserver.onCompleted();
            if (request.getHopCount() == 1) {
                // cancel the request
                logger.info("Invalidate lookup request from peer " + request.getFromNode() + " for product" +
                        " " + request.getProduct());
            } else {
                // Flood the lookup request
                logger.info("Flood Lookup request from peer " + request.getFromNode() + " for product " + request.getProduct());
                floodLookUp(request);
            }
        }
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
    private class MarketplaceSellerImpl extends PeerImpl.MarketPlaceImpl {
        /**
         * This would returns an ack empty message and check if it can reply. 
         * If the Seller isn't selling the product, it call a helper function to flood the request
         * */
        @Override
        public void lookupRPC(LookUpRequest request, StreamObserver<Empty> streamObserver) {
            logger.info("Receive lookup request at " + Seller.this.getPort() + " from peer " + request.getFromNode() + " " +
                    " for product " + request.getProduct());
            streamObserver.onNext(Empty.newBuilder().build());
            streamObserver.onCompleted();

            // propagate the lookup or return a reply
            // if the seller is selling the product
            if (request.getProduct().equals(product.name())) {
                logger.info("Reply to lookup request from peer " + request.getFromNode() + " for product " + request.getProduct());
                synchronized (this) {
                    replyPath.addAll(request.getPathList());
                    PeerId fromNode = Seller.this.getNeighbor(replyPath.remove(replyPath.size() - 1));
                    PeerId seller =
                            PeerId.newBuilder().setId(Seller.this.getId()).setIPAddress(Seller.this.getIPAddress()).setPort(Seller.this.getPort()).build();
                    Seller.this.reply(fromNode, seller);
                    replyPath.clear();
                }

            } else if (request.getHopCount() == 1) {
                // if hopCount is 1 then cannot flood further
                logger.info("Invalidate lookup request from peer " + request.getFromNode() + " for product" +
                        " " + request.getProduct());
            } else {
                logger.info("Flood Lookup request from peer " + request.getFromNode() + " for product " + request.getProduct());
                floodLookUp(request);
            }
        }
    }
}
```

The reply function from the Seller will call a replyRPC, and this replyRPC returns an ack message and call a helper function to traverse the reverse path to the buyer

```java
public class PeerImpl {
    class MarketPlaceImpl extends MarketPlaceGrpc.MarketPlaceImplBase {
        /**
         * This would return an ack empty message and call a helper function to traverse path the path 
         * */
        @Override
        public void replyRPC(ReplyRequest request, StreamObserver<Empty> streamObserver) {
            // Traverse the reverse path
            logger.info("Receive Reply request at " + PeerImpl.this.id + ". Size of reverse path " + request.getPathCount());
            logger.info("Continue down the path for reply");
            streamObserver.onNext(Empty.newBuilder().build());
            streamObserver.onCompleted();
            reverseReply(request);
        }
    }
}

public class Buyer {
    /**
     * A class that extends PeerImpl.MarketPlaceImpl to override replyRPC
     * Need to check if this Buyer is the original sender of the lookup request
     * */
    private class MarketPlaceBuyerImpl extends PeerImpl.MarketPlaceImpl {
        @Override
        public void replyRPC(ReplyRequest request, StreamObserver<Empty> streamObserver) {
            logger.info("Receive a reply request at " + Buyer.this.getId() + " size of path " + request.getPathCount());
            streamObserver.onNext(Empty.newBuilder().build());
            streamObserver.onCompleted();
            if (request.getPathCount() == 0 ) {
                logger.info("Request Product: " + request.getProduct());
                logger.info("Buyer Product " + Buyer.this.product.name());
                if (request.getProduct().equals(Buyer.this.product.name())) {
                    logger.info("Add the seller to the list of sellers");
                    Buyer.this.potentialSellers.add(request.getSellerId());
                }
                // discard reply
            } else {
                // continue down the path
                logger.info("Continue down the path for reply");
                streamObserver.onNext(Empty.newBuilder().build());
                streamObserver.onCompleted();
                reverseReply(request);
            }
        }
    }
}
```

The reply code in Buyer add a verification to make sure that the reply request is for the product Buyer's currently buying, if it is, the Buyer add the SellerID to a list for further buy request. 
The buy request connects directly with the Seller, the buyRPC blocks until the processing for buy completes.


```java
public class Seller {
    private class MarketplaceSellerImpl extends PeerImpl.MarketPlaceImpl {
        /**
         * THis is a synchronized method so only one thread can access it at a time
         * It first verifies again that the buy request is for the product it currently has
         * If the buy product is invalid, then it returns an error message
         * */
        @Override
        public synchronized void buyRPC(BuyRequest buyRequest, StreamObserver<Ack> streamObserver) {
            logger.info("Receive a buy request at " + Seller.this.getPort() + " from peer " + buyRequest.getId() + " " +
                    "for product " + buyRequest.getProduct());
            if (Seller.this.product.name().equals(buyRequest.getProduct())) {
                processBuy();
                streamObserver.onNext(Ack.newBuilder().setMessage("Ack Sell").build());
                streamObserver.onCompleted();
                logger.info("Finish a transaction for peer " + buyRequest.getId() + ". Currently, having: " + amount);
            } else {
                streamObserver.onNext(Ack.newBuilder().setMessage("Out of Stock").build());
                streamObserver.onCompleted();
                logger.info("Product out of stock! Now selling " + Seller.this.product.name());
            }

        }
    }
}
```

All RPCs except for buy are non-blocking!


# Design for lookup and flooding lookup

1. Synchronous/Blocking Lookup

Previously, I tried out a design with a blocking lookup which returns a reference to the Seller. 
When a seller opens up threads to send out a lookup request to a neighbor, the lookup will block in that thread until the result of the lookup request returns. 

* Pros: This design is straightforward and minimizes the amount of thread synchronization
* Cons: Even though a lookup happens in a thread, it blocks that thread, and this can waste resources.

2. Asynchronous/Non-blocking lookup

A non-blocking lookup means that once the buyer sends out a lookup request, it terminates the thread execution, and when the Seller receives the lookup request, it will send out a reply request, which traverses the reverse path back to the Buyer. 
In order for the reply request to traverse the reverse path back to the Seller, the RPC/RMI lookup request must include the traversal path. 
Here, I again face a decision-making point: when each peer receives a lookup request, it can either (1) flood the lookup request by calling the local lookup, (2) flood the lookup request by again call the RPC/RMI lookup request

I did explore both options, and for option (1), I find the following situation:

Because the local lookup must be as: lookup(product, hop_count), the question is how would the code inside the local lookup know what path for this request would be? 
The options I explored to solve this problem is: have a map where the key is (peer_id, request_id) and value is the path, and then have another map where the key is (product, hop_count) and the value is a list of (peer_id, request_id). 
Then for each peer, they would have a separate thread which will try to flood all the lookup requests it received and empties out the two described maps. 
But another question arises: for the Buyer, which will have to perform a lookup request for itself and floods the lookup request, how would the code inside the local lookup of the Buyer know whether it is flooding others lookup requests?
If the buyer is to send out a lookup request for itself, it would have to create a path parameter, and if the buyer is to flood the lookup request for others, it would have to use the two mentioned maps to find the path.
To this question, I was unable to find an answer. Hence, I decided to not follow this option.

For option (2), what I did is the following:

The local lookup interface would be implemented in the Buyer. What this lookup interface would do is to send out a RPC/RMI lookup request (which includes a path parameter) from Buyer to all of its neighbors. 
The neighbors can be a no-role peer or a buyer. What the neighbor would do when it receives the lookup request is it would return an acknowledgement for the request and further flood this lookup request via RMI/RPC lookup. 
When the Seller receives the lookup request, it first checks whether it can reply to the lookup request. 
If it can, it saves the path to a local variable and then calls the local reply(seller_id, buyer_id).
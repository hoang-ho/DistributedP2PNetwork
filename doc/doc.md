# Overall design

The design of the system is as follows. PeerImpl.java implements the Peer interface

```java
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
```

PeerImpl.java implements the interface, but we delegate the implementation subclass Buyer and Seller of PeerImpl:

```java
public class PeerImpl {
    ...
    /**
     * Lookup implemented in the Buyer
     * */
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
    public void reply(int buyerId, PeerId seller) {
    }

    /**
     * Buy implemented in Buyer
     * */
    @Override
    public void buy(PeerId peerId) { }
}
```

Buyer.java overrides the lookup and buy implementation. The lookup interface create a lookup request and call RPC lookup to all the buyer's neighbor. 
The buy interface create a buy request and send the request directly to the seller via RPC buy.

```java
public class Buyer {
    /**
     * Call a RPC lookup to lookup for seller peer
     * For Byuer to lookup seller to buy from
     * @param product is a string value for the name of the product
     * @param hopCount is an integer value for the hopCount
     * */
    @Override
    public void lookup(String product, int hopCount) {
        LookUpRequest request =
                LookUpRequest.newBuilder().setBuyer(this.getId()).setProduct(product).setHopCount(hopCount).addPath(this.getId()).build();

        Thread[] lookupThread = new Thread[this.getNumberNeighbor()];
        int counter = 0;
        startLookup = System.currentTimeMillis();
        for (PeerId neighbor: this.getAllNeighbors().values()){
            lookupThread[counter] = new Thread(() -> {
                // Open a new channel for
                ManagedChannel channel = ManagedChannelBuilder.forAddress(neighbor.getIPAddress(),
                        neighbor.getPort()).usePlaintext().build();
                // Wait for the server to start!
                MarketPlaceGrpc.MarketPlaceBlockingStub stub = MarketPlaceGrpc.newBlockingStub(channel).withWaitForReady();
                logger.info("Send a lookup request to peer " + neighbor.getId() + " at port " + neighbor.getIPAddress() + " " +
                        + neighbor.getPort() + " for product " + product);
                long start = System.currentTimeMillis();
                stub.lookupRPC(request);
                long finish = System.currentTimeMillis();
                writeToFile((finish - start) + " milliseconds", lookupRPCLatency);
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
     * Perform a buy operation. Buyer send request directly to the Seller.
     * Seller decrement the stock and buyer increments its inventory
     * Implemented in the Buyer class
     * @param peerId reference to the Seller, which is use for direct connection to the seller
     * */
    @Override
    public synchronized void buy(PeerId sellerId) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(sellerId.getIPAddress(),
                sellerId.getPort()).usePlaintext().build();
        try {
            // Wait for the server to start!
            MarketPlaceGrpc.MarketPlaceBlockingStub stub = MarketPlaceGrpc.newBlockingStub(channel).withWaitForReady();
            logger.info( "Send a buy request to peer " + sellerId.getId());
            BuyRequest request =
                    BuyRequest.newBuilder().setId(Buyer.this.getId()).setProduct(Buyer.this.product.name()).build();
            long start = System.currentTimeMillis();
            Ack message = stub.buyRPC(request);
            long finish = System.currentTimeMillis();
            writeToFile((finish - start) + " milliseconds", buyRPCLatency);
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

Seller.java implements the reply interface. The reply interface calls an RPC reply to traverse the reverse path back to the buyer

```java
public class Seller {
    /**
     * Call a RPC reply to traverse the reverse path back to the buyer
     * For the Seller to reply back to the Buyer
     * Implemented in the Seller class
     * @param buyerId the node id of the buyer
     * @param sellerId a reference to the seller (id, IPAddress and port)
     * */
    @Override
    public void reply(int buyerId, PeerId seller) {
        List<Integer> path = replyPath.get(buyerId).pollFirst();
        int previousNodeId = path.remove(path.size() - 1);
        PeerId previousNode = this.getNeighbor(previousNodeId);
        ManagedChannel channel = ManagedChannelBuilder.forAddress(previousNode.getIPAddress(), previousNode.getPort()).usePlaintext().build();
        MarketPlaceGrpc.MarketPlaceBlockingStub stub = MarketPlaceGrpc.newBlockingStub(channel).withWaitForReady();
        logger.info("Send a reply request to peer " + previousNode.getId() + " at port " + previousNode.getIPAddress() + " " +
                + previousNode.getPort() + " for product " + product);
        ReplyRequest replyRequest =
                ReplyRequest.newBuilder().setSellerId(seller).setProduct(this.product.name()).addAllPath(path).build();
        long start = System.currentTimeMillis();
        stub.replyRPC(replyRequest);
        long finish = System.currentTimeMillis();
        writeToFile((finish - start) + " milliseconds", replyRPCLatency);
        channel.shutdown();
        logger.info("Done Reply");
    }
}
```

The lookup, reply buy functions calls the lookRPC, replyRPC and buyRPC, specified as follows:

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
  int32 buyer = 1;
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

The lookup function calls lookupRPC to send lookup request to its neighbor. In the neighbors' server, the lookupRPC will again flood the lookup request to other neighbor. 
The server side code for lookupRPC and replyRPC are as follows: 

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
         * The lookupRPC will acks to the request immediately after it receives the request
         * Then the lookupRPC will check if it can further flood the request
         * */
        @Override
        public void lookupRPC(LookUpRequest request, StreamObserver<Empty> streamObserver) {
            logger.info("Receive lookup request at " + port + " from peer " + request.getBuyer() + " for product " + request.getProduct());
            // propagate the lookup or return a reply
            streamObserver.onNext(Empty.newBuilder().build());
            streamObserver.onCompleted();
            if (request.getHopCount() == 1) {
                // cancel the request
                logger.info("Invalidate lookup request from peer " + request.getBuyer() + " for product" +
                        " " + request.getProduct());
            } else {
                // Flood the lookup request
                logger.info("Flood Lookup request from peer " + request.getBuyer() + " for product " + request.getProduct());
                floodLookUp(request);
            }
        }

        /**
         * The replyRPC will acks to the request immediately after it receives the request
         * Then the replyRPC further continue down the reverse path
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

    /**
     * Further flood the request to neighbors
     * Flooding avoid sending back the request to the previous sender
     * @param request the LookupRequest to further flood
     * */
    protected void floodLookUp(LookUpRequest request) {
        List<Integer> path = new ArrayList<>(request.getPathList());
        path.add(this.id);
        LookUpRequest newRequest =
                LookUpRequest.newBuilder().setBuyer(request.getBuyer()).setProduct(request.getProduct()).setHopCount(request.getHopCount() - 1).addAllPath(path).build();

        Thread[] lookupThread = new Thread[neighbors.size()];
        int counter = 0;

        for (PeerId neighbor: neighbors.values()) {
            // Ignore the neighbor whom send us the request
            if (path.contains(neighbor.getId())) {
                continue;
            }

            lookupThread[counter] = new Thread(() -> {
                // Open a new channel for
                ManagedChannel channel = ManagedChannelBuilder.forAddress(neighbor.getIPAddress(),
                        neighbor.getPort()).usePlaintext().build();
                // Wait for the server to start!
                MarketPlaceGrpc.MarketPlaceBlockingStub stub = MarketPlaceGrpc.newBlockingStub(channel).withWaitForReady();
                logger.info("Send a lookup request to peer " + neighbor.getId() + " at port " + neighbor.getIPAddress() + " " +
                        + neighbor.getPort() + " for product " + request.getProduct());
                long start = System.currentTimeMillis();
                stub.lookupRPC(newRequest);
                long finish = System.currentTimeMillis();
                writeToFile((finish - start) + " milliseconds", lookupRPCLatency);
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
     * reverseReply further send the request back to the buyer via the reverse path
     * @param request the ReplyRequest to send back to buyer
     * */
    protected void reverseReply(ReplyRequest request) {
        List<Integer> path = new ArrayList<>(request.getPathList());
        PeerId fromNode = neighbors.get(path.remove(path.size() - 1));
        PeerId seller = request.getSellerId();

        ManagedChannel channel = ManagedChannelBuilder.forAddress(fromNode.getIPAddress(),
                fromNode.getPort()).usePlaintext().build();
        MarketPlaceGrpc.MarketPlaceBlockingStub stub = MarketPlaceGrpc.newBlockingStub(channel).withWaitForReady();
        logger.info("Send a reply request to peer " + fromNode.getId() + " at " + fromNode.getIPAddress() + " " + fromNode.getPort());
        ReplyRequest replyRequest =
                ReplyRequest.newBuilder().setSellerId(seller).setProduct(request.getProduct()).addAllPath(path).build();
        long start = System.currentTimeMillis();
        stub.replyRPC(replyRequest);
        long finish = System.currentTimeMillis();
        writeToFile((finish - start) + " milliseconds", replyRPCLatency);
        channel.shutdown();
    }
}
```

When Seller's server side receives a LookupRequest, it must check whether it can reply to that LookupRequest. Thus, the Seller overrides the implementation of lookupRPC as follows:

```
public class Seller {
    /**
     * Server side code for the Seller
     * The Seller has somewhat different implementation because
     * (1) only the Seller can sell - the buyRPC server code should only for the server
     * (2) The lookupRPC in the Seller will decide whether to propagate or to reply!
     * */
    private class MarketplaceSellerImpl extends PeerImpl.MarketPlaceImpl {
        /**
         * The lookupRPC will return an ack immediately when it receives the request and check if it can reply.
         * If the Seller isn't selling the product, it further flood the request
         * */
        @Override
        public void lookupRPC(LookUpRequest request, StreamObserver<Empty> streamObserver) {
            logger.info("Receive lookup request at " + Seller.this.getPort() + " from peer " + request.getBuyer() + " " +
                    " for product " + request.getProduct());
            streamObserver.onNext(Empty.newBuilder().build());
            streamObserver.onCompleted();

            // propagate the lookup or return a reply
            // if the seller is selling the product
            if (request.getProduct().equals(product.name())) {
                logger.info("Reply to lookup request from peer " + request.getBuyer() + " for product " + request.getProduct());
                synchronized (this) {
                    if (replyPath.containsKey(request.getBuyer())) {
                        replyPath.get(request.getBuyer()).addLast(new ArrayList<>(request.getPathList()));
                    } else {
                        replyPath.put(request.getBuyer(), new LinkedBlockingDeque<>());
                        replyPath.get(request.getBuyer()).addLast(new ArrayList<>(request.getPathList()));
                    }
                }

                PeerId seller =
                        PeerId.newBuilder().setId(Seller.this.getId()).setIPAddress(Seller.this.getIPAddress()).setPort(Seller.this.getPort()).build();
                Seller.this.reply(request.getBuyer(), seller);
            } else if (request.getHopCount() == 1) {
                // if hopCount is 1 then cannot flood further
                logger.info("Invalidate lookup request from peer " + request.getBuyer() + " for product" +
                        " " + request.getProduct());
            } else {
                logger.info("Flood Lookup request from peer " + request.getBuyer() + " for product " + request.getProduct());
                floodLookUp(request);
            }
        }

        /**
         * This is a synchronized method so only one thread can access it at a time
         * It first verifies again that the buy request is for the product it currently has
         * If the buy product is invalid, then it returns an error message
         * After verification, it process the BuyRequest by decrement the count for the product sold
         * and reply a Ack Sell message. This message is for the Buyer to increment it count for the product
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

    // this method will need to be synchronized!
    // decrement the amountSell and restock :)
    private void processBuy() {
        // decrement the count
        amount -= 1;
        if (amount == 0) {
            logger.info(product.name() + " runs out!!!! Restocking");
            // randomize and restock!
            product = Product.values()[RANDOM.nextInt(3)];
            amount = stock;
            logger.info("After randomize a new product and restock, now selling " + product.name());
        }
    }
}
```

If the Seller can reply, it will call the local reply interface. As we looked at before, the reply interface called a RPC reply to traverse the path back to the Buyer.
The server side code for replyRPC in intermediate peer further traverse the reverse path. 
When the Buyer receives the reply for it requests, it adds the Seller reference into a list for future buy. 

```java
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
                    writeToFile((System.currentTimeMillis() - startLookup) + " milliseconds", lookupResponseTime);
                    logger.info("Add the seller to the list of sellers");
                    Buyer.this.potentialSellers.add(request.getSellerId());
                }
                // discard reply
            } else {
                // continue down the path
                logger.info("Continue down the path for reply");
                reverseReply(request);
            }
        }
    }
    
    public void run() {
        logger.info("All neighbors of ours" + this.getAllNeighbors());
        this.startServer();

        // buyer keeps buying products forever
        // buyer keeps sending out lookup request
        while (true) {
            // buyer perform lookup
            Thread t = new Thread(() -> {
                logger.info("Currently buying " + this.product.name());
                this.lookup(this.product.name(), this.hopCount);

                try {
                    Thread.sleep(3000); // sleep a little bit
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (this.potentialSellers.size() > 0 ) {
                int index = RANDOM.nextInt(this.potentialSellers.size());
                PeerId seller = this.potentialSellers.get(index);
                this.potentialSellers.clear();
                this.buy(seller);
            }

            logger.info("Choose a new product to buy");
            this.product = Product.values()[RANDOM.nextInt(3)];
            logger.info("Now buying " + this.product.name());

        }
    }
}
```

Among that list of sellers, the buyers randomly choose a seller to buy from. 
Once the buy finishes, buyer randomly chooses a new product to buy!

Lookup and Reply are asynchronous. Once a request is received at the server, the server acks and terminates the RPC. 
All processing happens after the ack.  

# Design for lookup and flooding lookup and reverse reply

**1. Synchronous/Blocking Lookup** 

Previously, I tried out a design with a blocking lookup which returns a reference to the Seller. 
When a seller opens up threads to send out a lookup request to a neighbor, the lookup will block in that thread until the result of the lookup request returns. 

* Pros: This design is straightforward and minimizes the amount of thread synchronization
* Cons: Even though a lookup happens in a thread, it blocks that thread, and this can waste resources.

**2. Asynchronous/Non-blocking lookup**

A non-blocking lookup means that once the buyer sends out a lookup request, it terminates the thread execution. 
When the Seller receives the lookup request, it will send out a reply request, which traverses the reverse path back to the Buyer. Reply request is also async/non-blocking. 
In order for the reply request to traverse the reverse path back to the Seller, the RPC/RMI lookup request must include the traversal path. 
Here, I again face a decision-making point: when each peer receives a lookup request, it can either (1) flood the lookup request by calling the local lookup interface, (2) flood the lookup request by again call the RPC/RMI lookup request

I did explore both options, and for option (1), I find the following situation:

Because the local lookup must be as: lookup(product, hop_count), the question is how would the code inside the local lookup know what path for this request would be? 
The options I explored to solve this problem is: have a map where the key is (peer_id, request_id) and value is the path, and then have another map where the key is (product, hop_count) and the value is a list of (peer_id, request_id). 
Then for each peer, they would have a separate thread which will try to flood all the lookup requests it received and empties out the two described maps. 
But another question arises: for the Buyer, which will have to perform a lookup request for itself and floods the lookup request, how would the code inside the local lookup of the Buyer know whether it is flooding others lookup requests?
If the buyer is to send out a lookup request for itself, it would have to create a path parameter, and if the buyer is to flood the lookup request for others, it would have to use the two mentioned maps to find the path.
To this question, I was unable to find an answer. Hence, I decided to not follow this option.

For option (2), what I did is the following:

The local lookup interface would be implemented in the Buyer. What this lookup interface would do is to send out a RPC/RMI lookup request (which includes a path parameter) from Buyer to all of its neighbors. 
What the neighbor would do when it receives the lookup request is it first acks the request and further flood this lookup request via RMI/RPC lookup. 
When the Seller receives the lookup request, it first checks whether it can reply to the lookup request. 
If it can, it adds the path to ```Map<Integer, BlockingDeque<List<Integer>>> replyPath``` for the corresponding key (the buyer Id).
Since the Seller can reply to multiple lookup request at the same time, to achieve mutual exclusion, 
we synchronized the block of code for adding a path into replyPath. 
Furthermore, since lookup requests from a buyer can come different paths, each Buyer ID has a list of paths to traverse back. 
The implementation in replyRPC and reply guarantee that all lookup requests from a buyer are replied with corresponding traversal path.
Similarly for the Buyer, since the ```List<PeerId> potentialSellers``` is a synchronized list, it can handle multiple replies at the same time!

Pros: 
* Lookup and Reply call can be very resource-consuming, but both are non-blocking call, so the Peer's server will have resources to process others' requests instead of waiting on reply.

# Fixed Size ThreadPool vs Thread per request

In the implementation, we decided to go for a Fixed Size ThreadPool. 
The reason for this is that suppose there is a vicious buyer that keeps sending lookup request, 
with a thread per request implementation, the server of the neighboring peers are overloaded with the lookup request from that vicious buyer
and cannot process other requests. 

With the Fixed Size ThreadPool, I allow the Seller's server and the Buyer's server to handle at most (KNeighbor + 3) requests at a time because
each peer must be able to handle request from all its neighbors concurrently, and for Buyer and Seller, they also need to connect with each other for a buy request.
When a request is received, if there is a thread to handle, the server will handle it. If there isn't enough threads, then that request will wait in queue. 
With ThreadPool, we may somewhat avoid the issue of that vicious buyer overloading the neighbors' servers.


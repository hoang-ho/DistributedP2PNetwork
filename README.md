# Lab 1

Professor Serafini's suggestions on Monday March 1:
* We can have lookup, buy, reply functions local to the buyer/seller, and these local functions will do a remote call via RPC/RMI
* For milestone 1, we only need to instantiate a buyer and seller. For milestone 2, each peer will decide the role on its own independently. 

These suggestions led to the following changes in low-level design: 
* Instead of instantiating two Peer objects, we instantiate a Buyer and a Seller object, which , I can have a Buyer and Seller class each having different implementations for local lookup, reply and buy
    * This design will offer better encapsulation and better flexibility for buyer and seller
* The remote call lookup, reply and buy will still have the same functionality 


The Service has the following RPC calls

```
service MarketPlace {
  rpc lookup(BuyRequest) returns (Ack);
  rpc reply(SellerId) returns (Ack);
  rpc buy(PeerId) returns (Ack);
}

message Ack {
  string message = 1;
}
message BuyRequest {
  string productName = 1;
  int32 hopCount = 2;
  repeated PeerId peer = 3;
}
message SellerId {
  string IPAddress = 1;
  int32 port = 2;
  repeated PeerId peer = 3;
}

message PeerId {
  string IPAddress = 1;
  int32 port = 2;
}
```

For each RPC call, the caller have the pass in the required parameter and the callee will return an acknowledge message immediately (except for the buy RPC) and then perform further processing. We'll get to how that processing work!

For now, let's look at the Seller and Buyer class:
```java
public class Seller extends Peer {
    public Seller(int id, String IPAddress, int port, String product, int amount) {
        super(id, IPAddress, port, product, 0, amount);
    }

    public static void main(String[] args) {
        // args: id, port, product, amount, neighbors id, neighbor port
        // test case 1 and 3
        Seller seller = new Seller(Integer.parseInt(args[0]),"localhost", Integer.parseInt(args[1]), args[2],
                Integer.parseInt(args[3]));
        PeerReference peer = new PeerReference(Integer.parseInt(args[4]), "localhost", Integer.parseInt(args[5]));
        seller.setNeighbor(peer);
        seller.startServer();
        seller.blockUntilShutdown();

    }
}
```

The Seller class extends the peer class, its implementation is very simple right now, it receives arguments from command to instantiate a seller peer and run the code! Similarly the buyer class looks like the following:

```java
public class Buyer extends Peer{
    // a logger to log what happens
    private static final Logger logger = Logger.getLogger(Buyer.class.getName());

    public Buyer(int id, String IPAddress, int port, String product, int amount) {
        super(id, IPAddress, port, product, amount, -1);
    }

    /**
     * This is the local lookup of the buyer
     * The buyer's local lookup will do a remote lookup call to all its neighbors
     * @Return an acknowledgement string from the neighbor to verify message is received
     * */
    public String lookup(BuyRequest request) {
        // we only have one neighbors so we only need one acknowledgement message for now
        AtomicReference<String> ackMessage = new AtomicReference<>("");
        Thread t = new Thread(() -> {
            ManagedChannel channel = ManagedChannelBuilder.forAddress(super.getNeighborAddress(),
                    super.getNeighborPort()).usePlaintext().build();
            try {
                // Wait for the server to start!
                MarketPlaceGrpc.MarketPlaceBlockingStub stub = MarketPlaceGrpc.newBlockingStub(channel).withWaitForReady();
                logger.info(super.getPort() + " Send a buy request to " + super.getNeighborAddress() + " " + super.getNeighborPort());
                ackMessage.set(stub.lookup(request).getMessage());
            } finally {
                channel.shutdown();
            }
        });
        t.start();

        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return ackMessage.get();
    }

    // the main function for buyer
    // The buyer keeps sending buy messages
    public static void main(String[] args) {
        // test case 1 and 3
        Buyer buyer = new Buyer(Integer.parseInt(args[0]),"localhost", Integer.parseInt(args[1]), args[2],  0);
        PeerReference peer = new PeerReference(Integer.parseInt(args[3]), "localhost", Integer.parseInt(args[4]));
        buyer.setNeighbor(peer);
        buyer.startServer();

        // buyer keeps buying products forever :D
        // buyer keeps sending out lookup request
        PeerId peerId =
                PeerId.newBuilder().setIPAddress(buyer.getIPAddress()).setPort(buyer.getPort()).build();
        BuyRequest request =
                BuyRequest. newBuilder().setProductName(buyer.getProduct()).setHopCount(1).addPeer(peerId).build();
        while (true) {
            // buyer perform lookup
            buyer.lookup(request);
            try {
                Thread.sleep(3000); // sleep a little bit before lookup again
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
```

The Buyer class also extends the Peer class, in the main function, it receives paramaters from the command line to instantiate a Seller and perform functionality of a Seller: to lookup stuffs to buy! So how do the buying process happens? The logics inside the Peer class:

```java
public class Peer {
    ...
    /**
     * Server side code for each RPC call
     * Both Buyer and Seller have the server.
     * Buyer will be the server for lookup and reply.
     * Seller will be the server for lookup and buy.
     * All RPC calls return an acknowledge message immediately, except for the buy RPC which blocks until the buy
     * completes
     * */
    private class MarketPlaceImpl extends MarketPlaceGrpc.MarketPlaceImplBase {
      @Override
      public void lookup(BuyRequest request, StreamObserver<Ack> streamObserver) {
        logger.info("Receive lookup request at " + port);
        streamObserver.onNext(Ack.newBuilder().setMessage("Ack").build());
        streamObserver.onCompleted();
        lookupHelper(request);
      }
  
      @Override
      public void reply(SellerId message, StreamObserver<Ack> streamObserver) {
        logger.info("Receive reply request at " + port);
        streamObserver.onNext(Ack.newBuilder().setMessage("Ack").build());
        streamObserver.onCompleted();
        replyHelper(message);
      }
  
      @Override
      public void buy(PeerId seller, StreamObserver<Ack> streamObserver) {
        logger.info("Receive a buy request at " + port);
        buyHelper();
        streamObserver.onNext(Ack.newBuilder().setMessage("Ack").build());
        streamObserver.onCompleted();
        logger.info("Finish a transaction. Current have " + amountSell);
      }
    }
  ...
}
```
All RPC calls return an acknowledge message immediately, except for the buy RPC which blocks until the buy completes

When the Seller's client perform a lookup RPC call to the Buyer's server, the Buyer's server first return an acknowledgement immediately and call a local function to further processing. How that processing works is as follow:

```java
public class Peer {
    ...
    private void lookupHelper(BuyRequest request) {
      // if peer is a seller and is selling the product, reply to the caller
      if (amountSell > 0 && request.getProductName().equals(product)) {
        // RPC reply
        int lastIdx = request.getPeerCount() - 1;
        String IPAddress = request.getPeer(lastIdx).getIPAddress();
        int lastPort = request.getPeer(lastIdx).getPort();
        List<PeerId> peers = new ArrayList<>(request.getPeerList());
        peers.remove(lastIdx);
        SellerId message =
                SellerId.newBuilder().setIPAddress(this.IPAddress).setPort(port).addAllPeer(peers).build();
  
        ManagedChannel channel = ManagedChannelBuilder.forAddress(IPAddress, lastPort).usePlaintext().build();
        try {
          MarketPlaceGrpc.MarketPlaceBlockingStub stub = MarketPlaceGrpc.newBlockingStub(channel);
          logger.info("Send reply request at " + port);
          Ack acknowledge = stub.reply(message);
        } finally {
          channel.shutdown();
        }
      }
      // else pass the message to its neighbor, i.e., flooding
      // Nothing for milestone 1
    }
    ...
}
```

For milestone 1, the Seller only need to reply, so it creates a client to perform the RPC reply(SellerId), now Seller becomes reply RPC's client. The Buyer's Server will immediately return an acknowledgement message to the reply RPC and perform further processing via replyHelper():

```java
public class Peer {
    private void replyHelper(SellerId message) {
      // if the message arrives to the original sender
      if (message.getPeerList().size() == 0) {
        // open up a thread to send a buy request
        String sellerIPAddress = message.getIPAddress();
        int sellerPort = message.getPort();
        PeerId peer = PeerId.newBuilder().setIPAddress(IPAddress).setPort(port).build();
        ManagedChannel channel = ManagedChannelBuilder.forAddress(sellerIPAddress, sellerPort).usePlaintext().build();
        try {
          MarketPlaceGrpc.MarketPlaceBlockingStub stub = MarketPlaceGrpc.newBlockingStub(channel);
          logger.info("Send buy request at " + port);
          Ack acknowledge = stub.buy(peer);
          // increment your amount buy this need to be synchronized
          this.amountBuy += 1;
          logger.info("Buying " + product + " succeeds. Current have " + this.amountBuy + " " + product);
        } finally {
          channel.shutdown();
        }
      }
      // else open up a thread pass the message back the path
    }
}
```

For milestone 1, we don't need to traverse back the path, so the reply(SellerId) arrives at the Buyer, and the Buyer now creates a client to perform a buy RPC. For the buyHelper, we in fact just need to decrement the amount in the stock and restock if needed, so no further RPC is needed. Once the buyHelper() finish, the Seller return an acknowledgement message to the Buyer.

To run:
``` 
$ ./gradlew clean build
```

For **test case 1**, in one terminal run:
```
// to create a seller with id 0, at port 8080, sell fish, with stock 3, having a neighbor with id 1 at port 8081
$ gradle p2pSeller --args="0 8080 fish 3 1 8081"
```

In another terminal run: 
```
// to create a buyer with id 1, at port 8081, having a neighbor with id 0 at port 8080
$ gradle p2pBuyer --args="1 8081 fish 0 8080"
```

The log for test case 1, should look like this
```
// Seller process
> Task :p2pSeller
Mar 01, 2021 7:01:37 PM com.p2p.grpc.Peer startServer
INFO: Starting a server at localhost 8080
Mar 01, 2021 7:03:01 PM com.p2p.grpc.Peer$MarketPlaceImpl lookup
INFO: Receive lookup request at 8080
Mar 01, 2021 7:03:01 PM com.p2p.grpc.Peer lookupHelper
INFO: Send reply request at 8080
Mar 01, 2021 7:03:02 PM com.p2p.grpc.Peer$MarketPlaceImpl buy
INFO: Receive a buy request at 8080
Mar 01, 2021 7:03:02 PM com.p2p.grpc.Peer$MarketPlaceImpl buy
INFO: Finish a transaction. Current have 2
Mar 01, 2021 7:03:04 PM com.p2p.grpc.Peer$MarketPlaceImpl lookup
INFO: Receive lookup request at 8080
Mar 01, 2021 7:03:04 PM com.p2p.grpc.Peer lookupHelper
INFO: Send reply request at 8080
Mar 01, 2021 7:03:04 PM com.p2p.grpc.Peer$MarketPlaceImpl buy
INFO: Receive a buy request at 8080
Mar 01, 2021 7:03:04 PM com.p2p.grpc.Peer$MarketPlaceImpl buy
INFO: Finish a transaction. Current have 1
Mar 01, 2021 7:03:07 PM com.p2p.grpc.Peer$MarketPlaceImpl lookup
INFO: Receive lookup request at 8080
Mar 01, 2021 7:03:07 PM com.p2p.grpc.Peer lookupHelper
INFO: Send reply request at 8080
Mar 01, 2021 7:03:07 PM com.p2p.grpc.Peer$MarketPlaceImpl buy
INFO: Receive a buy request at 8080
Mar 01, 2021 7:03:07 PM com.p2p.grpc.Peer buyHelper
INFO: fish runs out!!!! Restocking
Mar 01, 2021 7:03:07 PM com.p2p.grpc.Peer$MarketPlaceImpl buy
INFO: Finish a transaction. Current have 3
Mar 01, 2021 7:03:10 PM com.p2p.grpc.Peer$MarketPlaceImpl lookup
INFO: Receive lookup request at 8080
Mar 01, 2021 7:03:10 PM com.p2p.grpc.Peer lookupHelper
INFO: Send reply request at 8080
Mar 01, 2021 7:03:10 PM com.p2p.grpc.Peer$MarketPlaceImpl buy
INFO: Receive a buy request at 8080
Mar 01, 2021 7:03:10 PM com.p2p.grpc.Peer$MarketPlaceImpl buy
INFO: Finish a transaction. Current have 2
Mar 01, 2021 7:03:13 PM com.p2p.grpc.Peer$MarketPlaceImpl lookup
INFO: Receive lookup request at 8080
Mar 01, 2021 7:03:13 PM com.p2p.grpc.Peer lookupHelper
INFO: Send reply request at 8080
Mar 01, 2021 7:03:13 PM com.p2p.grpc.Peer$MarketPlaceImpl buy
INFO: Receive a buy request at 8080
Mar 01, 2021 7:03:13 PM com.p2p.grpc.Peer$MarketPlaceImpl buy
INFO: Finish a transaction. Current have 1
```

``` 
// Buyer side
> Task :p2pBuyer
Mar 01, 2021 7:03:01 PM com.p2p.grpc.Peer startServer
INFO: Starting a server at localhost 8081
Mar 01, 2021 7:03:01 PM com.p2p.grpc.Buyer lambda$lookup$0
INFO: 8081 Send a buy request to localhost 8080
Mar 01, 2021 7:03:02 PM com.p2p.grpc.Peer$MarketPlaceImpl reply
INFO: Receive reply request at 8081
Mar 01, 2021 7:03:02 PM com.p2p.grpc.Peer replyHelper
INFO: Send buy request at 8081
Mar 01, 2021 7:03:02 PM com.p2p.grpc.Peer replyHelper
INFO: Buying fish succeeds. Current have 1 fish
Mar 01, 2021 7:03:04 PM com.p2p.grpc.Buyer lambda$lookup$0
INFO: 8081 Send a buy request to localhost 8080
Mar 01, 2021 7:03:04 PM com.p2p.grpc.Peer$MarketPlaceImpl reply
INFO: Receive reply request at 8081
Mar 01, 2021 7:03:04 PM com.p2p.grpc.Peer replyHelper
INFO: Send buy request at 8081
Mar 01, 2021 7:03:04 PM com.p2p.grpc.Peer replyHelper
INFO: Buying fish succeeds. Current have 2 fish
Mar 01, 2021 7:03:07 PM com.p2p.grpc.Buyer lambda$lookup$0
INFO: 8081 Send a buy request to localhost 8080
Mar 01, 2021 7:03:07 PM com.p2p.grpc.Peer$MarketPlaceImpl reply
INFO: Receive reply request at 8081
Mar 01, 2021 7:03:07 PM com.p2p.grpc.Peer replyHelper
INFO: Send buy request at 8081
Mar 01, 2021 7:03:07 PM com.p2p.grpc.Peer replyHelper
INFO: Buying fish succeeds. Current have 3 fish
Mar 01, 2021 7:03:10 PM com.p2p.grpc.Buyer lambda$lookup$0
INFO: 8081 Send a buy request to localhost 8080
Mar 01, 2021 7:03:10 PM com.p2p.grpc.Peer$MarketPlaceImpl reply
INFO: Receive reply request at 8081
Mar 01, 2021 7:03:10 PM com.p2p.grpc.Peer replyHelper
INFO: Send buy request at 8081
Mar 01, 2021 7:03:10 PM com.p2p.grpc.Peer replyHelper
INFO: Buying fish succeeds. Current have 4 fish
Mar 01, 2021 7:03:13 PM com.p2p.grpc.Buyer lambda$lookup$0
INFO: 8081 Send a buy request to localhost 8080
Mar 01, 2021 7:03:13 PM com.p2p.grpc.Peer$MarketPlaceImpl reply
INFO: Receive reply request at 8081
Mar 01, 2021 7:03:13 PM com.p2p.grpc.Peer replyHelper
INFO: Send buy request at 8081
Mar 01, 2021 7:03:13 PM com.p2p.grpc.Peer replyHelper
INFO: Buying fish succeeds. Current have 5 fish
Mar 01, 2021 7:03:16 PM com.p2p.grpc.Buyer lambda$lookup$0
INFO: 8081 Send a buy request to localhost 8080
Mar 01, 2021 7:03:16 PM com.p2p.grpc.Peer$MarketPlaceImpl reply
INFO: Receive reply request at 8081
Mar 01, 2021 7:03:16 PM com.p2p.grpc.Peer replyHelper
INFO: Send buy request at 8081
Mar 01, 2021 7:03:16 PM com.p2p.grpc.Peer replyHelper
INFO: Buying fish succeeds. Current have 6 fish
Mar 01, 2021 7:03:19 PM com.p2p.grpc.Buyer lambda$lookup$0
INFO: 8081 Send a buy request to localhost 8080
```

For **test case 3**, in one terminal run:

```
$ gradle p2pSeller --args="0 8080 boar 3 1 8081"
```

in another terminal run:

``` 
$ gradle p2pBuyer --args="1 8081 fish 0 8080" 
```

Your log should look like:

```
// Seller side
Mar 01, 2021 7:10:58 PM com.p2p.grpc.Peer startServer
INFO: Starting a server at localhost 8080
Mar 01, 2021 7:11:09 PM com.p2p.grpc.Peer$MarketPlaceImpl lookup
INFO: Receive lookup request at 8080
Mar 01, 2021 7:11:12 PM com.p2p.grpc.Peer$MarketPlaceImpl lookup
INFO: Receive lookup request at 8080
Mar 01, 2021 7:11:15 PM com.p2p.grpc.Peer$MarketPlaceImpl lookup
INFO: Receive lookup request at 8080
Mar 01, 2021 7:11:18 PM com.p2p.grpc.Peer$MarketPlaceImpl lookup
INFO: Receive lookup request at 8080
Mar 01, 2021 7:11:21 PM com.p2p.grpc.Peer$MarketPlaceImpl lookup
INFO: Receive lookup request at 8080
Mar 01, 2021 7:11:24 PM com.p2p.grpc.Peer$MarketPlaceImpl lookup
INFO: Receive lookup request at 8080
Mar 01, 2021 7:11:27 PM com.p2p.grpc.Peer$MarketPlaceImpl lookup
INFO: Receive lookup request at 8080
Mar 01, 2021 7:11:30 PM com.p2p.grpc.Peer$MarketPlaceImpl lookup
INFO: Receive lookup request at 8080
Mar 01, 2021 7:11:33 PM com.p2p.grpc.Peer$MarketPlaceImpl lookup
INFO: Receive lookup request at 8080
Mar 01, 2021 7:11:36 PM com.p2p.grpc.Peer$MarketPlaceImpl lookup
INFO: Receive lookup request at 8080
Mar 01, 2021 7:11:39 PM com.p2p.grpc.Peer$MarketPlaceImpl lookup
INFO: Receive lookup request at 8080
Mar 01, 2021 7:11:42 PM com.p2p.grpc.Peer$MarketPlaceImpl lookup
INFO: Receive lookup request at 8080
<===========--> 85% EXECUTING [50s]
```

```
// Buyer side
> Task :p2pBuyer
Mar 01, 2021 7:11:08 PM com.p2p.grpc.Peer startServer
INFO: Starting a server at localhost 8081
Mar 01, 2021 7:11:08 PM com.p2p.grpc.Buyer lambda$lookup$0
INFO: 8081 Send a buy request to localhost 8080
Mar 01, 2021 7:11:12 PM com.p2p.grpc.Buyer lambda$lookup$0
INFO: 8081 Send a buy request to localhost 8080
Mar 01, 2021 7:11:15 PM com.p2p.grpc.Buyer lambda$lookup$0
INFO: 8081 Send a buy request to localhost 8080
Mar 01, 2021 7:11:18 PM com.p2p.grpc.Buyer lambda$lookup$0
INFO: 8081 Send a buy request to localhost 8080
Mar 01, 2021 7:11:21 PM com.p2p.grpc.Buyer lambda$lookup$0
INFO: 8081 Send a buy request to localhost 8080
Mar 01, 2021 7:11:24 PM com.p2p.grpc.Buyer lambda$lookup$0
INFO: 8081 Send a buy request to localhost 8080
Mar 01, 2021 7:11:27 PM com.p2p.grpc.Buyer lambda$lookup$0
INFO: 8081 Send a buy request to localhost 8080
Mar 01, 2021 7:11:30 PM com.p2p.grpc.Buyer lambda$lookup$0
INFO: 8081 Send a buy request to localhost 8080
Mar 01, 2021 7:11:33 PM com.p2p.grpc.Buyer lambda$lookup$0
INFO: 8081 Send a buy request to localhost 8080
Mar 01, 2021 7:11:36 PM com.p2p.grpc.Buyer lambda$lookup$0
INFO: 8081 Send a buy request to localhost 8080
Mar 01, 2021 7:11:39 PM com.p2p.grpc.Buyer lambda$lookup$0
INFO: 8081 Send a buy request to localhost 8080
Mar 01, 2021 7:11:42 PM com.p2p.grpc.Buyer lambda$lookup$0
INFO: 8081 Send a buy request to localhost 8080
<===========--> 85% EXECUTING [39s]
```

### TODO 
- [X] Milestone 1 test case 1
- [ ] Milestone 1 test case 2
- [X] Milestone 1 test case 3
- [ ] Milestone 1 Randomly assigned role
- [ ] Milestone 2 Generalized the current implementation to N peers :D 
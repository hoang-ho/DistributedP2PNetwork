# Lab 1

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

For each RPC call, the caller have the pass in the required parameter and the callee will return an acknowledge message immediately (except for the buy RPC) and then perform further processing

The code for all peers are in main/java/com/p2p/grpc/Peer.java. We have Runner class in Runner.java to simulate the marketplace service

```java
public class Runner {
    public static void main(String[] args) {
        // Initialize the peer
        Peer peer1 = new Peer(1, "localhost",8080);
        Peer peer2 = new Peer(2, "localhost",8081);
        peer1.setNeighbor(peer2);
        peer2.setNeighbor(peer1);

        // create two threads to run the peer
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                peer1.run(3, 0, "fish");
            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                peer2.run(-1, 0, "fish");
            }
        });

        t1.start();
        t2.start();
    }
}
```

The run function for peer is as follow:

```java
public class Peer {
    ...
    public void run(int amountSell, int amountBuy, String product) {
        this.stock = amountSell;
        this.amountSell = this.stock;
        this.amountBuy = amountBuy;
        this.product = product;
        Server server =
                ServerBuilder.forPort(port).addService(new MarketPlaceImpl()).executor(Executors.newFixedThreadPool(10)).build();

        try {
            server.start();
            logger.info("starting a server at " + IPAddress + " " + port);
            // start buying if we are buyer
            if (this.stock < 0) {
                // perform lookup
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ManagedChannel channel = ManagedChannelBuilder.forAddress(neighbors.getIPAddress(),
                                neighbors.getPort()).usePlaintext().build();
                        try {
                            MarketPlaceGrpc.MarketPlaceBlockingStub stub = MarketPlaceGrpc.newBlockingStub(channel);
                            PeerId peerId = PeerId.newBuilder().setIPAddress(IPAddress).setPort(port).build();
                            BuyRequest request =
                                    BuyRequest.newBuilder().setProductName(product).setHopCount(1).addPeer(peerId).build();
                            logger.info(port + " send a buy request to " + neighbors.IPAddress + " " + neighbors.port);
                            stub.lookup(request);
                        } finally {
                            channel.shutdown();
                        }
                    }
                });
                t.start();
            }
            server.awaitTermination();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    ...
}
```

Here, I create a server to listen for RPC, and for the buyer peer (stock < 0), I create a client inside a thread to perform the lookup RPC call. See that both server and client are created with ThreadPool. How the callee will handle each RPC is as follow:

```java
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
        }
    }
```

The callee will return an acknowledgement message to the lookup RPC and perform further processing. How that processing works is as follow:

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
    }
    ...
}
```

The lookupHelper will decide whether to reply or to flood. For milestone 1, lookup RPC's callee only need to reply, so its creates a client and perform the RPC reply(SellerId), now it becomes the reply RPC's caller. The reply RPC's callee will immediately return an acknowledgement message and perform further processing via replyHelper():

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
            } finally {
                channel.shutdown();
            }
        }
        // else open up a thread pass the message back the path
    }
}
```

For milestone 1, we don't need to traverse back the path, so the reply(SellerId) arrives at the original sender, and the sender will further call a buy RPC. For the buyHelper, we in fact just need to decrement the amount in the stock and restock if needed, so no further RPC is needed. Once the buyHelper() finish, the callee return an acknowledgement message to the caller.

To run:
``` 
$ ./gradlew clean build
```

This is to generate the code for gRPC and to compile the code. Then run the Runner.java file. The log should be as follow:

```
Feb 28, 2021 1:24:45 AM com.p2p.grpc.Peer run
INFO: starting a server at localhost 8080
Feb 28, 2021 1:24:45 AM com.p2p.grpc.Peer run
INFO: starting a server at localhost 8081
Feb 28, 2021 1:24:45 AM com.p2p.grpc.Peer$1 run
INFO: 8081 send a buy request to localhost 8080
Feb 28, 2021 1:24:46 AM com.p2p.grpc.Peer$MarketPlaceImpl lookup
INFO: Receive lookup request at 8080
Feb 28, 2021 1:24:46 AM com.p2p.grpc.Peer lookupHelper
INFO: Send reply request at 8080
Feb 28, 2021 1:24:46 AM com.p2p.grpc.Peer$MarketPlaceImpl reply
INFO: Receive reply request at 8081
Feb 28, 2021 1:24:46 AM com.p2p.grpc.Peer replyHelper
INFO: Send buy request at 8081
Feb 28, 2021 1:24:46 AM com.p2p.grpc.Peer$MarketPlaceImpl buy
INFO: Receive a buy request at 8080
Feb 28, 2021 1:24:46 AM com.p2p.grpc.Peer replyHelper
INFO: Buying fish succeeds.
```

# Lab 1

## Build and Test

### Compiling

The project is build with Java 1.8, Gradle 6.7, and gRPC 1.36.0

To run:
``` 
$ ./gradlew clean build
```

All hopcount is hardcoded to be 1 for milestone 1. Professor confirmed that it's okay to have the two peers: one buyer, one seller for milestone 1.

### Testing

To follow the instruction below or run the **test.sh** script. 

BE AWARE: In **test.sh**, we run two processes in parallel and terminate them, so it will throw errors such as: "FAILURE: Build failed with an exception." or "SEVERE: Exception while executing runnable io.grpc.internal.ServerImplHalfClosed". These errors are due to we stop the gradle tasks while the communication between the two processes are happening. JUST IGNORE IT! 

For **test case 1**, in one terminal run:
```
// to create a seller with id 0, at port 8080, sell fish, with stock m = 3, having a neighbor with id 1 at port 8081
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
Mar 03, 2021 8:42:29 AM com.p2p.grpc.Seller startServer
INFO: Starting a server at localhost 8080
Mar 03, 2021 8:42:38 AM com.p2p.grpc.Seller$MarketPlaceBuyerImpl lookupRPC
INFO: Receive lookup request at 8080
Mar 03, 2021 8:42:38 AM com.p2p.grpc.Seller$MarketPlaceBuyerImpl buyRPC
INFO: Receive a buy request at 8080
Mar 03, 2021 8:42:38 AM com.p2p.grpc.Seller$MarketPlaceBuyerImpl buyRPC
INFO: Finish a transaction. Current have 2
Mar 03, 2021 8:42:41 AM com.p2p.grpc.Seller$MarketPlaceBuyerImpl lookupRPC
INFO: Receive lookup request at 8080
Mar 03, 2021 8:42:41 AM com.p2p.grpc.Seller$MarketPlaceBuyerImpl buyRPC
INFO: Receive a buy request at 8080
Mar 03, 2021 8:42:41 AM com.p2p.grpc.Seller$MarketPlaceBuyerImpl buyRPC
INFO: Finish a transaction. Current have 1
Mar 03, 2021 8:42:44 AM com.p2p.grpc.Seller$MarketPlaceBuyerImpl lookupRPC
INFO: Receive lookup request at 8080
Mar 03, 2021 8:42:44 AM com.p2p.grpc.Seller$MarketPlaceBuyerImpl buyRPC
INFO: Receive a buy request at 8080
Mar 03, 2021 8:42:44 AM com.p2p.grpc.Seller processBuy
INFO: FISH runs out!!!! Restocking
Mar 03, 2021 8:42:44 AM com.p2p.grpc.Seller processBuy
INFO: After randomize a new product and restock, now selling SALT
Mar 03, 2021 8:42:44 AM com.p2p.grpc.Seller$MarketPlaceBuyerImpl buyRPC
INFO: Finish a transaction. Current have 3
Mar 03, 2021 8:42:47 AM com.p2p.grpc.Seller$MarketPlaceBuyerImpl lookupRPC
INFO: Receive lookup request at 8080
Mar 03, 2021 8:42:50 AM com.p2p.grpc.Seller$MarketPlaceBuyerImpl lookupRPC
INFO: Receive lookup request at 8080
Mar 03, 2021 8:42:53 AM com.p2p.grpc.Seller$MarketPlaceBuyerImpl lookupRPC
INFO: Receive lookup request at 8080
Mar 03, 2021 8:42:56 AM com.p2p.grpc.Seller$MarketPlaceBuyerImpl lookupRPC
INFO: Receive lookup request at 8080
Mar 03, 2021 8:43:00 AM com.p2p.grpc.Seller$MarketPlaceBuyerImpl lookupRPC
INFO: Receive lookup request at 8080
<===========--> 85% EXECUTING [38s]
```

```
> Task :p2pBuyer
Mar 03, 2021 8:42:38 AM com.p2p.grpc.PeerImpl startServer
INFO: Starting a server at localhost 8081
Mar 03, 2021 8:42:38 AM com.p2p.grpc.PeerImpl lookup
INFO: Send a lookup request to localhost 8080
Mar 03, 2021 8:42:38 AM com.p2p.grpc.Buyer buy
INFO: Send a buy request to peer 0
Mar 03, 2021 8:42:38 AM com.p2p.grpc.Buyer buy
INFO: Bought FISH from peer 0. Buyer current has 1 FISH
Mar 03, 2021 8:42:41 AM com.p2p.grpc.PeerImpl lookup
INFO: Send a lookup request to localhost 8080
Mar 03, 2021 8:42:41 AM com.p2p.grpc.Buyer buy
INFO: Send a buy request to peer 0
Mar 03, 2021 8:42:41 AM com.p2p.grpc.Buyer buy
INFO: Bought FISH from peer 0. Buyer current has 2 FISH
Mar 03, 2021 8:42:44 AM com.p2p.grpc.PeerImpl lookup
INFO: Send a lookup request to localhost 8080
Mar 03, 2021 8:42:44 AM com.p2p.grpc.Buyer buy
INFO: Send a buy request to peer 0
Mar 03, 2021 8:42:44 AM com.p2p.grpc.Buyer buy
INFO: Bought FISH from peer 0. Buyer current has 3 FISH
Mar 03, 2021 8:42:47 AM com.p2p.grpc.PeerImpl lookup
INFO: Send a lookup request to localhost 8080
Mar 03, 2021 8:42:50 AM com.p2p.grpc.PeerImpl lookup
INFO: Send a lookup request to localhost 8080
Mar 03, 2021 8:42:53 AM com.p2p.grpc.PeerImpl lookup
INFO: Send a lookup request to localhost 8080
Mar 03, 2021 8:42:56 AM com.p2p.grpc.PeerImpl lookup
INFO: Send a lookup request to localhost 8080
Mar 03, 2021 8:43:00 AM com.p2p.grpc.PeerImpl lookup
INFO: Send a lookup request to localhost 8080
<===========--> 85% EXECUTING [27s]
```

For **test case 2**, in one terminal run:

```
// to create a seller of boar with stock 3 with id 0 at port 8080 and have a neighbor with id 1 at port 8081
$ gradle p2pSeller --args="0 8080 boar 3 1 8081"
```

in another terminal run:

``` 
// to create a buyer of fish with id 0 at port 8081 and have a neighbor with id 0 at port 8080
$ gradle p2pBuyer --args="1 8081 fish 0 8080" 
```

Your log should look like:

```
// Seller side
> Task :p2pSeller
Mar 03, 2021 8:49:32 AM com.p2p.grpc.Seller startServer
INFO: Starting a server at localhost 8080
Mar 03, 2021 8:49:48 AM com.p2p.grpc.Seller$MarketPlaceBuyerImpl lookupRPC
INFO: Receive lookup request at 8080
Mar 03, 2021 8:49:51 AM com.p2p.grpc.Seller$MarketPlaceBuyerImpl lookupRPC
INFO: Receive lookup request at 8080
Mar 03, 2021 8:49:54 AM com.p2p.grpc.Seller$MarketPlaceBuyerImpl lookupRPC
INFO: Receive lookup request at 8080
Mar 03, 2021 8:49:57 AM com.p2p.grpc.Seller$MarketPlaceBuyerImpl lookupRPC
INFO: Receive lookup request at 8080
Mar 03, 2021 8:50:00 AM com.p2p.grpc.Seller$MarketPlaceBuyerImpl lookupRPC
INFO: Receive lookup request at 8080
Mar 03, 2021 8:50:03 AM com.p2p.grpc.Seller$MarketPlaceBuyerImpl lookupRPC
INFO: Receive lookup request at 8080
Mar 03, 2021 8:50:06 AM com.p2p.grpc.Seller$MarketPlaceBuyerImpl lookupRPC
INFO: Receive lookup request at 8080
<===========--> 85% EXECUTING [37s]
```

```
// Buyer side
> Task :p2pBuyer
Mar 03, 2021 8:49:47 AM com.p2p.grpc.PeerImpl startServer
INFO: Starting a server at localhost 8081
Mar 03, 2021 8:49:47 AM com.p2p.grpc.PeerImpl lookup
INFO: Send a lookup request to localhost 8080
Mar 03, 2021 8:49:51 AM com.p2p.grpc.PeerImpl lookup
INFO: Send a lookup request to localhost 8080
Mar 03, 2021 8:49:54 AM com.p2p.grpc.PeerImpl lookup
INFO: Send a lookup request to localhost 8080
Mar 03, 2021 8:49:57 AM com.p2p.grpc.PeerImpl lookup
INFO: Send a lookup request to localhost 8080
Mar 03, 2021 8:50:00 AM com.p2p.grpc.PeerImpl lookup
INFO: Send a lookup request to localhost 8080
Mar 03, 2021 8:50:03 AM com.p2p.grpc.PeerImpl lookup
INFO: Send a lookup request to localhost 8080
Mar 03, 2021 8:50:06 AM com.p2p.grpc.PeerImpl lookup
INFO: Send a lookup request to localhost 8080
<===========--> 85% EXECUTING [23s]
```

### TODO 
- [X] Milestone 1 test case 1
- [X] Milestone 1 test case 2
- [X] Milestone 1 Randomly assigned role
- [ ] Milestone 2 Generalized the current implementation to N peers :D 
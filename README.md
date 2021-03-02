# Lab 1

## Build

The project is build with Java 1.8, Gradle 6.7, and gRPC 1.36.0

To run:
``` 
$ ./gradlew clean build
```

All hopcount is hardcoded to be 1 for milestone 1. Professor confirmed that it's okay to have the two peers: one buyer, one seller for milestone 1.

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
Mar 02, 2021 5:29:02 PM com.p2p.grpc.PeerImpl startServer
INFO: Starting a server at localhost 8080
Mar 02, 2021 5:29:10 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080
Mar 02, 2021 5:29:10 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl buyRPC
INFO: Receive a buy request at 8080
Mar 02, 2021 5:29:10 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl buyRPC
INFO: Finish a transaction. Current have 2
Mar 02, 2021 5:29:13 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080
Mar 02, 2021 5:29:13 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl buyRPC
INFO: Receive a buy request at 8080
Mar 02, 2021 5:29:13 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl buyRPC
INFO: Finish a transaction. Current have 1
Mar 02, 2021 5:29:16 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080
Mar 02, 2021 5:29:17 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl buyRPC
INFO: Receive a buy request at 8080
Mar 02, 2021 5:29:17 PM com.p2p.grpc.PeerImpl processBuy
INFO: FISH runs out!!!! Restocking
Mar 02, 2021 5:29:17 PM com.p2p.grpc.PeerImpl processBuy
INFO: After randomize a new product and restock, now selling BOAR
Mar 02, 2021 5:29:17 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl buyRPC
INFO: Finish a transaction. Current have 3
Mar 02, 2021 5:29:20 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080
Mar 02, 2021 5:29:23 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080
Mar 02, 2021 5:29:26 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080
<===========--> 85% EXECUTING [27s]
```

```
> Task :p2pBuyer
Mar 02, 2021 5:29:10 PM com.p2p.grpc.PeerImpl startServer
INFO: Starting a server at localhost 8081
Mar 02, 2021 5:29:10 PM com.p2p.grpc.PeerImpl lookup
INFO: Send a lookup request to localhost 8080
Mar 02, 2021 5:29:10 PM com.p2p.grpc.Buyer main
INFO: Is this initialized true
Mar 02, 2021 5:29:10 PM com.p2p.grpc.Buyer buy
INFO: Send a buy request to peer 0
Mar 02, 2021 5:29:10 PM com.p2p.grpc.Buyer buy
INFO: Bought FISH from peer 0. Buyer current has 1 FISH
Mar 02, 2021 5:29:13 PM com.p2p.grpc.PeerImpl lookup
INFO: Send a lookup request to localhost 8080
Mar 02, 2021 5:29:13 PM com.p2p.grpc.Buyer main
INFO: Is this initialized true
Mar 02, 2021 5:29:13 PM com.p2p.grpc.Buyer buy
INFO: Send a buy request to peer 0
Mar 02, 2021 5:29:13 PM com.p2p.grpc.Buyer buy
INFO: Bought FISH from peer 0. Buyer current has 2 FISH
Mar 02, 2021 5:29:16 PM com.p2p.grpc.PeerImpl lookup
INFO: Send a lookup request to localhost 8080
Mar 02, 2021 5:29:16 PM com.p2p.grpc.Buyer main
INFO: Is this initialized true
Mar 02, 2021 5:29:16 PM com.p2p.grpc.Buyer buy
INFO: Send a buy request to peer 0
Mar 02, 2021 5:29:17 PM com.p2p.grpc.Buyer buy
INFO: Bought FISH from peer 0. Buyer current has 3 FISH
Mar 02, 2021 5:29:20 PM com.p2p.grpc.PeerImpl lookup
INFO: Send a lookup request to localhost 8080
Mar 02, 2021 5:29:23 PM com.p2p.grpc.PeerImpl lookup
INFO: Send a lookup request to localhost 8080
Mar 02, 2021 5:29:26 PM com.p2p.grpc.PeerImpl lookup
INFO: Send a lookup request to localhost 8080
Mar 02, 2021 5:29:29 PM com.p2p.grpc.PeerImpl lookup
INFO: Send a lookup request to localhost 8080
<===========--> 85% EXECUTING [26s] 
```

For **test case 2**, in one terminal run:

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
> Task :p2pSeller
Mar 02, 2021 5:32:01 PM com.p2p.grpc.PeerImpl startServer
INFO: Starting a server at localhost 8080
Mar 02, 2021 5:32:09 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080
Mar 02, 2021 5:32:12 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080
Mar 02, 2021 5:32:15 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080
Mar 02, 2021 5:32:18 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080
<===========--> 85% EXECUTING [19s]
> :p2pSeller
```

```
// Buyer side
> Task :p2pBuyer
Mar 02, 2021 5:32:08 PM com.p2p.grpc.PeerImpl startServer
INFO: Starting a server at localhost 8081
Mar 02, 2021 5:32:09 PM com.p2p.grpc.PeerImpl lookup
INFO: Send a lookup request to localhost 8080
Mar 02, 2021 5:32:12 PM com.p2p.grpc.PeerImpl lookup
INFO: Send a lookup request to localhost 8080
Mar 02, 2021 5:32:15 PM com.p2p.grpc.PeerImpl lookup
INFO: Send a lookup request to localhost 8080
Mar 02, 2021 5:32:18 PM com.p2p.grpc.PeerImpl lookup
INFO: Send a lookup request to localhost 8080
Mar 02, 2021 5:32:21 PM com.p2p.grpc.PeerImpl lookup
INFO: Send a lookup request to localhost 8080
<===========--> 85% EXECUTING [18s]
> :p2pBuyer
```

### TODO 
- [X] Milestone 1 test case 1
- [X] Milestone 1 test case 2
- [X] Milestone 1 Randomly assigned role
- [ ] Milestone 2 Generalized the current implementation to N peers :D 
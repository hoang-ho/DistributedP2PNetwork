## How to make sure the test is correct?

### Milestone 1

Professor mentions that it's okay to have fixed role (buyer, seller, general peer). For milestone 1 test cases, we must have a Seller and a Buyer, so I created two processes: one for the Buyer and one for the Seller, and they communicate with each other via the implemented interface. 

When I run test case 1 (a buyer of fish and a seller of fish), based on the log, I can see that all the fish in the stock got sold. Then the Seller randomly pick another product to sell, and I can see that if the product isn't fish, then nothing is sold and Buyer keeps looking forever.

When I run test case 2 (a buyer of fish and a seller of boar), based on the log, I can see that no product is sold and the Buyer keeps sending out lookup request and Seller keeps receiving it, but since hopcount is hardcoded to 1 in Milestone 1, the lookup request isn't propagated.

BE AWARE: In **test.sh**, we run two processes in parallel and terminate them (or else they will run forever), so it will throw errors such as: "FAILURE: Build failed with an exception." or "SEVERE: Exception while executing runnable io.grpc.internal.ServerImplHalfClosed". These errors are due to we stop the gradle tasks while the communication between the two processes are happening. JUST IGNORE IT!

To run the test, follow the instruction below or run the **test.sh** script.

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
## How to make sure the test is correct?

### Milestone 2

First build the script with:

```
$ ./gradlew clean build
```

**Test case 1**:
Assumption: Each peer has at most K neighbors and all Buyer are exactly 2 hops away from Seller

Paste the following in the config file:

```
0 buyer 8080 boar 3 8083
1 buyer 8081 boar 3 8083
2 buyer 8082 boar 3 8083
3 peer 8083 0 8080 1 8081 2 8082 4 8084
4 seller 8084 boar 3 3 8083
```

Open up 5 terminals to run the test case. It's easier to see the log if open up 5 terminals

Seller  terminal

```
// to create a Seller with peerId 4 with 1 neighbors

$ gradle p2pSeller --args="4 1"
```

Peer 3 terminal

```
// to create a peer with peerId 2 with 1 neighbors

$ gradle p2pPeer --args="3 1" 
```

Buyer 0 terminal 

```
// to create a buyer with peerId 0 with 1 neighbors

$ gradle p2pBuyer --args="0 1" 
```

Buyer 1 terminal

```
// to create a buyer with peerId 1 with 1 neighbors

$ gradle p2pBuyer --args="1 1" 
```

Buyer 2 terminal

```
// to create a buyer with peerId 2 with 1 neighbors

$ gradle p2pBuyer --args="2 1" 
```

You can wait for it to execute for a minute or two. The terminal results should look like this:

Seller Terminal:

```
(base) Hoangs-MacBook-Pro:BuyerSellerNetwork hoangho$ gradle p2pSeller --args="4 1"

> Task :compileJava
Note: /Users/hoangho/IdeaProjects/BuyerSellerNetwork/src/main/java/com/p2p/utils/Pair.java uses unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.

> Task :p2pSeller
Mar 10, 2021 8:27:15 PM com.p2p.grpc.Seller startServer
INFO: Starting a server at localhost 8084
Mar 10, 2021 8:27:33 PM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8084 from peer 3  for product BOAR
Mar 10, 2021 8:27:33 PM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Reply to lookup request from peer 3 for product BOAR
Mar 10, 2021 8:27:33 PM com.p2p.grpc.Seller reply
INFO: Send a reply request to peer 3 at port localhost 8083 for product BOAR
Mar 10, 2021 8:27:33 PM com.p2p.grpc.Seller reply
INFO: Reply path size 1
Mar 10, 2021 8:27:34 PM com.p2p.grpc.Seller reply
INFO: Done Reply
Mar 10, 2021 8:27:36 PM com.p2p.grpc.Seller$MarketplaceSellerImpl buyRPC
INFO: Receive a buy request at 8084 from peer 0 for product BOAR
Mar 10, 2021 8:27:36 PM com.p2p.grpc.Seller$MarketplaceSellerImpl buyRPC
INFO: Finish a transaction for peer 0. Currently, having: 2
Mar 10, 2021 8:27:36 PM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8084 from peer 3  for product SALT
Mar 10, 2021 8:27:36 PM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Invalidate lookup request from peer 3 for product SALT
Mar 10, 2021 8:27:37 PM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8084 from peer 3  for product BOAR
Mar 10, 2021 8:27:37 PM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Reply to lookup request from peer 3 for product BOAR
Mar 10, 2021 8:27:37 PM com.p2p.grpc.Seller reply
INFO: Send a reply request to peer 3 at port localhost 8083 for product BOAR
Mar 10, 2021 8:27:37 PM com.p2p.grpc.Seller reply
INFO: Reply path size 1
Mar 10, 2021 8:27:37 PM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8084 from peer 3  for product BOAR
Mar 10, 2021 8:27:37 PM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Reply to lookup request from peer 3 for product BOAR
Mar 10, 2021 8:27:37 PM com.p2p.grpc.Seller reply
INFO: Done Reply
Mar 10, 2021 8:27:37 PM com.p2p.grpc.Seller reply
INFO: Send a reply request to peer 3 at port localhost 8083 for product BOAR
Mar 10, 2021 8:27:37 PM com.p2p.grpc.Seller reply
INFO: Reply path size 1
Mar 10, 2021 8:27:37 PM com.p2p.grpc.Seller reply
INFO: Done Reply
Mar 10, 2021 8:27:39 PM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8084 from peer 3  for product SALT
Mar 10, 2021 8:27:39 PM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Invalidate lookup request from peer 3 for product SALT
Mar 10, 2021 8:27:40 PM com.p2p.grpc.Seller$MarketplaceSellerImpl buyRPC
INFO: Receive a buy request at 8084 from peer 2 for product BOAR
Mar 10, 2021 8:27:40 PM com.p2p.grpc.Seller$MarketplaceSellerImpl buyRPC
INFO: Finish a transaction for peer 2. Currently, having: 1
Mar 10, 2021 8:27:40 PM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8084 from peer 3  for product SALT
Mar 10, 2021 8:27:40 PM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Invalidate lookup request from peer 3 for product SALT
Mar 10, 2021 8:27:40 PM com.p2p.grpc.Seller$MarketplaceSellerImpl buyRPC
INFO: Receive a buy request at 8084 from peer 1 for product BOAR
Mar 10, 2021 8:27:40 PM com.p2p.grpc.Seller processBuy
INFO: BOAR runs out!!!! Restocking
Mar 10, 2021 8:27:40 PM com.p2p.grpc.Seller processBuy
INFO: After randomize a new product and restock, now selling FISH
Mar 10, 2021 8:27:40 PM com.p2p.grpc.Seller$MarketplaceSellerImpl buyRPC
INFO: Finish a transaction for peer 1. Currently, having: 3 
```

Peer terminal

```
(base) Hoangs-MacBook-Pro:BuyerSellerNetwork hoangho$ gradle p2pPeer --args="3 1"
Starting a Gradle Daemon, 1 busy and 30 stopped Daemons could not be reused, use --status for details

> Task :p2pPeer
Mar 10, 2021 8:27:29 PM com.p2p.grpc.PeerImpl startServer
INFO: Starting a server at localhost 8083
Mar 10, 2021 8:27:33 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8083 from peer 0 for product BOAR
Mar 10, 2021 8:27:33 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Flood Lookup request from peer 0 for product BOAR
Mar 10, 2021 8:27:33 PM com.p2p.grpc.PeerImpl lambda$floodLookUp$1
INFO: Send a lookup request to peer 4 at port localhost 8084 for product BOAR
Mar 10, 2021 8:27:33 PM com.p2p.grpc.PeerImpl lambda$floodLookUp$1
INFO: Send a lookup request to peer 1 at port localhost 8081 for product BOAR
Mar 10, 2021 8:27:33 PM com.p2p.grpc.PeerImpl lambda$floodLookUp$1
INFO: Send a lookup request to peer 2 at port localhost 8082 for product BOAR
Mar 10, 2021 8:27:34 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl replyRPC
INFO: Receive Reply request at 3. Size of reverse path 1
Mar 10, 2021 8:27:34 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl replyRPC
INFO: Continue down the path for reply
Mar 10, 2021 8:27:34 PM com.p2p.grpc.PeerImpl reverseReply
INFO: Send a reply request to peer 0 at localhost 8080
Mar 10, 2021 8:27:34 PM com.p2p.grpc.PeerImpl reverseReply
INFO: Size of path 0
Mar 10, 2021 8:27:36 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8083 from peer 0 for product SALT
Mar 10, 2021 8:27:36 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Flood Lookup request from peer 0 for product SALT
Mar 10, 2021 8:27:36 PM com.p2p.grpc.PeerImpl lambda$floodLookUp$1
INFO: Send a lookup request to peer 1 at port localhost 8081 for product SALT
Mar 10, 2021 8:27:36 PM com.p2p.grpc.PeerImpl lambda$floodLookUp$1
INFO: Send a lookup request to peer 4 at port localhost 8084 for product SALT
Mar 10, 2021 8:27:36 PM com.p2p.grpc.PeerImpl lambda$floodLookUp$1
INFO: Send a lookup request to peer 2 at port localhost 8082 for product SALT
Mar 10, 2021 8:27:37 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8083 from peer 2 for product BOAR
Mar 10, 2021 8:27:37 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Flood Lookup request from peer 2 for product BOAR
Mar 10, 2021 8:27:37 PM com.p2p.grpc.PeerImpl lambda$floodLookUp$1
INFO: Send a lookup request to peer 1 at port localhost 8081 for product BOAR
Mar 10, 2021 8:27:37 PM com.p2p.grpc.PeerImpl lambda$floodLookUp$1
INFO: Send a lookup request to peer 4 at port localhost 8084 for product BOAR
Mar 10, 2021 8:27:37 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8083 from peer 1 for product BOAR
Mar 10, 2021 8:27:37 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Flood Lookup request from peer 1 for product BOAR
Mar 10, 2021 8:27:37 PM com.p2p.grpc.PeerImpl lambda$floodLookUp$1
INFO: Send a lookup request to peer 2 at port localhost 8082 for product BOAR
Mar 10, 2021 8:27:37 PM com.p2p.grpc.PeerImpl lambda$floodLookUp$1
INFO: Send a lookup request to peer 4 at port localhost 8084 for product BOAR
Mar 10, 2021 8:27:37 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl replyRPC
INFO: Receive Reply request at 3. Size of reverse path 1
Mar 10, 2021 8:27:37 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl replyRPC
INFO: Continue down the path for reply
Mar 10, 2021 8:27:37 PM com.p2p.grpc.PeerImpl reverseReply
INFO: Send a reply request to peer 2 at localhost 8082
Mar 10, 2021 8:27:37 PM com.p2p.grpc.PeerImpl reverseReply
INFO: Size of path 0
Mar 10, 2021 8:27:37 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl replyRPC
INFO: Receive Reply request at 3. Size of reverse path 1
Mar 10, 2021 8:27:37 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl replyRPC
INFO: Continue down the path for reply
Mar 10, 2021 8:27:37 PM com.p2p.grpc.PeerImpl reverseReply
INFO: Send a reply request to peer 1 at localhost 8081
Mar 10, 2021 8:27:37 PM com.p2p.grpc.PeerImpl reverseReply
INFO: Size of path 0
Mar 10, 2021 8:27:39 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8083 from peer 0 for product SALT
Mar 10, 2021 8:27:39 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Flood Lookup request from peer 0 for product SALT
Mar 10, 2021 8:27:39 PM com.p2p.grpc.PeerImpl lambda$floodLookUp$1
INFO: Send a lookup request to peer 1 at port localhost 8081 for product SALT
Mar 10, 2021 8:27:39 PM com.p2p.grpc.PeerImpl lambda$floodLookUp$1
INFO: Send a lookup request to peer 2 at port localhost 8082 for product SALT
Mar 10, 2021 8:27:39 PM com.p2p.grpc.PeerImpl lambda$floodLookUp$1
INFO: Send a lookup request to peer 4 at port localhost 8084 for product SALT
Mar 10, 2021 8:27:40 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8083 from peer 2 for product SALT
Mar 10, 2021 8:27:40 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Flood Lookup request from peer 2 for product SALT
```

Buyer 0 terminal:
```
(base) Hoangs-MacBook-Pro:BuyerSellerNetwork hoangho$ gradle p2pBuyer --args="0 1" 
Starting a Gradle Daemon, 1 busy and 30 stopped Daemons could not be reused, use --status for details

> Task :p2pBuyer
Mar 10, 2021 8:27:32 PM com.p2p.grpc.Buyer startServer
INFO: Starting a server at localhost localhost
Mar 10, 2021 8:27:32 PM com.p2p.grpc.Buyer lambda$run$1
INFO: Currently buying BOAR
Mar 10, 2021 8:27:32 PM com.p2p.grpc.Buyer lambda$lookup$0
INFO: Send a lookup request to peer 3 at port localhost 8083 for product BOAR
Mar 10, 2021 8:27:34 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Receive a reply request at 0 size of path 0
Mar 10, 2021 8:27:34 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Request Product: BOAR
Mar 10, 2021 8:27:34 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Buyer Product BOAR
Mar 10, 2021 8:27:34 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Add the seller to the list of sellers
Mar 10, 2021 8:27:36 PM com.p2p.grpc.Buyer buy
INFO: Send a buy request to peer 4
Mar 10, 2021 8:27:36 PM com.p2p.grpc.Buyer buy
INFO: Bought BOAR from peer 4. Buyer current has 1 BOAR
Mar 10, 2021 8:27:36 PM com.p2p.grpc.Buyer run
INFO: Choose a new product to buy
Mar 10, 2021 8:27:36 PM com.p2p.grpc.Buyer run
INFO: Now buying SALT
Mar 10, 2021 8:27:36 PM com.p2p.grpc.Buyer lambda$run$1
INFO: Currently buying SALT
Mar 10, 2021 8:27:36 PM com.p2p.grpc.Buyer lambda$lookup$0
INFO: Send a lookup request to peer 3 at port localhost 8083 for product SALT
Mar 10, 2021 8:27:39 PM com.p2p.grpc.Buyer run
INFO: Choose a new product to buy
Mar 10, 2021 8:27:39 PM com.p2p.grpc.Buyer run
INFO: Now buying SALT
Mar 10, 2021 8:27:39 PM com.p2p.grpc.Buyer lambda$run$1
INFO: Currently buying SALT
Mar 10, 2021 8:27:39 PM com.p2p.grpc.Buyer lambda$lookup$0
INFO: Send a lookup request to peer 3 at port localhost 8083 for product SALT
Mar 10, 2021 8:27:40 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 3 for product SALT
Mar 10, 2021 8:27:40 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Invalidate lookup request from peer 3 for product SALT
Mar 10, 2021 8:27:40 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 3 for product SALT
Mar 10, 2021 8:27:40 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Invalidate lookup request from peer 3 for product SALT
Mar 10, 2021 8:27:42 PM com.p2p.grpc.Buyer run
INFO: Choose a new product to buy
Mar 10, 2021 8:27:42 PM com.p2p.grpc.Buyer run
INFO: Now buying BOAR
Mar 10, 2021 8:27:42 PM com.p2p.grpc.Buyer lambda$run$1
INFO: Currently buying BOAR
```

Buyer 1 terminal

```
(base) Hoangs-MacBook-Pro:BuyerSellerNetwork hoangho$ gradle p2pBuyer --args="1 1" 
Starting a Gradle Daemon, 3 busy and 30 stopped Daemons could not be reused, use --status for details

> Task :p2pBuyer
Mar 10, 2021 8:27:36 PM com.p2p.grpc.Buyer startServer
INFO: Starting a server at localhost localhost
Mar 10, 2021 8:27:36 PM com.p2p.grpc.Buyer lambda$run$1
INFO: Currently buying BOAR
Mar 10, 2021 8:27:36 PM com.p2p.grpc.Buyer lambda$lookup$0
INFO: Send a lookup request to peer 3 at port localhost 8083 for product BOAR
Mar 10, 2021 8:27:36 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8081 from peer 3 for product SALT
Mar 10, 2021 8:27:36 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Invalidate lookup request from peer 3 for product SALT
Mar 10, 2021 8:27:37 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8081 from peer 3 for product BOAR
Mar 10, 2021 8:27:37 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Invalidate lookup request from peer 3 for product BOAR
Mar 10, 2021 8:27:37 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Receive a reply request at 1 size of path 0
Mar 10, 2021 8:27:37 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Request Product: BOAR
Mar 10, 2021 8:27:37 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Buyer Product BOAR
Mar 10, 2021 8:27:37 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Add the seller to the list of sellers
Mar 10, 2021 8:27:38 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8081 from peer 3 for product BOAR
Mar 10, 2021 8:27:38 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Invalidate lookup request from peer 3 for product BOAR
Mar 10, 2021 8:27:39 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8081 from peer 3 for product SALT
Mar 10, 2021 8:27:39 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Invalidate lookup request from peer 3 for product SALT
Mar 10, 2021 8:27:40 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8081 from peer 3 for product SALT
Mar 10, 2021 8:27:40 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Invalidate lookup request from peer 3 for product SALT
Mar 10, 2021 8:27:40 PM com.p2p.grpc.Buyer buy
INFO: Send a buy request to peer 4
Mar 10, 2021 8:27:40 PM com.p2p.grpc.Buyer buy
INFO: Bought BOAR from peer 4. Buyer current has 1 BOAR
Mar 10, 2021 8:27:40 PM com.p2p.grpc.Buyer run
INFO: Choose a new product to buy
Mar 10, 2021 8:27:40 PM com.p2p.grpc.Buyer run
INFO: Now buying SALT
Mar 10, 2021 8:27:40 PM com.p2p.grpc.Buyer lambda$run$1
INFO: Currently buying SALT 
```

Buyer 2 terminal

```
(base) Hoangs-MacBook-Pro:BuyerSellerNetwork hoangho$ gradle p2pBuyer --args="2 1" 
Starting a Gradle Daemon, 3 busy and 30 stopped Daemons could not be reused, use --status for details

> Task :p2pBuyer
Mar 10, 2021 8:27:37 PM com.p2p.grpc.Buyer startServer
INFO: Starting a server at localhost localhost
Mar 10, 2021 8:27:37 PM com.p2p.grpc.Buyer lambda$run$1
INFO: Currently buying BOAR
Mar 10, 2021 8:27:37 PM com.p2p.grpc.Buyer lambda$lookup$0
INFO: Send a lookup request to peer 3 at port localhost 8083 for product BOAR
Mar 10, 2021 8:27:37 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8082 from peer 3 for product SALT
Mar 10, 2021 8:27:37 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Invalidate lookup request from peer 3 for product SALT
Mar 10, 2021 8:27:37 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8082 from peer 3 for product BOAR
Mar 10, 2021 8:27:37 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Invalidate lookup request from peer 3 for product BOAR
Mar 10, 2021 8:27:37 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Receive a reply request at 2 size of path 0
Mar 10, 2021 8:27:37 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Request Product: BOAR
Mar 10, 2021 8:27:37 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Buyer Product BOAR
Mar 10, 2021 8:27:37 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Add the seller to the list of sellers
Mar 10, 2021 8:27:38 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8082 from peer 3 for product BOAR
Mar 10, 2021 8:27:38 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Invalidate lookup request from peer 3 for product BOAR
Mar 10, 2021 8:27:39 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8082 from peer 3 for product SALT
Mar 10, 2021 8:27:39 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Invalidate lookup request from peer 3 for product SALT
Mar 10, 2021 8:27:40 PM com.p2p.grpc.Buyer buy
INFO: Send a buy request to peer 4
Mar 10, 2021 8:27:40 PM com.p2p.grpc.Buyer buy
INFO: Bought BOAR from peer 4. Buyer current has 1 BOAR
Mar 10, 2021 8:27:40 PM com.p2p.grpc.Buyer run
INFO: Choose a new product to buy
Mar 10, 2021 8:27:40 PM com.p2p.grpc.Buyer run
INFO: Now buying SALT
Mar 10, 2021 8:27:40 PM com.p2p.grpc.Buyer lambda$run$1
INFO: Currently buying SALT
```

**Test case 2**:

Paste the following in the config file:

```
0 buyer 8080 fish 2 8082
1 buyer 8081 fish 2 8082
2 seller 8082 fish 3 0 8080 1 8081 
```

Open up three terminals and try to run the following at a similar time:

```
// to create a Seller with peerId 2 with 2 neighbors
$ gradle p2pSeller --args="2 2"
```

```
// to create a buyer with peerId 0 with 1 neighbors
$ gradle p2pBuyer --args="0 1"
```

```
// to create a buyer with peerId 1 with 1 neighbors
gradle p2pBuyer --args="1 1" 
```

Due to us running the test on one single, allows the program to run for at least 3 minutes to see the race conditions happens! Looking for the message "INFO: Product out of stock! Now selling BOAR
" from the Seller terminal. Request arrives at similar time even though the log is at different time. This is because we are testing on a single machine, and each of the Seller, Buyer are multi-threading process. 
The time gap between a lookup and a buy request is 10 seconds, because I force the thread to sleep for 10 seconds 

There will be two cases that happens: 

* The Seller restock and choose the same product to sell, then both buyers would be able to buy product
* The Seller restock adn choose a different product to sell, then the buyer that comes later may not be able to buy the product and will receive a buy unsucessful message

**Case 1**:

The terminal of the Seller should look like:

```
> Task :p2pSeller
Mar 10, 2021 7:46:40 PM com.p2p.grpc.Seller startServer
INFO: Starting a server at localhost 8082
Mar 10, 2021 7:46:52 PM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8082 from peer 0  for product FISH
Mar 10, 2021 7:46:52 PM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Reply to lookup request from peer 0 for product FISH
Mar 10, 2021 7:46:52 PM com.p2p.grpc.Seller reply
INFO: Send a reply request to peer 0 at port localhost 8080 for product FISH
Mar 10, 2021 7:46:52 PM com.p2p.grpc.Seller reply
INFO: Reply path size 0
Mar 10, 2021 7:46:53 PM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8082 from peer 1  for product FISH
Mar 10, 2021 7:46:53 PM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Reply to lookup request from peer 1 for product FISH
Mar 10, 2021 7:46:53 PM com.p2p.grpc.Seller reply
INFO: Send a reply request to peer 1 at port localhost 8081 for product FISH
Mar 10, 2021 7:46:53 PM com.p2p.grpc.Seller reply
INFO: Reply path size 0
Mar 10, 2021 7:46:55 PM com.p2p.grpc.Seller$MarketplaceSellerImpl buyRPC
INFO: Receive a buy request at 8082 from peer 0 for product FISH
Mar 10, 2021 7:46:55 PM com.p2p.grpc.Seller processBuy
INFO: FISH runs out!!!! Restocking
Mar 10, 2021 7:46:55 PM com.p2p.grpc.Seller processBuy
INFO: After randomize a new product and restock, now selling FISH
Mar 10, 2021 7:46:55 PM com.p2p.grpc.Seller$MarketplaceSellerImpl buyRPC
INFO: Finish a transaction for peer 0. Currently, having: 1
Mar 10, 2021 7:46:55 PM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8082 from peer 0  for product SALT
Mar 10, 2021 7:46:55 PM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Flood Lookup request from peer 0 for product SALT
Mar 10, 2021 7:46:55 PM com.p2p.grpc.PeerImpl lambda$floodLookUp$1
INFO: Send a lookup request to peer 1 at port localhost 8081 for product SALT
Mar 10, 2021 7:46:56 PM com.p2p.grpc.Seller$MarketplaceSellerImpl buyRPC
INFO: Receive a buy request at 8082 from peer 1 for product FISH
Mar 10, 2021 7:46:56 PM com.p2p.grpc.Seller processBuy
INFO: FISH runs out!!!! Restocking
Mar 10, 2021 7:46:56 PM com.p2p.grpc.Seller processBuy
INFO: After randomize a new product and restock, now selling SALT
Mar 10, 2021 7:46:56 PM com.p2p.grpc.Seller$MarketplaceSellerImpl buyRPC
INFO: Finish a transaction for peer 1. Currently, having: 1
```

The Seller chooses to sell FISH to Buyer 0 first, and after out of stock and randomizing, it still sells FISH, so at Mar 10, 2021 7:46:56, we see that the Seller again sells FISH to Buyer 1. 

The terminal of Buyer 0 should look like
```
> Task :p2pBuyer
Mar 10, 2021 7:46:51 PM com.p2p.grpc.Buyer startServer
INFO: Starting a server at localhost localhost
Mar 10, 2021 7:46:51 PM com.p2p.grpc.Buyer lambda$run$1
INFO: Currently buying FISH
Mar 10, 2021 7:46:51 PM com.p2p.grpc.Buyer lambda$lookup$0
INFO: Send a lookup request to peer 2 at port localhost 8082 for product FISH
Mar 10, 2021 7:46:52 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Receive a reply request at 0 size of path 0
Mar 10, 2021 7:46:52 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Request Product: FISH
Mar 10, 2021 7:46:52 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Buyer Product FISH
Mar 10, 2021 7:46:52 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Add the seller to the list of sellers
Mar 10, 2021 7:46:55 PM com.p2p.grpc.Buyer buy
INFO: Send a buy request to peer 2
Mar 10, 2021 7:46:55 PM com.p2p.grpc.Buyer buy
INFO: Bought FISH from peer 2. Buyer current has 1 FISH
```

The terminal of Buyer 1 should look like:

```
> Task :p2pBuyer
Mar 10, 2021 7:46:52 PM com.p2p.grpc.Buyer startServer
INFO: Starting a server at localhost localhost
Mar 10, 2021 7:46:52 PM com.p2p.grpc.Buyer lambda$run$1
INFO: Currently buying FISH
Mar 10, 2021 7:46:52 PM com.p2p.grpc.Buyer lambda$lookup$0
INFO: Send a lookup request to peer 2 at port localhost 8082 for product FISH
Mar 10, 2021 7:46:53 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Receive a reply request at 1 size of path 0
Mar 10, 2021 7:46:53 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Request Product: FISH
Mar 10, 2021 7:46:53 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Buyer Product FISH
Mar 10, 2021 7:46:53 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Add the seller to the list of sellers
Mar 10, 2021 7:46:55 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8081 from peer 2 for product SALT
Mar 10, 2021 7:46:55 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Invalidate lookup request from peer 2 for product SALT
Mar 10, 2021 7:46:56 PM com.p2p.grpc.Buyer buy
INFO: Send a buy request to peer 2
Mar 10, 2021 7:46:56 PM com.p2p.grpc.Buyer buy
INFO: Bought FISH from peer 2. Buyer current has 1 FISH
```

**Case 2**:

The terminal of the Seller should look like:

```
Mar 10, 2021 7:47:32 PM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8082 from peer 1  for product FISH
Mar 10, 2021 7:47:32 PM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Reply to lookup request from peer 1 for product FISH
Mar 10, 2021 7:47:32 PM com.p2p.grpc.Seller reply
INFO: Send a reply request to peer 1 at port localhost 8081 for product FISH
Mar 10, 2021 7:47:32 PM com.p2p.grpc.Seller reply
INFO: Reply path size 0
Mar 10, 2021 7:47:34 PM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8082 from peer 0  for product FISH
Mar 10, 2021 7:47:34 PM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Reply to lookup request from peer 0 for product FISH
Mar 10, 2021 7:47:34 PM com.p2p.grpc.Seller reply
INFO: Send a reply request to peer 0 at port localhost 8080 for product FISH
Mar 10, 2021 7:47:34 PM com.p2p.grpc.Seller reply
INFO: Reply path size 0
Mar 10, 2021 7:47:35 PM com.p2p.grpc.Seller$MarketplaceSellerImpl buyRPC
INFO: Receive a buy request at 8082 from peer 1 for product FISH
Mar 10, 2021 7:47:35 PM com.p2p.grpc.Seller processBuy
INFO: FISH runs out!!!! Restocking
Mar 10, 2021 7:47:35 PM com.p2p.grpc.Seller processBuy
INFO: After randomize a new product and restock, now selling BOAR
Mar 10, 2021 7:47:35 PM com.p2p.grpc.Seller$MarketplaceSellerImpl buyRPC
INFO: Finish a transaction for peer 1. Currently, having: 1
Mar 10, 2021 7:47:35 PM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8082 from peer 1  for product BOAR
Mar 10, 2021 7:47:35 PM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Reply to lookup request from peer 1 for product BOAR
Mar 10, 2021 7:47:35 PM com.p2p.grpc.Seller reply
INFO: Send a reply request to peer 1 at port localhost 8081 for product BOAR
Mar 10, 2021 7:47:35 PM com.p2p.grpc.Seller reply
INFO: Reply path size 0
Mar 10, 2021 7:47:37 PM com.p2p.grpc.Seller$MarketplaceSellerImpl buyRPC
INFO: Receive a buy request at 8082 from peer 0 for product FISH
Mar 10, 2021 7:47:37 PM com.p2p.grpc.Seller$MarketplaceSellerImpl buyRPC
INFO: Product out of stock! Now selling BOAR
```

At Mar 10, 2021 7:47:32 PM, the Seller receives a lookup request for FISH from Buyer 1 and replies to it.
At Mar 10, 2021 7:47:34, the Seller receives a lookup request for FISH from Buyer 0 and replies to it.
At Mar 10, 2021 7:47:35, the Seller receives a buy request for FISH from Buyer 1 and replies to it, the transaction is successful. After running out of fish, the Seller pick BOAR to sell.
At Mar 10, 2021 7:47:37, the Seller receives a buy request for FISH from Buyer 0 and replies to it, the transaction is unsuccessful because the Seller is no longer selling FISH

The terminal for Buyer 0 should look like:

``` 
Mar 10, 2021 7:47:34 PM com.p2p.grpc.Buyer lambda$run$1
INFO: Currently buying FISH
Mar 10, 2021 7:47:34 PM com.p2p.grpc.Buyer lambda$lookup$0
INFO: Send a lookup request to peer 2 at port localhost 8082 for product FISH
Mar 10, 2021 7:47:34 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Receive a reply request at 0 size of path 0
Mar 10, 2021 7:47:34 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Request Product: FISH
Mar 10, 2021 7:47:34 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Buyer Product FISH
Mar 10, 2021 7:47:34 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Add the seller to the list of sellers
Mar 10, 2021 7:47:37 PM com.p2p.grpc.Buyer buy
INFO: Send a buy request to peer 2
Mar 10, 2021 7:47:37 PM com.p2p.grpc.Buyer buy
INFO: Buy unsuccessful!
```


The terminal for Buyer 1 should look like:

```
Mar 10, 2021 7:47:32 PM com.p2p.grpc.Buyer lambda$run$1
INFO: Currently buying FISH
Mar 10, 2021 7:47:32 PM com.p2p.grpc.Buyer lambda$lookup$0
INFO: Send a lookup request to peer 2 at port localhost 8082 for product FISH
Mar 10, 2021 7:47:32 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Receive a reply request at 1 size of path 0
Mar 10, 2021 7:47:32 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Request Product: FISH
Mar 10, 2021 7:47:32 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Buyer Product FISH
Mar 10, 2021 7:47:32 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Add the seller to the list of sellers
Mar 10, 2021 7:47:35 PM com.p2p.grpc.Buyer buy
INFO: Send a buy request to peer 2
Mar 10, 2021 7:47:35 PM com.p2p.grpc.Buyer buy
INFO: Bought FISH from peer 2. Buyer current has 3 FISH
```


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
# Testing Process


## Milestone 3

Test cases are:

![Screenshot](../TestCases.png)

Test Case 1 is to verify that the network with two peers work correctly, hence the basic setup is correct for buyer and seller!

Test Case 2 is for the race simulation. We create a simple network with 3 peers, 2 buyers and 1 seller.
Once the Buyers start running, they immediately buys from Seller!. With this we can verify that Seller is able to handle concurrent buy request!

Test Case 3 is to test a whole network for flooding lookup request, reverse traversal for reply request!

Test Case 3 modified is to check how performance of the network changes when a node 2 and node 3 is connected!  

### Steps to run locally

Example for config files are TestCase1.json, TestCase2.json and TestCase3.json. 

To run locally on your computer, for each test case with config file TestCase1.json/TestCase2.json/TestCase3.json, 
open up N terminals (where N is the number of peers specified in each test case). In each terminal, decides which id, which role you want the node to take.
For Buyer and Seller, you will also need to specify the product you want the peer to start with. For Seller, you will also need to specify stock, and for Buyer, you will also need to specify HopCount
Try to start all peers at the same time!

Example:
For **TestCase1.json**, we can specify the buyer as:

```
java -jar build/libs/BuyerSellerNetwork-1.0-SNAPSHOT.jar -config TestCase1.json -id 0 -role buyer -product fish -hop 1
```

and the seller as: 
```
java -jar build/libs/BuyerSellerNetwork-1.0-SNAPSHOT.jar -config TestCase1.json -id 1 -role seller -product fish -stock 1
```

The test script for this test case is in doc/test/TestCase1.sh, which will run 2 processes in parallel


For **TestCase2.json**, let node 0 and 1 be the buyer and node 2 be the seller, we can specify the buyer as:

```
gradle simulateRace --args="-config TestCase2.json -id 0 -role buyer -product fish -hop 1"
```

```
gradle simulateRace --args="-config TestCase2.json -id 1 -role buyer -product fish -hop 1"
```

and the seller as:
```
gradle simulateRace --args="-config TestCase2.json -id 2 -role seller -product fish -stock 1"
```

The test script for this test case is in doc/test/TestCase2.sh, which will run 3 processes in parallel

Example output for race condition:

Peer 0 (Buyer) terminal:

```
(base) Hoangs-MacBook-Pro:BuyerSellerNetwork hoangho$ gradle simulateRace --args="-config TestCase2.json -id 0 -role buyer -product fish -hop 1" 
Starting a Gradle Daemon, 1 busy Daemon could not be reused, use --status for details

> Task :simulateRace
Mar 17, 2021 8:45:00 PM com.p2p.grpc.Buyer buy
INFO: Send a buy request to peer 2
Mar 17, 2021 8:45:00 PM com.p2p.grpc.Buyer buy
INFO: Bought FISH from peer 2. Buyer current has 1 FISH
Mar 17, 2021 8:45:01 PM com.p2p.SimulateRace main
INFO: Choose a new product to buy
Mar 17, 2021 8:45:01 PM com.p2p.SimulateRace main
INFO: Now buying FISH
Mar 17, 2021 8:45:01 PM com.p2p.grpc.Buyer buy
INFO: Send a buy request to peer 2
Mar 17, 2021 8:45:01 PM com.p2p.grpc.Buyer buy
INFO: Buy unsuccessful!
```

Peer 1 (Buyer) terminal:

```
(base) Hoangs-MacBook-Pro:BuyerSellerNetwork hoangho$ gradle simulateRace --args="-config TestCase2.json -id 1 -role buyer -product fish -hop 1"
Starting a Gradle Daemon, 1 busy Daemon could not be reused, use --status for details

> Task :simulateRace
Mar 17, 2021 8:44:59 PM com.p2p.grpc.Buyer buy
INFO: Send a buy request to peer 2
Mar 17, 2021 8:45:00 PM com.p2p.grpc.Buyer buy
INFO: Bought FISH from peer 2. Buyer current has 1 FISH
Mar 17, 2021 8:45:01 PM com.p2p.SimulateRace main
INFO: Choose a new product to buy
Mar 17, 2021 8:45:01 PM com.p2p.SimulateRace main
INFO: Now buying FISH
Mar 17, 2021 8:45:01 PM com.p2p.grpc.Buyer buy
INFO: Send a buy request to peer 2
Mar 17, 2021 8:45:01 PM com.p2p.grpc.Buyer buy
INFO: Buy unsuccessful!
Mar 17, 2021 8:45:02 PM com.p2p.SimulateRace main
INFO: Choose a new product to buy
Mar 17, 2021 8:45:02 PM com.p2p.SimulateRace main
INFO: Now buying SALT
Mar 17, 2021 8:45:02 PM com.p2p.grpc.Buyer buy
INFO: Send a buy request to peer 2
Mar 17, 2021 8:45:02 PM com.p2p.grpc.Buyer buy
INFO: Bought SALT from peer 2. Buyer current has 1 SALT
Mar 17, 2021 8:45:03 PM com.p2p.SimulateRace main
INFO: Choose a new product to buy
```

Peer 2 (Seller) terminal: 

``` 
(base) Hoangs-MacBook-Pro:BuyerSellerNetwork hoangho$ gradle simulateRace --args="-config TestCase2.json -id 2 -role seller -product fish -stock 1"

> Task :simulateRace
Mar 17, 2021 8:44:51 PM com.p2p.grpc.Seller startServer
INFO: Starting a server at localhost 8082
Mar 17, 2021 8:45:00 PM com.p2p.grpc.Seller$MarketplaceSellerImpl buyRPC
INFO: Receive a buy request at 8082 from peer 1 for product FISH
Mar 17, 2021 8:45:00 PM com.p2p.grpc.Seller processBuy
INFO: FISH runs out!!!! Restocking
Mar 17, 2021 8:45:00 PM com.p2p.grpc.Seller processBuy
INFO: After randomize a new product and restock, now selling FISH
Mar 17, 2021 8:45:00 PM com.p2p.grpc.Seller$MarketplaceSellerImpl buyRPC
INFO: Finish a transaction for peer 1. Currently, having: 1
Mar 17, 2021 8:45:00 PM com.p2p.grpc.Seller$MarketplaceSellerImpl buyRPC
INFO: Receive a buy request at 8082 from peer 0 for product FISH
Mar 17, 2021 8:45:00 PM com.p2p.grpc.Seller processBuy
INFO: FISH runs out!!!! Restocking
Mar 17, 2021 8:45:00 PM com.p2p.grpc.Seller processBuy
INFO: After randomize a new product and restock, now selling SALT
Mar 17, 2021 8:45:00 PM com.p2p.grpc.Seller$MarketplaceSellerImpl buyRPC
INFO: Finish a transaction for peer 0. Currently, having: 1
```



For **TestCase3.json**, let node 0, 1, 2 be the buyer, and node 3 and 4 be the no-role peer, and node 5 and 6 be the seller. 
Open up 7 terminals and try the following: 

```
java -jar build/libs/BuyerSellerNetwork-1.0-SNAPSHOT.jar -config TestCase3.json -id 0 -role buyer -product boar -hop 4 
```

```
java -jar build/libs/BuyerSellerNetwork-1.0-SNAPSHOT.jar -config TestCase3.json -id 1 -role buyer -product boar -hop 3
```

```
java -jar build/libs/BuyerSellerNetwork-1.0-SNAPSHOT.jar -config TestCase3.json -id 2 -role buyer -product fish -hop 3
```

```
java -jar build/libs/BuyerSellerNetwork-1.0-SNAPSHOT.jar -config TestCase3.json -id 3 -role peer 
```

```
java -jar build/libs/BuyerSellerNetwork-1.0-SNAPSHOT.jar -config TestCase3.json -id 4 -role peer 
```

```
java -jar build/libs/BuyerSellerNetwork-1.0-SNAPSHOT.jar -config TestCase3.json -id 5 -role seller -product boar -stock 3 
```

```
java -jar build/libs/BuyerSellerNetwork-1.0-SNAPSHOT.jar -config TestCase3.json -id 6 -role seller -product fish -stock 3 
```

The test script for this test case is in doc/test/TestCase3.sh, which will run 7 processes in parallel

For single server deployment, I ran the test case in my laptop, which is a 4-core CPU with 16GB RAM. 
If you run this on single server EC2, you may need at least a server with similar specs! 

### Steps to run on EC2 cluster. 

Examples config files are TestCase2EC2.json and TestCase3EC2.json

1. Make sure you have key pair, here I use 677kp. Creates N EC2 instances using the following image: ami-0e02db02181e75d1c, this image has all dependencies installed and code clone for you.
If you want to use your own image or If you cannot use the cannot use the image because of visibility issue, you need to install Java 8 on EC2 instance, git and clone the repo!

```
$ aws ec2 run-instances --image-id ami-0e02db02181e75d1c --instance-type t2.micro --key-name 677kp
```

2. Save the PrivateIpAddress for each peer. You can find this from the terminal output of the above command, or from sudo ifconfig when you already ssh into the EC2 instance

3. Put the PrivateIpAddress of each peer as the IPAddress in the config file. Remember that if you put PrivateIpAddress of one instance as IPAddress for a peer, you later must call that peer with the same id, 
   e.g. if you put IPAddress for peer 0 as "172.31.55.0", then later in the instance with that PrivateIpAddress, you must specify "-id 0"
    
4. After you replace all IPAddress, move the config file into each instance with scp

```
$ scp -i "677kp.pem" TestCase3EC2.json ec2-user@$PublicDnsName:~/CompSci677-Lab1/
```

Where $PublicDnsName is the Public DNS name of your instance, obtained from running: ```aws ec2 describe-instances --instance-id $INSTANCEID```

5. ssh into your instance:

```
$ ssh -i "677kp.pem" ec2-user@$PublicDnsName
```

6. For each instance, go to the directory and compile the code:

```
$ cd CompSci677-Lab1
$ ./gradlew clean build
```

7. Start the test. For the instance which you use the PrivateIpAddress for peer $ID, you need to specify that $ID again when running the jar file. 
For example, I use "172.31.45.247" for peer 0, later in the instance with PrivateIpAddress "172.31.45.247", I ran:
   
```
java -jar build/libs/BuyerSellerNetwork-1.0-SNAPSHOT.jar -config TestCase3EC2.json -id 0 -role buyer -product boar -hop 4 
```

For other peers in other EC2 instance, run the corresponding command:

```
java -jar build/libs/BuyerSellerNetwork-1.0-SNAPSHOT.jar -config TestCase3EC2.json -id 1 -role buyer -product boar -hop 3
```

```
java -jar build/libs/BuyerSellerNetwork-1.0-SNAPSHOT.jar -config TestCase3EC2.json -id 2 -role buyer -product fish -hop 3
```

```
java -jar build/libs/BuyerSellerNetwork-1.0-SNAPSHOT.jar -config TestCase3EC2.json -id 3 -role peer 
```

```
java -jar build/libs/BuyerSellerNetwork-1.0-SNAPSHOT.jar -config TestCase3EC2.json -id 4 -role peer 
```

```
java -jar build/libs/BuyerSellerNetwork-1.0-SNAPSHOT.jar -config TestCase3EC2.json -id 5 -role seller -product boar -stock 3 
```

```
java -jar build/libs/BuyerSellerNetwork-1.0-SNAPSHOT.jar -config TestCase3EC2.json -id 6 -role seller -product fish -stock 3 
```

Feel free to let the peer running for 1-2 minutes to see the process in action!

Example Output for TestCase3 on EC2 cluster:

In node 0 terminal:

```
[ec2-user@ip-172-31-45-247 CompSci677-Lab1]$ java -jar build/libs/BuyerSellerNetwork-1.0-SNAPSHOT.jar -config TestCase3EC2Modified.json -id 0 -role buyer -product boar -hop 4
Mar 17, 2021 11:09:56 PM com.p2p.grpc.Buyer run
INFO: All neighbors of ours{1=id: 1
IPAddress: "172.31.91.178"
port: 8080
, 3=id: 3
IPAddress: "172.31.55.0"
port: 8080
}
Mar 17, 2021 11:09:56 PM com.p2p.grpc.Buyer startServer
INFO: Starting a server at 172.31.45.247 172.31.45.247
Mar 17, 2021 11:09:56 PM com.p2p.grpc.Buyer lambda$run$1
INFO: Currently buying BOAR
Mar 17, 2021 11:09:56 PM com.p2p.grpc.Buyer lambda$lookup$0
INFO: Send a lookup request to peer 3 at port 172.31.55.0 8080 for product BOAR
Mar 17, 2021 11:09:56 PM com.p2p.grpc.Buyer lambda$lookup$0
INFO: Send a lookup request to peer 1 at port 172.31.91.178 8080 for product BOAR
Mar 17, 2021 11:09:58 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 1 for product BOAR
Mar 17, 2021 11:09:58 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Flood Lookup request from peer 1 for product BOAR
Mar 17, 2021 11:09:58 PM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 3 at port 172.31.55.0 8080 for product BOAR
Mar 17, 2021 11:09:59 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 2 for product FISH
Mar 17, 2021 11:09:59 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Flood Lookup request from peer 2 for product FISH
Mar 17, 2021 11:09:59 PM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 3 at port 172.31.55.0 8080 for product FISH
Mar 17, 2021 11:09:59 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 1 for product BOAR
Mar 17, 2021 11:09:59 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Invalidate lookup request from peer 1 for product BOAR
Mar 17, 2021 11:09:59 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 2 for product FISH
Mar 17, 2021 11:09:59 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Invalidate lookup request from peer 2 for product FISH
Mar 17, 2021 11:10:00 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 2 for product FISH
Mar 17, 2021 11:10:00 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Flood Lookup request from peer 2 for product FISH
Mar 17, 2021 11:10:00 PM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 1 at port 172.31.91.178 8080 for product FISH
Mar 17, 2021 11:10:00 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 1 for product BOAR
Mar 17, 2021 11:10:00 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Flood Lookup request from peer 1 for product BOAR
Mar 17, 2021 11:10:00 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 2 for product FISH
Mar 17, 2021 11:10:00 PM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Invalidate lookup request from peer 2 for product FISH
Mar 17, 2021 11:10:00 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Receive a reply request at 0 size of path 0
Mar 17, 2021 11:10:00 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Request Product: BOAR
Mar 17, 2021 11:10:00 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Buyer Product BOAR
Mar 17, 2021 11:10:00 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Add the seller to the list of sellers
Mar 17, 2021 11:10:00 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Receive a reply request at 0 size of path 1
Mar 17, 2021 11:10:00 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Continue down the path for reply
Mar 17, 2021 11:10:00 PM com.p2p.grpc.PeerImpl reverseReply
INFO: Send a reply request to peer 1 at 172.31.91.178 8080
Mar 17, 2021 11:10:00 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Receive a reply request at 0 size of path 0
Mar 17, 2021 11:10:00 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Request Product: BOAR
Mar 17, 2021 11:10:00 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Buyer Product BOAR
Mar 17, 2021 11:10:00 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Add the seller to the list of sellers
Mar 17, 2021 11:10:01 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Receive a reply request at 0 size of path 0
Mar 17, 2021 11:10:01 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Request Product: BOAR
Mar 17, 2021 11:10:01 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Buyer Product BOAR
Mar 17, 2021 11:10:01 PM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Add the seller to the list of sellers
Mar 17, 2021 11:10:03 PM com.p2p.grpc.Buyer buy
INFO: Send a buy request to peer 5
Mar 17, 2021 11:10:03 PM com.p2p.grpc.Buyer buy
INFO: Bought BOAR from peer 5. Buyer current has 1 BOAR
```

Node 1 terminal:

```
[ec2-user@ip-172-31-18-44 CompSci677-Lab1]$ java -jar build/libs/BuyerSellerNetwork-1.0-SNAPSHOT.jar -config TestCase3EC2.json -id 1 -role buyer -product boar -hop 3
Mar 18, 2021 1:34:29 AM com.p2p.grpc.Buyer run
INFO: All neighbors of ours{0=IPAddress: "172.31.45.247"
port: 8080
, 2=id: 2
IPAddress: "172.31.26.99"
port: 8080
, 3=id: 3
IPAddress: "172.31.87.102"
port: 8080
}
Mar 18, 2021 1:34:29 AM com.p2p.grpc.Buyer startServer
INFO: Starting a server at 172.31.18.44 172.31.18.44
Mar 18, 2021 1:34:29 AM com.p2p.grpc.Buyer lambda$run$1
INFO: Currently buying BOAR
Mar 18, 2021 1:34:29 AM com.p2p.grpc.Buyer lambda$lookup$0
INFO: Send a lookup request to peer 2 at port 172.31.26.99 8080 for product BOAR
Mar 18, 2021 1:34:29 AM com.p2p.grpc.Buyer lambda$lookup$0
INFO: Send a lookup request to peer 0 at port 172.31.45.247 8080 for product BOAR
Mar 18, 2021 1:34:29 AM com.p2p.grpc.Buyer lambda$lookup$0
INFO: Send a lookup request to peer 3 at port 172.31.87.102 8080 for product BOAR
Mar 18, 2021 1:34:29 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 0 for product BOAR
Mar 18, 2021 1:34:29 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Flood Lookup request from peer 0 for product BOAR
Mar 18, 2021 1:34:29 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 2 at port 172.31.26.99 8080 for product BOAR
Mar 18, 2021 1:34:29 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 3 at port 172.31.87.102 8080 for product BOAR
Mar 18, 2021 1:34:31 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 2 for product FISH
Mar 18, 2021 1:34:31 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Flood Lookup request from peer 2 for product FISH
Mar 18, 2021 1:34:31 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 0 at port 172.31.45.247 8080 for product FISH
Mar 18, 2021 1:34:31 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 3 at port 172.31.87.102 8080 for product FISH
Mar 18, 2021 1:34:34 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 2 for product SALT
Mar 18, 2021 1:34:34 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Flood Lookup request from peer 2 for product SALT
Mar 18, 2021 1:34:34 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 0 at port 172.31.45.247 8080 for product SALT
Mar 18, 2021 1:34:34 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 3 at port 172.31.87.102 8080 for product SALT
Mar 18, 2021 1:34:37 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 2 for product SALT
Mar 18, 2021 1:34:37 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Flood Lookup request from peer 2 for product SALT
Mar 18, 2021 1:34:37 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 0 at port 172.31.45.247 8080 for product SALT
Mar 18, 2021 1:34:37 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 3 at port 172.31.87.102 8080 for product SALT
Mar 18, 2021 1:34:38 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 0 for product BOAR
Mar 18, 2021 1:34:38 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Flood Lookup request from peer 0 for product BOAR
Mar 18, 2021 1:34:38 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 2 at port 172.31.26.99 8080 for product BOAR
Mar 18, 2021 1:34:38 AM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Receive a reply request at 1 size of path 0
Mar 18, 2021 1:34:38 AM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Request Product: BOAR
Mar 18, 2021 1:34:38 AM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Buyer Product BOAR
Mar 18, 2021 1:34:38 AM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Add the seller to the list of sellers
Mar 18, 2021 1:34:38 AM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Receive a reply request at 1 size of path 1
Mar 18, 2021 1:34:38 AM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Continue down the path for reply
Mar 18, 2021 1:34:38 AM com.p2p.grpc.PeerImpl reverseReply
INFO: Send a reply request to peer 0 at 172.31.45.247 8080
Mar 18, 2021 1:34:39 AM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Receive a reply request at 1 size of path 0
Mar 18, 2021 1:34:39 AM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Request Product: BOAR
Mar 18, 2021 1:34:39 AM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Buyer Product BOAR
Mar 18, 2021 1:34:39 AM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Add the seller to the list of sellers
Mar 18, 2021 1:34:40 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 2 for product BOAR
Mar 18, 2021 1:34:41 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Flood Lookup request from peer 2 for product BOAR
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Buyer buy
INFO: Send a buy request to peer 5
Mar 18, 2021 1:34:41 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 0 at port 172.31.45.247 8080 for product BOAR
Mar 18, 2021 1:34:41 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 3 at port 172.31.87.102 8080 for product BOAR
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Buyer buy
INFO: Bought BOAR from peer 5. Buyer current has 1 BOAR 
```

Node 2 terminal:

```
[ec2-user@ip-172-31-26-99 CompSci677-Lab1]$ java -jar build/libs/BuyerSellerNetwork-1.0-SNAPSHOT.jar -config TestCase3EC2.json -id 2 -role buyer -product fish -hop 3
Mar 18, 2021 1:34:30 AM com.p2p.grpc.Buyer run
INFO: All neighbors of ours{1=id: 1
IPAddress: "172.31.18.44"
port: 8080
, 4=id: 4
IPAddress: "172.31.84.101"
port: 8080
}
Mar 18, 2021 1:34:31 AM com.p2p.grpc.Buyer startServer
INFO: Starting a server at 172.31.26.99 172.31.26.99
Mar 18, 2021 1:34:31 AM com.p2p.grpc.Buyer lambda$run$1
INFO: Currently buying FISH
Mar 18, 2021 1:34:31 AM com.p2p.grpc.Buyer lambda$lookup$0
INFO: Send a lookup request to peer 4 at port 172.31.84.101 8080 for product FISH
Mar 18, 2021 1:34:31 AM com.p2p.grpc.Buyer lambda$lookup$0
INFO: Send a lookup request to peer 1 at port 172.31.18.44 8080 for product FISH
Mar 18, 2021 1:34:32 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 1 for product BOAR
Mar 18, 2021 1:34:32 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Flood Lookup request from peer 1 for product BOAR
Mar 18, 2021 1:34:32 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 4 at port 172.31.84.101 8080 for product BOAR
Mar 18, 2021 1:34:32 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 0 for product BOAR
Mar 18, 2021 1:34:32 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Flood Lookup request from peer 0 for product BOAR
Mar 18, 2021 1:34:32 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 4 at port 172.31.84.101 8080 for product BOAR
Mar 18, 2021 1:34:32 AM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Receive a reply request at 2 size of path 0
Mar 18, 2021 1:34:32 AM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Request Product: FISH
Mar 18, 2021 1:34:32 AM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Buyer Product FISH
Mar 18, 2021 1:34:32 AM com.p2p.grpc.Buyer$MarketPlaceBuyerImpl replyRPC
INFO: Add the seller to the list of sellers
Mar 18, 2021 1:34:34 AM com.p2p.grpc.Buyer buy
INFO: Send a buy request to peer 6
Mar 18, 2021 1:34:34 AM com.p2p.grpc.Buyer buy
INFO: Bought FISH from peer 6. Buyer current has 1 FISH
``` 

Node 3 terminal:

```
9 actionable tasks: 8 executed, 1 up-to-date
[ec2-user@ip-172-31-87-102 CompSci677-Lab1]$ java -jar build/libs/BuyerSellerNetwork-1.0-SNAPSHOT.jar -config TestCase3EC2.json -id 3 -role peer 
Mar 18, 2021 1:34:35 AM com.p2p.grpc.PeerImpl startServer
INFO: Starting a server at 172.31.87.102 8080
Mar 18, 2021 1:34:36 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 2 for product SALT
Mar 18, 2021 1:34:36 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 2 for product SALT
Mar 18, 2021 1:34:36 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Invalidate lookup request from peer 2 for product SALT
Mar 18, 2021 1:34:36 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Flood Lookup request from peer 2 for product SALT
Mar 18, 2021 1:34:36 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 0 at port 172.31.45.247 8080 for product SALT
Mar 18, 2021 1:34:36 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 5 at port 172.31.86.154 8080 for product SALT
Mar 18, 2021 1:34:36 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 2 for product FISH
Mar 18, 2021 1:34:36 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Invalidate lookup request from peer 2 for product FISH
Mar 18, 2021 1:34:36 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 2 for product FISH
Mar 18, 2021 1:34:36 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Flood Lookup request from peer 2 for product FISH
Mar 18, 2021 1:34:36 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 0 at port 172.31.45.247 8080 for product FISH
Mar 18, 2021 1:34:36 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 5 at port 172.31.86.154 8080 for product FISH
Mar 18, 2021 1:34:37 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 1 for product BOAR
Mar 18, 2021 1:34:37 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Flood Lookup request from peer 1 for product BOAR
Mar 18, 2021 1:34:37 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 0 at port 172.31.45.247 8080 for product BOAR
Mar 18, 2021 1:34:38 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 5 at port 172.31.86.154 8080 for product BOAR
Mar 18, 2021 1:34:38 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 2 for product SALT
Mar 18, 2021 1:34:38 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Flood Lookup request from peer 2 for product SALT
Mar 18, 2021 1:34:38 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 0 at port 172.31.45.247 8080 for product SALT
```

Node 4 terminal

```
[ec2-user@ip-172-31-84-101 CompSci677-Lab1]$ java -jar build/libs/BuyerSellerNetwork-1.0-SNAPSHOT.jar -config TestCase3EC2.json -id 4 -role peer 
Mar 18, 2021 1:34:31 AM com.p2p.grpc.PeerImpl startServer
INFO: Starting a server at 172.31.84.101 8080
Mar 18, 2021 1:34:31 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 2 for product FISH
Mar 18, 2021 1:34:31 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Flood Lookup request from peer 2 for product FISH
Mar 18, 2021 1:34:32 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 1 for product BOAR
Mar 18, 2021 1:34:32 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Flood Lookup request from peer 1 for product BOAR
Mar 18, 2021 1:34:32 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 6 at port 172.31.86.91 8080 for product BOAR
Mar 18, 2021 1:34:32 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 6 at port 172.31.86.91 8080 for product FISH
Mar 18, 2021 1:34:32 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 0 for product BOAR
Mar 18, 2021 1:34:32 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Flood Lookup request from peer 0 for product BOAR
Mar 18, 2021 1:34:32 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 6 at port 172.31.86.91 8080 for product BOAR
Mar 18, 2021 1:34:32 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl replyRPC
INFO: Receive Reply request at 4. Size of reverse path 1
Mar 18, 2021 1:34:32 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl replyRPC
INFO: Continue down the path for reply
Mar 18, 2021 1:34:32 AM com.p2p.grpc.PeerImpl reverseReply
INFO: Send a reply request to peer 2 at 172.31.26.99 8080
Mar 18, 2021 1:34:34 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 2 for product SALT
Mar 18, 2021 1:34:34 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Flood Lookup request from peer 2 for product SALT
Mar 18, 2021 1:34:34 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 6 at port 172.31.86.91 8080 for product SALT
Mar 18, 2021 1:34:37 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 2 for product SALT
Mar 18, 2021 1:34:37 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Flood Lookup request from peer 2 for product SALT
Mar 18, 2021 1:34:37 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 6 at port 172.31.86.91 8080 for product SALT
Mar 18, 2021 1:34:38 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 0 for product BOAR
Mar 18, 2021 1:34:38 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Invalidate lookup request from peer 0 for product BOAR
Mar 18, 2021 1:34:41 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 2 for product BOAR
Mar 18, 2021 1:34:41 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl lookupRPC
INFO: Flood Lookup request from peer 2 for product BOAR
Mar 18, 2021 1:34:41 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 6 at port 172.31.86.91 8080 for product BOAR
Mar 18, 2021 1:34:41 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl replyRPC
INFO: Receive Reply request at 4. Size of reverse path 1
Mar 18, 2021 1:34:41 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl replyRPC
INFO: Continue down the path for reply
Mar 18, 2021 1:34:41 AM com.p2p.grpc.PeerImpl reverseReply
INFO: Send a reply request to peer 2 at 172.31.26.99 8080 
```

Node 5 terminal:

```
[ec2-user@ip-172-31-86-154 CompSci677-Lab1]$ java -jar build/libs/BuyerSellerNetwork-1.0-SNAPSHOT.jar -config TestCase3EC2.json -id 5 -role seller -product boar -stock 3 
Mar 18, 2021 1:34:31 AM com.p2p.grpc.Seller startServer
INFO: Starting a server at 172.31.86.154 8080
Mar 18, 2021 1:34:35 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 2  for product SALT
Mar 18, 2021 1:34:35 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Invalidate lookup request from peer 2 for product SALT
Mar 18, 2021 1:34:36 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 2  for product SALT
Mar 18, 2021 1:34:36 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Invalidate lookup request from peer 2 for product SALT
Mar 18, 2021 1:34:36 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 2  for product FISH
Mar 18, 2021 1:34:36 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Invalidate lookup request from peer 2 for product FISH
Mar 18, 2021 1:34:37 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 2  for product SALT
Mar 18, 2021 1:34:38 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Invalidate lookup request from peer 2 for product SALT
Mar 18, 2021 1:34:38 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 1  for product BOAR
Mar 18, 2021 1:34:38 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Reply to lookup request from peer 1 for product BOAR
Mar 18, 2021 1:34:38 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 2  for product SALT
Mar 18, 2021 1:34:38 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Invalidate lookup request from peer 2 for product SALT
Mar 18, 2021 1:34:38 AM com.p2p.grpc.Seller reply
INFO: Send a reply request to peer 3 at port 172.31.87.102 8080 for product BOAR
Mar 18, 2021 1:34:38 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 0  for product BOAR
Mar 18, 2021 1:34:38 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Reply to lookup request from peer 0 for product BOAR
Mar 18, 2021 1:34:38 AM com.p2p.grpc.Seller reply
INFO: Send a reply request to peer 3 at port 172.31.87.102 8080 for product BOAR
Mar 18, 2021 1:34:38 AM com.p2p.grpc.Seller reply
INFO: Done Reply
Mar 18, 2021 1:34:38 AM com.p2p.grpc.Seller reply
INFO: Done Reply
Mar 18, 2021 1:34:38 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 0  for product BOAR
Mar 18, 2021 1:34:38 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Reply to lookup request from peer 0 for product BOAR
Mar 18, 2021 1:34:38 AM com.p2p.grpc.Seller reply
INFO: Send a reply request to peer 3 at port 172.31.87.102 8080 for product BOAR
Mar 18, 2021 1:34:38 AM com.p2p.grpc.Seller reply
INFO: Done Reply
Mar 18, 2021 1:34:39 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 1  for product BOAR
Mar 18, 2021 1:34:39 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Reply to lookup request from peer 1 for product BOAR
Mar 18, 2021 1:34:39 AM com.p2p.grpc.Seller reply
INFO: Send a reply request to peer 3 at port 172.31.87.102 8080 for product BOAR
Mar 18, 2021 1:34:39 AM com.p2p.grpc.Seller reply
INFO: Done Reply
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Seller$MarketplaceSellerImpl buyRPC
INFO: Receive a buy request at 8080 from peer 1 for product BOAR
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Seller$MarketplaceSellerImpl buyRPC
INFO: Finish a transaction for peer 1. Currently, having: 2
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 2  for product BOAR
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Reply to lookup request from peer 2 for product BOAR
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Seller reply
INFO: Send a reply request to peer 6 at port 172.31.86.91 8080 for product BOAR
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Seller reply
INFO: Done Reply
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 2  for product BOAR
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Reply to lookup request from peer 2 for product BOAR
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Seller reply
INFO: Send a reply request to peer 3 at port 172.31.87.102 8080 for product BOAR
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Seller reply
INFO: Done Reply
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 1  for product SALT
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Flood Lookup request from peer 1 for product SALT
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 1  for product SALT
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Invalidate lookup request from peer 1 for product SALT
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Seller$MarketplaceSellerImpl buyRPC
INFO: Receive a buy request at 8080 from peer 0 for product BOAR
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Seller$MarketplaceSellerImpl buyRPC
INFO: Finish a transaction for peer 0. Currently, having: 1
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 0  for product SALT
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Flood Lookup request from peer 0 for product SALT
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 0  for product SALT
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Flood Lookup request from peer 0 for product SALT
Mar 18, 2021 1:34:41 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 6 at port 172.31.86.91 8080 for product SALT
Mar 18, 2021 1:34:41 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 6 at port 172.31.86.91 8080 for product SALT
Mar 18, 2021 1:34:41 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 6 at port 172.31.86.91 8080 for product SALT
Mar 18, 2021 1:34:44 AM com.p2p.grpc.Seller$MarketplaceSellerImpl buyRPC
INFO: Receive a buy request at 8080 from peer 2 for product BOAR
Mar 18, 2021 1:34:44 AM com.p2p.grpc.Seller processBuy
INFO: BOAR runs out!!!! Restocking
Mar 18, 2021 1:34:44 AM com.p2p.grpc.Seller processBuy
INFO: After randomize a new product and restock, now selling FISH
Mar 18, 2021 1:34:44 AM com.p2p.grpc.Seller$MarketplaceSellerImpl buyRPC
INFO: Finish a transaction for peer 2. Currently, having: 3 
```

Node 6 terminal

``` 
[ec2-user@ip-172-31-86-91 CompSci677-Lab1]$ java -jar build/libs/BuyerSellerNetwork-1.0-SNAPSHOT.jar -config TestCase3EC2.json -id 6 -role seller -product fish -stock 3
Mar 18, 2021 1:34:31 AM com.p2p.grpc.Seller startServer
INFO: Starting a server at 172.31.86.91 8080
Mar 18, 2021 1:34:32 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 1  for product BOAR
Mar 18, 2021 1:34:32 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 2  for product FISH
Mar 18, 2021 1:34:32 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Invalidate lookup request from peer 1 for product BOAR
Mar 18, 2021 1:34:32 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Reply to lookup request from peer 2 for product FISH
Mar 18, 2021 1:34:32 AM com.p2p.grpc.Seller reply
INFO: Send a reply request to peer 4 at port 172.31.84.101 8080 for product FISH
Mar 18, 2021 1:34:32 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 0  for product BOAR
Mar 18, 2021 1:34:32 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Invalidate lookup request from peer 0 for product BOAR
Mar 18, 2021 1:34:32 AM com.p2p.grpc.Seller reply
INFO: Done Reply
Mar 18, 2021 1:34:34 AM com.p2p.grpc.Seller$MarketplaceSellerImpl buyRPC
INFO: Receive a buy request at 8080 from peer 2 for product FISH
Mar 18, 2021 1:34:34 AM com.p2p.grpc.Seller$MarketplaceSellerImpl buyRPC
INFO: Finish a transaction for peer 2. Currently, having: 2
Mar 18, 2021 1:34:34 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 2  for product SALT
Mar 18, 2021 1:34:34 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Flood Lookup request from peer 2 for product SALT
Mar 18, 2021 1:34:35 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 5 at port 172.31.86.154 8080 for product SALT
Mar 18, 2021 1:34:37 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 2  for product SALT
Mar 18, 2021 1:34:37 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Flood Lookup request from peer 2 for product SALT
Mar 18, 2021 1:34:37 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 5 at port 172.31.86.154 8080 for product SALT
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 2  for product BOAR
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Flood Lookup request from peer 2 for product BOAR
Mar 18, 2021 1:34:41 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 5 at port 172.31.86.154 8080 for product BOAR
Mar 18, 2021 1:34:41 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl replyRPC
INFO: Receive Reply request at 6. Size of reverse path 2
Mar 18, 2021 1:34:41 AM com.p2p.grpc.PeerImpl$MarketPlaceImpl replyRPC
INFO: Continue down the path for reply
Mar 18, 2021 1:34:41 AM com.p2p.grpc.PeerImpl reverseReply
INFO: Send a reply request to peer 4 at 172.31.84.101 8080
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 1  for product SALT
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Invalidate lookup request from peer 1 for product SALT
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 0  for product SALT
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Invalidate lookup request from peer 0 for product SALT
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 0  for product SALT
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Invalidate lookup request from peer 0 for product SALT
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 1  for product SALT
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Invalidate lookup request from peer 1 for product SALT
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 0  for product SALT
Mar 18, 2021 1:34:41 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Flood Lookup request from peer 0 for product SALT
Mar 18, 2021 1:34:41 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 4 at port 172.31.84.101 8080 for product SALT
Mar 18, 2021 1:34:44 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 2  for product BOAR
Mar 18, 2021 1:34:44 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Flood Lookup request from peer 2 for product BOAR
Mar 18, 2021 1:34:44 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 5 at port 172.31.86.154 8080 for product BOAR
Mar 18, 2021 1:34:44 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 1  for product SALT
Mar 18, 2021 1:34:44 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Invalidate lookup request from peer 1 for product SALT
Mar 18, 2021 1:34:44 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 1  for product SALT
Mar 18, 2021 1:34:44 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Invalidate lookup request from peer 1 for product SALT
Mar 18, 2021 1:34:44 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 0  for product SALT
Mar 18, 2021 1:34:44 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Invalidate lookup request from peer 0 for product SALT
Mar 18, 2021 1:34:44 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 0  for product SALT
Mar 18, 2021 1:34:44 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Flood Lookup request from peer 0 for product SALT
Mar 18, 2021 1:34:44 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 4 at port 172.31.84.101 8080 for product SALT
Mar 18, 2021 1:34:44 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 0  for product SALT
Mar 18, 2021 1:34:44 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Invalidate lookup request from peer 0 for product SALT
Mar 18, 2021 1:34:47 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 2  for product BOAR
Mar 18, 2021 1:34:47 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Flood Lookup request from peer 2 for product BOAR
Mar 18, 2021 1:34:47 AM com.p2p.grpc.PeerImpl lambda$floodLookUp$0
INFO: Send a lookup request to peer 5 at port 172.31.86.154 8080 for product BOAR
Mar 18, 2021 1:34:47 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 1  for product BOAR
Mar 18, 2021 1:34:47 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Invalidate lookup request from peer 1 for product BOAR
Mar 18, 2021 1:34:47 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 1  for product BOAR
Mar 18, 2021 1:34:47 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Invalidate lookup request from peer 1 for product BOAR
Mar 18, 2021 1:34:47 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Receive lookup request at 8080 from peer 0  for product BOAR
Mar 18, 2021 1:34:47 AM com.p2p.grpc.Seller$MarketplaceSellerImpl lookupRPC
INFO: Flood Lookup request from peer 0 for product BOAR
```
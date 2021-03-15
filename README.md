# Lab 1

## Project structure


```
project
│   README.md
│   build.gradle
│   Config.txt    
│
└───doc
│   │   doc.md // design description
│   │   testVerify.md // test verification
│   └───test
│       │   test.sh
│   
└───src
│   └───main
│       └───java
│           └───com.p2p.grpc
│               │   Buyer.java
│               │   Peer.java
│               │   PeerImpl.java
│               │   PeerReference.java
│               │   Seller.java
│       └───proto
│           │   Interfaces.proto
```

## Build and Test

### Compiling

The project is build with Java 1.8, Gradle 6.7, and gRPC 1.36.0

To run:
``` 
$ ./gradlew clean build
```

**hopcount** is hardcoded to be 1 for milestone 1. Professor confirmed that it's okay to have the two peers: one buyer, one seller for milestone 1.

Image ID for the customized EC2 instance:

{
"ImageId": "ami-07916b33d72291f85"
}

### TODO
- [X] Flood Lookup Request
- [X] Synchronization
- [X] Test case 1: 1 buyer and 1 seller 
- [X] Test case 2: Race condition 2 buyers 1 seller
- [X] Test case 3: 7 peers 3 Buyers 2 Sellers 2 peers
- [X] Running test case on a single server EC2
- [] Deploy test case on EC2 cluster
- [] Measure latency
- [] Dynamically generate a network (e.g. assign role for peer and assign their neighbors)
# Lab 1

## Project structure


```
project
│   README.md
│   build.gradle    
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


### TODO 
- [X] Milestone 1 test case 1
- [X] Milestone 1 test case 2
- [X] Milestone 1 Randomly assigned role
- [X] Milestone 2 Flooding nonblocking Lookup Request
- [X] Milestone 2 Synchronization for race condition 
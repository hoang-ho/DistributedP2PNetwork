# Lab 1

## Project structure

```
project
│   README.md
│   build.gradle
│   TestCase1.json
│   TestCase2.json
│   TestCase3.json
│   TestCase2EC2.json
│   TestCase3EC2.json
│
└───doc
│   │   doc.md // design description
│   │   testVerify.md // test verification
│   └───test
│       │   TestCase1.sh
│       │   TestCase2.sh
│       │   TestCase3.sh
└───data
│   │   TestCase3
│   │   TestCase3Modified
│   │   TestCase3EC2
│   │   TestCase3Modified
│   │   CalculateAvg.py
│   
└───src
│   └───main
│       └───java
│           └───com.p2p.grpc
│               │   Buyer.java
│               │   Peer.java
│               │   PeerImpl.java
│               │   Seller.java
│           │   Runner.java
│       └───proto
│           │   Interfaces.proto
```

## Performance Evaluation

**Local Computer**

The data are in the data folder, and I skipped the first few entries due to some processes start up slower than others. 

| TestCase    | Peer   | Lookup ResponseTime (ms) | lookupRPC (ms)  | replyRPC (ms)  | buyRPC (ms)  |
| :---        |    :----: | :----:  |          :---: | :---: | ---: |
| TestCase3.json  | Node 0    |   41.36     | 6.04  |  4.6  |  3.5  |
| TestCase3.json   | Node 1   |   35.72   |  6.3  |  3.4  |  3.3  |
| TestCase3.json   | Node 2   |   31.53    |  6.4  | 3.9 |  3.5  |
| TestCase3.json   | Node 3   |    _    |  7.8  |  5  |  _  |
| TestCase3.json   | Node 4   |    _    |   7  |  4.9  |  _  |
| TestCase3.json   | Node 5   |    _    |   7.9  |  9.7  |  _  |
| TestCase3.json   | Node 6   |    _    |   8.1  |  6.9  |  _  |
| TestCase3Modified.json  | Node 0    |   74.06     | 14.5  |  5.1  |  3.9  |
| TestCase3Modified.json   | Node 1   |   58.71   |  9.06  |  3  |  3.24  |
| TestCase3Modified.json   | Node 2   |   47    |  11.66  | 4.9 |  3.22  |
| TestCase3Modified.json   | Node 3   |    _    |  8.6  |   5  |  _  |
| TestCase3Modified.json   | Node 4   |    _    |  7.5  |  7.6  |  _  |
| TestCase3Modified.json   | Node 5   |    _    |  7.2  |  14  |  _  |
| TestCase3Modified.json   | Node 6   |    _    |  8.4  |  7  |  _  |

**EC2 Cluster**



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
= [ ] Simulate a race condition 
- [X] Running test case on a single server EC2
- [X] Deploy test case on EC2 cluster
- [X] Measure latency
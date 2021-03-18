# Lab 1

## Project structure

```
project
│   README.md
│   build.gradle
│   TestCase1.json
│   TestCase2.json
│   TestCase3.json
│   TestCase3Modified.json
│   TestCase2EC2.json
│   TestCase3EC2.json
│   TestCase3EC2Modified.json
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

Test cases are:

![Screenshot](TestCases.png)

Test Case 3 modified is to check how performance of the network changes when a node 2 and node 3 is connected!

The data are in the data folder, and I skipped the first 2 entries due to some processes start up slower than others. 
There are around 1000 data points for each measurement criteria. I average all data points to got the results in the table. 
Lookup Response Time is the time elapsed between when the Buyer first sends out a lookup request and when the Buyer receives a reply from Seller! 

**Local Computer**

My local computer is a 4 core 16GB RAM. 

| TestCase    | Peer   | Lookup Response Time (ms) | lookupRPC latency (ms)  | replyRPC latency (ms)  | buyRPC latency (ms)  |
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

As we can see from the table, the lookup response time increases drastically when a new connection between node 2 and node 3 is introduced. 
The increase in response time for node 0 and 1 is bigger than increase in response time for node 2. This is due to node 3 being a connecting node for node 0 and node 1 to the Seller, but now node 3 also have to serve node 2.
The increase in response time can be due to (1) our single machine has to create extra threads to handle service call and context switching takes more time; (2) node 3 becoming a bottleneck since it has so many clients to serve!
There are some increases in latency in lookupRPC and replyRPC, but this is much smaller compare to the increase in lookup response time!

**EC2 Cluster**

I deploy each peer on a t2-micro EC2 instance. I skipped the first 2 entries due to some processes start up slower than others!

| TestCase    | Peer   | Lookup Response Time (ms) | lookupRPC latency (ms)  | replyRPC latency (ms)  | buyRPC latency (ms)  |
| :---        |    :----: | :----:  |          :---: | :---: | ---: |
| TestCase3.json  | Node 0          |   34.5  | 5.77  |  5.4  |  5.9  |
| TestCase3.json   | Node 1         |   29.7  |  6.6  |  4.8  |  3.9  |
| TestCase3.json   | Node 2         |   26.1  |  5.7  | 4 |  3.9  |
| TestCase3.json   | Node 3         |    _    |  6.5  |  5  |  _  |
| TestCase3.json   | Node 4         |    _    |   5.3 |  4.6  |  _  |
| TestCase3.json   | Node 5         |    _    |   4.7 |  6.04  |  _  |
| TestCase3.json   | Node 6         |    _    |   4.9 |  5.3  |  _  |
| TestCase3Modified.json  | Node 0    |   41.1  | 7.3  |  5.0  |  5.3  |
| TestCase3Modified.json   | Node 1   |   35.7  |  6.3  | 4.8  |  3.6  |
| TestCase3Modified.json   | Node 2   |   28.8  |  6.3  | 3.8 |  3.1  |
| TestCase3Modified.json   | Node 3   |    _    |  7.4  |   5  |  _  |
| TestCase3Modified.json   | Node 4   |    _    |  5.7  |  4.7  |  _  |
| TestCase3Modified.json   | Node 5   |    _    |  4.03  |  6  |  _  |
| TestCase3Modified.json   | Node 6   |    _    |  4.64  |  4.6  |  _  |

Similarly, on a distributed environment, the response time for lookup request increase when we introduce a new connection between 2 and 3.
The increase in response time for node 0 and 1 is bigger than increase in response time for node 2. This is due to node 3 being a connecting node for node 0 and node 1 to the Seller, but now node 3 also have to serve node 2.
However, the increase is much less compare to single server! This can be due the cost of new connection is distributed among involved peers on different machine, 
while on a single server, the machine incurs the whole cost. 

From the experimental results, we can conclude that when a 


## Build and Test

### Compiling

The project is build with Java 1.8, Gradle 6.7, and gRPC 1.36.0

To compile the code:
``` 
$ ./gradlew clean build
```

### To deploy on EC2 instance 

View instruction in doc/testVerify.md. Customized image with Java 8, git installed and repo cloned:

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
- [X] Deploy test case on EC2 cluster
- [X] Measure latency
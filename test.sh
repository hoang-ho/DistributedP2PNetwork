# to compile
echo "This script is to run the test!"

echo "PLEASE IGNORE the \"FAILURE: Build failed with an exception.\" and the \"SEVERE: Exception while executing runnable io.grpc.internal.ServerImpl$JumpToApplicationThreadServerStreamListener$1HalfClosed\""

echo "These errors are due we stopped the gradle while the communication is happening!"

echo ""

echo "Let's compile"

./gradlew clean build

sleep 3s

# TEST CASE 1
echo ""

echo "STARTING TEST CASE  1"

echo ""
# to create a seller with id 0, at port 8080, sell fish, with stock m = 3, having a neighbor with id 1 at port 8081
gradle p2pSeller --args="0 8080 fish 3 1 8081" &

# to create a buyer with id 1, at port 8081, having a neighbor with id 0 at port 8080
gradle p2pBuyer --args="1 8081 fish 0 8080" &

# let's sleep a bit for the program to run.
# THis is hardcoded so that both seller and buyer have enough time to sell and buy stuffs
sleep 50s

gradle --stop

sleep 3s

pkill -f '.*GradleDaemon.*' &&

# TEST CASE 2
echo ""

echo "STARTING TEST CASE 2"

echo ""
# to create a seller of boar with stock 3 with id 0 at port 8080 and have a neighbor with id 1 at port 8081
gradle p2pSeller --args="0 8080 boar 3 1 8081" &

# to create a buyer of fish with id 0 at port 8081 and have a neighbor with id 0 at port 8080
gradle p2pBuyer --args="1 8081 fish 0 8080" &

sleep 30s

gradle --stop

sleep 3s

pkill -f '.*GradleDaemon.*' &&

EOF
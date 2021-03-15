echo "This script is to run TestCase3 on one single machine"

echo "We will run two processes in parallel in this test script"

# Start the Buyer
java -jar build/libs/BuyerSellerNetwork-1.0-SNAPSHOT.jar -config TestCase2.json -id 0 -role buyer -product boar -hop 4 &

java -jar build/libs/BuyerSellerNetwork-1.0-SNAPSHOT.jar -config TestCase2.json -id 1 -role buyer -product boar -hop 3 &

java -jar build/libs/BuyerSellerNetwork-1.0-SNAPSHOT.jar -config TestCase2.json -id 2 -role buyer -product fish -hop 3 &

# start the no-role peer

java -jar build/libs/BuyerSellerNetwork-1.0-SNAPSHOT.jar -config TestCase2.json -id 3 -role peer &

java -jar build/libs/BuyerSellerNetwork-1.0-SNAPSHOT.jar -config TestCase2.json -id 4 -role peer &

# Start the Seller
java -jar build/libs/BuyerSellerNetwork-1.0-SNAPSHOT.jar -config TestCase2.json -id 5 -role seller -product boar -stock 3 &

java -jar build/libs/BuyerSellerNetwork-1.0-SNAPSHOT.jar -config TestCase2.json -id 6 -role seller -product fish -stock 3 &

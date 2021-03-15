echo "This script is to run TestCase1 on one single machine"

echo "We will run two processes in parallel in this test script"

# Start the Buyer
java -jar build/libs/BuyerSellerNetwork-1.0-SNAPSHOT.jar -config TestCase1.json -id 0 -role buyer -product fish -hop 1 &

# Start the Seller
java -jar build/libs/BuyerSellerNetwork-1.0-SNAPSHOT.jar -config TestCase1.json -id 1 -role seller -product fish -stock 1 &
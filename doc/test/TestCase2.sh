echo "This script is to run TestCase2 on one single machine"

echo "We will run two processes in parallel in this test script"

# Start the Buyer
gradle simulateRace --args="-config TestCase2.json -id 0 -role buyer -product fish -hop 1" &

gradle simulateRace --args="-config TestCase2.json -id 1 -role buyer -product fish -hop 1" &

# Start the Seller
gradle simulateRace --args="-config TestCase2.json -id 2 -role seller -product fish -stock 1" &
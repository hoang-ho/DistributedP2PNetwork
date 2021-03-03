## How to make sure the test is correct?

### Milestone 1

When I run test case 1 (a buyer of fish and a seller of fish), based on the log, I can see that all the fish in the stock got sold. Then the Seller randomly pick another product to sell, and I can see that if the product isn't fish, then nothing is sold and Buyer keeps looking forever.

When I run test case 2 (a buyer of fish and a seller of boar), based on the log, I can see that no product is sold and the Buyer keeps sending out lookup request and Seller keeps receiving it, but since hopcount is hardcoded to 1 in Milestone 1, the lookup request isn't propagated. 
package com.p2p.configure;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ConfigureNetwork {
    public static void main(String[] args) throws IOException {
        // read config file
        int N = 0, K = 0, buyerNum = 0, sellerNum = 0, stock = 0;
        String product = "";
        List<List<String>> config = new ArrayList<>();

        FileInputStream fstream = new FileInputStream("Args.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
        String strLine;
        while ((strLine = br.readLine()) != null) {
            String[] vals = strLine.split(" ");
            if (vals[0].equals("N")) {
                N = Integer.parseInt(vals[1]);
            } else if (vals[0].equals("K")) {
                K = Integer.parseInt(vals[1]);
            } else if (vals[0].equals("buyerNum")) {
                buyerNum = Integer.parseInt(vals[1]);
            } else if (vals[0].equals("sellerNum")) {
                sellerNum = Integer.parseInt(vals[1]);
            } else if (vals[0].equals("product")) {
                product = vals[1];
            } else if (vals[0].equals("stock")) {
                stock = Integer.parseInt(vals[1]);
            } else {
                config.add(Arrays.asList(vals));
            }
        }

        // generate structure
        Random rand = new Random(0);
        List<Integer> peer = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            peer.add(i);
        }

        List<Integer> buyer = new ArrayList<>();
        List<Integer> seller = new ArrayList<>();

        for (int i = 0; i < buyerNum + sellerNum; i++) {
            if (buyer.size() < buyerNum) {
                int randomIndex = rand.nextInt(peer.size());
                buyer.add(peer.get(randomIndex));
                peer.remove(randomIndex);
            } else if (seller.size() < sellerNum){
                int randomIndex = rand.nextInt(peer.size());
                seller.add(peer.get(randomIndex));
                peer.remove(randomIndex);
            }
        }

        Map<Integer, List<Integer>> network = new HashMap<>();


        // Connect peer to seller
        for (int i = 0; i < peer.size(); i++) {
            network.put(peer.get(i), new ArrayList<>());
            int randomSellerNeighbor = 1 + rand.nextInt(Math.min(seller.size(), K - 1));

            for (int j = 0; j < randomSellerNeighbor; j++) {
                int randomIndex = rand.nextInt(seller.size());

                while (network.getOrDefault(seller.get(randomIndex), new ArrayList<>()).size() == K) {
                    randomIndex = rand.nextInt(seller.size());
                }

                if (!network.containsKey(seller.get(randomIndex))) {
                    network.put(seller.get(randomIndex), new ArrayList<>());
                    network.get(seller.get(randomIndex)).add(peer.get(i));
                    network.get(peer.get(i)).add(seller.get(randomIndex));
                } else if (network.get(seller.get(randomIndex)).size() < K - 1) {
                    network.get(seller.get(randomIndex)).add(peer.get(i));
                    network.get(peer.get(i)).add(seller.get(randomIndex));
                }
            }
        }

        // Randomly connect sellers
        for (int i = 0; i < sellerNum; i++) {
            int idx1 = rand.nextInt(sellerNum);
            int idx2 = rand.nextInt(sellerNum);

            if (idx1 != idx2 && network.get(seller.get(idx1)).size() < K && network.get(seller.get(idx2)).size() < K) {
                network.get(seller.get(idx1)).add(seller.get(idx2));
                network.get(seller.get(idx2)).add(seller.get(idx1));
            }
        }

        // Randomly connect peer and buyer
        for (int i = 0; i < buyerNum; i++) {
            network.put(buyer.get(i), new ArrayList<>());
            int randomNumNeighbor = 1 +  rand.nextInt(Math.min(peer.size(), K - 1));
            for (int j = 0; j < randomNumNeighbor; j++) {
                int randomIndex = rand.nextInt(peer.size());

                while (network.getOrDefault(peer.get(randomIndex), new ArrayList<>()).size() == K) {
                    randomIndex = rand.nextInt(peer.size());
                }

                if (!network.containsKey(peer.get(randomIndex))) {
                    network.put(peer.get(randomIndex), new ArrayList<>(buyer.get(i)));
                    network.get(buyer.get(i)).add(peer.get(randomIndex));
                } else if (network.get(peer.get(randomIndex)).size() < K) {
                    network.get(peer.get(randomIndex)).add(buyer.get(i));
                    network.get(buyer.get(i)).add(peer.get(randomIndex));
                }

            }
        }

        // Randomly connect buyer together
        for (int i = 0; i < buyerNum; i++) {
            int idx1 = rand.nextInt(buyerNum);
            int idx2 = rand.nextInt(buyerNum);

            if (idx1 != idx2 && network.get(buyer.get(idx1)).size() < K && network.get(buyer.get(idx2)).size() < K) {
                network.get(buyer.get(idx1)).add(buyer.get(idx2));
                network.get(buyer.get(idx2)).add(buyer.get(idx1));
            }
        }

        // write to file
        System.out.println(network);
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("Config.txt"));
            out.write("N" + " " + N + "\n");
            out.write("K" + " " + K + "\n");
            for (int buyerID: buyer) {
                for (String val: config.get(buyerID)) {
                    out.write(val + " ");
                }
                out.write("buyer" + " " + product + " ");

                for (int neighborId: network.get(buyerID)) {
                    for (String val: config.get(neighborId)) {
                        out.write(val + " ");
                    }
                }
                out.write("\n");
            }

            for (int peerId: peer) {
                for (String val: config.get(peerId)) {
                    out.write(val + " ");
                }

                out.write("peer" + " ");

                for (int neighborId: network.get(peerId)) {
                    for (String val: config.get(neighborId)) {
                        out.write(val + " ");
                    }
                }
                out.write("\n");
            }

            for (int sellerId: seller) {
                for (String val: config.get(sellerId)) {
                    out.write(val + " ");
                }
                out.write("seller" + " " + product + " " + stock + " ");

                for (int neighborId: network.get(sellerId)) {
                    for (String val: config.get(neighborId)) {
                        out.write(val + " ");
                    }
                }
                out.write("\n");
            }

            out.close();
            System.out.println("File created successfully");
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

package com.p2p.configure;

import com.p2p.grpc.Buyer;
import com.p2p.grpc.PeerImpl;
import com.p2p.grpc.Seller;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class Runner {
    public static void main(String[] args) throws IOException {
        // Read the config file and start running on EC2
        int id = Integer.parseInt(args[0]);
        int N = 0, K = 0;
        FileInputStream fstream = new FileInputStream("Config.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
        String strLine;
        while ((strLine = br.readLine()) != null) {
            String[] vals = strLine.split(" ");
            if (vals[0].equals("N")) {
                N = Integer.parseInt(vals[1]);
            } else if (vals[0].equals("K")) {
                K = Integer.parseInt(vals[1]);
            } else if (Integer.parseInt(vals[0]) == id) {
                // create the peer accordingly to the config file
                if (vals[3].equals("buyer")) {
                    // create a buyer
                    Buyer buyer = new Buyer(id, K);
                    buyer.run();
                } else if (vals[3].equals("seller")) {
                    // create a seller
                    Seller seller = new Seller(id, K);
                    seller.run();
                } else {
                    PeerImpl peer = new PeerImpl(id, K);
                    peer.run();
                }
            }
        }
    }
}
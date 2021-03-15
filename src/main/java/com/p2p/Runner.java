package com.p2p;

import com.p2p.grpc.Buyer;
import com.p2p.grpc.PeerId;
import com.p2p.grpc.PeerImpl;
import com.p2p.grpc.Product;
import com.p2p.grpc.Seller;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

public class Runner {
    static final String CONFIG = "-config";
    static final String ROLE = "-role";
    static final String ID = "-id";
    static final String PRODUCT = "-product";
    static final String STOCK = "-stock";
    static final String HOP = "-hop";

    public static void main(String[] args) throws IOException, ParseException {
        // Read the config file and start running on EC2
        String configFile = "", role = "", id = "", product = "FISH";
        int stock = 1, hop = 1;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(CONFIG)) {
                i++;
                configFile = args[i];
            } else if (args[i].equals(ROLE)) {
                i++;
                role = args[i];
            } else if (args[i].equals(ID)) {
                i++;
                id = args[i];
            } else if (args[i].equals(PRODUCT)) {
                i++;
                product = args[i].toUpperCase();
            } else if (args[i].equals(STOCK)) {
                i++;
                stock = Integer.parseInt(args[i]);
            } else if (args[i].equals(HOP)) {
                i++;
                hop = Integer.parseInt(args[i]);
            }
        }

        // create the peer and run it
        JSONObject jsonParser =  (JSONObject) new JSONParser().parse(new FileReader(configFile));
        JSONArray network = (JSONArray) jsonParser.get("network");
        JSONObject nodeConfig = (JSONObject) network.get(Integer.parseInt(id));
        JSONArray neighbors = (JSONArray) nodeConfig.get("neighbors");
        PeerImpl peer;
        if (role.toLowerCase().equals("buyer")) {
            // Create a buyer
            peer = new Buyer(Integer.parseInt(id), (String) nodeConfig.get("IPAddress"),
                    Integer.parseInt((String) nodeConfig.get("port")), neighbors.size(), Product.valueOf(product));
            ((Buyer) peer).setHopCount(hop);

        } else if (role.toLowerCase().equals("seller")) {
            peer = new Seller(Integer.parseInt(id), (String) nodeConfig.get("IPAddress"),
                    Integer.parseInt((String) nodeConfig.get("port")), neighbors.size(), Product.valueOf(product), stock);
        } else {
            peer = new PeerImpl(Integer.parseInt(id), (String) nodeConfig.get("IPAddress"),
                    Integer.parseInt((String) nodeConfig.get("port")), neighbors.size());
        }

        for (int i = 0; i < neighbors.size(); i++) {
            int neighborId = Integer.parseInt((String) neighbors.get(i));
            String neighIP = (String) ((JSONObject) network.get(neighborId)).get("IPAddress");
            int port = Integer.parseInt((String) ((JSONObject) network.get(neighborId)).get("port"));
            PeerId neigh = PeerId.newBuilder().setId(neighborId).setIPAddress(neighIP).setPort(port).build();
            peer.addNeighbor(neigh);
        }
        peer.run();
    }

}
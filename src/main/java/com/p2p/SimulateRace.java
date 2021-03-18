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
import java.util.Random;
import java.util.logging.Logger;

public class SimulateRace {
    // We will use the network in TestCase 2 to simulate a race condition

    static final String CONFIG = "-config";
    static final String ROLE = "-role";
    static final String ID = "-id";
    static final String PRODUCT = "-product";
    static final String STOCK = "-stock";
    static final String HOP = "-hop";
    private static final Logger logger = Logger.getLogger(SimulateRace.class.getName());

    public static void main(String[] args) throws IOException, ParseException {
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
        PeerImpl peer = null;
        if (role.toLowerCase().equals("buyer")) {
            // Create a buyer
            peer = new Buyer(Integer.parseInt(id), (String) nodeConfig.get("IPAddress"),
                    Integer.parseInt((String) nodeConfig.get("port")), neighbors.size(), Product.valueOf(product));
            ((Buyer) peer).setHopCount(hop);

        } else if (role.toLowerCase().equals("seller")) {
            peer = new Seller(Integer.parseInt(id), (String) nodeConfig.get("IPAddress"),
                    Integer.parseInt((String) nodeConfig.get("port")), neighbors.size(), Product.valueOf(product), stock);
        }

        for (int i = 0; i < neighbors.size(); i++) {
            int neighborId = Integer.parseInt((String) neighbors.get(i));
            String neighIP = (String) ((JSONObject) network.get(neighborId)).get("IPAddress");
            int port = Integer.parseInt((String) ((JSONObject) network.get(neighborId)).get("port"));
            PeerId neigh = PeerId.newBuilder().setId(neighborId).setIPAddress(neighIP).setPort(port).build();
            peer.addNeighbor(neigh);
        }

        if (role.toLowerCase().equals("buyer")) {
            // buy directly from our neighbor - which is the sole seller
            Random RANDOM = new Random(0);
            while (true) {
                for (PeerId neigh: peer.getAllNeighbors().values()) {
                    peer.buy(neigh);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                logger.info("Choose a new product to buy");
                ((Buyer) peer).setProduct(Product.values()[RANDOM.nextInt(3)].name());
                logger.info("Now buying " + ((Buyer) peer).getProduct());
            }
        } else {
            // if we are seller, just run the server
            peer.run();
        }
    }
}

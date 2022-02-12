package woolbattle.woolbattle.woolsystem;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonArray;
import org.bson.BsonDouble;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import woolbattle.woolbattle.Main;

import java.util.ArrayList;

public class BlockBreakingSystemB {

    private static ArrayList<Location> mapBlocks = new ArrayList<Location>();
    private static boolean collectBrokenBlocks = false;

    public static void fetchMapBlocks() {
        //Is supposed to fetch the Blocks, about to become the map from a database

        MongoDatabase db = Main.getMongoClient().getDatabase("woolbattle");
            MongoCollection<Document> blockBreaking = db.getCollection("mapBlocks");
            ArrayList<Location> updatedMapBlocks = mapBlocks;

            for(BsonValue argValue : (BsonArray) blockBreaking.find(
                    new Document(
                            "mapBlocks",
                            new BsonArray()
                    )
            ).first().get("mapBlocks")
            ){
                BsonArray argArray = argValue.asArray();

                for(BsonValue argValue2 : argArray){
                    BsonArray argArray2 = argValue2.asArray();

                    Location location = new Location(
                            Bukkit.getWorlds().get(0),
                            argArray2.get(0).asDouble().doubleValue(),
                            argArray2.get(1).asDouble().doubleValue(),
                            argArray2.get(0).asDouble().doubleValue()
                    );
                    updatedMapBlocks.add(location);
                }
            }
            mapBlocks = updatedMapBlocks;
        }


        public static void pushMapBlocks(){
            MongoDatabase db = Main.getMongoClient().getDatabase("woolbattle");
            boolean hasWoolbattle = false;

            MongoCollection<Document> collection;

            if(!db.listCollectionNames().into(
                    new ArrayList<String>()).contains("woolbattle")
            ){
                db.createCollection("woolbattle");
            }else{}

            collection = db.getCollection("woolbattle");

            if(!collection.listIndexes().
                    into(new ArrayList<Document>()).contains(new Document("mapBlocks",new BsonArray()))
            ){
                collection.createIndex(new Document("mapBlocks", new BsonArray()));
            }else{}

            BsonArray update = (BsonArray) (collection.find(new Document("mapBlocks", new BsonArray())).first().get("mapBlocks"));

            for(Location loc : mapBlocks){
                update.add(new BsonArray(){
                    {
                        add(new BsonDouble(loc.getX()));
                        add(new BsonDouble(loc.getY()));
                        add(new BsonDouble(loc.getZ()));
                    }
                });
            }
            collection.updateOne(new Document("mapBlocks", new BsonArray()), new Document("mapBlocks", update));
        }

        public static boolean isCollectBrokenBlocks() {
            return collectBrokenBlocks;
        }

        public static void setCollectBrokenBlocks(boolean collectBrokenBlocksArg) {
            collectBrokenBlocks = collectBrokenBlocksArg;
        }

        public static void setMapBlocks(ArrayList<Location> mapBlocksArg) {
            mapBlocks = mapBlocksArg;
        }

        public static ArrayList<Location> getMapBlocks() {
            return mapBlocks;
        }
    }
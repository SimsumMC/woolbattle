package woolbattle.woolbattle.woolsystem;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonArray;
import org.bson.BsonDouble;
import org.bson.BsonValue;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import woolbattle.woolbattle.Main;

import java.util.ArrayList;


public class BlockBreakingSystem {
    private static ArrayList<Location> mapBlocks = new ArrayList<Location>();
    private static BsonArray mapBlocksB = new BsonArray();
    private static boolean collectBrokenBlocks = false;

    public static void fetchMapBlocks() {
        //Is supposed to fetch the Blocks, about to become the map from a database

        MongoDatabase db = Main.getMongoClient().getDatabase("woolbattle");
        MongoCollection<Document> blockBreaking = db.getCollection("mapBlocks");

        /*MongoDatabase db = Main.getMongoClient().getDatabase("woolbattle");

        if(db.getCollection("blockBreaking").count() == 0||db.getCollection("blockBreaking").find(new Document("mapBlocks", new BsonArray())).first() == null){

            db.createCollection("blockBreaking");
            db.getCollection("blockBreaking").insertOne(new Document("mapBlocks", new BsonArray()));
        }else{

            ArrayList<Location> locMapBlocks = new ArrayList<Location>();
            Document locMapBlocksDoc = db.getCollection("blockBreaking").find(new Document("mapBlocks", new BsonArray())).first();

            ArrayList<BsonArray> currentMapBlocks = (ArrayList<BsonArray>) locMapBlocksDoc.get("mapBlocks");

            for(int i = 0; i<currentMapBlocks.size();i++){
                BsonArray iterValue = currentMapBlocks.get(i);
                Location fetchedLoc = new Location(Bukkit.getWorlds().get(0), iterValue.get(0).asDouble().doubleValue(), iterValue.get(1).asDouble().doubleValue(), iterValue.get(2).asDouble().doubleValue());

                if(iterValue != null){
                    locMapBlocks.add(fetchedLoc);
                }
            }
            setMapBlocks(locMapBlocks);
        }*/
    }


    public static void pushMapBlocks(){
        MongoDatabase db = Main.getMongoClient().getDatabase("woolbattle");
        boolean hasWoolbattle = false;
        boolean hasBlockBreaking;
        MongoCollection<Document> collection;

        if(!db.listCollectionNames().into(new ArrayList<String>()).contains("woolbattle")){
            db.createCollection("woolbattle");
        }else{}

        collection = db.getCollection("woolbattle");

        if(!collection.listIndexes().into(new ArrayList<Document>()).contains(new Document("mapBlocks", new BsonArray()))){
          collection.createIndex(new Document("mapBlocks", new BsonArray()));
        }else{}

        BsonArray update = new BsonArray(){
        };

        for(BsonValue value : (BsonArray) (collection.find(new Document("mapBlocks", new BsonArray())).first().get("mapBlocks"))){
            BsonArray varArray = (BsonArray) value;
            update.add(varArray);
        }
        for(Location loc : mapBlocks){
            update.add(new BsonArray(){
                {
                    add(0, new BsonDouble(loc.getX()));
                    add(1, new BsonDouble(loc.getY()));
                    add(2, new BsonDouble(loc.getZ()));
                }
            });
        }
        db.getCollection("woolbattle").updateOne(new Document("mapBlocks", new BsonArray()), new Document("mapBlocks", update));
    }

    public static boolean isCollectBrokenBlocks() {
        return collectBrokenBlocks;
    }

    public static void setCollectBrokenBlocks(boolean collectBrokenBlocks) {
        BlockBreakingSystem.collectBrokenBlocks = collectBrokenBlocks;
    }

    public static void setMapBlocks(ArrayList<Location> mapBlocksArg) {
        BsonArray locArray = new BsonArray();

        for(Location locArg : mapBlocksArg){
            locArray.add(new BsonArray(){
                {
                    add(0, new BsonDouble(locArg.getX()));
                    add(1, new BsonDouble(locArg.getY()));
                    add(2, new BsonDouble(locArg.getZ()));
                }
            });

        }

        mapBlocksB = locArray;
    }
    public static ArrayList<Location> getMapBlocks() {
       ArrayList<Location> ret = new ArrayList<Location>();
        for(BsonValue argArray : mapBlocksB){
            BsonArray argArrayNest = (BsonArray) argArray;
            ret.add(new Location(Bukkit.getWorlds().get(0),argArrayNest.get(0).asDouble().doubleValue(), argArrayNest.get(1).asDouble().doubleValue(), argArrayNest.get(2).asDouble().doubleValue()));
        }
        return ret;
    }
}
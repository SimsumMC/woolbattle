package woolbattle.woolbattle.woolsystem;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import org.bson.BsonArray;
import org.bson.BsonDouble;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import woolbattle.woolbattle.Main;

import java.util.ArrayList;


public class BlockBreakingSystem {

    private static ArrayList<Location> mapBlocks = new ArrayList<Location>();
    private static boolean collectBrokenBlocks = false;

    public static void fetchMapBlocks() {
        //Is supposed to fetch the Blocks, about to become the map from a database
        MongoDatabase db = Main.getMongoClient().getDatabase("woolbattle");

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
        }
    }


    public static void pushMapBlocks(){
        MongoDatabase db = Main.getMongoClient().getDatabase("woolbattle");
        BsonArray blocksToPush = new BsonArray();

        if(db.getCollection("mapBlocks").find(new Document("mapBlocks", new BsonArray())).first() == null||db.getCollection("blockBreaking").count() == 0){
            db.getCollection("mapBlocks").insertOne(new Document("mapBlocks", new BsonArray()));
        }
        blocksToPush.addAll((ArrayList<BsonArray>) db.getCollection("mapBlocks").find(new Document("mapBlocks", new BsonArray())).first().get("mapBlocks"));

        for(Location iterBlock : mapBlocks){
            BsonArray arr = new BsonArray(){
                {
                    add(new BsonDouble(iterBlock.getBlockX()));
                    add(new BsonDouble(iterBlock.getBlockY()));
                    add(new BsonDouble(iterBlock.getBlockZ()));
                }
            };
            blocksToPush.add(arr);

        }

        Document formerMapBlocksDoc = db.getCollection("blockBreaking").find(new Document("mapBlocks", new BsonArray())).first();
        Document mapBlocksDoc = new Document();
        /*for(BsonArray iterArray : blocksToPush){
            List<BsonArray> formerMapBlocksIter = (List<BsonArray>) formerMapBlocksDoc.get("mapBlocks");
            formerMapBlocksIter.add(iterArray);
        }*/

        db.getCollection("woolbattle").updateOne(new Document("mapBlocks", new BsonArray()), new Document("mapBlocks", blocksToPush), new UpdateOptions());
    }

    public static boolean isCollectBrokenBlocks() {
        return collectBrokenBlocks;
    }

    public static void setCollectBrokenBlocks(boolean collectBrokenBlocks) {
        BlockBreakingSystem.collectBrokenBlocks = collectBrokenBlocks;
    }

    public static void setMapBlocks(ArrayList<Location> mapBlocksArg) {
        mapBlocks = mapBlocksArg;
    }
    public static ArrayList<Location> getMapBlocks() {
        return mapBlocks;
    }
}
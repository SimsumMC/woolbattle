package woolbattle.woolbattle.woolsystem;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import org.bson.BsonArray;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import woolbattle.woolbattle.Main;

import java.util.ArrayList;

import static com.mongodb.client.model.Filters.exists;

public class BlockBreakingSystem {

    private static ArrayList<Location> mapBlocks = new ArrayList<Location>();
    private static boolean collectBrokenBlocks = false;
    private static ArrayList<Location> removedBlocks = new ArrayList<Location>();

    //Setter and getter, concerning the previously defined private variables

    public static boolean isCollectBrokenBlocks() {return collectBrokenBlocks;}
    public static void setCollectBrokenBlocks(boolean collectBrokenBlocksArg) {collectBrokenBlocks = collectBrokenBlocksArg;}

    public static ArrayList<Location> getMapBlocks() {return mapBlocks;}
    public static void setMapBlocks(ArrayList<Location> mapBlocksArg) {mapBlocks = mapBlocksArg;}

    public static ArrayList<Location> getRemovedBlocks() {return removedBlocks;}
    public static void setRemovedBlocks(ArrayList<Location> removedBlocks) {BlockBreakingSystem.removedBlocks = removedBlocks;}

    public static void clearDbMapBlocks(){

        //Clears the mapBlocks, stored in the db.

        MongoDatabase db = Main.getMongoClient().getDatabase("woolbattle");
        db.getCollection("blockBreaking").replaceOne(exists("mapBlocks"), new Document("mapBlocks", new ArrayList<ArrayList<Double>>()));
    }

    public static void fetchMapBlocks() {

        //Fetches stored mapBlocks from the db into the cached blocks array.

        MongoDatabase db = Main.getMongoClient().getDatabase("woolbattle");
        MongoCollection<Document> blockBreaking;
        ArrayList<Location> updatedMapBlocks = mapBlocks;

        //Checks, whether the "blockBreaking" collection and the "mapBlocks" documents exist in the db, creates them, if this is not the case.

        if(!db.listCollectionNames().into(new ArrayList<String>()).contains("blockBreaking")){
            db.createCollection("blockBreaking");
        }else{}

        if(!db.getCollection("blockBreaking").listIndexes().into(new ArrayList<Document>()).contains(new Document("mapBlocks",new BsonArray()))){
            db.getCollection("blockBreaking").insertOne(new Document("mapBlocks", new ArrayList<ArrayList<Double>>()));
        }else{}
        
        //Iterates over the mapBlocks, present in the db, converts the into valid locations and ultimately add them to a previously created array.

        for(ArrayList<Double> argArray: (ArrayList<ArrayList<Double>>) db.getCollection("blockBreaking").find(exists("mapBlocks")).first().get("mapBlocks")){
            if(argArray.size() == 0){
                break;
            }else {
                Location location = new Location(
                        Bukkit.getWorlds().get(0),
                        argArray.get(0),
                        argArray.get(1),
                        argArray.get(0)
                );

                updatedMapBlocks.add(location);
            }
        }

        //Replaces the currently cached blocks with the previously prepared updated mapBlocks array.

        BlockBreakingSystem.setMapBlocks(updatedMapBlocks);
    }

    public static void pushMapBlocks(){

        //Pushes the currently present cached blocks into the database.

        if(mapBlocks.size() == 0){

            return;
        }

        MongoDatabase db = Main.getMongoClient().getDatabase("woolbattle");

        //Checks, whether the "blockBreaking" collection and the "mapBlocks" documents exist in the db, creates them, if this is not the case.

        if(!db.listCollectionNames().into(new ArrayList<String>()).contains("blockBreaking")){
            db.createCollection("blockBreaking");
        }else{}

        if(!db.getCollection("blockBreaking").listIndexes().into(new ArrayList<Document>()).contains(new Document("mapBlocks", new ArrayList<ArrayList<Double>>()))){
           db.getCollection("blockBreaking").insertOne(new Document("mapBlocks", new BsonArray()));
        }else{}

        //Fetches the stored mapBlocks from the db into a new array (update).

        ArrayList<ArrayList<Double>> update = (ArrayList<ArrayList<Double>>) db.getCollection("blockBreaking").find(exists("mapBlocks")).first().get("mapBlocks");

        //Adds the cached blocks to the updated array, in case they are not already present in said collection.

        for(Location loc : mapBlocks){
            ArrayList<Double> locArray = new ArrayList<Double>(){
                {
                    add(loc.getX());
                    add(loc.getY());
                    add(loc.getZ());
                }
            };

            if(!update.contains(locArray)){
                update.add(locArray);
            }

        }

        //Searches for blocks in the array, about to replace the mapBlocks-array in the db, that are present in the removed-blocks-array and deletes them.

        for(ArrayList<Double> locArray : update/*((ArrayList<ArrayList<Double>>)db.getCollection("blockBreaking").find(exists("mapBlocks")).first().get("mapBlocks"))*/){
            Location loc = new Location(Bukkit.getWorlds().get(0), locArray.get(0), locArray.get(1), locArray.get(2));
            if(removedBlocks.contains(loc)){
                update.remove(locArray);
            }
        }

        //Replaces the mapBlocksArray in the db with the previously-prepared one (update).

        db.getCollection("blockBreaking").replaceOne(db.getCollection("blockBreaking").find(exists("mapBlocks")).first(), new Document("mapBlocks", update), new UpdateOptions().upsert(true));
    }

    public static String locArrayToString(ArrayList<Location> locs){

        //Method, meant to convert an array of locations towards an appropriately coloured string, representing it.

        StringBuilder result = new StringBuilder(ChatColor.DARK_PURPLE + "[");

        if(locs.size() == 0){
            result.append(ChatColor.DARK_PURPLE + "]");
            return result.toString();
        }
        else{
            for(int i = 0; i<locs.size(); i++){

                result.append("\n" + ChatColor.GREEN + "[" + ChatColor.RED + locs.get(i).getX() +", " + locs.get(i).getY() +", " +locs.get(i).getZ() + ChatColor.GREEN + "]");

                if(i == (locs.size() -1)){
                    result.append(ChatColor.DARK_PURPLE + "\n]");
                }else{
                    result.append(ChatColor.AQUA + ", ");
                }
            }
        }

        return result.toString();
    }
}
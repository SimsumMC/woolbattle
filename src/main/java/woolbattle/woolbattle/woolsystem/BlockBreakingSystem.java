package woolbattle.woolbattle.woolsystem;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bukkit.*;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import woolbattle.woolbattle.Main;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;


public class BlockBreakingSystem {
    private static boolean collectBlocksTroughDiff = false;

    public static boolean isCollectBlocksTroughDiff() {
        return collectBlocksTroughDiff;
    }

    public static void setCollectBlocksTroughDiff(boolean collectBlocksTroughDiff) {
        BlockBreakingSystem.collectBlocksTroughDiff = collectBlocksTroughDiff;
    }

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

        if(!db.getCollection("blockBreaking").listIndexes().into(new ArrayList<Document>()).contains(eq("_id", Main.getObjectId()))){
            db.getCollection("blockBreaking").insertOne(new Document("mapBlocks", new ArrayList<ArrayList<Double>>()));
            Main.setObjectId(db.getCollection("blockBreaking").find(exists("mapBlocks")).first().getObjectId("_id"));
        }else{}
        
        //Iterates over the mapBlocks, present in the db, converts the into valid locations and ultimately add them to a previously created array.

        for(ArrayList<Double> argArray: (ArrayList<ArrayList<Double>>) db.getCollection("blockBreaking").find(eq("_id", Main.getObjectId())).first().get("mapBlocks")){
            if(argArray.size() == 0){
                break;
            }else {
                Location location = new Location(
                        Bukkit.getWorlds().get(0),
                        argArray.get(0),
                        argArray.get(1),
                        argArray.get(2)
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

        if(!db.getCollection("blockBreaking").listIndexes().into(new ArrayList<Document>()).contains(eq("_id", Main.getObjectId()))){
           db.getCollection("blockBreaking").insertOne(new Document("mapBlocks", new ArrayList<ArrayList<Double>>()));
           Main.setObjectId(db.getCollection("blockBreaking").find(exists("mapBlocks")).first().getObjectId("_id"));
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

        db.getCollection("blockBreaking").replaceOne(
                        db.getCollection("blockBreaking").
                                find(exists("mapBlocks")).
                                first(),
                        new Document("mapBlocks", update)
                        //,new UpdateOptions().upsert(true)
        );
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
    public static String doubleArrArrToString(ArrayList<ArrayList<Double>> locs){

        //Method, meant to convert an array of locations towards an appropriately coloured string, representing it.

        StringBuilder result = new StringBuilder(ChatColor.DARK_PURPLE + "[");

        if(locs.size() == 0){
            result.append(ChatColor.DARK_PURPLE + "]");
            return result.toString();
        }
            for(int i = 0; i<locs.size(); i++){

                result.append("\n" + ChatColor.GREEN + "[" + ChatColor.RED + locs.get(i).get(0) +", " + locs.get(i).get(1) +", " +locs.get(i).get(2) + ChatColor.GREEN + "]");

                if(i == (locs.size() -1)){
                    result.append(ChatColor.DARK_PURPLE + "\n]");
                }else{
                    result.append(ChatColor.AQUA + ", ");
                }

            }
        return result.toString();
    }

    public static void addBlocksByRange(Location a, Location b) {

        World standard = Bukkit.getWorlds().get(0);

        int xdiff = (int) a.getX() - (int) b.getX();
        int ydiff = (int) a.getY() - (int) b.getY();
        int zdiff = (int) a.getZ() - (int) b.getZ();
        ArrayList<Location> locs = new ArrayList<>();
        ArrayList<Integer>
                xs = new ArrayList<Integer>(),
                ys = new ArrayList<Integer>(),
                zs = new ArrayList<Integer>();


        if(Integer.signum(xdiff) == 0){
            xs.add((int) b.getX());
        }else{
            for(int i = (int) b.getX(); ((Integer.signum(xdiff)) == -1)? (i>a.getX()) : (i<a.getX()); i += Integer.signum(xdiff)){
                xs.add(i);
            }
        }

        if(Integer.signum(ydiff) == 0){
            xs.add((int) b.getX());
        }else{
            for(int i = (int) b.getY();((Integer.signum(ydiff)) == -1)? (i>a.getY()) : (i<a.getY()); i += Integer.signum(ydiff)){
                ys.add(i);
            }
        }

        if(Integer.signum(zdiff) == 0){
            zs.add((int) b.getZ());
        }else{
            for(int i = (int) b.getZ();((Integer.signum(zdiff)) == -1)? (i>a.getZ()) : (i<a.getZ()); i += Integer.signum(zdiff)){
                zs.add(i);
            }
        }

        for(int x : xs){
           for(int y : ys){
               for(int z : zs){
                   if(new Location(standard, x, y, z).getBlock().getType().equals(Material.WOOL)){
                       locs.add(new Location(standard, x, y, z));
                   }
               }
           }
        }
        for(Location l : locs){
            if(mapBlocks.contains(l)){
                break;
            }
            mapBlocks.add(l);
        }


    }
}
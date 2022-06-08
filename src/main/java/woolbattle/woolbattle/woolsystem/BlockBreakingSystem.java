package woolbattle.woolbattle.woolsystem;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bukkit.*;
import org.bukkit.block.Block;
import woolbattle.woolbattle.Config;
import woolbattle.woolbattle.Main;
import java.util.ArrayList;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;
import static java.lang.String.format;

public class BlockBreakingSystem {

    private static final String green = ChatColor.GREEN.toString();
    private static final String blue = ChatColor.BLUE.toString();

    private static ArrayList<Location> mapBlocks = new ArrayList<>();
    private static boolean collectBrokenBlocks = false;
    private static ArrayList<Location> removedBlocks = new ArrayList<>();


    //Setter and getter, concerning the previously defined private variables

    public static boolean isCollectBrokenBlocks() {return collectBrokenBlocks;}
    public static void setCollectBrokenBlocks(boolean collectBrokenBlocksArg) {collectBrokenBlocks = collectBrokenBlocksArg;}

    public static ArrayList<Location> getMapBlocks() {return mapBlocks;}
    public static void setMapBlocks(ArrayList<Location> mapBlocksArg) {mapBlocks = mapBlocksArg;}

    public static ArrayList<Location> getRemovedBlocks() {return removedBlocks;}
    public static void setRemovedBlocks(ArrayList<Location> removedBlocks) {BlockBreakingSystem.removedBlocks = removedBlocks;}



    /**
     * Method, dedicated to clearing the mapBlocks array, stored in the specified database.
     * @author Servaturus
     * */
    public static void clearDbMapBlocks(){

        MongoDatabase db = Main.getMongoClient().getDatabase("woolbattle");
        db.getCollection("map").
                replaceOne(
                eq("_id", "mapBlocks"),
                new Document("_id", "mapBlocks").append("mapBlocks", new ArrayList<ArrayList<Double>>())
        );
    }

    /**
     * Method that fetches stored mapBlocks from the db into the cached blocks array.
     * @author Servaturus
     *
     */
    public static void fetchMapBlocks() {
        MongoDatabase db = Main.getMongoClient().getDatabase("woolbattle");
        ArrayList<Location> updatedMapBlocks = mapBlocks;

        //Checks, whether the "map" collection and the "mapBlocks" documents exist in the db, creates them, if this is not the case.

        if(!db.listCollectionNames().into(new ArrayList<>()).contains("map")){
            db.createCollection("map");
        }
        Document found = db.getCollection("map").find(eq("_id", "mapBlocks")).first();
        if(found == null){
            db.getCollection("map").insertOne( new Document("_id", "mapBlocks").append("mapBlocks", new ArrayList<ArrayList<Double>>()));
        }

        //Iterates over the mapBlocks, present in the db, converts the into valid locations and ultimately add them to a previously created array.


        for(ArrayList<Double> argArray: (ArrayList<ArrayList<Double>>) Main.getMongoDatabase().getCollection("map").find(eq("_id", "mapBlocks")).first().get("mapBlocks")/*db.getCollection("map").find(eq("_id", "mapBlocks")).first().get("mapBlocks")*/){
            if(argArray.size() == 0){
                break;
            }else {
                Location location = new Location(
                        Bukkit.getWorlds().get(0),
                        argArray.get(0),
                        argArray.get(1),
                        argArray.get(2)
                );
                if(updatedMapBlocks.contains(location)){
                    continue;
                }
                updatedMapBlocks.add(location);
            }
        }

        //Replaces the currently cached blocks with the previously prepared updated mapBlocks array.

        BlockBreakingSystem.setMapBlocks(updatedMapBlocks);
    }

    /**
     * Method pushing cached mapBlocks towards the specified database.
     * @author Servaturus
     */
    public static void pushMapBlocks(){

        //Pushes the currently present cached blocks into the database.
        String collectionString = "map";
        String objectIdString = "mapBlocks";
        String key = "mapBlocks";
        if(mapBlocks.size() == 0){

            return;
        }
        if(!Main.getMongoDatabase().listCollectionNames().into(new ArrayList<>()).contains("map")){
            Main.getMongoDatabase().createCollection("map");
        }
        Document found = Main.getMongoDatabase().getCollection("map").find(eq("_id", "mapBlocks")).first();
        if(found == null){
            Main.getMongoDatabase().getCollection("map").insertOne(new Document("_id", "mapBlocks").append("mapBlocks", new ArrayList<ArrayList<Double>>()));
        }

        //Fetches the stored mapBlocks from the db into a new array (update).
            ArrayList<ArrayList<Double>> update = (ArrayList<ArrayList<Double>>) Main.getMongoDatabase().getCollection("map").find(eq("_id", "mapBlocks")).first().get("mapBlocks");

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

            for(ArrayList<Double> locArray : update/*((ArrayList<ArrayList<Double>>)db.getCollection("map").find(exists("mapBlocks")).first().get("mapBlocks"))*/){
                Location loc = new Location(Bukkit.getWorlds().get(0), locArray.get(0), locArray.get(1), locArray.get(2));
                if(removedBlocks.contains(loc)){
                    update.remove(locArray);
                }
            }

            //Replaces the mapBlocksArray in the db with the previously-prepared one (update).
            Main.getMongoDatabase().getCollection("map").replaceOne(eq("_id", "mapBlocks"), new Document("_id", "mapBlocks").append("mapBlocks", update));
    }

    /**
     *  Method, meant to convert an array of locations towards an appropriately coloured string, representing it.
     * @param locs The ArrayList of locations, meant to be converted into a string.
     * @return The string, generated according to the input ArrayList of locations.
     */
    public static String locArrayToString(ArrayList<Location> locs){

        StringBuilder result = new StringBuilder(ChatColor.DARK_PURPLE + "[");

        if(locs.size() == 0){
            result.append(format("%s]", ChatColor.DARK_PURPLE));
            return result.toString();
        }
        else{

            for(int i = 0; i<locs.size(); i++){

                result.append(format("\n%s[%s%f,%f,%f%s]",
                        green,
                        blue,
                        locs.get(i).getX(),
                        locs.get(i).getY(),
                        locs.get(i).getZ(),
                        green
                ));

                if(i == (locs.size() -1)){
                    result.append(format("%s]", ChatColor.DARK_PURPLE));
                }else{
                    result.append(format("%s,", ChatColor.AQUA));
                }

            }
        }

        return result.toString();
    }

    /**
     *  Method, meant to convert an array of ArrayList of doubles towards an appropriately coloured string, representing it.
     * @param locs The ArrayList of locations, meant to be converted into a string.
     * @return The String, corresponding with the specified input ArrayList of ArrayLists of doubles.
     */
    public static String doubleArrArrToString(ArrayList<ArrayList<Double>> locs){

        StringBuilder result = new StringBuilder(ChatColor.DARK_PURPLE + "[");

        if(locs.size() == 0){
            result.append(format("%s]", ChatColor.DARK_PURPLE));
            return result.toString();
        }
        for(int i = 0; i<locs.size(); i++){

            result.append(format("\n%s[%s%f,%f,%f%s]",
                    green,
                    blue,
                    locs.get(i).get(0),
                    locs.get(i).get(1),
                    locs.get(i).get(2),
                    green
            ));

            if(i == (locs.size() -1)){
                result.append(format("%s]", ChatColor.DARK_PURPLE));
            }else{
                result.append(format("%s,", ChatColor.AQUA));
            }

        }
        return result.toString();
    }

    /**
     * Method, capable of adding locations to the local array of map-Blocks, using two input location-vectors.
     * The differences of these vectors in the respective dimensions serve as the height, width and depth of a volume of blocks, whose elements are added to the array of map-blocks.
     * @param a The location, specifying the origin vector of the range, used to add the blocks to the array of map-blocks.
     * @param b The location, specifying the end vector of the range, used to add the blocks to the array of map-blocks.
     * @author Servaturus
     */
    public static void addBlocksByRange(Location a, Location b) {

        World standard = Bukkit.getWorlds().get(0);

        int xdiff = (int) a.getX() - (int) b.getX();
        int ydiff = (int) a.getY() - (int) b.getY();
        int zdiff = (int) a.getZ() - (int) b.getZ();
        ArrayList<Location> locs = new ArrayList<>();
        ArrayList<Integer>
                xs = new ArrayList<>(),
                ys = new ArrayList<>(),
                zs = new ArrayList<>();

        //Adds every x value in the range of xdiff to the xs array.
        if(Integer.signum(xdiff) == 0){
            xs.add((int) b.getX());
        }else{
            for(int i = (int) b.getX(); ((Integer.signum(xdiff)) == -1)? (i>a.getX()) : (i<a.getX()); i += Integer.signum(xdiff)){
                xs.add(i);
            }
        }
        //Similar approach regarding ydiff and ys.
        if(Integer.signum(ydiff) == 0){
            xs.add((int) b.getX());
        }else{
            for(int i = (int) b.getY();((Integer.signum(ydiff)) == -1)? (i>a.getY()) : (i<a.getY()); i += Integer.signum(ydiff)){
                ys.add(i);
            }
        }
        //Another repetition on regard of zdiff and zs.
        if(Integer.signum(zdiff) == 0){
            zs.add((int) b.getZ());
        }else{
            for(int i = (int) b.getZ();((Integer.signum(zdiff)) == -1)? (i>a.getZ()) : (i<a.getZ()); i += Integer.signum(zdiff)){
                zs.add(i);
            }
        }
        //Combines every element of xs with every element of y and the resulting combinations with every element of z.
        for(int x : xs){
            for(int y : ys){
                for(int z : zs){

                    if(new Location(standard, x, y, z).getBlock().getType().equals(Material.WOOL)){
                        locs.add(new Location(standard, x, y, z));
                    }
                }
            }
        }
        //Adds locations, constituted by the former created value pairs (of xs, ys, and zs), to the global mapBlocks array.
        for(Location l : locs){
            System.out.println(l);
            if(!mapBlocks.contains(l)){
                mapBlocks.add(l);
                System.out.println(l);
            }
        }
    }

    /**
     * Removes any wool blocks in a previously defined range of chunks, not belonging to the blocks of the same map,
     * defined in addition to that.
     * @author Servaturus
     *
     */
    public static void resetMap(){
        Document doc = Main.getMongoDatabase().getCollection("map").find(eq("_id", "mapChunks")).first();
        if(doc == null){
            System.out.println("There are no chunks, belonging to the map, specified in the database");
            return;
        }

        System.out.println(doc.toString());


        ArrayList<ArrayList<Long>> mapChunks = (ArrayList<ArrayList<Long>>) doc.get("chunks");
        if(mapChunks == null){
            System.out.println("Couldn't reset map as there were no chunks, belonging to the map defined in the db.\nConsult /map def, in order to specify said ones.");
            return;
        }
        World world = Bukkit.getWorlds().get(0);
        for(ArrayList<Long> chunkCoords : mapChunks){
            Chunk chunk = world.getChunkAt( (int) (long) chunkCoords.get(0), (int) (long) chunkCoords.get(1));
            for(int x = 0;x<16;x++){
                for(int y = 0; y< Config.maxHeight; y++){
                    for(int z = 0; z<16;z++){
                        Block block = chunk.getBlock(x,y,z);
                        if(!block.getType().equals(Material.WOOL)){
                            continue;
                        }
                        if(mapBlocks.contains(block.getLocation())){
                            continue;
                        }

                        block.setType(Material.AIR);
                    }
                }
            }
        }
    }
}
package woolbattle.woolbattle.maprestaurationsystem;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.World;
import woolbattle.woolbattle.Main;
import woolbattle.woolbattle.MongoDbWrapper;

import java.util.ArrayList;

public class MapSystem {

    public static void defineMapChunks(ArrayList<ArrayList<Long>> chunks){
        MongoDbWrapper wrapper = new MongoDbWrapper(Main.getMongoDatabase());

        if(wrapper.get("map", "mapChunks") == null){
            wrapper.set("map", new Document("_id", "mapChunks").append("mapChunks", new ArrayList<ArrayList<Long>>()));
        }

        ArrayList<ArrayList<Long>> dbChunks = null;
        try{
            dbChunks = (ArrayList<ArrayList<Long>>) wrapper.get("map", "mapChunks").get("chunks");
        }catch(ClassCastException e){
            System.out.println("The value of the chunks, belonging to the map, stored in the database consists of a value, not capable of being cast to an ArrayList.");
            dbChunks = new ArrayList<ArrayList<Long>>();
        }

        if(dbChunks != null && dbChunks.size() >0 ){
            for(ArrayList<Long> chunk : dbChunks){
                if (!chunks.contains(chunk)) {
                    chunks.add(chunk);
                }
            }
        }

        Document chunkDoc = new Document("_id", "mapChunks").append("chunks", chunks);
        wrapper.set("map", chunkDoc);
    }

    public static ArrayList<ArrayList<Long>> getChunksInRange(World world, long bx, long by, long ex, long ey){

        boolean bxGreaterEqualsEx= bx>=ex;
        boolean byGreaterEqualsEy = by>=ey;
        ArrayList<ArrayList<Long>> result = new ArrayList<>();
        for(long i = (bxGreaterEqualsEx)?  ex : bx; (bxGreaterEqualsEx)? i<bx : i<ex; i++){
            for(long j = (byGreaterEqualsEy)? ey : by; (byGreaterEqualsEy)? j<by : j<ey; j++){
                ArrayList<Long> iterChunk = new ArrayList<Long>();
                iterChunk.add(i);
                iterChunk.add(j);
                result.add(iterChunk);
                Bukkit.broadcastMessage("[" + i + ", " + j + "]");
            }
        }
        return result;
    }
}

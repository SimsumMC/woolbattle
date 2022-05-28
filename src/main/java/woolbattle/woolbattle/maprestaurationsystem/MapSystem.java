package woolbattle.woolbattle.maprestaurationsystem;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.World;
import woolbattle.woolbattle.Main;
import java.util.ArrayList;
import static com.mongodb.client.model.Filters.eq;


public class MapSystem {

    public static void defineMapChunks(ArrayList<ArrayList<Long>> chunks){

        if(Main.getMongoDatabase().getCollection("map").find(eq("_id", "mapChunks")).first() == null){
            Main.getMongoDatabase().getCollection("map").insertOne(new Document("_id", "mapChunks").append("chunks", new ArrayList<ArrayList<Long>>()));
        }

        ArrayList<ArrayList<Long>> dbChunks;
        try{
            dbChunks = (ArrayList<ArrayList<Long>>) Main.getMongoDatabase().getCollection("map").find(eq("_id", "mapChunks")).first().get("chunks");

        }catch(ClassCastException e){
            System.out.println("The value of the chunks, belonging to the map, stored in the database consists of a value, not capable of being cast to an ArrayList.");
            dbChunks = new ArrayList<>();
        }


        if(dbChunks != null && dbChunks.size() >0 ){
            for(ArrayList<Long> chunk : dbChunks){
                if (!chunks.contains(chunk)) {
                    chunks.add(chunk);
                }
            }
        }

        Document chunkDoc = new Document("_id", "mapChunks").append("chunks", chunks);
        Main.getMongoDatabase().getCollection("map").replaceOne(eq("_id", "mapChunks"), chunkDoc);
    }

    public static ArrayList<ArrayList<Long>> getChunksInRange(World world /*temporary undefined parameter, to be modified in the future*/, long bx, long by, long ex, long ey){

        boolean bxGreaterEqualsEx= bx>=ex;
        boolean byGreaterEqualsEy = by>=ey;
        ArrayList<ArrayList<Long>> result = new ArrayList<>();
        for(long i = (bxGreaterEqualsEx)?  ex : bx; (bxGreaterEqualsEx)? i<bx : i<ex; i++){
            for(long j = (byGreaterEqualsEy)? ey : by; (byGreaterEqualsEy)? j<by : j<ey; j++){
                ArrayList<Long> iterChunk = new ArrayList<>();
                iterChunk.add(i);
                iterChunk.add(j);
                result.add(iterChunk);
                Bukkit.broadcastMessage("[" + i + ", " + j + "]");
            }
        }
        return result;
    }
}
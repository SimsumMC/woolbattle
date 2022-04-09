package woolbattle.woolbattle.maprestaurationsystem;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import woolbattle.woolbattle.Main;
import woolbattle.woolbattle.MongoDbWrapper;

import java.util.ArrayList;
import java.util.UUID;

public class MapDefineCommand implements CommandExecutor {
    String usage = "/mapdefine <uuid> <beginning[x,y]> <end[x,y]>";
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        World world = null;
        if(args.length == 5){
            try{
                world = Bukkit.getWorld(UUID.fromString(args[0]));
            }catch(NullPointerException e){
                System.out.println(world);
            }

        }else if(args.length==4){
            if(commandSender instanceof Player){
                world = ((Player) commandSender).getWorld();
            }else{
                world = Bukkit.getWorlds().get(0);
            }

        }else{
            commandSender.sendMessage("The specified number of arguments is either to low, or to high, in order to be"
                    + " parsed. Usage: " + usage);
            return false;
        }
        ArrayList<Chunk> chunks = null;
        try{

            if(args.length == 5){
                chunks = getChunksInRange(world,
                        Integer.parseInt(args[1]),
                        Integer.parseInt(args[2]),
                        Integer.parseInt(args[3]),
                        Integer.parseInt(args[4])
                );
            }else{
                chunks = getChunksInRange(world,
                        Integer.parseInt(args[0]),
                        Integer.parseInt(args[1]),
                        Integer.parseInt(args[2]),
                        Integer.parseInt(args[3])
                );
            }
        }catch(NumberFormatException e){
            commandSender.sendMessage(ChatColor.RED + "On of the latter 4 arguments does not seem to possess the right format (integer), to be parsed properly.");
            return false;
        }
        MongoDbWrapper wrapper = new MongoDbWrapper(Main.getMongoDatabase());
        Object protoDbChunks = wrapper.get("map", "mapChunks").get("chunks");
        ArrayList<Chunk> dbChunks = null;
        if(protoDbChunks == null){
            dbChunks = new ArrayList<Chunk>();
        }else{
            dbChunks = (ArrayList<Chunk>) protoDbChunks;
        }

        for(Chunk chunk : dbChunks){
            if (!chunks.contains(chunk)) {
                chunks.add(chunk);
            }
        }
        Document chunkDoc = new Document("_id", "mapChunks").append("chunks", chunks);
        wrapper.set("map", chunkDoc);
        return false;
    }
    public ArrayList<Chunk> getChunksInRange(World world, int bx, int by, int ex, int ey){

        boolean bxGreaterEqualsEx= bx>=ex;
        boolean byGreaterEqualsEy = by>=ey;
        ArrayList<Chunk> result = new ArrayList<>();
        for(int i = (bxGreaterEqualsEx)?  ex : bx; (bxGreaterEqualsEx)? i<bx : i<ex; i++){
            for(int j = (byGreaterEqualsEy)? ey : by; (byGreaterEqualsEy)? j<by : j<ey; j++){
                result.add(world.getChunkAt(i, j));
                Bukkit.broadcastMessage("[" + i + ", " + j + "]");
            }
        }
        return result;
    }
}

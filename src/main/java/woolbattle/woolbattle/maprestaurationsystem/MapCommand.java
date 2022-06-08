package woolbattle.woolbattle.maprestaurationsystem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import woolbattle.woolbattle.woolsystem.BlockBreakingSystem;

import java.util.ArrayList;
import java.util.UUID;

public class MapCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        final String usage = "/mapdefine <reset/def> <uuid> <beginning[x,y]> <end[x,y]>";
        if(!(args.length > 0)){

            return false;
        }
        switch(args[0]){
            case "def":
                System.out.println("Def is called");
                World world = null;
                if(args.length == 6){
                    try{
                        world = Bukkit.getWorld(UUID.fromString(args[5]));
                    }catch(NullPointerException e){
                        System.out.println(world);
                    }

                }else if(args.length==5){
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
                ArrayList<ArrayList<Long>> chunks;
                try{
                        chunks = MapSystem.getChunksInRange(world,

                                Long.parseLong(args[1]),
                                Long.parseLong(args[2]),
                                Long.parseLong(args[3]),
                                Long.parseLong(args[4])
                        );
                }catch(NumberFormatException e){
                    commandSender.sendMessage(ChatColor.RED + "One of the latter 4 arguments does not seem to possess the right format (integer), to be parsed properly.");
                    return false;
                }
                System.out.println("is called\n\n");
                MapSystem.defineMapChunks(chunks);
                break;
            case "reset":
                BlockBreakingSystem.resetMap();
                break;
        }

        return false;
    }
}
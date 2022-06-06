package woolbattle.woolbattle.woolsystem;

import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BlockRegistrationCommand implements CommandExecutor {
    private String syntax = ChatColor.GREEN + "Proper syntax: <blockregistration> <<init/terminate>||range> < || 6*<int> ";

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "init":
                if (!BlockBreakingSystem.isCollectBrokenBlocks()) {
                    BlockBreakingSystem.setCollectBrokenBlocks(true);
                    Bukkit.broadcastMessage(ChatColor.GREEN + "The block-scanning-process was successfully initiated.");
                }
                else {
                    sender.sendMessage(ChatColor.RED + "The Block-breaking-system is registering the placed blocks already.\nIf you want to terminate the scan for newly placed blocks, use the argument" + ChatColor.GREEN + "terminate");
                }

                return false;
            case "terminate":

                if (BlockBreakingSystem.isCollectBrokenBlocks()) {
                    BlockBreakingSystem.setCollectBrokenBlocks(false);
                    Bukkit.broadcastMessage(ChatColor.GREEN + "The block-scanning-process was successfully terminated.");
                }

                else {
                    sender.sendMessage(ChatColor.RED + "The Block-breaking-system is currently not registering new blocks, being placed.\n If you want to begin the registration of newly placed blocks, use the argument " + ChatColor.GREEN + "init");
                }
                break;

            case "range":
                //if(args.length < 7){
                //}
                if(args[1].toLowerCase(Locale.ROOT) == null || args[2].toLowerCase(Locale.ROOT) == null || args[3] == null || args[4].toLowerCase(Locale.ROOT) == null || args[5].toLowerCase(Locale.ROOT) == null || args[6] == null){
                    sender.sendMessage(ChatColor.RED + "At least one of the given coordinate-arguments hasn't had a form of a parsable integer. To use this command, all of them have to fulfill this requirement." + syntax);
                    break;
                }
                else{
                    try{

                        Location start = new Location(Bukkit.getWorlds().get(0), Double.parseDouble(args[1].toLowerCase(Locale.ROOT)),Double.parseDouble(args[2].toLowerCase(Locale.ROOT)), Double.parseDouble(args[3].toLowerCase(Locale.ROOT)));
                        Location end = new Location(Bukkit.getWorlds().get(0), Double.parseDouble(args[4].toLowerCase(Locale.ROOT)),Double.parseDouble(args[5].toLowerCase(Locale.ROOT)), Double.parseDouble(args[6].toLowerCase(Locale.ROOT)));

                        BlockBreakingSystem.addBlocksByRange(start, end);
                    }catch(NumberFormatException e){
                        e.printStackTrace();
                    }
                }
                break;

            //}
        }
        return false;
    }
}
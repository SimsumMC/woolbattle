package woolbattle.woolbattle.woolsystem;

import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import static java.lang.String.format;

public class BlockRegistrationCommand implements CommandExecutor {
    private final String syntax = ChatColor.GREEN + "\nProper syntax: <blockregistration> <<init/terminate>||range> < || 6*<int> ";

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§cThe specified number of arguments is too little, than it would be necessary for the" +
                    "command to work properly." + syntax
            );
            return false;
        }
        switch (args[0].toLowerCase()) {
            case "init":
                if (!BlockBreakingSystem.isCollectBrokenBlocks()) {
                    BlockBreakingSystem.setCollectBrokenBlocks(true);
                    Bukkit.broadcastMessage("§aThe block-scanning-process was successfully initiated.");
                } else {
                    sender.sendMessage(ChatColor.RED + "The Block-breaking-system is registering the placed blocks already.\n" +
                            "If you want to terminate the scan for newly placed blocks, use the argument §a terminate"
                    );
                }

                return false;
            case "terminate":

                if (BlockBreakingSystem.isCollectBrokenBlocks()) {
                    BlockBreakingSystem.setCollectBrokenBlocks(false);
                    Bukkit.broadcastMessage("§9The block-scanning-process was successfully terminated.");
                } else {
                    sender.sendMessage("§cThe Block-breaking-system is currently not registering new blocks, being placed.\n" +
                            "If you want to begin the registration of newly placed blocks, use the argument §a init"
                    );
                }
                break;

            case "range":
                if (args.length < 7) {
                    sender.sendMessage("§cThe specified number of arguments is too little, than it would be necessary for the" +
                            "command to work properly." + syntax
                    );
                    return false;
                }else {
                    try {

                        Location start = new Location(Bukkit.getWorlds().get(0), Double.parseDouble(args[1].toLowerCase()), Double.parseDouble(args[2].toLowerCase()), Double.parseDouble(args[3].toLowerCase()));
                        Location end = new Location(Bukkit.getWorlds().get(0), Double.parseDouble(args[4].toLowerCase()), Double.parseDouble(args[5].toLowerCase()), Double.parseDouble(args[6].toLowerCase()));

                        BlockBreakingSystem.addBlocksByRange(start, end);

                        sender.sendMessage(ChatColor.GREEN + "Successfully registered all blocks in the given range. [Only Local - use /mapblocks push to put it in the database]");

                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                sender.sendMessage(format("§5%s§c is not a valid argument, concerning this command. " + syntax, args[0])
                );
        }
        return false;
    }
}
package woolbattle.woolbattle.woolsystem;

import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BlockRegistrationCommand implements CommandExecutor {
    private String syntax = ChatColor.GREEN + "Proper syntax: /<blockregistration/blckreg> <init/terminate>";

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "The number of arguments, given in order to issue this command, was to low, to do said thing.\n" + this.syntax);
        }
        else if (args.length > 1) {
            sender.sendMessage(ChatColor.RED + "The number of arguments, given in order to issue this command, was to great, to do said thing.\n" + this.syntax);
        }
        else {

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
                    return false;
            }
        }
        return false;
    }
}
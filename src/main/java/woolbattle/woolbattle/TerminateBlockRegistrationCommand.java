package woolbattle.woolbattle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TerminateBlockRegistrationCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(BlockBreakingSystem.isCollectBrokeBlocks()){
            BlockBreakingSystem.setCollectBrokeBlocks(false);
            Bukkit.broadcastMessage(ChatColor.GREEN + "The block-scanning-process was successfully terminated.");
            return false;
        }else{
            commandSender.sendMessage(ChatColor.RED + "The Block-breaking-system is currently not scanning the blocks, being placed.\n If you want to begin the scan for newly placed blocks, use " + ChatColor.GREEN + "/startBlockRegistration.");
            return false;
        }

    }
}

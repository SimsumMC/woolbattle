package woolbattle.woolbattle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class InitiateBlockRegistrationCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!BlockBreakingSystem.isCollectBrokeBlocks()){
            BlockBreakingSystem.setCollectBrokeBlocks(true);
            Bukkit.broadcastMessage(ChatColor.GREEN + "The block-scanning-process was successfully initiated.");
            return false;
        }else{
            commandSender.sendMessage(ChatColor.RED + "The Block-breaking-system is collecting the placed blocks already.\n If you want to terminate the scan for newly placed blocks, use " + ChatColor.GREEN + "/endBlockRegistration.");
            return false;
        }

    }
}

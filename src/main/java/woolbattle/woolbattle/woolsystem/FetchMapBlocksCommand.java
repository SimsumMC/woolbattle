package woolbattle.woolbattle.woolsystem;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class FetchMapBlocksCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        int previousAmount = BlockBreakingSystemB.getMapBlocks().size();
        BlockBreakingSystem.fetchMapBlocks();
        commandSender.sendMessage(ChatColor.GREEN +
                "In advance of the fetching process, there were " +
                ChatColor.BLUE+
                previousAmount+
                ChatColor.GREEN+
                "mapBlocks, the current amount of them is equal to"+
                ChatColor.BLUE+
                BlockBreakingSystemB.getMapBlocks());
        return false;
    }
}

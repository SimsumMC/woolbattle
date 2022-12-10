package woolbattle.woolbattle.stats;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + "You can only use this command as a normal player!");
            return true;
        }
        if (strings.length == 0) {
            commandSender.sendMessage(StatsSystem.getPlayerStatsFormatted((OfflinePlayer) commandSender));
            return true;
        }
        String playerName = strings[0];
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        commandSender.sendMessage(StatsSystem.getPlayerStatsFormatted(player));
        return true;
    }
}

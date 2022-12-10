package woolbattle.woolbattle.lobby;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static woolbattle.woolbattle.lobby.LobbySystem.endGame;

public class StopGameCommand implements CommandExecutor {

    /**
     * A Command that calls the endGame() method to stop the game.
     *
     * @author SimsumMC
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.isOp()) {
                player.sendMessage(ChatColor.RED + "You need OP to use this command!");
                return true;
            }
            if (endGame(ChatColor.YELLOW + "Yellow")) {
                player.sendMessage("§aStopped the game successfully.");
            } else {
                player.sendMessage(ChatColor.RED + "There is no running game!");
            }
            return true;
        }
        return false;
    }
}
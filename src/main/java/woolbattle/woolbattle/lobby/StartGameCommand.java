package woolbattle.woolbattle.lobby;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static woolbattle.woolbattle.lobby.LobbySystem.startGame;

public class StartGameCommand implements CommandExecutor {

    /**
     * A Command that calls the startGame() method to start the game.
     * @author SimsumMC
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if(!player.isOp()){
                return false;
            }
            if (startGame()){
                player.sendMessage(ChatColor.GREEN + "Started the game.");
            } else{
                player.sendMessage(ChatColor.RED + "The game has already been started!");
            }
            return true;
        }
        return false;
    }
}
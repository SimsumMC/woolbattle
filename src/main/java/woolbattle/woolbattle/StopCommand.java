package woolbattle.woolbattle;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static woolbattle.woolbattle.LobbySystem.endGame;

public class StopCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if(endGame()){
                player.sendMessage("§aStopped the game successfully.");
            }
            else{
                player.sendMessage("§cThere is no running game!");
            }
            return true;
        }
        return false;
    }
}

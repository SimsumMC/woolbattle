package woolbattle.woolbattle;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static woolbattle.woolbattle.LobbySystem.startGame;

public class StartCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (startGame()){
                player.sendMessage("§aStarted the game.");
            }
            else{
                player.sendMessage("§cThe game has already been started!");
            }
            return true;
        }
        return false;
    }
}

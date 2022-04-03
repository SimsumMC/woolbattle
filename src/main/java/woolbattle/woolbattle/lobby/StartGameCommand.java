package woolbattle.woolbattle.lobby;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import woolbattle.woolbattle.Cache;

import java.util.ArrayList;
import java.util.HashMap;

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
            HashMap<String, ArrayList<Player>> teamMembers = Cache.getTeamMembers();
            ArrayList<Player> blueMembers = teamMembers.get("Blue");
            blueMembers.add(player);
            teamMembers.put("Blue", blueMembers);
            Cache.setTeamMembers(teamMembers);
            if (startGame()){
                player.sendMessage("§aStarted the game.");
            } else{
                player.sendMessage("§cThe game has already been started!");
            }
            return true;
        }
        return false;
    }
}
package woolbattle.woolbattle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Listener implements org.bukkit.event.Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage(ChatColor.GRAY + "Der Spieler " + ChatColor.GREEN + player.getDisplayName()
                + ChatColor.GRAY + " ist dem Spiel beigetreten.");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        int playerAmount = Bukkit.getOnlinePlayers().size();
        int maxPlayers = Bukkit.getServer().getMaxPlayers();
        event.setJoinMessage(ChatColor.GRAY + "Der Spieler " + ChatColor.GREEN + player.getDisplayName()
                + ChatColor.GRAY + " ist dem Spiel beigetreten.");

        LobbySystem.setXPBar();

        }

    }



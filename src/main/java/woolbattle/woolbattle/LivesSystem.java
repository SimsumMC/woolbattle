package woolbattle.woolbattle;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import woolbattle.woolbattle.lobby.LobbySystem;

import java.util.HashMap;

public class LivesSystem implements Listener {

    /**
     * An Event that gets executed whenever a Player moves to use it as a kill event when a player gets under a
     * specific y coordinate.
     *
     * @param event the PlayerMoveEvent
     * @author SimsumMC & Beelzebub
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if(!LobbySystem.gameStarted){
            return;
        }

        Player player = event.getPlayer();
        HashMap<Player, Long> lastDeath = Cache.getLastDeath();
        if (player.getLocation().getY() <= Config.minHeight) {
            if (lastDeath.containsKey(player)) {
                long realLastDeath = lastDeath.get(player);
                long unixTime = System.currentTimeMillis() / 1000L;
                if (unixTime - realLastDeath >= Config.deathCooldown) {
                    // ignore deaths that are old enough
                    return;
                }
            }

            String team = TeamSystem.getPlayerTeam(player, true);
            HashMap<String, Integer> teamLives = Cache.getTeamLives();
            int lives = teamLives.get(team);
            if (lives == 0) {
                TeamSystem.removePlayerTeam(player);
                setPlayerSpectator(player);
            } else {
                lives -= 1;
                teamLives.put(team, lives);
                Cache.setTeamLives(teamLives);

                EntityDamageEvent lastDamage = event.getPlayer().getLastDamageCause();

                Entity entity = lastDamage.getEntity();

                if (entity instanceof Player) {
                    String message = "§7The player " + TeamSystem.getPlayerTeam(player, false).substring(2)
                            + player.getDisplayName() + "§7was killed by " +
                           TeamSystem.getPlayerTeam((Player) entity, false).substring(2) +
                            ((Player) entity).getDisplayName() + "§7.";
                    Bukkit.broadcastMessage(message);

                    String killTeam = TeamSystem.getPlayerTeam((Player) entity, true);
                    HashMap<Player, Integer> kills = Cache.getKillStreaks().get(killTeam);
                    int amKills = kills.get(entity) + 1;
                    HashMap<String, HashMap<Player, Integer>> teamKills = new HashMap<>();
                    HashMap<Player, Integer> entityKills = new HashMap<>();
                    if (amKills % 5 == 1) {
                        entityKills.put((Player) entity, 0);
                        teamKills.put(killTeam, entityKills);
                        teamLives.put(killTeam, teamLives.get(killTeam) + 1);
                        Cache.setTeamLives(teamLives);
                        Cache.setKillStreaks(teamKills);
                    }
                }
                Bukkit.broadcastMessage(String.valueOf(Cache.getTeamLives().get("Blue")));
                Bukkit.broadcastMessage(String.valueOf(Cache.getKillStreaks().get("Green").get(entity)));
                LobbySystem.determinateWinnerTeam();
            }
        }
    }


    public void setPlayerSpectator(Player player) {
        player.teleport(Config.midLocation);
        player.setGameMode(GameMode.SPECTATOR);
    }
}
package woolbattle.woolbattle.lives;

import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import woolbattle.woolbattle.Cache;
import woolbattle.woolbattle.Config;
import woolbattle.woolbattle.lobby.*;
import woolbattle.woolbattle.team.TeamSystem;

import java.util.HashMap;

public class LivesSystem implements Listener {

    /**
     * A Method that teleports the player to the team spawn.
     * @param player the player that gets teleported
     * @author SimsumMC & Beelzebub
     */
    public void teleportPlayertoTeamSpawn(Player player){
        String team = TeamSystem.getPlayerTeam(player, true);

        switch(team){
            case "Blue":
                player.teleport(Config.blueLocation);
                break;
            case "Red":
                player.teleport(Config.redLocation);
                break;
            case "Green":
                player.teleport(Config.greenLocation);
                break;
            case "Yellow":
                player.teleport(Config.yellowLocation);
                break;
            default:
                player.teleport(Config.midLocation);
                break;
        }
    }

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

        HashMap<Player, Long> lastDamage = Cache.getLastDamage();

        long unixTime = System.currentTimeMillis() / 1000L;

        if (player.getLocation().getY() <= Config.minHeight) {
            if (lastDamage.containsKey(player)) {
                long realLastDamage = lastDamage.get(player);
                if (unixTime - realLastDamage >= Config.deathCooldown) {
                    teleportPlayertoTeamSpawn(player);
                    return;
                }
            }

            String team = TeamSystem.getPlayerTeam(player, true);

            HashMap<String, Integer> teamLives = Cache.getTeamLives();

            int lives = teamLives.get(team);
            if (lives == 0) {
                TeamSystem.removePlayerTeam(player);
                LobbySystem.setPlayerSpectator(player);
            } else {

                teleportPlayertoTeamSpawn(player);


                EntityDamageEvent lastDamageEvent = event.getPlayer().getLastDamageCause();

                Entity damager;

                if(lastDamageEvent instanceof EntityDamageByEntityEvent){
                    damager = ((EntityDamageByEntityEvent) lastDamageEvent).getDamager();
                }
                else{
                    damager = null;
                }

                if(damager instanceof Arrow){
                    Arrow arrow = (Arrow) damager;
                    damager = (Entity) arrow.getShooter();

                }

                if (damager instanceof Player) {
                    lives -= 1;
                    teamLives.put(team, lives);
                    Cache.setTeamLives(teamLives);

                    String damagerTeam = TeamSystem.getPlayerTeam((Player) damager, true);
                    String damagerTeamColour = TeamSystem.getTeamColour(damagerTeam) ;
                    String killMessage = "§7The player " + TeamSystem.getTeamColour(team)
                            + player.getDisplayName() + "§7 was killed by " +
                            damagerTeamColour + ((Player) damager).getDisplayName() + "§7.";

                    Bukkit.broadcastMessage(killMessage);
                    HashMap<String, HashMap<Player, Integer>> killStreaks = Cache.getKillStreaks();

                    HashMap<Player, Integer> kills = killStreaks.get(damagerTeam);

                    int amKills;
                    if(kills.containsKey(damager)){
                        amKills = kills.get(damager) + 1;
                    }
                    else{
                        amKills = 1;
                    }

                    kills.put((Player) damager, amKills);

                    if(amKills == 5){
                        String streakMessage = "§7The player " + damagerTeamColour + ((Player) damager).getDisplayName() + "§7 has a 5er kill streak!";
                        Bukkit.broadcastMessage(streakMessage);

                        //reset deaths

                        kills.put((Player) damager, 0);
                        killStreaks.put(damagerTeam, kills);
                        Cache.setKillStreaks(killStreaks);

                        teamLives = Cache.getTeamLives();
                        teamLives.put(damagerTeam, (teamLives.get(damagerTeam) + 1));
                        Cache.setTeamLives(teamLives);

                    }

                }
                LobbySystem.determinateWinnerTeam();
            }
        }
    }
}
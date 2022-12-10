package woolbattle.woolbattle.lives;

import org.bukkit.Bukkit;

import org.bukkit.ChatColor;
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
import woolbattle.woolbattle.achievements.AchievementSystem;
import woolbattle.woolbattle.team.TeamSystem;
import woolbattle.woolbattle.lobby.LobbySystem;

import java.util.HashMap;

import static woolbattle.woolbattle.base.Base.resetEnderPearls;
import static woolbattle.woolbattle.team.TeamSystem.getPlayerTeam;
import static woolbattle.woolbattle.team.TeamSystem.getTeamColour;

public class LivesSystem implements Listener {

    /**
     * A Method that teleports the player to the team spawn.
     *
     * @param player the player that gets teleported
     * @author SimsumMC
     */
    public static void teleportPlayerTeamSpawn(Player player) {
        String team = TeamSystem.getPlayerTeam(player, true);

        switch (team) {
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
     * A Method that updates the spawnProtection HashMap in the Cache with the current unix timestamp.
     *
     * @param player the player that gets teleported
     * @param length the length of the spawn protection in seconds
     * @author SimsumMC
     */
    public static void setPlayerSpawnProtection(Player player, int length) {
        long unixTime = (System.currentTimeMillis() / 1000L) + length;

        HashMap<Player, Long> spawnProtection = Cache.getSpawnProtection();

        spawnProtection.put(player, unixTime);

        Cache.setSpawnProtection(spawnProtection);
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
        if (!LobbySystem.gameStarted) {
            return;
        }

        Player player = event.getPlayer();

        HashMap<Player, Long> lastDamage = Cache.getLastDamage();

        long unixTime = System.currentTimeMillis() / 1000L;

        if (player.getLocation().getY() <= Config.minHeight) {
            resetEnderPearls(player);
            if (lastDamage.containsKey(player)) {
                long realLastDamage = lastDamage.get(player);
                if (unixTime - realLastDamage >= Config.deathCooldown) {
                    teleportPlayerTeamSpawn(player);
                    setPlayerSpawnProtection(player, Config.spawnProtectionLengthAfterDeath);
                    return;
                }
            }

            HashMap<Player, Player> playerDuels = Cache.getPlayerDuels();
            Player otherPlayer = playerDuels.get(player);
            if (otherPlayer != null) {
                playerDuels.remove(player);
                playerDuels.remove(otherPlayer);
                Cache.setPlayerDuels(playerDuels);

                String otherPlayerName = getTeamColour(getPlayerTeam(otherPlayer, true)) + otherPlayer.getDisplayName();
                String playerName = getTeamColour(getPlayerTeam(player, true)) + player.getDisplayName();

                player.sendMessage(ChatColor.GOLD + "You are now in a duel with " + otherPlayerName + ChatColor.GOLD + "!");
                otherPlayer.sendMessage(ChatColor.GOLD + "You are now in a duel with " + playerName + ChatColor.GOLD + "!");
            }

            String team = TeamSystem.getPlayerTeam(player, true);

            HashMap<String, Integer> teamLives = Cache.getTeamLives();

            int lives = teamLives.get(team);

            EntityDamageEvent lastDamageEvent = event.getPlayer().getLastDamageCause();

            Entity damager;

            if (lastDamageEvent instanceof EntityDamageByEntityEvent) {
                damager = ((EntityDamageByEntityEvent) lastDamageEvent).getDamager();
            } else {
                damager = null;
            }

            if (damager == null) {
                teleportPlayerTeamSpawn(player);
                setPlayerSpawnProtection(player, Config.spawnProtectionLengthAfterDeath);
            }

            if (damager instanceof Arrow) {
                Arrow arrow = (Arrow) damager;
                damager = (Entity) arrow.getShooter();

            }

            if (damager instanceof Player) {
                if (lives != 0) {
                    lives -= 1;
                }
                teamLives.put(team, lives);
                Cache.setTeamLives(teamLives);

                lastDamage.remove(damager);
                Cache.setLastDamage(lastDamage);

                String damagerTeam = TeamSystem.getPlayerTeam((Player) damager, true);
                ChatColor damagerTeamColour = TeamSystem.getTeamColour(damagerTeam);
                String killMessage = ChatColor.GRAY + "The player " + TeamSystem.getTeamColour(team)
                        + player.getDisplayName() + ChatColor.GRAY + " was killed by " +
                        damagerTeamColour + ((Player) damager).getDisplayName() + ChatColor.GRAY + ".";

                Bukkit.broadcastMessage(killMessage);
                HashMap<String, HashMap<Player, Integer>> killStreaks = Cache.getKillStreaks();

                HashMap<Player, Integer> kills = killStreaks.get(damagerTeam);

                killStreaks.put(team, new HashMap<Player, Integer>() {{
                    put(player, 0);
                }});

                int amKills;
                if (kills.containsKey(damager)) {
                    amKills = kills.get(damager) + 1;
                } else {
                    amKills = 1;
                }

                kills.put((Player) damager, amKills);

                HashMap<Player, HashMap<String, Integer>> playerStats = Cache.getPlayerStats();

                if (amKills == 5) {

                    AchievementSystem.giveKillstreak5((Player) damager);

                    String streakMessage = ChatColor.GRAY + "The player " + damagerTeamColour +
                            ((Player) damager).getDisplayName() + ChatColor.GRAY + " has a 5er kill streak!";
                    Bukkit.broadcastMessage(streakMessage);

                    kills.put((Player) damager, 0);


                    teamLives = Cache.getTeamLives();
                    teamLives.put(damagerTeam, (teamLives.get(damagerTeam) + 1));
                    Cache.setTeamLives(teamLives);

                    HashMap<String, Integer> damagerStatsNew = playerStats.get(damager);
                    damagerStatsNew.put("streaks", (damagerStatsNew.get("streaks") + 1));
                    playerStats.put((Player) damager, damagerStatsNew);
                }

                killStreaks.put(damagerTeam, kills);
                Cache.setKillStreaks(killStreaks);

                if (lives == 0) {
                    TeamSystem.removePlayerTeam(player);
                    LobbySystem.setPlayerSpectator(player);
                } else {
                    teleportPlayerTeamSpawn(player);
                    setPlayerSpawnProtection(player, Config.spawnProtectionLengthAfterDeath);
                }

                HashMap<String, Integer> damagerStats = playerStats.get(damager);
                damagerStats.put("kills", (damagerStats.get("kills") + 1));
                playerStats.put((Player) damager, damagerStats);

                HashMap<String, Integer> damagedStats = playerStats.get(player);
                damagedStats.put("deaths", (damagedStats.get("deaths") + 1));
                playerStats.put(player, damagedStats);

                Cache.setPlayerStats(playerStats);

            }
            LobbySystem.determinateWinnerTeam();
        }
    }
}
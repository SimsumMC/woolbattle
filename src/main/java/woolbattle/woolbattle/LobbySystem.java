package woolbattle.woolbattle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.Collection;

public class LobbySystem implements Listener {

    static boolean gameStarted = false;
    static boolean runCooldownTask = false;
    static boolean runScoreBoardTask = false;
    static int cooldown = 60;

    static String dummyTeam = "Rot";
    static String dummyMap = "Default";

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage(ChatColor.GRAY + "The player " + ChatColor.GREEN + player.getDisplayName()
                + ChatColor.GRAY + " left the game.");
        //TODO: implement Logic if a player who is in a game leaves
        if(Bukkit.getServer().getOnlinePlayers().size() >= 1){
            endGame();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        event.setJoinMessage(ChatColor.GRAY + "The player " + ChatColor.GREEN + player.getDisplayName()
                + ChatColor.GRAY + " joined the game.");

        if(gameStarted){
            player.setGameMode(GameMode.SPECTATOR);
            player.sendMessage(ChatColor.RED + "There is already a running game!");
        }

        setPlayerScoreBoard(player);

        if(!runCooldownTask){
            updatePlayerCooldown();
        }

        if(!runScoreBoardTask){
            updateScoreBoard();
        }

    }

    public static void startGame(){
        //TODO: basic whole method
        gameStarted = true;
        Bukkit.broadcastMessage(ChatColor.GREEN + "Game Starting...");
    }

    public static void endGame(){
        //TODO: basic whole method
        gameStarted = false;
        Bukkit.broadcastMessage(ChatColor.RED + "Game Ending...");
    }


    public static void updatePlayerCooldown(){
        runCooldownTask = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                if(!gameStarted){
                    Collection<? extends Player> players = Bukkit.getOnlinePlayers();
                    int playerAmount = players.size();
                    int maxPlayers = Bukkit.getServer().getMaxPlayers();
                    if(playerAmount >= (maxPlayers / 2)){ // TODO: && playerAmount != 1
                        if(cooldown == 0){
                            startGame();
                        }
                        else if(cooldown > 15 && playerAmount >= ((maxPlayers / 4) * 3)){
                            cooldown = 15;
                        }
                        cooldown -= 1;
                    }
                    else{
                        cooldown = 60;
                    }
                    for(Player player: players){

                        if(player.getGameMode() == GameMode.SURVIVAL) {
                            setPlayerCooldown(player, cooldown);
                        }

                    }

                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 20);

    }



    public static void setPlayerCooldown(Player player, int level) {
        float exp = (float) level/60;
        player.setExp(exp);
        player.setLevel(level);

    }

    public void updateScoreBoard(){
        runScoreBoardTask = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                //TODO: Check if player is participating in the game NOT if any game running
                if(!gameStarted){
                    Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();

                    for(Player player : players){
                        updatePlayerScoreBoard(player);

                    }
                }

            }
        }.runTaskTimer(Main.getInstance(), 0, 20);

    }

    public static void setPlayerScoreBoard(Player player){
        int maxPlayers = Bukkit.getServer().getMaxPlayers();
        int actualPlayers = Bukkit.getServer().getOnlinePlayers().size();

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.registerNewObjective("Woolbattle", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName("§aWoolbattle Lobby");

        Team team = board.registerNewTeam("team");
        Team map = board.registerNewTeam("map");
        Team players = board.registerNewTeam("players");

        obj.getScore("\u1CBC\u1CBC\u1CBC").setScore(8);

        obj.getScore("§7»Team").setScore(7);
        obj.getScore("§c").setScore(6);

        obj.getScore("\u1CBC\u1CBC").setScore(5);

        obj.getScore("§7»Map").setScore(4);
        obj.getScore("§d").setScore(3);

        obj.getScore("\u1CBC").setScore(2);

        obj.getScore("§7»Spieler").setScore(1);
        obj.getScore("§b").setScore(0);

        team.addEntry("§c");
        team.setPrefix("§c" + dummyTeam);

        map.addEntry("§d");
        map.setPrefix("§d" + dummyMap);
        players.addEntry("§b");
        players.setPrefix("§b" + actualPlayers + "/" + maxPlayers);

        player.setScoreboard(board);

    }

    public void updatePlayerScoreBoard(Player player){
        int maxPlayers = Bukkit.getServer().getMaxPlayers();
        int actualPlayers = Bukkit.getServer().getOnlinePlayers().size();
        Scoreboard board = player.getScoreboard();

        Team team = board.getTeam("team");
        Team map = board.getTeam("map");
        Team players = board.getTeam("players");

        team.setPrefix("§c" + dummyTeam);
        map.setPrefix("§d" + dummyMap);
        players.setPrefix("§b" + actualPlayers + "/" + maxPlayers);

    }

}

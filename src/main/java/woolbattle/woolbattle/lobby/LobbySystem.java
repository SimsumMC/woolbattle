package woolbattle.woolbattle.lobby;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import woolbattle.woolbattle.Cache;
import woolbattle.woolbattle.Config;
import woolbattle.woolbattle.Main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class LobbySystem implements Listener{

    static boolean gameStarted = false;
    static boolean runCooldownTask = false;
    static boolean runScoreBoardTask = false;
    static int cooldown = Config.startCooldown;


    /**
     * An Event that gets executed whenever a player dies to send a custom death message.
     * @param event the PlayerDeathEvent event
     * @author SimsumMC
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        // TODO: Colour should fit the team & reason should be written in the msg.
        Player player = event.getEntity();
        event.setDeathMessage(ChatColor.GRAY + "The player " + ChatColor.GREEN + player.getDisplayName()
                + ChatColor.GRAY + " died.");
    }

    /**
     * An Event that gets executed whenever a player leaves the server to send a custom death message and
     * eventually end the current game.
     * @param event the PlayerQuitEvent event
     * @author SimsumMC
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();

        event.setQuitMessage(ChatColor.GRAY + "The player " + ChatColor.GREEN + player.getDisplayName()
                + ChatColor.GRAY + " left the game.");

        // remove voting if any
        HashMap<Integer, ArrayList<Player>> lifeVoting = Cache.getLifeVoting();
        for(Integer key : lifeVoting.keySet()){
            ArrayList<Player> players = lifeVoting.get(key);
            if(players.contains(player)){
                players.remove(player);
                lifeVoting.put(key, players);
                Cache.setLifeVoting(lifeVoting);
                break;
            }
        }

        //TODO: implement Logic if a player who is in a game leaves -> look at the teams
        if(Bukkit.getServer().getOnlinePlayers().size() <= 1){
            endGame("§cUnknown");
        }

        removeTeam(player);
    }

    /**
     * An Event that gets executed whenever a player joins the server to send a custom join message, set the
     * right GameMode, update the ScoreBoard, teleport to the right position or maybe start the cooldown.
     * @param event the PlayerJoinEvent event
     * @author SimsumMC
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        event.setJoinMessage(ChatColor.GRAY + "The player " + ChatColor.GREEN + player.getDisplayName()
                + ChatColor.GRAY + " joined the game.");

        if(gameStarted){
            setGameScoreBoard(player);
            player.setGameMode(GameMode.SPECTATOR);
            player.sendMessage(ChatColor.RED + "There is already a running game!");
        }
        else{
            setLobbyScoreBoard(player);
            giveLobbyItems(player);
        }
        if(!runCooldownTask){
            updatePlayerCooldown();
        }

        if(!runScoreBoardTask){
            updateScoreBoard();
        }

    }


    /**
     * An Event that gets executed whenever a player tries to move an item in the inventory to prevent the moving of
     * lobby items.
     * @param event the InventoryClickEvent event
     * @author SimsumMC
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if(gameStarted){
            return;
        }
        if(event.getWhoClicked() instanceof Player && event.getClickedInventory() != null) {
            List<ItemStack> items = new ArrayList<>();
            items.add(event.getCurrentItem());
            items.add(event.getCursor());
            items.add((event.getClick() == org.bukkit.event.inventory.ClickType.NUMBER_KEY) ?
                    event.getWhoClicked().getInventory().getItem(event.getHotbarButton()) : event.getCurrentItem());
            for(ItemStack item : items) {
                if(item != null && item.hasItemMeta()) {
                    event.setCancelled(true);
                }
            }
            Player player = (Player) event.getWhoClicked();
            if(event.getClickedInventory().getName().substring(2).equals("Voting for the Amount of Lives")){
                HashMap<Integer, ArrayList<Player>> votingData = Cache.getLifeVoting();
                Inventory inv = event.getClickedInventory();
                ItemMeta clickedItemMeta = event.getCurrentItem().getItemMeta();
                int lifeAmount = Integer.parseInt(clickedItemMeta.getDisplayName().substring(2).split(" ")[0]);

                // doesn't change anything if the player already voted for the given value
                if(votingData.get(lifeAmount).contains(player)){
                    return;
                }

                //check if the player voted for another value before, if true remove the vote there
                for(Integer key: votingData.keySet()){
                    ArrayList<Player> players = votingData.get(key);
                    if(players.contains(player)){
                        players.remove(player);
                        votingData.put(key, players);
                    }
                }

                //update the cache
                ArrayList<Player> players = votingData.get(lifeAmount);
                players.add(player);
                votingData.put(lifeAmount, players);
                Cache.setLifeVoting(votingData);

                //update the inventory

                showLifeAmountVoting(player);
            }
        }
    }

    /**
     * An Event that gets executed whenever a player interacts with an item to make the lobby items functional.
     * @param event the PlayerInteractEvent event
     * @author SimsumMC
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        if (event.getItem() == null || event.getItem().getItemMeta() == null || event.getItem().getItemMeta().getDisplayName() == null) return;

        Player player = event.getPlayer();

        if (event.getItem().getItemMeta().getDisplayName().equals("§c§lLeave")){
            player.kickPlayer("§c§lYou left the game.");
        }
        else if (event.getItem().getItemMeta().getDisplayName().equals("§a§lAmount of Lives")){
            showLifeAmountVoting(player);

        }
    }

    /**
     * A Method that shows / updates the inventar to vote for the life count.
     * @author SimsumMC
     */
    private static void showLifeAmountVoting(Player player){
        Inventory inv = Bukkit.createInventory(null, 3*9, "§aVoting for the Amount of Lives");

        // Glass Background
        ItemStack glassStack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        ItemMeta glassMeta = glassStack.getItemMeta();
        glassMeta.setDisplayName(" ");
        glassStack.setItemMeta(glassMeta);

        for (int i = 0; i<= 26; i++) {
            inv.setItem(i, glassStack);
        }

        // 5 Votes Item
        int fiveVoteCount = Cache.getLifeVoting().get(5).size();
        ItemStack fiveLivesStack = new ItemStack(Material.INK_SACK, 5, (short) 0, (byte) 10);
        ItemMeta fiveLivesMeta = fiveLivesStack.getItemMeta();
        fiveLivesMeta.setDisplayName("§a5 Lives");
        fiveLivesMeta.setLore(new ArrayList<String>(){{add("§7»Votes: §a" + fiveVoteCount);}});
        fiveLivesStack.setItemMeta(fiveLivesMeta);
        inv.setItem(11, fiveLivesStack);

        // 10 Votes Item
        int tenVoteCount = Cache.getLifeVoting().get(10).size();
        ItemStack tenLivesStack = new ItemStack(Material.INK_SACK, 10, (short) 0, (byte) 10);
        ItemMeta tenLivesMeta = tenLivesStack.getItemMeta();
        tenLivesMeta.setDisplayName("§a10 Lives");
        tenLivesMeta.setLore(new ArrayList<String>(){{add("§7»Votes: §a" + tenVoteCount);}});
        tenLivesStack.setItemMeta(tenLivesMeta);
        inv.setItem(13, tenLivesStack);

        // 15 Votes Item
        int fifteenVoteCount = Cache.getLifeVoting().get(15).size();
        ItemStack fifteenLivesStack = new ItemStack(Material.INK_SACK, 15, (short) 0, (byte) 10);
        ItemMeta fifteenLivesMeta = fifteenLivesStack.getItemMeta();
        fifteenLivesMeta.setDisplayName("§a15 Lives");
        fifteenLivesMeta.setLore(new ArrayList<String>(){{add("§7»Votes: §a" + fifteenVoteCount);}});
        fifteenLivesStack.setItemMeta(fifteenLivesMeta);
        inv.setItem(15, fifteenLivesStack);

        player.openInventory(inv);
    }

    /**
     * A Method that removes the player from the current team
     * @param player which gets removed from his team
     * @author SimsumMC
     */
    public static void removeTeam(Player player){
        HashMap<String, ArrayList<Player>> teamMembers = Cache.getTeamMembers();
        for(String key : teamMembers.keySet()){
            ArrayList<Player> players = teamMembers.get(key);
            if(players.contains(player)){
                players.remove(player);
                teamMembers.put(key, players);
                Cache.setTeamMembers(teamMembers);
                break;
            }
        }
    }

    /**
     * A Method that starts the game, gives every player the right items, changes the scoreboard, resets the cooldown and
     * teleports the players to the right position.
     * @author SimsumMC
     */
    public static boolean startGame(){
        //TODO: give items to the player
        if(gameStarted){
            return false;
        }
        gameStarted = true;

        Collection<? extends Player> players = Bukkit.getOnlinePlayers();

        for(Player player: players){
            String team = getPlayerTeam(player, true);
            Location location;
            switch (team){
                default:
                    location = Config.blueLocation;
                case "Red":
                    location = Config.redLocation;
                case "Green":
                    location = Config.greenLocation;
                case "Yellow":
                    location = Config.yellowLocation;
            }
            setPlayerCooldown(player, 0);
            setGameScoreBoard(player);
            player.teleport(location);

        }

        Bukkit.broadcastMessage(ChatColor.GREEN + "Game Starting...");

        return true;
    }

    /**
     * A Method that ends the game, gives every player the lobby items, changes the scoreboard, resets the Cache,
     * teleports the players to the lobby and announces the winner team.
     * @param winnerTeam the winner team as a **COLORED** String
     * @author SimsumMC
     */
    public static boolean endGame(String winnerTeam){
        if(!gameStarted){
            return false;
        }
        gameStarted = false;

        Cache.clear();

        String message = "§7The team §l"  + winnerTeam + " §r§7won!";
        Bukkit.getServer().sendPluginMessage(Main.getInstance(), message, null);

        Collection<? extends Player> players = Bukkit.getOnlinePlayers();

        for(Player player: players){
            setLobbyScoreBoard(player);
            player.teleport(Config.lobbyLocation);
            if(player.getGameMode() == GameMode.SPECTATOR){
                player.setGameMode(GameMode.SURVIVAL);
            }
        }

        Bukkit.broadcastMessage(ChatColor.RED + "Game Ending...");
        return true;
    }

    /**
     * A Method that updates the player cooldown (the number in the XP bar) every 20 ticks depended on the player
     * amount.
     * @author SimsumMC
     */
    public static void updatePlayerCooldown(){
        runCooldownTask = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                if(!gameStarted){
                    Collection<? extends Player> players = Bukkit.getOnlinePlayers();
                    int playerAmount = players.size();
                    int maxPlayers = Bukkit.getServer().getMaxPlayers();
                    if(playerAmount >= (maxPlayers / 2) && playerAmount != 1){
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

    /**
     * A Method that gives the lobby items to the given player.
     * @param player the player that becomes the items in the inventory
     * @author SimsumMC
     */
    public static void giveLobbyItems(Player player){

        PlayerInventory inv = player.getInventory();

        inv.clear();

        // Team Choose Item TODO: add interaction @Beelzebub
        ItemStack teamStack = new ItemStack(Material.BED);
        ItemMeta teamMeta = teamStack.getItemMeta();
        teamMeta.setDisplayName("§e§lChoose Team");
        teamStack.setItemMeta(teamMeta);
        inv.setItem(0, teamStack);

        // Vote Life Count Item TODO: add interaction
        ItemStack livesStack = new ItemStack(Material.FEATHER);
        ItemMeta livesMeta = livesStack.getItemMeta();
        livesMeta.setDisplayName("§a§lAmount of Lives");
        livesStack.setItemMeta(livesMeta);
        inv.setItem(2, livesStack);

        // Edit Inventory Item TODO: add interaction
        ItemStack inventoryStack = new ItemStack(Material.CHEST);
        ItemMeta inventoryMeta = inventoryStack.getItemMeta();
        inventoryMeta.setDisplayName("§b§lEdit Inventory");
        inventoryStack.setItemMeta(inventoryMeta);
        inv.setItem(4, inventoryStack);

        // Choose Perks Item TODO: add interaction
        ItemStack perksStack = new ItemStack(Material.ENDER_CHEST);
        ItemMeta perksMeta = perksStack.getItemMeta();
        perksMeta.setDisplayName("§d§lPerks");
        perksStack.setItemMeta(perksMeta);
        inv.setItem(6, perksStack);

        // Leave Server Item
        ItemStack leaveStack = new ItemStack(Material.SLIME_BALL);
        ItemMeta leaveMeta = leaveStack.getItemMeta();
        leaveMeta.addEnchant(Enchantment.KNOCKBACK, 1, true);
        leaveMeta.setDisplayName("§c§lLeave");
        leaveMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        leaveStack.setItemMeta(leaveMeta);
        inv.setItem(8, leaveStack);

    }

    /**
     * A Method that sets the cooldown for the given player.
     * @param player the player that gets the cooldown modified
     * @author SimsumMC
     */
    public static void setPlayerCooldown(Player player, int level){
        float exp = (float) level/60;
        player.setExp(exp);
        player.setLevel(level);

    }

    /**
     * A Method that returns the team of the player with the colour as a string.
     * @param player the player which team gets returned
     * @author SimsumMC
     */
    public static String getPlayerTeam(Player player, boolean raw){

        String teamName = "§cNot selected";
        HashMap<String, ArrayList<Player>> data = Cache.getTeamMembers();

        for(String key : data.keySet()){
            ArrayList<Player> players = data.get(key);
            if(players.contains(player)){
                if(!raw){
                    switch(key){
                        case "Blue":
                            teamName = "§1";
                        case "Green":
                            teamName = "§2";
                        case "Yellow":
                            teamName = "§e";
                        default:
                            teamName = "§4";
                    }
                }
                else{
                    teamName = "";
                }
                teamName += key;
                break;
            }
        }
        return teamName;

    }

    /**
     * A Method that updates the scoreboard for every player, depending on the game status.
     * @author SimsumMC
     */
    public static void updateScoreBoard(){

        runScoreBoardTask = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();

                for(Player player : players) {
                    if (!gameStarted) {
                        updateLobbyScoreBoard(player);
                    } else {
                        updateGameScoreBoard(player);

                    }
                }            }
        }.runTaskTimer(Main.getInstance(), 0, 20);

    }

    /**
     * A Method that changes the scoreboard for the given player to the lobby scoreboard.
     * @param player the player that gets the scoreboard modified
     * @author SimsumMC
     */
    public static void setLobbyScoreBoard(Player player){

        int maxPlayers = Bukkit.getServer().getMaxPlayers();
        int actualPlayers = Bukkit.getServer().getOnlinePlayers().size();

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.registerNewObjective("Woolbattle", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName("§a§lWoolbattle");

        Team team = board.registerNewTeam("team");
        Team map = board.registerNewTeam("map");
        Team lives = board.registerNewTeam("lives");
        Team players = board.registerNewTeam("players");

        obj.getScore("\u1CBC\u1CBC\u1CBC\u1CBC").setScore(11);

        obj.getScore("§7»Team").setScore(10);
        obj.getScore("§c").setScore(9);

        obj.getScore("\u1CBC\u1CBC\u1CBC").setScore(8);

        obj.getScore("§7»Map").setScore(7);
        obj.getScore("§d").setScore(6);

        obj.getScore("\u1CBC\u1CBC").setScore(5);

        obj.getScore("§7»Amount of Lives").setScore(4);
        obj.getScore("§e").setScore(3);

        obj.getScore("\u1CBC").setScore(2);

        obj.getScore("§7»Players").setScore(1);
        obj.getScore("§b").setScore(0);

        team.addEntry("§c");
        team.setPrefix(getPlayerTeam(player, false));

        map.addEntry("§d");
        map.setPrefix("§d" + Config.defaultMap);

        lives.addEntry("§e");
        lives.setPrefix("§e" + Config.defaultLives);

        players.addEntry("§b");
        players.setPrefix("§b" + actualPlayers + "/" + maxPlayers);

        player.setScoreboard(board);

    }

    /**
     * A Method that updates the scoreboard for the given player with the current values.
     * @param player the player that gets the scoreboard modified
     * @author SimsumMC
     */
    public static void updateLobbyScoreBoard(Player player){

        int maxPlayers = Bukkit.getServer().getMaxPlayers();
        int actualPlayers = Bukkit.getServer().getOnlinePlayers().size();

        // get the current top-voted amount of lives
        int topKey = Config.defaultLives;
        int topVoters = 0;
        HashMap<Integer, ArrayList<Player>> data = Cache.getLifeVoting();
        for(Integer key: data.keySet()){
            ArrayList<Player> players = data.get(key);
            if(players.size() > topVoters){
                topVoters = players.size();
                topKey = key;
            }
        }

        Scoreboard board = player.getScoreboard();

        Team team = board.getTeam("team");
        Team map = board.getTeam("map");
        Team lives = board.getTeam("lives");
        Team players = board.getTeam("players");

        team.setPrefix(getPlayerTeam(player, false));
        map.setPrefix("§d" + Config.defaultMap);
        lives.setPrefix("§e" + topKey);
        players.setPrefix("§b" + actualPlayers + "/" + maxPlayers);

    }

    /**
     * A Method that changes the scoreboard for the given player to the game scoreboard.
     * @param player the player that gets the scoreboard modified
     * @author SimsumMC
     */
    public static void setGameScoreBoard(Player player){

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.registerNewObjective("Woolbattle", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName("§a§lWoolbattle");

        Team team = board.registerNewTeam("team");

        obj.getScore("\u1CBC\u1CBC\u1CBC\u1CBC").setScore(11);

        obj.getScore("§7»Your Team").setScore(1);
        obj.getScore("§c").setScore(0);

        team.addEntry("§c");
        team.setPrefix(getPlayerTeam(player,false));

        player.setScoreboard(board);

    }

    /**
     * A Method that updates the scoreboard for the given player with the current values.
     * @param player the player that gets the scoreboard modified
     * @author SimsumMC
     */
    public static void updateGameScoreBoard(Player player){

        Scoreboard board = player.getScoreboard();

        Team team = board.getTeam("team");

        team.setPrefix(getPlayerTeam(player, false));

    }
}
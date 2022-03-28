package woolbattle.woolbattle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Wool;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LobbySystem implements Listener {

    static boolean gameStarted = false;
    static boolean runCooldownTask = false;
    static boolean runScoreBoardTask = false;
    static int cooldown = 60;

    // TODO: use the config.json here
    static String dummyTeam = "Red";
    static String dummyMap = "Vimo";
    static Integer dummyLives = 10;

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event){
        // Disables Fall Damage
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHungerDeplete(FoodLevelChangeEvent event){
        // Disables Hunger
        event.setCancelled(true);
        Player player = (Player) event.getEntity();
        player.setFoodLevel(20);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event){
        // Disables Dropping of Items
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        if(!(event.getBlock() instanceof Wool)){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Disables Moving Items in the Inventory
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
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        // Custom death messages
        // TODO: Colour should fit the team
        Player player = event.getEntity();
        event.setDeathMessage(ChatColor.GREEN + player.getDisplayName()
                + ChatColor.GRAY + " died.");
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        event.setQuitMessage(ChatColor.GRAY + "The player " + ChatColor.GREEN + player.getDisplayName()
                + ChatColor.GRAY + " left the game.");
        //TODO: implement Logic if a player who is in a game leaves -> look at the teams
        if(Bukkit.getServer().getOnlinePlayers().size() <= 1){
            endGame();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        event.setJoinMessage(ChatColor.GRAY + "The player " + ChatColor.GREEN + player.getDisplayName()
                + ChatColor.GRAY + " joined the game.");

        if(gameStarted){
            player.setGameMode(GameMode.SPECTATOR);
            player.sendMessage(ChatColor.RED + "There is already a running game!");
        }
        else{
            giveLobbyItems(player);
        }

        setPlayerLobbyScoreBoard(player);

        if(!runCooldownTask){
            updatePlayerCooldown();
        }

        if(!runScoreBoardTask){
            updateScoreBoard();
        }

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        if (event.getItem() != null){
            if (event.getItem().getItemMeta().getDisplayName().equals("§c§lLeave")){
                event.getPlayer().kickPlayer("§c§lYou left the game.");
            }
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

    public static void giveLobbyItems(Player player){

        PlayerInventory inv = player.getInventory();

        inv.clear();

        // Team Choose Item TODO: add interaction
        ItemStack teamItemStack = new ItemStack(Material.BED);
        ItemMeta teamItemMeta = teamItemStack.getItemMeta();
        teamItemMeta.setDisplayName("§e§lChoose Team");
        teamItemStack.setItemMeta(teamItemMeta);
        inv.setItem(0, teamItemStack);

        // Vote Life Count Item TODO: add interaction
        ItemStack livesItemStack = new ItemStack(Material.FEATHER);
        ItemMeta livesItemMeta = livesItemStack.getItemMeta();
        livesItemMeta.setDisplayName("§a§lAmount of Lives");
        livesItemStack.setItemMeta(livesItemMeta);
        inv.setItem(2, livesItemStack);

        // Edit Inventory Item TODO: add interaction
        ItemStack inventoryItemStack = new ItemStack(Material.CHEST);
        ItemMeta inventoryItemMeta = inventoryItemStack.getItemMeta();
        inventoryItemMeta.setDisplayName("§b§lEdit Inventory");
        inventoryItemStack.setItemMeta(inventoryItemMeta);
        inv.setItem(4, inventoryItemStack);

        // Choose Perks Item TODO: add interaction
        ItemStack perksItemStack = new ItemStack(Material.ENDER_CHEST);
        ItemMeta perksItemMeta = perksItemStack.getItemMeta();
        perksItemMeta.setDisplayName("§d§lPerks");
        perksItemStack.setItemMeta(perksItemMeta);
        inv.setItem(6, perksItemStack);

        // Leave Server Item
        ItemStack leaveItemStack = new ItemStack(Material.SLIME_BALL);
        ItemMeta leaveItemMeta = leaveItemStack.getItemMeta();
        leaveItemMeta.addEnchant(Enchantment.KNOCKBACK, 1, true);
        leaveItemMeta.setDisplayName("§c§lLeave");
        leaveItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        leaveItemStack.setItemMeta(leaveItemMeta);
        inv.setItem(8, leaveItemStack);

    }

    public static void setPlayerCooldown(Player player, int level){
        float exp = (float) level/60;
        player.setExp(exp);
        player.setLevel(level);

    }

    public static void updateScoreBoard(){
        runScoreBoardTask = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                if(!gameStarted){

                    Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();

                    for(Player player : players){
                        updateLobbyScoreBoard(player);

                    }
                }
                else{
                    // TODO: add Game Scoreboard method here
                }

            }
        }.runTaskTimer(Main.getInstance(), 0, 20);

    }

    public static void setPlayerLobbyScoreBoard(Player player){
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
        team.setPrefix("§c" + dummyTeam);

        map.addEntry("§d");
        map.setPrefix("§d" + dummyMap);

        lives.addEntry("§e");
        lives.setPrefix("§e" + dummyLives.toString());

        players.addEntry("§b");
        players.setPrefix("§b" + actualPlayers + "/" + maxPlayers);

        player.setScoreboard(board);

    }

    public static void updateLobbyScoreBoard(Player player){
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

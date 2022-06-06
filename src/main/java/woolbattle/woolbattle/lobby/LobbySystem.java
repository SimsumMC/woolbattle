package woolbattle.woolbattle.lobby;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
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
import woolbattle.woolbattle.AchievementSystem.AchievementGUI;
import woolbattle.woolbattle.AchievementSystem.AchievementSystem;
import woolbattle.woolbattle.Cache;
import woolbattle.woolbattle.Config;
import woolbattle.woolbattle.Enums.PerkType;
import woolbattle.woolbattle.Main;
import woolbattle.woolbattle.itemsystem.ItemSystem;
import woolbattle.woolbattle.perks.ActivePerk;
import woolbattle.woolbattle.team.TeamSystem;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static woolbattle.woolbattle.lives.LivesSystem.setPlayerSpawnProtection;
public class LobbySystem implements Listener {

    public static boolean gameStarted = false;
    public static boolean runCooldownTask = false;
    public static boolean runScoreBoardTask = false;
    private static int cooldown = Config.startCooldown;
    public static int teamLimit = Config.teamSize;

    /**
     * An Event that gets executed whenever a player dies to send a custom death message.
     *
     * @param event the PlayerDeathEvent event
     * @author SimsumMC
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // this is basically an unnecessary event -> only gets called when something goes wrong
        Player player = event.getEntity();
        event.setDeathMessage(ChatColor.GRAY + "The player " + ChatColor.GREEN + player.getDisplayName()
                + ChatColor.GRAY + " died.");
        if(!gameStarted){
            giveLobbyItems(player);
        }

    }

    /**
     * An Event that gets executed whenever a player joins the server to send a custom join message, set the
     * right GameMode, update the ScoreBoard, teleport to the right position or maybe start the cooldown.
     *
     * @param event the PlayerJoinEvent event
     * @author SimsumMC
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        event.setJoinMessage(ChatColor.GRAY + "The player " + ChatColor.GREEN + player.getDisplayName()
                + ChatColor.GRAY + " joined the game.");

        if (gameStarted) {
            setGameScoreBoard(player);
            setPlayerSpectator(player);
            player.sendMessage(ChatColor.RED + "There is already a running game!");
        } else {
            setLobbyScoreBoard(player);
            giveLobbyItems(player);
            player.teleport(Config.lobbyLocation);
        }
        if (!runCooldownTask) {
            updatePlayerCooldown();
        }

        if (!runScoreBoardTask) {
            updateScoreBoard();
        }

    }

    /**
     * An Event that gets executed whenever a player leaves the server to send a custom death message and
     * eventually end the current game.
     *
     * @param event the PlayerQuitEvent event
     * @author SimsumMC
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        event.setQuitMessage(ChatColor.GRAY + "The player " + ChatColor.GREEN + player.getDisplayName()
                + ChatColor.GRAY + " left the game.");

        // remove voting if any
        HashMap<Integer, ArrayList<Player>> lifeVoting = Cache.getLifeVoting();
        for (Integer key : lifeVoting.keySet()) {
            ArrayList<Player> players = lifeVoting.get(key);
            if (players.contains(player)) {
                players.remove(player);
                lifeVoting.put(key, players);
                Cache.setLifeVoting(lifeVoting);
                break;
            }
        }

        TeamSystem.removePlayerTeam(player);

        determinateWinnerTeam();
    }

    /**
     * An Event that gets executed whenever a player tries to move an item in the inventory to prevent the moving of
     * lobby items.
     *
     * @param event the InventoryClickEvent event
     * @author SimsumMC & Beelzebub
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if(event.getCurrentItem() == null){
            return;
        }

        if(event.getCurrentItem().getType() == Material.SULPHUR){
            player.sendMessage(ChatColor.RED + "You can't move items that are on cooldown!");
            event.setCancelled(true);
        }
        if (gameStarted) {
            return;
        }

        if (event.getWhoClicked() instanceof Player && event.getClickedInventory() != null && event.getCurrentItem().getItemMeta() != null) {
            if (!event.getClickedInventory().getName().equals("§bEdit Inventory") || event.getCurrentItem().getItemMeta().getDisplayName().equals(" ")) {
                List<ItemStack> items = new ArrayList<>();
                items.add(event.getCurrentItem());
                items.add(event.getCursor());
                items.add((event.getClick() == org.bukkit.event.inventory.ClickType.NUMBER_KEY) ?
                        event.getWhoClicked().getInventory().getItem(event.getHotbarButton()) : event.getCurrentItem());
                for (ItemStack item : items) {
                    if (item != null && item.hasItemMeta()) {
                        event.setCancelled(true);
                    }
                }
            }
        }

        if(event.getClickedInventory().getName() == null ||!event.getCurrentItem().hasItemMeta() || event.getCurrentItem().getItemMeta().getDisplayName().equals(" ")) {
            return;
        }

        String rawInventoryName = event.getClickedInventory().getName().substring(2);
        String rawItemName = event.getCurrentItem().getItemMeta().getDisplayName().substring(2);

        switch(rawInventoryName) {
            case "Voting for the Amount of Lives":
                HashMap<Integer, ArrayList<Player>> votingData = Cache.getLifeVoting();
                ItemMeta clickedItemMeta = event.getCurrentItem().getItemMeta();
                int lifeAmount = Integer.parseInt(clickedItemMeta.getDisplayName().substring(2).split(" ")[0]);

                //doesn't change anything if the player already voted for the given value
                if (votingData.get(lifeAmount).contains(player)) {
                    return;
                }

                //check if the player voted for another value before, if true remove the vote there
                for (Integer key : votingData.keySet()) {
                    ArrayList<Player> players = votingData.get(key);
                    if (players.contains(player)) {
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
                break;
            case "Team Selecting":
                String teamName = event.getCurrentItem().getItemMeta().getDisplayName().substring(7);
                ChatColor chatColor = TeamSystem.getTeamColour(teamName);

                if ((Cache.getTeamMembers().get(teamName)).contains(player)) {
                    return;

                } else if ((Cache.getTeamMembers().get(teamName)).size() >= teamLimit) {
                    player.sendMessage(ChatColor.RED + "The team already has " + teamLimit + " Members!");

                } else {
                    TeamSystem.removePlayerTeam(player);
                    (Cache.getTeamMembers().get(teamName)).add(player);
                    player.sendMessage(ChatColor.GRAY + "You have entered team " + chatColor + teamName + ChatColor.GRAY + ".");
                }

                TeamSystem.showTeamSelectionInventory((Player) event.getWhoClicked());

                break;

            case "Choose Perks":
                switch(rawItemName){
                    case "Active Perk #1":
                        showActivePerkMenu(player, PerkType.FIRST_ACTIVE);
                        break;
                    case "Active Perk #2":
                        showActivePerkMenu(player, PerkType.SECOND_ACTIVE);
                        break;
                    case "Passive Perk":
                        break;
                }

                break;
            case "Active Perk #1":
                if(rawItemName.equals("Go Back")){
                    showPerkMenu(player);
                }
                else{
                    savePerkSelection(player, rawItemName, PerkType.FIRST_ACTIVE);
                    showActivePerkMenu(player, PerkType.FIRST_ACTIVE);
                }
                break;
            case "Active Perk #2":
                if(rawItemName.equals("Go Back")){
                    showPerkMenu(player);
                }
                else {
                    savePerkSelection(player, rawItemName, PerkType.SECOND_ACTIVE);
                    showActivePerkMenu(player, PerkType.SECOND_ACTIVE);
                }
                break;
        }
    }


    /**
     * An Event that gets executed whenever a player interacts with an item to make the lobby items functional.
     * @param event the PlayerInteractEvent event
     * @author SimsumMC
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() == null || event.getItem().getItemMeta() == null || event.getItem().getItemMeta().getDisplayName() == null) return;

        Player player = event.getPlayer();
        String displayName = event.getItem().getItemMeta().getDisplayName();

        switch (displayName) {
            case "§lAchievements":
                AchievementGUI.showAchievementGUI(player);
                break;
            case "§c§lLeave":
                player.kickPlayer("§c§lYou left the game.");
                break;
            case "§a§lAmount of Lives":
                showLifeAmountVoting(player);
                break;
            case "§lAchievements":
                AchievementGUI.showAchievementGUI(player);
                break;
            case "§b§lEdit Inventory":
                showEditInventoryMenu(player);
                break;
            case "§d§lPerks":
                showPerkMenu(player);
                break;
            case "§e§lTeam Selecting":
                TeamSystem.showTeamSelectionInventory(player);
                break;
        }

        ActivePerk activePerk = Cache.getActivePerks().get(displayName.substring(2));
        if(activePerk != null){
            activePerk.execute(event, player);
        }

    }

    /**
     * An Event that gets executed whenever a player closes the current inventory which is required to save the new
     * inventory of the "Edit Inventory" Item
     * @param event the InventoryCloseEvent event
     * @author SimsumMC
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if(event.getInventory().getName() == null || !event.getInventory().getName().equals("§bEdit Inventory")){
            return;
        }

        Player player = (Player) event.getPlayer();
        Inventory eventInventory = event.getInventory();

        int shearsPosition = 0;
        int bowPosition = 0;
        int enderPearlPosition = 0;
        int activePerk1Position = 0;
        int activePerk2Position = 0;
        int position = 0;

        for (ItemStack itemStack : eventInventory.getContents()) {
            if(itemStack != null && itemStack.hasItemMeta() && !itemStack.getType().equals(Material.GLASS)){
                switch(itemStack.getItemMeta().getDisplayName()){
                    case "§bShears":
                        shearsPosition = position - 9;
                        break;
                    case "§bBow":
                        bowPosition = position - 9;
                        break;
                    case "§bEnder Pearl":
                        enderPearlPosition = position - 9;
                        break;
                    case "§bActive Perk 1":
                        activePerk1Position = position - 9;
                        break;
                    case "§bActive Perk 2":
                        activePerk2Position = position - 9;
                        break;
                }
            }
            position += 1;
        }

        int finalShearsPosition = shearsPosition;
        int finalBowPosition = bowPosition;
        int finalEnderPearlPosition = enderPearlPosition;
        int finalActivePerk1Position = activePerk1Position;
        int finalActivePerk2Position = activePerk2Position;

        //check for mistakes
        ArrayList<Integer> values= new ArrayList<Integer>(){{
            add(finalShearsPosition);
            add(finalBowPosition);
            add(finalEnderPearlPosition);
        }};
        int notDefined = 0;
        for(Integer value: values){
            if(value==0){
                notDefined +=1;
            }
        }
        if(notDefined >= 2){
            new BukkitRunnable(){

                @Override
                public void run() {
                    giveLobbyItems(player);
                }

            }.runTaskLaterAsynchronously(Main.getInstance(), 10);
            player.sendMessage(ChatColor.RED + "Something went wrong!");
            return;
        }

        //put the new configuration in the database
        MongoDatabase db = Main.getMongoDatabase();
        MongoCollection<Document> collection = db.getCollection("playerInventories");

        Document foundDocument = collection.find(eq("_id", player.getUniqueId().toString())).first();
        if(foundDocument == null){
            HashMap<String, Object> playerData = new HashMap<String, Object>(){{
                put("_id", player.getUniqueId().toString());
                put("shears", finalShearsPosition);
                put("bow", finalBowPosition);
                put("ender_pearl", finalEnderPearlPosition);
                put("active_perk1", finalActivePerk1Position);
                put("active_perk2", finalActivePerk2Position);
            }};

            Document document = new Document(playerData);
            collection.insertOne(document);
        }
        else{
            Document query = new Document().append("_id",  player.getUniqueId().toString());

            Bson updates = Updates.combine(
                    Updates.set("shears", finalShearsPosition),
                    Updates.set("bow", finalBowPosition),
                    Updates.set("ender_pearl", finalEnderPearlPosition),
                    Updates.set("active_perk1", finalActivePerk1Position),
                    Updates.set("active_perk2", finalActivePerk2Position)
            );

            collection.updateOne(query, updates);
        }

        player.sendMessage(ChatColor.GREEN + "Your new inventory was successfully saved.");

        new BukkitRunnable(){

            @Override
            public void run() {
                giveLobbyItems(player);
            }

        }.runTaskLaterAsynchronously(Main.getInstance(), 10);

    }

    /**
     * A Method that starts the game, gives every player the right items, changes the scoreboard, resets the cooldown and
     * teleports the players to the right position.
     * @return A boolean whether the game could successfully be started
     * @author SimsumMC
     */
    public static boolean startGame() {
        if(gameStarted){
            return false;
        }

        TeamSystem.teamsOnStart();

        int topVotedLifeAmount = getTopVotedLifeAmount();
        HashMap<String, Integer> teamLives = Cache.getTeamLives();

        Collection<? extends Player> players = Bukkit.getOnlinePlayers();

        long unixTime = (System.currentTimeMillis() / 1000L) + Config.spawnProtectionLengthAtGameStart;

        for(Player player: players){
            String team = TeamSystem.getPlayerTeam(player, true);
            teamLives.put(team, topVotedLifeAmount);
            Location location;
            switch (team){
                case "Red":
                    location = Config.redLocation;
                    break;
                case "Green":
                    location = Config.greenLocation;
                    break;
                case "Yellow":
                    location = Config.yellowLocation;
                    break;
                default:
                    location = Config.blueLocation;
                    break;
            }
            setPlayerCooldown(player, 0);
            setGameScoreBoard(player);

            gameStarted = true;

            ItemSystem.giveItems(player);
            player.teleport(location);

            setPlayerSpawnProtection(player, Config.spawnProtectionLengthAtGameStart);

            if(player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE){
                player.setGameMode(GameMode.SURVIVAL);
            }

        }

        Cache.setTeamLives(teamLives);

        return true;
    }

    /**
     * A Method that ends the game, gives every player the lobby items, changes the scoreboard, resets the Cache,
     * teleports the players to the lobby and announces the winner team.
     * @param winnerTeam the winner team as a **COLORED** String
     * @return A boolean whether the game could be successfully ended
     * @author SimsumMC
     */
    public static boolean endGame(String winnerTeam) {
        runScoreBoardTask = false;

        if(!gameStarted){
            return false;
        }

        gameStarted = false;

        cooldown = 60;

        Cache.clear();

        Bukkit.broadcastMessage(
                ChatColor.GRAY + "The team " + ChatColor.BOLD + winnerTeam + ChatColor.RESET + ChatColor.GRAY + " won!"
        );

        Collection<? extends Player> players = Bukkit.getOnlinePlayers();

        for(Player player: players){
            setLobbyScoreBoard(player);
            giveLobbyItems(player);
            player.teleport(Config.lobbyLocation);
            if(player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE){
                player.setGameMode(GameMode.SURVIVAL);
            }
        }

        updateScoreBoard();

<<<<<<< Updated upstream
        AchievementSystem.carried(winnerTeam.substring(2));
=======
        AchievementSystem.closeCall(winnerTeam.substring(2));
>>>>>>> Stashed changes
        return true;
    }

    /**
     * A Method that trys to find a winner team and ends the game if a team is found.
     * @author SimsumMC
     */
    public static void determinateWinnerTeam() {
        //check for teams -> if only one is left over
        HashMap<String, ArrayList<Player>> teamMembers = Cache.getTeamMembers();

        int emptyTeams = 0;
        String existingTeam = null;

        for(String key : teamMembers.keySet()){
            ArrayList<Player> players = teamMembers.get(key);
            if(players.size() == 0){
                emptyTeams += 1;
            }
            else{
                existingTeam = key;
            }

        }
        if(emptyTeams >= 3){
            if(existingTeam != null){
                endGame(TeamSystem.getTeamColour(existingTeam) + existingTeam);
            }
            else{
                endGame("§cUnknown");
            }
            return;
        }

        //check for lives
        HashMap<String, Integer> teamLives = Cache.getTeamLives();

        int deathTeams = 0;
        existingTeam = null;

        for(String key : teamLives.keySet()){
            int lives = teamLives.get(key);
            if(lives == 0){
                deathTeams += 1;
            }
            else{
                existingTeam = key;
            }

        }
        if(deathTeams >= 3){
            if(existingTeam != null){
                endGame(TeamSystem.getTeamColour(existingTeam) + existingTeam);
            }
            else{
                endGame("§cUnknown");
            }
        }
    }


    /**
     * A Method that saves the Perk that was selected by a player.
     * @param player the Player that selected the perk
     * @param perkName the Name of the Perk without colour code
     * @param perkType the Type of the perk as an enum
     * @author SimsumMC
     */
    public void savePerkSelection(Player player, String perkName, PerkType perkType){
        String perkTypeString = perkType.toString().toLowerCase();

        MongoDatabase db = Main.getMongoDatabase();
        MongoCollection<Document> collection = db.getCollection("playerPerks");

        Document foundDocument = collection.find(eq("_id", player.getUniqueId().toString())).first();
        if(foundDocument == null){

            HashMap<String, Object> playerData = new HashMap<String, Object>(){{
                put("_id", player.getUniqueId().toString());
                put("first_active", null);
                put("second_active", null);
                put("passive", null);
            }};

            playerData.put(perkTypeString, perkName);

            Document document = new Document(playerData);
            collection.insertOne(document);
        }
        else{
            if(perkType == PerkType.FIRST_ACTIVE){
                Object get = foundDocument.get(PerkType.SECOND_ACTIVE.toString().toLowerCase());
                if(get != null && get.equals(perkName)){
                    player.sendMessage(ChatColor.RED + "You can't have the same active perks!");
                    return;
                }
            }
            else if (perkType == PerkType.SECOND_ACTIVE){
                Object get = foundDocument.get(PerkType.FIRST_ACTIVE.toString().toLowerCase());
                if(get != null && get.equals(perkName)){
                    player.sendMessage(ChatColor.RED + "You can't have the same active perks!");
                    return;
                }
            }
            Document query = new Document().append("_id",  player.getUniqueId().toString());

            Bson updates = Updates.set(perkTypeString, perkName);

            collection.updateOne(query, updates);
        }
    }


    /**
     * A Method that gives the lobby items to the given player.
     * @param player the player that is given the items in the inventory
     * @author SimsumMC
     */
    public static void giveLobbyItems(Player player) {

        PlayerInventory inv = player.getInventory();

        // clear players inventory
        inv.clear();

        inv.setBoots(null);
        inv.setLeggings(null);
        inv.setChestplate(null);
        inv.setHelmet(null);

        // Achievement Item
        ItemStack achievementStack = new ItemStack(Material.DIAMOND);
        ItemMeta achievementMeta = achievementStack.getItemMeta();
        achievementMeta.setDisplayName("§lAchievements");
        achievementStack.setItemMeta(achievementMeta);
        inv.setItem(1, achievementStack);

        // Team Selecting Item
        ItemStack teamStack = new ItemStack(Material.BED);
        ItemMeta teamMeta = teamStack.getItemMeta();
        teamMeta.setDisplayName("§e§lTeam Selecting");
        teamStack.setItemMeta(teamMeta);
        inv.setItem(0, teamStack);

        // Vote Life Count Item
        ItemStack livesStack = new ItemStack(Material.FEATHER);
        ItemMeta livesMeta = livesStack.getItemMeta();
        livesMeta.setDisplayName("§a§lAmount of Lives");
        livesStack.setItemMeta(livesMeta);
        inv.setItem(2, livesStack);

        // Edit Inventory Item
        ItemStack inventoryStack = new ItemStack(Material.CHEST);
        ItemMeta inventoryMeta = inventoryStack.getItemMeta();
        inventoryMeta.setDisplayName("§b§lEdit Inventory");
        inventoryStack.setItemMeta(inventoryMeta);
        inv.setItem(4, inventoryStack);

        // Choose Perks Item TODO: add interaction -> LATER
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
     * A Method that shows / updates the inventar to vote for the life count.
     * @param player the player that gets the life amount voting inventory opened
     * @author SimsumMC
     */
    private static void showLifeAmountVoting(Player player) {
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
        fiveLivesMeta.setLore(new ArrayList<String>(){{add(ChatColor.GRAY + "»Votes: §a" + fiveVoteCount);}});
        fiveLivesStack.setItemMeta(fiveLivesMeta);
        inv.setItem(11, fiveLivesStack);

        // 10 Votes Item
        int tenVoteCount = Cache.getLifeVoting().get(10).size();
        ItemStack tenLivesStack = new ItemStack(Material.INK_SACK, 10, (short) 0, (byte) 10);
        ItemMeta tenLivesMeta = tenLivesStack.getItemMeta();
        tenLivesMeta.setDisplayName("§a10 Lives");
        tenLivesMeta.setLore(new ArrayList<String>(){{add(ChatColor.GRAY + "»Votes: §a" + tenVoteCount);}});
        tenLivesStack.setItemMeta(tenLivesMeta);
        inv.setItem(13, tenLivesStack);

        // 15 Votes Item
        int fifteenVoteCount = Cache.getLifeVoting().get(15).size();
        ItemStack fifteenLivesStack = new ItemStack(Material.INK_SACK, 15, (short) 0, (byte) 10);
        ItemMeta fifteenLivesMeta = fifteenLivesStack.getItemMeta();
        fifteenLivesMeta.setDisplayName("§a15 Lives");
        fifteenLivesMeta.setLore(new ArrayList<String>(){{add(ChatColor.GRAY + "»Votes: §a" + fifteenVoteCount);}});
        fifteenLivesStack.setItemMeta(fifteenLivesMeta);
        inv.setItem(15, fifteenLivesStack);

        player.openInventory(inv);
    }

    /**
     * A Method that shows / updates the inventar to change the inventory sort.
     * @param player - the Player that gets the inventory opened
     * @author SimsumMC
     */
    private static void showEditInventoryMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 3*9, "§bEdit Inventory");

        // Glass Background
        ItemStack glassStack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        ItemMeta glassMeta = glassStack.getItemMeta();
        glassMeta.setDisplayName(" ");
        glassStack.setItemMeta(glassMeta);

        for (int i = 0; i<= 26; i++) {
            if(i >= 9 && i <=17){
                continue;
            }
            inv.setItem(i, glassStack);
        }

        int shearsSlot;
        int bowSlot;
        int enderPearlSlot;
        int activePerk1Slot;
        int activePerk2Slot;

        MongoDatabase db = Main.getMongoDatabase();
        MongoCollection<Document> collection = db.getCollection("playerInventories");

        Document foundDocument = collection.find(eq("_id", player.getUniqueId().toString())).first();
        if(foundDocument == null){
            shearsSlot = 9;
            bowSlot = 10;
            enderPearlSlot = 17;
            activePerk1Slot = 16;
            activePerk2Slot = 11;
        }
        else{
            shearsSlot = (int) foundDocument.get("shears") + 9;
            bowSlot = (int) foundDocument.get("bow") + 9;
            enderPearlSlot = (int) foundDocument.get("ender_pearl") + 9;
            activePerk1Slot = (int) foundDocument.get("active_perk1") + 9;
            activePerk2Slot = (int) foundDocument.get("active_perk2") + 9;

        }

        // shears
        ItemStack shearsStack = new ItemStack(Material.SHEARS);
        ItemMeta shearsMeta = shearsStack.getItemMeta();
        shearsMeta.addEnchant(Enchantment.KNOCKBACK, 1, true);
        shearsMeta.setDisplayName("§bShears");
        shearsStack.setItemMeta(shearsMeta);
        inv.setItem(shearsSlot, shearsStack);

        // bow
        ItemStack bowStack = new ItemStack(Material.BOW);
        ItemMeta bowMeta = bowStack.getItemMeta();
        bowMeta.addEnchant(Enchantment.KNOCKBACK, 1, true);
        bowMeta.setDisplayName("§bBow");
        bowStack.setItemMeta(bowMeta);
        inv.setItem(bowSlot, bowStack);

        // ender pearl
        ItemStack enderPearlStack = new ItemStack(Material.ENDER_PEARL);
        ItemMeta enderPearlMeta = enderPearlStack.getItemMeta();
        enderPearlMeta.setDisplayName("§bEnder Pearl");
        enderPearlStack.setItemMeta(enderPearlMeta);
        inv.setItem(enderPearlSlot, enderPearlStack);

        // active perk 1
        ItemStack activePerk1Stack = new ItemStack(Material.BARRIER);
        ItemMeta activePerk1Meta = activePerk1Stack.getItemMeta();
        activePerk1Meta.setDisplayName("§bActive Perk 1");
        activePerk1Stack.setItemMeta(activePerk1Meta);
        inv.setItem(activePerk1Slot, activePerk1Stack);

        // active perk 1
        ItemStack activePerk2Stack = new ItemStack(Material.BARRIER);
        ItemMeta activePerk2Meta = activePerk2Stack.getItemMeta();
        activePerk2Meta.setDisplayName("§bActive Perk 2");
        activePerk2Stack.setItemMeta(activePerk2Meta);
        inv.setItem(activePerk2Slot, activePerk2Stack);

        player.openInventory(inv);
    }

    /**
     * A Method that shows the inventar to choose between the different perk types to change them.
     * @param player - The player gets an inventory opened to choose the perks.
     * @author SimsumMC
     */
    private static void showPerkMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 3*9, "§dChoose Perks");

        // Glass Background
        ItemStack glassStack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        ItemMeta glassMeta = glassStack.getItemMeta();
        glassMeta.setDisplayName(" ");
        glassStack.setItemMeta(glassMeta);

        for (int i = 0; i<= 26; i++) {
            inv.setItem(i, glassStack);
        }

        // Active Perk #1
        ItemStack activeOneStack = new ItemStack(Material.CHEST);
        ItemMeta activeOneMeta = activeOneStack.getItemMeta();
        activeOneMeta.setDisplayName("§dActive Perk #1");
        activeOneStack.setItemMeta(activeOneMeta);
        inv.setItem(11, activeOneStack);

        // Active Perk #2
        ItemStack activeTwoStack = new ItemStack(Material.CHEST);
        ItemMeta activeTwoMeta = activeTwoStack.getItemMeta();
        activeTwoMeta.setDisplayName("§dActive Perk #2");
        activeTwoStack.setItemMeta(activeTwoMeta);
        inv.setItem(13, activeTwoStack);

        // Passive Perk
        ItemStack passiveStack = new ItemStack(Material.ENDER_CHEST);
        ItemMeta passiveMeta = passiveStack.getItemMeta();
        passiveMeta.setDisplayName("§dPassive Perk");
        passiveStack.setItemMeta(passiveMeta);
        inv.setItem(15, passiveStack);

        player.openInventory(inv);
    }

    /**
     * A Method that shows the inventar to choose between the different perk types to change them.
     * @author SimsumMC
     */
    private static void showActivePerkMenu(Player player, PerkType perkType) {

        String perkTypeString = perkType.toString().toLowerCase();
        String selectedPerk = null;

        MongoDatabase db = Main.getMongoDatabase();
        MongoCollection<Document> collection = db.getCollection("playerPerks");

        Document foundDocument = collection.find(eq("_id", player.getUniqueId().toString())).first();
        if(foundDocument != null){
            if(foundDocument.get(perkTypeString) != null){
                selectedPerk = (String) foundDocument.get(perkTypeString);
            }
        }

        Inventory inv = Bukkit.createInventory(null, 3*9, "§dActive Perk #" + perkType.value);

        ArrayList<String> newLore;

        HashMap<String, ActivePerk> activePerks = Cache.getActivePerks();
        for(ActivePerk perk : activePerks.values()){
            if(!perk.getSelectableStatus()){
                continue;
            }

            ItemStack itemStack = perk.getItemStack().clone();
            ItemMeta itemMeta = itemStack.getItemMeta();

            if(selectedPerk != null && itemMeta.getDisplayName().substring(2).equals(selectedPerk)){
                itemMeta.addEnchant(Enchantment.KNOCKBACK, 1, true);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            else{
                if (itemMeta.hasEnchants()){
                    for(Enchantment enchantment : itemMeta.getEnchants().keySet()){
                        itemMeta.removeEnchant(enchantment);
                    }
                }
            }


            newLore = new ArrayList<>();

            newLore.add(ChatColor.WHITE + perk.getDescription());
            newLore.add("\u1CBC");
            newLore.add(ChatColor.GOLD + "WoolCost: " + ChatColor.DARK_PURPLE + perk.getWoolCost());
            newLore.add(ChatColor.GOLD + "Cooldown: " + ChatColor.DARK_PURPLE + perk.getCooldown());

            itemMeta.setLore(newLore);

            itemStack.setItemMeta(itemMeta);

            inv.addItem(itemStack);
        }

        // Back Item
        ItemStack backStack = new ItemStack(Material.WOOD_DOOR);
        ItemMeta backMeta = backStack.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Go Back");
        backStack.setItemMeta(backMeta);
        inv.setItem(26, backStack);

        player.openInventory(inv);
    }



    /**
     * A Method that updates the player cooldown (the number in the XP bar) every 20 ticks depended on the player
     * amount.
     * @author SimsumMC
     */
    public static void updatePlayerCooldown() {
        runCooldownTask = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                if(runScoreBoardTask){
                    if(!gameStarted) {
                        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
                        int playerAmount = players.size();
                        if (playerAmount >= (Config.teamSize * 2)) {
                            if (cooldown == 0) {
                                startGame();
                            } else if (cooldown > Config.skipCooldown) {
                                cooldown = Config.skipCooldown;
                            }
                            cooldown -= 1;
                        } else {
                            cooldown = 60;
                        }
                        for (Player player : players) {

                            if (player.getGameMode() == GameMode.SURVIVAL) {
                                setPlayerCooldown(player, cooldown);
                            }

                        }
                    }
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 20);

    }

    /**
     * A Method that sets the cooldown for the given player.
     * @param player the player that gets the cooldown modified
     * @author SimsumMC
     */
    public static void setPlayerCooldown(Player player, int level) {
        float exp = (float) level/60;
        player.setExp(exp);
        player.setLevel(level);

    }

    /**
     * A Method that returns the current top-voted amount of lives.
     * @return The top voted life amount as an Integer
     * @author SimsumMC
     */
    public static int getTopVotedLifeAmount(){
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
        return topKey;
    }

    /**
     * A Method that sets a player to a spectator -> game mode & position changes
     *
     * @param player the player that gets into spectator mode
     * @author SimsumMC
     */
    public static void setPlayerSpectator(Player player) {
        player.teleport(Config.midLocation);
        player.setGameMode(GameMode.SPECTATOR);
    }

    /**
     * A Method that updates the scoreboard for every player, depending on the game status.
     * @author SimsumMC
     */
    public static void updateScoreBoard() {
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
    public static void setLobbyScoreBoard(Player player) {

        int maxPlayers = Bukkit.getServer().getMaxPlayers();
        int actualPlayers = Bukkit.getServer().getOnlinePlayers().size();

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();

        Objective obj = board.registerNewObjective("Lobby", "dummy");

        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName("§a§lWoolbattle");

        Team team = board.registerNewTeam("team");
        Team map = board.registerNewTeam("map");
        Team lives = board.registerNewTeam("lives");
        Team players = board.registerNewTeam("players");

        obj.getScore("\u1CBC\u1CBC\u1CBC\u1CBC").setScore(11);

        obj.getScore(ChatColor.GRAY + "»Team").setScore(10);
        obj.getScore("§c").setScore(9);

        obj.getScore("\u1CBC\u1CBC\u1CBC").setScore(8);

        obj.getScore(ChatColor.GRAY + "»Map").setScore(7);
        obj.getScore("§d").setScore(6);

        obj.getScore("\u1CBC\u1CBC").setScore(5);

        obj.getScore(ChatColor.GRAY + "»Amount of Lives").setScore(4);
        obj.getScore("§e").setScore(3);

        obj.getScore("\u1CBC").setScore(2);

        obj.getScore(ChatColor.GRAY + "»Players").setScore(1);
        obj.getScore("§b").setScore(0);

        team.addEntry("§c");
        team.setPrefix(TeamSystem.getPlayerTeam(player, false));

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
    public static void updateLobbyScoreBoard(Player player) {
        if (!runScoreBoardTask){return;}

        int maxPlayers = Bukkit.getServer().getMaxPlayers();
        int actualPlayers = Bukkit.getServer().getOnlinePlayers().size();

        Scoreboard board = player.getScoreboard();

        Team team = board.getTeam("team");
        Team map = board.getTeam("map");
        Team lives = board.getTeam("lives");
        Team players = board.getTeam("players");

        team.setPrefix(TeamSystem.getPlayerTeam(player, false));
        map.setPrefix("§d" + Config.defaultMap);
        lives.setPrefix("§e" + getTopVotedLifeAmount());
        players.setPrefix("§b" + actualPlayers + "/" + maxPlayers);

    }

    /**
     * A Method that changes the scoreboard for the given player to the game scoreboard.
     * @param player the player that gets the scoreboard modified
     * @author SimsumMC
     */
    public static void setGameScoreBoard(Player player) {

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();

        Objective obj = board.registerNewObjective("Game", "dummy");

        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName("§a§lWoolbattle");

        Team team = board.registerNewTeam("team");
        Team map = board.registerNewTeam("map");

        Team redTeam = board.registerNewTeam("redTeam");
        Team blueTeam = board.registerNewTeam("blueTeam");
        Team greenTeam = board.registerNewTeam("greenTeam");
        Team yellowTeam = board.registerNewTeam("yellowTeam");

        obj.getScore("\u1CBC\u1CBC\u1CBC").setScore(11);

        obj.getScore(ChatColor.GRAY + "»Team").setScore(10);
        obj.getScore("§c").setScore(9);

        obj.getScore("\u1CBC\u1CBC").setScore(8);

        obj.getScore(ChatColor.GRAY + "»Map").setScore(7);
        obj.getScore("§d").setScore(6);

        obj.getScore("\u1CBC").setScore(5);

        obj.getScore(ChatColor.GRAY + "»Lives").setScore(4);

        obj.getScore("§4").setScore(3);
        obj.getScore(ChatColor.BLUE.toString()).setScore(2);
        obj.getScore("§2").setScore(1);
        obj.getScore("§e").setScore(0);

        team.addEntry("§c");
        team.setPrefix(TeamSystem.getPlayerTeam(player,false));

        map.addEntry("§d");
        map.setPrefix("§d" + Config.defaultMap);

        HashMap<String, Integer> teamLives = Cache.getTeamLives();
        int redLives = teamLives.get("Red");
        int blueLives = teamLives.get("Blue");
        int greenLives = teamLives.get("Green");
        int yellowLives = teamLives.get("Yellow");

        redTeam.addEntry("§4");
        redTeam.setPrefix("§4❤ " + redLives);

        blueTeam.addEntry(ChatColor.BLUE.toString());
        blueTeam.setPrefix(ChatColor.BLUE + "❤ " + blueLives);

        greenTeam.addEntry("§2");
        greenTeam.setPrefix("§2❤ " + greenLives);

        yellowTeam.addEntry("§e");
        yellowTeam.setPrefix("§e❤ " + yellowLives);

        player.setScoreboard(board);

    }

    /**
     * A Method that updates the scoreboard for the given player with the current values.
     * @param player the player that gets the scoreboard modified
     * @author SimsumMC
     */
    public static void updateGameScoreBoard(Player player) {
        if (!runScoreBoardTask){return;}

        Scoreboard board = player.getScoreboard();

        Team team = board.getTeam("team");
        Team map = board.getTeam("map");

        Team redTeam = board.getTeam("redTeam");
        Team blueTeam = board.getTeam("blueTeam");
        Team greenTeam = board.getTeam("greenTeam");
        Team yellowTeam = board.getTeam("yellowTeam");

        team.setPrefix(TeamSystem.getPlayerTeam(player,false));
        map.setPrefix("§d" + Config.defaultMap);

        HashMap<String, Integer> teamLives = Cache.getTeamLives();
        int redLives = teamLives.get("Red");
        int blueLives = teamLives.get("Blue");
        int greenLives = teamLives.get("Green");
        int yellowLives = teamLives.get("Yellow");

        redTeam.setPrefix("§4❤ " + redLives);
        blueTeam.setPrefix(ChatColor.BLUE + "❤ " + blueLives);
        greenTeam.setPrefix("§2❤ " + greenLives);
        yellowTeam.setPrefix("§e❤ " + yellowLives);

    }
}
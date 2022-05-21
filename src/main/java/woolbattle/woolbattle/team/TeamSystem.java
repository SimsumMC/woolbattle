package woolbattle.woolbattle.team;

import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import woolbattle.woolbattle.Cache;
import woolbattle.woolbattle.Config;
import woolbattle.woolbattle.lobby.LobbySystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import static woolbattle.woolbattle.Cache.getTeamMembers;


public class TeamSystem implements Listener {

    /**
     * A method that handles the team division.
     * @author Beelzebub
     */
    public static void teamsOnStart() {
        int numActiveTeams = 0;
        String teamWithMembers = null;
        ArrayList<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        ArrayList<Player> teamLessPlayers = new ArrayList<>();

        for (int i = Bukkit.getOnlinePlayers().size() - 1; i>=0; i--) {
            Player player = onlinePlayers.get(i);
            if (TeamSystem.getPlayerTeam(player, true).equals("§cNot selected")) {
                teamLessPlayers.add(player);
            }
        }

        for (int i = teamLessPlayers.size() - 1; i>=0; i--) {
            int[] sizes = {
                    getTeamMembers().get("Red").size(),
                    getTeamMembers().get("Blue").size(),
                    getTeamMembers().get("Green").size(),
                    getTeamMembers().get("Yellow").size()
            };
            int smallestNumber = 0;
            int temp = sizes[0];
            for(int a=0;a<sizes.length;a++) {
                if(sizes[a] <= temp) {
                    temp = sizes[a];
                    smallestNumber = a;
                }
            }
            switch (smallestNumber){
                case 0:
                    (getTeamMembers().get("Red")).add(teamLessPlayers.get(i));
                    teamLessPlayers.get(i).sendMessage(ChatColor.GRAY + "You didn't enter a team so you were put into team " + ChatColor.RED + "red" + ChatColor.GRAY + "!");
                    break;
                case 1: (getTeamMembers().get("Blue")).add(teamLessPlayers.get(i));
                    teamLessPlayers.get(i).sendMessage(ChatColor.GRAY + "You didn't enter a team so you were put into team " + ChatColor.DARK_BLUE + "blue" + ChatColor.GRAY + "!");
                    break;
                case 2: (getTeamMembers().get("Green")).add(teamLessPlayers.get(i));
                    teamLessPlayers.get(i).sendMessage(ChatColor.GRAY + "You didn't enter a team so you were put into team " + ChatColor.DARK_GREEN + "green" + ChatColor.GRAY + "!");
                    break;
                case 3: (getTeamMembers().get("Yellow")).add(teamLessPlayers.get(i));
                    teamLessPlayers.get(i).sendMessage(ChatColor.GRAY + "You didn't enter a team so you were put into team " + ChatColor.YELLOW + "yellow" + ChatColor.GRAY + "!");
                    break;
            }
            teamLessPlayers.remove(teamLessPlayers.get(i));
        }
        int[] sizes = {
                getTeamMembers().get("Red").size(),
                getTeamMembers().get("Blue").size(),
                getTeamMembers().get("Green").size(),
                getTeamMembers().get("Yellow").size()
        };
        for (int i = 0; i < sizes.length; i++) {
            if (sizes[i] > 0) {
                switch (i) {
                    case 0: teamWithMembers = "Red"; break;
                    case 1: teamWithMembers = "Blue"; break;
                    case 2: teamWithMembers = "Green"; break;
                    case 3: teamWithMembers = "Yellow"; break;
                }
                numActiveTeams += 1;
            }
        }

        if (numActiveTeams < 2) {
            int size = getTeamMembers().get(teamWithMembers).size();
            if (size / 2 != 0) {

                ArrayList<Player> member = getTeamMembers().get(teamWithMembers);

                if (!teamWithMembers.equals("Blue")) {
                    HashMap<String, ArrayList<Player>> members = getTeamMembers();

                    ArrayList<Player> newMem = new ArrayList<Player>() {{
                        add(member.get(0));
                    }};
                    member.remove(0);
                    members.put("Blue", newMem);

                    members.put(teamWithMembers, member);

                    Cache.setTeamMembers(members);
                }
                else {
                    HashMap<String, ArrayList<Player>> members = getTeamMembers();

                    ArrayList<Player> newMem = new ArrayList<Player>() {{
                        add(member.get(0));
                    }};
                    member.remove(0);
                    members.put("Red", newMem);

                    members.put(teamWithMembers, member);

                    Cache.setTeamMembers(members);
                }

            }
        }
    }

    /**
     * A method that opens the Inventory for the team selection for the given player.
     * @param player - the player that gets the inventory "shown" (opened)
     * @author Beelzebub
     */
    public static void showTeamSelectionInventory(Player player) {
        Inventory voting = Bukkit.createInventory(null, 27, ChatColor.YELLOW + "Team Selecting");

        //adding glass
        ItemStack Glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 15);
        ItemMeta GlassMeta = Glass.getItemMeta();
        GlassMeta.setDisplayName(" ");
        Glass.setItemMeta(GlassMeta);

        for (int i = 0; i <= 26; i++) {
            voting.setItem(i, Glass);
        }

        // Adding the "Vote for red" item
        ItemStack voteRed = new ItemStack(Material.WOOL, 1, (byte) 14);
        ArrayList<String> voteRedLore = new ArrayList<>();
        ItemMeta voteRedMeta = voteRed.getItemMeta();
        for (int i = getTeamMembers().get("Red").size() - 1; i >= 0; i--) {
            voteRedLore.add(ChatColor.GRAY + "» " + getTeamMembers().get("Red").get(i).getDisplayName());
        }

        voteRedMeta.setDisplayName(ChatColor.RED + "Team Red");
        voteRedMeta.setLore(voteRedLore);
        voteRed.setItemMeta(voteRedMeta);
        voting.setItem(11, voteRed);

        // Adding the "Vote for blue" item
        ItemStack voteBlue = new ItemStack(Material.WOOL, 1, (byte) 11);
        ArrayList<String> voteBlueLore = new ArrayList<>();
        ItemMeta voteBlueMeta = voteBlue.getItemMeta();
        for (int i = getTeamMembers().get("Blue").size() - 1; i >= 0; i--) {
            voteBlueLore.add(ChatColor.GRAY + "» " + getTeamMembers().get("Blue").get(i).getDisplayName());
        }

        voteBlueMeta.setDisplayName(ChatColor.BLUE + "Team Blue");
        voteBlueMeta.setLore(voteBlueLore);
        voteBlue.setItemMeta(voteBlueMeta);
        voting.setItem(12, voteBlue);

        // Adding the "Vote for Green" item
        ItemStack voteGreen = new ItemStack(Material.WOOL, 1, (byte) 5);
        ArrayList<String> voteGreenLore = new ArrayList<>();
        ItemMeta voteGreenMeta = voteGreen.getItemMeta();
        for (int i = getTeamMembers().get("Green").size() - 1; i >= 0; i--) {
            voteGreenLore.add(ChatColor.GRAY + "» " + getTeamMembers().get("Green").get(i).getDisplayName());
        }

        voteGreenMeta.setDisplayName(ChatColor.GREEN + "Team Green");
        voteGreenMeta.setLore(voteGreenLore);
        voteGreen.setItemMeta(voteGreenMeta);
        voting.setItem(14, voteGreen);

        // Adding the "Vote for Yellow" item
        ItemStack voteYellow = new ItemStack(Material.WOOL, 1, (byte) 4);
        ArrayList<String> voteYellowLore = new ArrayList<>();
        ItemMeta voteYellowMeta = voteYellow.getItemMeta();
        for (int i = getTeamMembers().get("Yellow").size() - 1; i >= 0; i--) {
            voteYellowLore.add(ChatColor.GRAY + "» " + getTeamMembers().get("Yellow").get(i).getDisplayName());
        }

        voteYellowMeta.setDisplayName(ChatColor.YELLOW + "Team Yellow");
        voteYellowMeta.setLore(voteYellowLore);
        voteYellow.setItemMeta(voteYellowMeta);
        voting.setItem(15, voteYellow);

        player.openInventory(voting);
    }

    /**
     * An event that gets executed whenever an entity damages another entity to prevent hitting team members.
     * @param event - the EntityDamageByEntityEvent
     * @author Beelzebub & SimsumMC
     */
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event){
        HashMap<Player, Long> lastDamage = Cache.getLastDamage();
        long unixTime = System.currentTimeMillis() / 1000L;
        lastDamage.put((Player) event.getEntity(), unixTime);
        Cache.setLastDamage(lastDamage);

        Player damager;
        Player damaged = (Player) event.getEntity();

        if(event.getDamager() instanceof Arrow){
            Arrow arrow = (Arrow) event.getDamager();
            damager = (Player) arrow.getShooter();

        }
        else{
            if(!(event.getDamager() instanceof Player)){
                return;
            }
            damager = (Player) event.getDamager();
        }

        if (damaged != null && damager != null) {
            if (TeamSystem.getPlayerTeam(damager, true).equals(TeamSystem.getPlayerTeam(damaged, true)) || !LobbySystem.gameStarted) {
                Vector velocity;
                velocity = event.getEntity().getVelocity();
                event.getEntity().setVelocity(velocity);
                event.setCancelled(true);
                return;
            }
            HashMap<Player, Long> spawnProtection = Cache.getSpawnProtection();
            if(spawnProtection.containsKey(damaged) && (unixTime - spawnProtection.get(damaged)) <= Config.spawnProtectionLength){
                if(damager.getUniqueId() != damaged.getUniqueId()){
                    damager.sendMessage("§cThe player has spawn protection!");
                }
                event.setCancelled(true);
            }
        }
    }

    /**
     * A Method that returns the team of the player with the colour as a string.
     * @param player the player which team gets returned
     * @param raw a boolean whether the method should return a raw string or a colored one
     * @return the team name as a string if any, else "§cNot selected"
     * @author SimsumMC
     */
    public static String getPlayerTeam(Player player, boolean raw) {

        String teamName = ChatColor.RED + "Not selected";
        HashMap<String, ArrayList<Player>> data = getTeamMembers();

        for(String key : data.keySet()){
            ArrayList<Player> players = data.get(key);
            if(players.contains(player)){
                if(!raw){
                    teamName = getTeamColour(key).toString();
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
     * A Method that removes the player from the current team
     * @param player which gets removed from his team
     * @author SimsumMC
     */
    public static void removePlayerTeam(Player player) {
        HashMap<String, ArrayList<Player>> teamMembers = getTeamMembers();

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
     * A Method that returns the team of the player with the colour as a ChatColor Enum.
     * @param team a S
     * @return the colour of the team
     * @author SimsumMC
     */
    public static ChatColor getTeamColour(String team) {
        switch(team){
            case "Blue":
                return ChatColor.DARK_BLUE;
            case "Green":
                return ChatColor.DARK_GREEN;
            case "Yellow":
                return ChatColor.YELLOW;
            default:
                return ChatColor.DARK_RED;
        }
    }

    /**
     * Method that returns the team-color of the specified player as a DyeColor.
     * @param p The player to get the team-color of
     * @author Servaturus
     */
    public static DyeColor findTeamDyeColor(Player p){
        String team = getPlayerTeam(p, true);
        switch(team){
            case "Blue":
                return DyeColor.BLUE;
            case "Red":
                return DyeColor.RED;
            case "Green":
                return DyeColor.GREEN;
            case "Yellow":
                return DyeColor.YELLOW;
            default:
                return DyeColor.WHITE;
        }
    }

    /**
     * Method that returns the team-color of the specified player as a Color.
     * @param p The player to get the team-color of
     * @author Servaturus
     */
    public static Color findTeamColor(Player p){
        String team = getPlayerTeam(p, true);
        switch(team){
            case "Blue":
                return Color.BLUE;
            case "Red":
                return Color.RED;
            case "Green":
                return Color.GREEN;
            case "Yellow":
                return Color.YELLOW;
            default:
                return Color.WHITE;
        }
    }
}

package woolbattle.woolbattle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;


public class TeamSystem implements Listener {
    public static void teamsOnStart() {
        int numActiveTeams = 0;
        String teamWithMembers = null;
        ArrayList<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        ArrayList<Player> teamlessPlayers = new ArrayList<>();

        for (int i = Bukkit.getOnlinePlayers().size() - 1; i>=0; i--) {
            Player player = onlinePlayers.get(i);
            if (TeamSystem.getPlayerTeam(player, true).equals("§cNot selected")) {
                teamlessPlayers.add(player);
            }
        }

        for (int i = teamlessPlayers.size() - 1; i>=0; i--) {
            int[] sizes = {
                    Cache.getTeamMembers().get("Red").size(),
                    Cache.getTeamMembers().get("Blue").size(),
                    Cache.getTeamMembers().get("Green").size(),
                    Cache.getTeamMembers().get("Yellow").size()
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
                    (Cache.getTeamMembers().get("Red")).add(teamlessPlayers.get(i));
                    teamlessPlayers.get(i).sendMessage(ChatColor.GRAY + "You didn't enter a team so you were put into team " + ChatColor.RED + " red" + ChatColor.GRAY + "!");
                break;
                case 1: (Cache.getTeamMembers().get("Blue")).add(teamlessPlayers.get(i));
                    teamlessPlayers.get(i).sendMessage(ChatColor.GRAY + "You didn't enter a team so you were put into team " + ChatColor.DARK_BLUE + " blue" + ChatColor.GRAY + "!");
                break;
                case 2: (Cache.getTeamMembers().get("Green")).add(teamlessPlayers.get(i));
                    teamlessPlayers.get(i).sendMessage(ChatColor.GRAY + "You didn't enter a team so you were put into team " + ChatColor.DARK_GREEN + " green" + ChatColor.GRAY + "!");
                break;
                case 3: (Cache.getTeamMembers().get("Yellow")).add(teamlessPlayers.get(i));
                    teamlessPlayers.get(i).sendMessage(ChatColor.GRAY + "You didn't enter a team so you were put into team " + ChatColor.YELLOW + " yellow" + ChatColor.GRAY + "!");
                break;
            }
            teamlessPlayers.remove(teamlessPlayers.get(i));
        }
        int[] sizes = {
                Cache.getTeamMembers().get("Red").size(),
                Cache.getTeamMembers().get("Blue").size(),
                Cache.getTeamMembers().get("Green").size(),
                Cache.getTeamMembers().get("Yellow").size()
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
            int size = Cache.getTeamMembers().get(teamWithMembers).size();
            if (size / 2 != 0) {

                ArrayList<Player> member = Cache.getTeamMembers().get(teamWithMembers);

                if (!teamWithMembers.equals("Blue")) {
                    HashMap<String, ArrayList<Player>> members = Cache.getTeamMembers();

                    ArrayList<Player> newMem = new ArrayList<Player>() {{
                       add(member.get(0));
                    }};
                    member.remove(0);
                    members.put("Blue", newMem);

                    members.put(teamWithMembers, member);

                    Cache.setTeamMembers(members);
                }
                else {
                    HashMap<String, ArrayList<Player>> members = Cache.getTeamMembers();

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

    public static void loadSelection (Player player) {
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
        for (int i = Cache.getTeamMembers().get("Red").size() - 1; i >= 0; i--) {
            voteRedLore.add("§7» " + Cache.getTeamMembers().get("Red").get(i).getDisplayName());
        }

        voteRedMeta.setDisplayName(ChatColor.RED + "Team Red");
        voteRedMeta.setLore(voteRedLore);
        voteRed.setItemMeta(voteRedMeta);
        voting.setItem(11, voteRed);

        // Adding the "Vote for blue" item
        ItemStack voteBlue = new ItemStack(Material.WOOL, 1, (byte) 11);
        ArrayList<String> voteBlueLore = new ArrayList<>();
        ItemMeta voteBlueMeta = voteBlue.getItemMeta();
        for (int i = Cache.getTeamMembers().get("Blue").size() - 1; i >= 0; i--) {
            voteBlueLore.add("§7» " + Cache.getTeamMembers().get("Blue").get(i).getDisplayName());
        }

        voteBlueMeta.setDisplayName(ChatColor.BLUE + "Team Blue");
        voteBlueMeta.setLore(voteBlueLore);
        voteBlue.setItemMeta(voteBlueMeta);
        voting.setItem(12, voteBlue);

        // Adding the "Vote for Green" item
        ItemStack voteGreen = new ItemStack(Material.WOOL, 1, (byte) 5);
        ArrayList<String> voteGreenLore = new ArrayList<>();
        ItemMeta voteGreenMeta = voteGreen.getItemMeta();
        for (int i = Cache.getTeamMembers().get("Green").size() - 1; i >= 0; i--) {
            voteGreenLore.add("§7» " + Cache.getTeamMembers().get("Green").get(i).getDisplayName());
        }

        voteGreenMeta.setDisplayName(ChatColor.GREEN + "Team Green");
        voteGreenMeta.setLore(voteGreenLore);
        voteGreen.setItemMeta(voteGreenMeta);
        voting.setItem(14, voteGreen);

        // Adding the "Vote for Yellow" item
        ItemStack voteYellow = new ItemStack(Material.WOOL, 1, (byte) 4);
        ArrayList<String> voteYellowLore = new ArrayList<>();
        ItemMeta voteYellowMeta = voteYellow.getItemMeta();
        for (int i = Cache.getTeamMembers().get("Yellow").size() - 1; i >= 0; i--) {
            voteYellowLore.add("§7» " + Cache.getTeamMembers().get("Yellow").get(i).getDisplayName());
        }

        voteYellowMeta.setDisplayName(ChatColor.YELLOW + "Team Yellow");
        voteYellowMeta.setLore(voteYellowLore);
        voteYellow.setItemMeta(voteYellowMeta);
        voting.setItem(15, voteYellow);

        player.openInventory(voting);
    }

    //Event which cancels team Damage
    @EventHandler
    public void DamageProtection(EntityDamageByEntityEvent damage){
        HashMap<Player, Long> lastDamage = Cache.getLastDamage();
        long unixTime = System.currentTimeMillis() / 1000L;
        lastDamage.put((Player) damage.getEntity(), unixTime);
        Cache.setLastDamage(lastDamage);

        if (damage.getEntity() instanceof Player && damage.getDamager() instanceof Player) {
            Player damager = (Player) damage.getDamager();
            Player damaged = (Player) damage.getEntity();
            if (TeamSystem.getPlayerTeam(damager, true).equals(TeamSystem.getPlayerTeam(damaged, true))) {
                Vector velocity;
                velocity = damage.getEntity().getVelocity();
                damage.getEntity().setVelocity(velocity);
                damage.setCancelled(true);
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

        String teamName = "§cNot selected";
        HashMap<String, ArrayList<Player>> data = Cache.getTeamMembers();

        for(String key : data.keySet()){
            ArrayList<Player> players = data.get(key);
            if(players.contains(player)){
                if(!raw){
                    teamName = getTeamColour(key);
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
     * A Method that returns the team of the player with the colour as a string.
     * @param team a S
     * @return the colour of the team
     * @author SimsumMC
     */
    public static String getTeamColour(String team) {
        switch(team){
            case "Blue":
                return "§1";
            case "Green":
                return "§2";
            case "Yellow":
                return "§e";
            default:
                return "§4";
        }
    }
}


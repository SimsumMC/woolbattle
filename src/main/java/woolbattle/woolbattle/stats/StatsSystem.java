package woolbattle.woolbattle.stats;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import woolbattle.woolbattle.Cache;
import woolbattle.woolbattle.Main;

import java.util.HashMap;

import static com.mongodb.client.model.Filters.eq;

public class StatsSystem {

    /**
     * A method that adds a perk usage to the cache.
     * @param player - the player that used the perk
     * @author SimsumMC
     */
    public static void addActivePerkUsage(Player player){
        HashMap<Player, HashMap<String, Integer>> playerStats = Cache.getPlayerStats();

        HashMap<String, Integer> stats = playerStats.get(player);

        stats.put("used_perks", (stats.get("used_perks") + 1));

        playerStats.put(player, stats);

        Cache.setPlayerStats(playerStats);
    }

    /**
     * A method that adds the default values in the Cache.
     * @author SimsumMC
     */
    public static void addDefaultStats(){
        HashMap<Player, HashMap<String, Integer>> playerStats = Cache.getPlayerStats();

        for(Player player : Bukkit.getOnlinePlayers()){
            HashMap<String, Integer> stats = new HashMap<String, Integer>(){{
                put("games", 1);
                put("wins",  0);
                put("kills", 0);
                put("deaths", 0);
                put("streaks", 0);
                put("used_perks", 0);
            }};

            playerStats.put(player, stats);
        }

        Cache.setPlayerStats(playerStats);
    }

    /**
     * A method that saves the stats (from Cache) from all players of the round in the database.
     * @author SimsumMC
     */
    public static void saveAllPlayerStats(String winnerTeam){
        HashMap<Player, HashMap<String, Integer>> playerStats = Cache.getPlayerStats();

        for(Player player : Cache.getTeamMembers().get(winnerTeam.substring(2))){
            HashMap<String, Integer> stats = playerStats.get(player);
            stats.put("wins", 1);
            playerStats.put(player, stats);
        }

        for(Player player : playerStats.keySet()){
            savePlayerStats(player, playerStats.get(player));
        }
    }

    /**
     * A method that saves the stats (from Cache) of a given player in the database.
     * @param player - The player that stats get updated in the database
     * @param playerStats - The stats of the player for the last game
     * @author SimsumMC
     */
    public static void savePlayerStats(Player player, HashMap<String, Integer> playerStats){

        int games = playerStats.get("games");
        int wins = playerStats.get("wins");
        int kills = playerStats.get("kills");
        int deaths = playerStats.get("deaths");
        int streaks = playerStats.get("streaks");
        int usedPerks = playerStats.get("used_perks");

        MongoDatabase db = Main.getMongoDatabase();
        MongoCollection<Document> collection = db.getCollection("playerStats");

        Document foundDocument = collection.find(eq("_id", player.getUniqueId().toString())).first();

        if(foundDocument == null){

            HashMap<String, Object> newPlayerStats = new HashMap<String, Object>(){{
                put("_id", player.getUniqueId().toString());
                put("games", games);
                put("wins", wins);
                put("kills", kills);
                put("deaths", deaths);
                put("streaks", streaks);
                put("used_perks", usedPerks);
            }};

            Document document = new Document(newPlayerStats);
            collection.insertOne(document);
        }
        else{
            int totalGames = (int) foundDocument.get("games") + games;
            int totalWins = (int) foundDocument.get("wins") + wins;
            int totalKills = (int) foundDocument.get("kills") + kills;
            int totalDeaths = (int) foundDocument.get("deaths") + deaths;
            int totalStreaks = (int) foundDocument.get("streaks") + streaks;
            int totalUsedPerks = (int) foundDocument.get("used_perks") + usedPerks;

            Bson updates = Updates.combine(
                    Updates.set("games", totalGames),
                    Updates.set("wins", totalWins),
                    Updates.set("kills", totalKills),
                    Updates.set("deaths", totalDeaths),
                    Updates.set("streaks", totalStreaks),
                    Updates.set("used_perks", totalUsedPerks)
            );

            Document query = new Document().append("_id",  player.getUniqueId().toString());

            collection.updateOne(query, updates);
        }
    }

    /**
     * A method that fetches the stats from a given player from the database and returns it as a beautiful string.
     * @param player- The player that stats get returned as a formatted string
     * @author SimsumMC
     */
    public static String getPlayerStatsFormatted(OfflinePlayer player) {
        MongoDatabase db = Main.getMongoDatabase();
        MongoCollection<Document> collection = db.getCollection("playerStats");

        Document foundDocument = collection.find(eq("_id", player.getUniqueId().toString())).first();

        if (foundDocument == null) {
            return ChatColor.RED + "This player has no stats!";
        } else {
            int games = (int) foundDocument.get("games");
            int wins = (int) foundDocument.get("wins");
            int kills = (int) foundDocument.get("kills");
            int deaths = (int) foundDocument.get("deaths");
            int streaks = (int) foundDocument.get("streaks");
            int usedPerks = (int) foundDocument.get("used_perks");
            int winProbability;
            int kd;

            try {
                winProbability = (wins / games) * 100;
            } catch (ArithmeticException e) {
                winProbability = 0;
            }

            try {
                kd = kills / deaths;
            } catch (ArithmeticException e) {
                kd = kills;
            }

            String formattedString = ChatColor.GRAY + "-= " + ChatColor.YELLOW + "Statistics from " + ChatColor.GOLD +
                    player.getName() + ChatColor.GRAY + " (Alltime) =-\n"
                    + "Games: " + ChatColor.YELLOW + games + "\n" + ChatColor.GRAY
                    + "Wins: " + ChatColor.YELLOW + wins + "\n" + ChatColor.GRAY
                    + "Win Probability: " + ChatColor.YELLOW + winProbability + "%" + "\n" + ChatColor.GRAY
                    + "Kills: " + ChatColor.YELLOW + kills + "\n" + ChatColor.GRAY
                    + "Deaths: " + ChatColor.YELLOW + deaths + "\n" + ChatColor.GRAY
                    + "K/D: " + ChatColor.YELLOW + kd + "\n" + ChatColor.GRAY
                    + "Streaks: " + ChatColor.YELLOW + streaks + "\n" + ChatColor.GRAY
                    + "Used Perks Amount: " + ChatColor.YELLOW + usedPerks + "\n" + ChatColor.GRAY
                    + "---------------------------------";

            return formattedString;
        }
    }

}


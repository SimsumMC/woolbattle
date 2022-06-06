package woolbattle.woolbattle.AchievementSystem;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import woolbattle.woolbattle.Cache;
import woolbattle.woolbattle.Config;
import woolbattle.woolbattle.Main;
import woolbattle.woolbattle.lobby.LobbySystem;
import java.util.ArrayList;
import java.util.HashMap;

import static com.mongodb.client.model.Filters.eq;

public class AchievementSystem implements Listener {

    /**
     * A method which, upon being called, checks if the player is qualified to be given
     * the Strategist achievement.
     * @param player - The player which the achievement is to be assigned to
     * @author Beelzebub
     */
    public static void giveFullwool(Player player) {
        MongoDatabase db = Main.getMongoDatabase();
        MongoCollection<Document> collection = db.getCollection("playerAchievements");
        Document query = new Document().append("_id",  player.getUniqueId().toString());
        ArrayList<String> arrayList = (ArrayList<String>) collection.find(eq("_id", player.getUniqueId().toString())).first().get("achievements");
        System.out.println(arrayList);
        if (arrayList.contains("fullwool")) {
            return;
        }
        else if (player.getInventory().contains(Material.WOOL, Config.maxStacks * 64)) {
            player.sendMessage(ChatColor.GREEN + "You just received the 'Strategist' Achievement!");

            Bson updates = Updates.push("achievements", "fullwool");

            collection.updateOne(query, updates);
        }
    }

    /**
     * A method which, upon being called, gives the Player the Dominator achievement
     * (No separate check needed as method is only called when the player is qualified)
     * @param player - The player which the achievement is to be assigned to
     * @author Beelzebub
     */
    public static void giveKillstreak5(Player player) {
        MongoDatabase db = Main.getMongoDatabase();
        MongoCollection<Document> collection = db.getCollection("playerAchievements");
        Document query = new Document().append("_id",  player.getUniqueId().toString());
        ArrayList<String> arrayList = (ArrayList<String>) collection.find(eq("_id", player.getUniqueId().toString())).first().get("achievements");

        if (!LobbySystem.gameStarted) {
            return;
        }
        else if (arrayList.contains("killstreak5")){
            return;
        }
        else {
            player.sendMessage(ChatColor.GREEN + "You just received the 'Dominator' Achievement!");
            arrayList.add("fullwool");
            Bson updates = (Bson) arrayList;

            collection.updateOne(query, updates);
        }
    }

    /**
     * A method which, upon being called, gives the Player the British Way achievement
     * (No separate check needed as method is only called when the player is qualified)
     * @param winner - The team which has won the game
     * @author Beelzebub
     */
    public static void carried(String winner) {
        /* MongoDatabase db = Main.getMongoDatabase();


        for (int i = 0; i<= Cache.getTeamMembers().get(winner).size(); i++) {
            Player player = Cache.getTeamMembers().get(winner).get(i);
            if (Cache.getAchievements().get(player.getUniqueId()).contains("carried")){
                return;
            }
            else if (Cache.getKillStreaks().get(winner).get(player) == 0) {
                HashMap<UUID, ArrayList<String>> currentAchievements = Cache.getAchievements();
                currentAchievements.get(player.getUniqueId()).add("carried");
                Cache.setAchievements(currentAchievements);
                player.sendMessage(ChatColor.GREEN + "You just received the 'British way' Achievement!");
            }
        } */
    }
     /**
     * A method which, upon being called, gives the Player the British Way achievement
     * (No separate check needed as method is only called when the player is qualified)
     * @param winner - The team which has won the game
     * @author Beelzebub
     */
        public static void closeCall(String winner) {
            MongoDatabase db = Main.getMongoDatabase();
            MongoCollection<Document> collection = db.getCollection("playerAchievements");
            for (int i = 0; i <= Cache.getTeamMembers().get(winner).size(); i++){
                Document query = new Document().append("_id",  Cache.getTeamMembers().get(winner).get(i).getUniqueId().toString());
                ArrayList<String> arrayList = (ArrayList<String>) collection.find(eq("_id", Cache.getTeamMembers().get(winner).get(i).getUniqueId().toString())).first().get("achievements");

                if (!LobbySystem.gameStarted) {
                    return;
                }
                else if (arrayList.contains("killstreak5")){
                    return;
                }
                else {
                    Cache.getTeamMembers().get(winner).get(i).sendMessage(ChatColor.GREEN + "You just received the 'Dominator' Achievement!");
                    arrayList.add("fullwool");
                    Bson updates = (Bson) arrayList;

                    collection.updateOne(query, updates);
                }
            }
            Bukkit.broadcastMessage(winner);
    }

    /**
     * A method which puts players into the temporary achievement Hashmap upon them joining,
     * provided they are not yet in said Hashmap
     * @param event - The PlayerJoinEvent
     * @author Beelzebub
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        MongoDatabase db = Main.getMongoDatabase();
        MongoCollection<Document> collection = db.getCollection("playerAchievements");

        Document foundDocument = collection.find(eq("_id", player.getUniqueId().toString())).first();
        if(foundDocument == null){
            HashMap<String, Object> playerData = new HashMap<String, Object>(){{
                put("_id", player.getUniqueId().toString());
                put("achievements", new ArrayList<String>());
            }};
            Document document = new Document(playerData);
            collection.insertOne(document);
        }
    }
}


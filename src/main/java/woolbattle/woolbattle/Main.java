package woolbattle.woolbattle;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import woolbattle.woolbattle.AchievementSystem.AchievementSystem;
import woolbattle.woolbattle.base.Base;
import woolbattle.woolbattle.lives.LivesSystem;
import woolbattle.woolbattle.lobby.LobbySystem;
import woolbattle.woolbattle.lobby.StartGameCommand;
import woolbattle.woolbattle.lobby.StopGameCommand;
import woolbattle.woolbattle.perks.AllActivePerks;
import woolbattle.woolbattle.team.TeamSystem;
import woolbattle.woolbattle.woolsystem.BlockBreakingSystem;
import woolbattle.woolbattle.woolsystem.BlockRegistrationCommand;
import woolbattle.woolbattle.woolsystem.MapBlocksCommand;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static com.mongodb.client.model.Filters.eq;

public final class Main extends JavaPlugin {

    private static Main instance;
    private static final ConnectionString connectionString = new ConnectionString(
            "mongodb://woolbattle:iloveminecraft@cluster0-shard-00-00.eqlbi.mongodb.net:27017," +
            "cluster0-shard-00-01.eqlbi.mongodb.net:27017,cluster0-shard-00-02.eqlbi.mongodb.net:27017/" +
            "myFirstDatabase?ssl=true&replicaSet=atlas-5qmtum-shard-0&authSource=admin&retryWrites=true&w=majority");
    private static final MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .build();
    private static final MongoClient mongoClient = MongoClients.create(settings);
    private static final MongoDatabase db = mongoClient.getDatabase("woolbattle");

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        File file = new File("config.json");

        if(!file.exists()){
            try {
                file.createNewFile();
                Files.write(Paths.get(file.toURI()), Collections.singleton("{" +
                        "\"mapName\": \"Vimo\",\n" +
                        "\"mapSpawn\": [0, 71, 28],\n" +
                        "\"lobbySpawn\": [1000, 100, 1000],\n" +
                        "\"defaultLives\": 10,\n" +
                        "\"startCooldown\": 60,\n" +
                        "\"deathCooldown\": 20,\n" +
                        "\"maxHeight\": 100,\n" +
                        "\"minHeight\": 0,\n" +
                        "\"teamSize\": 1,\n" +
                        "\"teamSpawns\": [[0, 66, 57], [0, 66, 0], [-29, 66, 28], [28, 66, 28]],\n" +
                        "\"givenWoolAmount\": 1,\n" +
                        "\"maxStacks\": 3,\n" +
                        "\"jumpCooldown\": 10,\n" +
                        "\"woolReplaceDelay\" : 10\n" +
                        "}"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // SimsumMC's Things
        Bukkit.getPluginManager().registerEvents(new LobbySystem(), this);
        Bukkit.getPluginManager().registerEvents(new Base(), this);
        Bukkit.getPluginManager().registerEvents(new AllActivePerks(), this);

        this.getCommand("gstart").setExecutor(new StartGameCommand());
        this.getCommand("gstop").setExecutor(new StopGameCommand());

        AllActivePerks.load();

        // Beelzebub's Stuff
        Bukkit.getPluginManager().registerEvents(new TeamSystem(), this);
        Bukkit.getPluginManager().registerEvents(new LivesSystem(), this);
        Bukkit.getPluginManager().registerEvents(new AchievementSystem(), this);

        //Servaturus' belongings
        Bukkit.getPluginManager().registerEvents(new Listener(), this);
        getCommand("blockregistration").setExecutor(new BlockRegistrationCommand());
        getCommand("mapblocks").setExecutor(new MapBlocksCommand());

        Document found = db.getCollection("blockBreaking").find(eq("_id", "mapBlocks")).first();
        if (found == null) {
            db.getCollection("blockBreaking").insertOne(new Document("_id", "mapBlocks").append("mapBlocks", new ArrayList<ArrayList<Double>>()));//append("_id", "mapBlocks"));
        }

        Bukkit.getPluginManager().registerEvents(new Listener(), this);
        getCommand("blockregistration").setExecutor(new BlockRegistrationCommand());
        getCommand("mapblocks").setExecutor(new MapBlocksCommand());
        BlockBreakingSystem.setCollectBrokenBlocks(false);
        BlockBreakingSystem.fetchMapBlocks();
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setAllowFlight(true);
        }

        for (Player player : Bukkit.getOnlinePlayers())
        {
            MongoCollection<Document> collection = db.getCollection("playerAchievements");

            Document foundDocument = collection.find(eq("_id", player.getUniqueId().toString())).first();
            if(foundDocument == null) {
                HashMap<String, Object> playerData = new HashMap<String, Object>() {{
                    put("_id", player.getUniqueId().toString());
                    put("achievements", new ArrayList<String>());
                }};
                Document document = new Document(playerData);
                collection.insertOne(document);
            }
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static Main getInstance(){
        return instance;
    }

    public static MongoDatabase getMongoDatabase() {
        return db;
    }

    public static MongoClient getMongoClient() {
        return mongoClient;
    }

}


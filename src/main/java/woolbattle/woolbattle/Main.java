package woolbattle.woolbattle;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
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

import static com.mongodb.client.model.Filters.eq;

public final class Main extends JavaPlugin {

    private static Main instance;
    private static final ConnectionString connectionString = new ConnectionString(Config.connectionString);
    private static final MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .build();
    private static final MongoClient mongoClient = MongoClients.create(settings);
    private static final MongoDatabase db = mongoClient.getDatabase("woolbattle");

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

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


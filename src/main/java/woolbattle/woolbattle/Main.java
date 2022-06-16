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
import woolbattle.woolbattle.maprestaurationsystem.MapCommand;
import woolbattle.woolbattle.perks.AllActivePerks;
import woolbattle.woolbattle.perks.AllPassivePerks;
import woolbattle.woolbattle.team.TeamSystem;
import woolbattle.woolbattle.woolsystem.BlockBreakingSystem;
import woolbattle.woolbattle.woolsystem.BlockRegistrationCommand;
import woolbattle.woolbattle.woolsystem.MapBlocksCommand;

import java.util.ArrayList;

import static com.mongodb.client.model.Filters.eq;

public final class Main extends JavaPlugin {

    private static Main instance;

    private static MongoClient mongoClient;
    private static MongoDatabase db;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        System.out.println(Config.mongoDatabase);

        ConnectionString connectionString = new ConnectionString(Config.mongoDatabase);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        mongoClient = MongoClients.create(settings);
        db = mongoClient.getDatabase("woolbattle");

        // SimsumMC's Things
        Bukkit.getPluginManager().registerEvents(new LobbySystem(), this);
        Bukkit.getPluginManager().registerEvents(new Base(), this);
        Bukkit.getPluginManager().registerEvents(new AllActivePerks(), this);

        this.getCommand("gstart").setExecutor(new StartGameCommand());
        this.getCommand("gstop").setExecutor(new StopGameCommand());

        AllActivePerks.load();
        AllPassivePerks.load();
        // Beelzebub's Stuff
        Bukkit.getPluginManager().registerEvents(new TeamSystem(), this);
        Bukkit.getPluginManager().registerEvents(new LivesSystem(), this);

        //Servaturus' belongings
        Bukkit.getPluginManager().registerEvents(new Listener(), this);

        getCommand("blockregistration").setExecutor(new BlockRegistrationCommand());
        getCommand("mapblocks").setExecutor(new MapBlocksCommand());
        getCommand("map").setExecutor(new MapCommand());

        Document found = db.getCollection("map").find(eq("_id", "mapBlocks")).first();
        if (found == null) {
            db.getCollection("map").insertOne(new Document("_id", "mapBlocks").append("mapBlocks", new ArrayList<ArrayList<Double>>()));//append("_id", "mapBlocks"));
        }

        BlockBreakingSystem.setCollectBrokenBlocks(false);
        BlockBreakingSystem.fetchMapBlocks();

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setAllowFlight(true);
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
package woolbattle.woolbattle;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import woolbattle.woolbattle.base.Base;
import woolbattle.woolbattle.maprestaurationsystem.*;
import woolbattle.woolbattle.woolsystem.*;
import woolbattle.woolbattle.lobby.*;
import woolbattle.woolbattle.team.*;
import woolbattle.woolbattle.lives.*;
import java.io.*;
import java.util.*;
import java.nio.file.*;

import static com.mongodb.client.model.Filters.eq;

public final class Main extends JavaPlugin {

    private static Main instance;
    /*private static final ConnectionString connectionString = new ConnectionString(
            "mongodb://woolbattle:iloveminecraft@cluster0-shard-00-00.eqlbi.mongodb.net:27017," +
                    "cluster0-shard-00-01.eqlbi.mongodb.net:27017,cluster0-shard-00-02.eqlbi.mongodb.net:27017/" +
=======

    private static final ConnectionString connectionString = new ConnectionString(
            "mongodb://woolbattle:iloveminecraft@cluster0-shard-00-00.eqlbi.mongodb.net:27017,"+
                    "cluster0-shard-00-01.eqlbi.mongodb.net:27017,cluster0-shard-00-02.eqlbi.mongodb.net:27017/"+
>>>>>>> Stashed changes
                    "myFirstDatabase?ssl=true&replicaSet=atlas-5qmtum-shard-0&authSource=admin&retryWrites=true&w=majority");
    private static final MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .build();*/
    private static final MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");/*MongoClients.create(settings);*/
    private static final MongoDatabase db = mongoClient.getDatabase("woolbattle");
    private static final UUID worldUUID = readUUID(new File("/world/uid.dat"));

    public static Main getInstance(){
        return instance;
    }
    public static MongoDatabase getMongoDatabase() {
        return db;
    }
    public static MongoClient getMongoClient() {
        return mongoClient;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        // SimsumMC's Things
        Bukkit.getPluginManager().registerEvents(new LobbySystem(), this);
        Bukkit.getPluginManager().registerEvents(new Base(), this);
        this.getCommand("gstart").setExecutor(new StartGameCommand());
        this.getCommand("gstop").setExecutor(new StopGameCommand());

        // Beelzebub's Stuff
        Bukkit.getPluginManager().registerEvents(new TeamSystem(), this);
        Bukkit.getPluginManager().registerEvents(new LivesSystem(), this);

        //Servaturus' belongings
        Bukkit.getPluginManager().registerEvents(new Listener(), this);
        getCommand("blockregistration").setExecutor(new BlockRegistrationCommand());
        getCommand("mapblocks").setExecutor(new MapBlocksCommand());
        getCommand("map").setExecutor(new MapCommand());

        Document found = db.getCollection("blockBreaking").find(eq("_id", "mapBlocks")).first();
        if (found == null) {
            db.getCollection("blockBreaking").insertOne(new Document("_id", "mapBlocks").append("mapBlocks", new ArrayList<ArrayList<Double>>()));//append("_id", "mapBlocks"));
        }

        Bukkit.getPluginManager().registerEvents(new Listener(), this);
        getCommand("blockregistration").setExecutor(new BlockRegistrationCommand());
        getCommand("mapblocks").setExecutor(new MapBlocksCommand());
        getCommand("mapdefine").setExecutor(new MapCommand());
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

    public static UUID readUUID(File file) {
        try (DataInputStream dataInput = new DataInputStream(new FileInputStream(file))) {
            return new UUID(dataInput.readLong(), dataInput.readLong());
        } catch (IOException e) {
            return null;
        }
    }
    public static UUID getWorldUUID(){
        return worldUUID;
    }

}



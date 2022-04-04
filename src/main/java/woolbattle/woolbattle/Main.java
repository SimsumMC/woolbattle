package woolbattle.woolbattle;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.LoggerFactory;
import woolbattle.woolbattle.woolsystem.BlockBreakingSystem;
import woolbattle.woolbattle.woolsystem.BlockRegistrationCommand;
import woolbattle.woolbattle.woolsystem.MapBlocksCommand;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;

public final class Main extends JavaPlugin {
    private static Main instance;
    private static ConnectionString connectionString = new ConnectionString("mongodb://woolbattle:iloveminecraft@cluster0-shard-00-00.eqlbi.mongodb.net:27017,cluster0-shard-00-01.eqlbi.mongodb.net:27017,cluster0-shard-00-02.eqlbi.mongodb.net:27017/myFirstDatabase?ssl=true&replicaSet=atlas-5qmtum-shard-0&authSource=admin&retryWrites=true&w=majority");
    private static MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .build();
    private static final MongoClient mongoClient = MongoClients.create(settings);
    private final MongoDatabase db = mongoClient.getDatabase("woolbattle");
    private static ObjectId mapBlocksObjectId = new ObjectId();


    public static ObjectId getMapBlocksObjectId() {
        return mapBlocksObjectId;
    }

    public static void setMapBlocksObjectId(ObjectId objectIdArg) {
        mapBlocksObjectId = objectIdArg;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic

        instance = this;

        /*// Disable MongoDB Debug Output **AFTER ENABLING**
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
        rootLogger.setLevel(Level.OFF);*/

        if (!db.listCollectionNames().into(new ArrayList<String>()).contains("blockBreaking")) {
            db.createCollection("blockBreaking");

        } else {
        }

        Document found = db.getCollection("blockBreaking").find(eq("_id", "mapBlocks")).first();
        if (found == null) {
            System.out.println("\n\n\nThere seems to be no document in the indexes with an id of mapBlocks\n\n\n");
            HashMap<String, Object> mapBlocks = new HashMap(){
                {
                    put("mapBlocks", new ArrayList<ArrayList<Double>>());
                    put("_id", "mapBlocks");
                }
            };
            //mapBlocks.remove("_id");
            db.getCollection("blockBreaking").insertOne(new Document("_id", "mapBlocks").append("mapBlocks", new ArrayList<ArrayList<Double>>()));//append("_id", "mapBlocks"));
        } else {
        }
        Bukkit.getPluginManager().registerEvents(new Listener(), this);
        getCommand("blockregistration").setExecutor(new BlockRegistrationCommand());
        getCommand("mapblocks").setExecutor(new MapBlocksCommand());
        BlockBreakingSystem.setCollectBrokenBlocks(false);
        BlockBreakingSystem.fetchMapBlocks();
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setAllowFlight(true);
            //p.setFlying(false);
        }
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
        BlockBreakingSystem.pushMapBlocks();
        HandlerList.unregisterAll();
    }

    public static Main getInstance(){
        return instance;
    }
    public static MongoClient getMongoClient() {
        return mongoClient;
    }

    public static UUID readWorldUID(File file) {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            return new UUID(dis.readLong(), dis.readLong());
        } catch (IOException e) {

            return null;
        }
    }
}
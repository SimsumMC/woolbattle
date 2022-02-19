package woolbattle.woolbattle;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import woolbattle.woolbattle.woolsystem.*;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import static com.mongodb.client.model.Filters.exists;

public final class Main extends JavaPlugin {
    private static Main instance;
    private static final MongoClient mongoClient = new MongoClient("localhost");
    private static ObjectId objectId = new ObjectId();
    private final MongoDatabase db = mongoClient.getDatabase("woolbattle");

    public static ObjectId getObjectId() {
        return objectId;
    }

    public static void setObjectId(ObjectId objectIdArg) {
        objectId = objectIdArg;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic

        instance = this;
        if(!db.listCollectionNames().into(new ArrayList<String>()).contains("blockBreaking")){
            db.createCollection("blockBreaking");

        }else{}

        if(!db.getCollection("blockBreaking").listIndexes().into(new ArrayList<Document>()).contains(new Document("mapBlocks", new ArrayList<>()))){
            db.getCollection("blockBreaking").insertOne(new Document("mapBlocks", new ArrayList<ArrayList<Double>>()));
            objectId = db.getCollection("blockBreaking").find(exists("mapBlocks")).first().getObjectId("_id");
        }else{}
        Bukkit.getPluginManager().registerEvents(new Listener(), this);
        getCommand("blockregistration").setExecutor(new BlockRegistrationCommand());
        getCommand("mapblocks").setExecutor(new MapBlocksCommand());
        BlockBreakingSystem.setCollectBrokenBlocks(false);
        BlockBreakingSystem.fetchMapBlocks();


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
package woolbattle.woolbattle;

import com.mongodb.MongoClient;
import org.bson.BsonDocumentWrapper;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import woolbattle.woolbattle.woolsystem.BlockBreakingSystem;
import woolbattle.woolbattle.woolsystem.DatabaseCommand;
import woolbattle.woolbattle.woolsystem.InitiateBlockRegistrationCommand;
import woolbattle.woolbattle.woolsystem.TerminateBlockRegistrationCommand;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

public final class Main extends JavaPlugin {
    private static Main instance;
    private static final MongoClient mongoClient = new MongoClient("localhost");

    @Override
    public void onEnable() {
        // Plugin startup logic

        Bukkit.getPluginManager().registerEvents(new Listener(), this);
        instance = this;

        Bukkit.getPluginManager().registerEvents(new Listener(),this);
        getCommand("initBlockRegistration").setExecutor(new InitiateBlockRegistrationCommand());
        getCommand("terminateBlockRegistration").setExecutor(new TerminateBlockRegistrationCommand());
        getCommand("listDatabases").setExecutor(new DatabaseCommand());
        BlockBreakingSystem.fetchMapBlocks();
        Document doc = new Document("key", "{ddbs}");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        BlockBreakingSystem.pushMapBlocks();
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
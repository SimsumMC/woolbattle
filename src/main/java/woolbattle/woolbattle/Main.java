package woolbattle.woolbattle;

import com.mongodb.MongoClient;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import woolbattle.woolbattle.woolsystem.*;

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
        instance = this;
        BlockBreakingSystem.setCollectBrokenBlocks(false);
        BlockBreakingSystem.fetchMapBlocks();

        Bukkit.getPluginManager().registerEvents(new Listener(), this);
        getCommand("blockregistration").setExecutor(new BlockRegistrationCommand());
        getCommand("mapblocks").setExecutor(new MapBlocksCommand());
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
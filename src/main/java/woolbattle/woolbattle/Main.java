package woolbattle.woolbattle;

import com.mongodb.MongoClient;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import woolbattle.woolbattle.woolsystem.DatabaseCommand;
import woolbattle.woolbattle.woolsystem.InitiateBlockRegistrationCommand;
import woolbattle.woolbattle.woolsystem.TerminateBlockRegistrationCommand;

public final class Main extends JavaPlugin {
    private static Main instance;

    private static MongoClient mongoClient = new MongoClient("localhost");


    @Override
    public void onEnable() {
        // Plugin startup log

        Bukkit.getPluginManager().registerEvents(new Listener(), this);
        instance = this;
        Bukkit.getPluginManager().registerEvents(new Listener(),this);
        getCommand("initBlockRegistration").setExecutor(new InitiateBlockRegistrationCommand());
        getCommand("terminateBlockRegistration").setExecutor(new TerminateBlockRegistrationCommand());
        getCommand("listDatabases").setExecutor(new DatabaseCommand());


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        
    }
    public static Main getInstance(){
        return instance;
    }
    public static MongoClient getMongoClient() {
        return mongoClient;
    }

    public static void setMongoClient(MongoClient mongoClientArg) {
        mongoClient = mongoClientArg;
    }
}

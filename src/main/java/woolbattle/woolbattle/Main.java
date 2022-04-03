package woolbattle.woolbattle;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.LoggerFactory;
import woolbattle.woolbattle.base.Base;
import woolbattle.woolbattle.lobby.LobbySystem;
import woolbattle.woolbattle.lobby.StartGameCommand;
import woolbattle.woolbattle.lobby.StopGameCommand;

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

        // Disable MongoDB Debug Output **AFTER ENABLING**
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
        rootLogger.setLevel(Level.OFF);

        // SimsumMC's Things
        Bukkit.getPluginManager().registerEvents(new LobbySystem(), this);
        Bukkit.getPluginManager().registerEvents(new Base(), this);
        this.getCommand("gstart").setExecutor(new StartGameCommand());
        this.getCommand("gstop").setExecutor(new StopGameCommand());

        // Beelzebub's Stuff
        Bukkit.getPluginManager().registerEvents(new TeamSystem(), this);
        Bukkit.getPluginManager().registerEvents(new LivesSystem(), this);
        getCommand("teamVote").setExecutor(new TeamSystem());
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

}


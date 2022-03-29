package woolbattle.woolbattle;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private static Main instance;

    @Override
    public void onEnable() {
        // Plugin startup login
        Bukkit.getPluginManager().registerEvents(new LobbySystem(), this);
        this.getCommand("start").setExecutor(new StartCommand());
        this.getCommand("stop").setExecutor(new StopCommand());
        instance = this;


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static Main getInstance(){
        return instance;
    }

}

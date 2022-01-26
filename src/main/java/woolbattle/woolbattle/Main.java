package woolbattle.woolbattle;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private static Main instance;
    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(new Listener(), this);
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

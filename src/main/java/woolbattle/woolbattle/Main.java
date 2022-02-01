package woolbattle.woolbattle;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new TeamSystem(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}

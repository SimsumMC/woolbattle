package woolbattle.woolbattle;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class Main extends JavaPlugin {
    private static Main instance;




    @Override
    public void onEnable() {
        // Plugin startup logic

        Bukkit.getPluginManager().registerEvents(new Listener(), this);
        instance = this;

        Bukkit.getPluginManager().registerEvents(new Listener(),this);
        getCommand("initBlockRegistration").setExecutor(new InitiateBlockRegistrationCommand());
        getCommand("terminateBlockRegistration").setExecutor(new TerminateBlockRegistrationCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        
    }
    public static Main getInstance(){
        return instance;
    }

}

package woolbattle.woolbattle;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import woolbattle.woolbattle.base.Base;
import woolbattle.woolbattle.lobby.LobbySystem;
import woolbattle.woolbattle.lobby.StartGameCommand;
import woolbattle.woolbattle.lobby.StopGameCommand;

public final class Main extends JavaPlugin {

    private static Main instance;

    @Override
    public void onEnable() {
        // Plugin startup login
        Bukkit.getPluginManager().registerEvents(new LobbySystem(), this);
        Bukkit.getPluginManager().registerEvents(new Base(), this);
        this.getCommand("gstart").setExecutor(new StartGameCommand());
        this.getCommand("gstop").setExecutor(new StopGameCommand());
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

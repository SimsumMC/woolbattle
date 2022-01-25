package woolbattle.woolbattle;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;

public class LobbySystem {

    private static boolean gameStarted = false;

    public static boolean isAlreadyStarted(){
        return gameStarted;
    }

    public static void setXPBar() {
        Collection<Player> onlinePlayers = Bukkit.getOnlinePlayers();

    }

}

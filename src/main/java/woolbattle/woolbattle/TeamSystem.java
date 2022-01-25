package woolbattle.woolbattle;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import javax.swing.text.html.parser.Entity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class TeamSystem {
    public TeamSystem(EntityDamageByEntityEvent event){
        String DamagingPlayer = String.valueOf(event.getDamager());
        String DamagedPlayer = String.valueOf(event.getEntity());

        HashMap<Integer, String> players = new HashMap<Integer, String>();
        players.put(1, "randPlayer1");
        players.put(2, "randPlayer2");
        players.put(3, "randPlayer3");
        players.put(4, "randPlayer4");

        for(int amPlayers = players.size(); amPlayers > 0; amPlayers--){
            if(players.get(amPlayers) = ){

            }
        }
    }
}
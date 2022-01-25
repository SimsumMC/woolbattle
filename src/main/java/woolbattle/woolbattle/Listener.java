package woolbattle.woolbattle;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class Listener implements org.bukkit.event.Listener {
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if(!(event.getDamager() instanceof Player) && !(event.getEntity() instanceof Player)){
            return;
        }
        else{
            new TeamSystem(event);

        }
    }
}

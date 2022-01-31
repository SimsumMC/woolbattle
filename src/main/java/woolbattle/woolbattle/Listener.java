package woolbattle.woolbattle;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

public class Listener implements org.bukkit.event.Listener {
    @EventHandler
    public void DamageProtection(EntityDamageByEntityEvent damage){
        if (damage.getEntity() instanceof Player && damage.getDamager() instanceof Player) {
            // if (players.get(damage.getDamager()) == players.get(damage.getEntity())){
            Vector velocity;
            velocity = damage.getEntity().getVelocity();
            damage.setDamage(0.0);
            damage.getEntity().setVelocity(velocity);
            damage.setCancelled(true);

            /* }
            else{
                damage.setCancelled(true);
            } */
        }
        else{
            damage.setCancelled(true);
        }
    }
}

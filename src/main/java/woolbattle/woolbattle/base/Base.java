package woolbattle.woolbattle.base;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.material.Wool;
import woolbattle.woolbattle.Config;

public class Base implements Listener {


    /**
     * An Event that gets executed whenever an entity gets damage to prevent any damage.
     *
     * @param event the EntityDamageEvent
     * @author SimsumMC
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        //disable fall damage
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            event.setCancelled(true);
        }
        else{
            Player player = (Player) event.getEntity();
            player.setHealth(20);
        }
    }

    /**
     * An Event that gets executed whenever a player gets hungry to disable hunger completely.
     *
     * @param event the FoodLevelChangeEvent
     * @author SimsumMC
     */
    @EventHandler
    public void onHungerDeplete(FoodLevelChangeEvent event) {
        // Disables Hunger
        event.setCancelled(true);
        Player player = (Player) event.getEntity();
        player.setFoodLevel(20);
    }

    /**
     * An Event that gets executed whenever a player drops an item to prevent dropping of items.
     *
     * @param event the PlayerDropItemEvent
     * @author SimsumMC
     */
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
            event.setCancelled(true);
        }
    }

    /**
     * An Event that gets executed whenever a block is being placed to prevent placing blocks that are not wool & to
     * execute the max Height.
     *
     * @param event the BlockPlaceEvent
     * @author SimsumMC
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock() instanceof Wool && event.getBlock().getLocation().getY() <= Config.maxHeight || event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        event.setCancelled(true);
    }

    /**
     * An Event that gets executed whenever a player drags an item from one spot to another to prevent stealing items
     * from the lobby items.
     * @param event the InventoryDragEvent event
     * @author SimsumMC
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event){
        event.setCancelled(true);
    }

}
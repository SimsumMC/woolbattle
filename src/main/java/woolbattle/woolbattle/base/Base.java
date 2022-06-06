package woolbattle.woolbattle.base;

import org.bukkit.*;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import woolbattle.woolbattle.Config;
import woolbattle.woolbattle.lobby.LobbySystem;
import woolbattle.woolbattle.team.TeamSystem;

public class Base implements Listener {


    /**
     * An Event that gets executed whenever an entity gets damage to prevent any damage.
     *
     * @param event the EntityDamageEvent
     * @author SimsumMC
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if(!(event.getEntity() instanceof Player)){
            return;
        }
        //disable fall damage
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK && event.getCause() != EntityDamageEvent.DamageCause.PROJECTILE) {
            event.setCancelled(true);
        }
        Player player = (Player) event.getEntity();
        player.setHealth(20);
    }

    /**
    * An Event which changes chat messages upon being typed in order to provide better looking chat messages which also
    * show the team a player is on
    * @param event - the AsyncPlayerChatEvent
    * @author Beelzebub
     */
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        if (LobbySystem.gameStarted) {
            Bukkit.broadcastMessage(TeamSystem.getTeamColour(TeamSystem.getPlayerTeam(event.getPlayer(), true)) + "[" + TeamSystem.getPlayerTeam(event.getPlayer(),
                    false) + "] " + event.getPlayer().getDisplayName() + ChatColor.GRAY + ": " + ChatColor.WHITE + event.getMessage());
        }
        else {
            Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + event.getPlayer().getDisplayName() + ChatColor.GRAY + ": " + ChatColor.WHITE + event.getMessage());
        }
    }

    /**
     * An event that gets executed whenever an entity damages another entity to prevent damaging each other.
     * @param event - the EntityDamageByEntityEvent
     * @author SimsumMC
     */
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event){
        Player player = (Player) event.getEntity();
        player.setHealth(20);
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
        if (event.getBlock().getType() == Material.WOOL && event.getBlock().getLocation().getY() <= Config.maxHeight || event.getPlayer().getGameMode() == GameMode.CREATIVE) {
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

    /**
     * An Event that gets executed whenever the weather changes to prevent the changing of weather.
     * @param event the InventoryDragEvent event
     * @author SimsumMC
     */
    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (!event.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL)) {return;}

        // if ()
        // event.setCancelled(true);
    }
}
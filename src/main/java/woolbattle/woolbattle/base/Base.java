package woolbattle.woolbattle.base;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.projectiles.ProjectileSource;
import woolbattle.woolbattle.Cache;
import woolbattle.woolbattle.Config;
import woolbattle.woolbattle.lobby.LobbySystem;
import woolbattle.woolbattle.team.TeamSystem;

import java.util.ArrayList;
import java.util.HashMap;

public class Base implements Listener {


    /**
     * An Event that gets executed whenever an entity gets damage to prevent any damage.
     *
     * @param event the EntityDamageEvent
     * @author SimsumMC
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK && event.getCause() != EntityDamageEvent.DamageCause.PROJECTILE) {
            event.setCancelled(true);
        } else {
            event.setDamage(0);
        }
    }

    /**
     * An event which changes the chat format to one that looks better
     *
     * @param event the AsyncPlayerChatEvent
     * @author Beelzebub
     */
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        if (LobbySystem.gameStarted) {
            Bukkit.broadcastMessage(TeamSystem.getTeamColour(TeamSystem.getPlayerTeam(event.getPlayer(), true)) + "[" + TeamSystem.getPlayerTeam(event.getPlayer(), false) + "] " + event
                    .getPlayer().getDisplayName() + ChatColor.GRAY + ": " + ChatColor.WHITE + event.getMessage());
        } else {
            Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + event.getPlayer().getDisplayName() + ChatColor.GRAY + ": " + ChatColor.WHITE + event.getMessage());
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
        if (event.getBlock().getType() == Material.WOOL && event.getBlock().getLocation().getY() <= Config.maxHeight || event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        event.setCancelled(true);
    }

    /**
     * An Event that gets executed whenever a player drags an item from one spot to another to prevent stealing items
     * from the lobby items.
     *
     * @param event the InventoryDragEvent event
     * @author SimsumMC
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        event.setCancelled(true);
    }

    /**
     * An Event that gets executed whenever the weather changes to prevent the changing of weather.
     *
     * @param event the InventoryDragEvent event
     * @author SimsumMC
     */
    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        event.setCancelled(true);
    }

    /**
     * A Method that adds the given Ender Pearl Entity to the Cache.
     *
     * @param enderPearl the Ender Pearl Entity
     * @author SimsumMC
     */
    public static void addEnderPearl(EnderPearl enderPearl) {
        ProjectileSource source = enderPearl.getShooter();
        if (source instanceof Player) {
            HashMap<Player, ArrayList<EnderPearl>> enderPearls = Cache.getEnderPearls();
            ArrayList<EnderPearl> playerPearls;
            if (!enderPearls.containsKey((Player) source)) {
                playerPearls = new ArrayList<>();
            } else {
                playerPearls = enderPearls.get((Player) source);
            }

            playerPearls.add(enderPearl);
            enderPearls.put((Player) source, playerPearls);

            Cache.setEnderPearls(enderPearls);
        }
    }

    /**
     * A Method that removes all ender pearls from a given player from the hashmap & from the world.
     *
     * @param player the player that ender pearls get removed
     * @author SimsumMC
     */
    public static void resetEnderPearls(Player player) {
        HashMap<Player, ArrayList<EnderPearl>> enderPearls = Cache.getEnderPearls();

        if (!enderPearls.containsKey(player)) {
            return;
        }

        for (EnderPearl enderPearl : enderPearls.get(player)) {
            enderPearl.remove();
        }

        enderPearls.put(player, new ArrayList<>());
        Cache.setEnderPearls(enderPearls);
    }
}
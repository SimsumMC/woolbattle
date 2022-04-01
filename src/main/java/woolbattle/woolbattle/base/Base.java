package woolbattle.woolbattle.base;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.material.Wool;
import woolbattle.woolbattle.Cache;
import woolbattle.woolbattle.Config;
import woolbattle.woolbattle.lobby.LobbySystem;

import java.util.HashMap;

public class Base implements Listener {


    /**
     * An Event that gets executed whenever an entity gets damage to prevent fall damage.
     * @param event the EntityDamageEvent
     * @author SimsumMC
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event){
        // Disables Fall Damage
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }
    }

    /**
     * An Event that gets executed whenever a player gets hungry to disable hunger completely.
     * @param event the FoodLevelChangeEvent
     * @author SimsumMC
     */
    @EventHandler
    public void onHungerDeplete(FoodLevelChangeEvent event){
        // Disables Hunger
        event.setCancelled(true);
        Player player = (Player) event.getEntity();
        player.setFoodLevel(20);
    }

    /**
     * An Event that gets executed whenever a player drops an item to prevent dropping of items.
     * @param event the PlayerDropItemEvent
     * @author SimsumMC
     */
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event){
        if(event.getPlayer().getGameMode() == GameMode.SURVIVAL){
            event.setCancelled(true);
        }
    }

    /**
     * An Event that gets executed whenever a block is being placed to prevent placing blocks that are not wool.
     * @param event the BlockPlaceEvent
     * @author SimsumMC
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        // TODO: check for max block height
        if(event.getBlock() instanceof Wool || event.getPlayer().getGameMode() == GameMode.CREATIVE){
            return;
        }
        event.setCancelled(true);
    }

    /**
     * A Method that sets a player to a spectator -> game mode & position changes
     * @param player the player
     * @author SimsumMC
     */
    public void setPlayerSpectator(Player player){
        player.teleport(Config.midLocation);
        player.setGameMode(GameMode.SPECTATOR);
    }

    /**
     * An Event that gets executed whenever a Player moves to use it as a kill event when a player gets under a
     * specific y coordinate
     * @param event the PlayerMoveEvent
     * @author SimsumMC
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){
        // TODO: global method to determinate the winner
        Player player = event.getPlayer();
        if (player.getLocation().getY() <= Config.minHeight){
            String team = LobbySystem.getPlayerTeam(player, true);
            HashMap<String, Integer> teamLives = Cache.getTeamLives();
            int lives = teamLives.get(team);
            if(lives==0){
                LobbySystem.removeTeam(player);
                setPlayerSpectator(player);
            }
            else{
                lives -= 1;
                teamLives.put(team, lives);
                Cache.setTeamLives(teamLives);
            }
        }

    }
}

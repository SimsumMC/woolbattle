package woolbattle.woolbattle.map;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Colorable;
import org.bukkit.scheduler.BukkitRunnable;
import woolbattle.woolbattle.Cache;
import woolbattle.woolbattle.Config;
import woolbattle.woolbattle.Main;
import woolbattle.woolbattle.achievements.AchievementSystem;

import java.util.ArrayList;

import static woolbattle.woolbattle.team.TeamSystem.findTeamDyeColor;

public class Listener implements org.bukkit.event.Listener {

    /**
     * @param event The spigot-api's event class, specifying, to which occasion the method is called and delivering
     *              information, concerning these circumstances.
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        Player p = event.getPlayer();
        //Checks, whether the player, having broken the event's block is in the creative, or spectator mode, returns if
        //this is the case
        if (p.getGameMode().equals(GameMode.CREATIVE) || p.getGameMode().equals(GameMode.SPECTATOR)) {
            return;
        }

        AchievementSystem.giveFullwool(p);

        //Internal variables of the plugin, not meant to be modifiable by the end-user

        DyeColor teamColor = findTeamDyeColor(p);//Is to be implemented in the team-system, being created
        Inventory inventory = p.getInventory();
        Block block = event.getBlock();
        ItemStack itemStack = new ItemStack(Material.WOOL, 0, teamColor.getWoolData()) {
        };
        Material type = block.getType();
        boolean blockIsMap = false;
        int itemAmount = 0;

        int givenWoolAmount = Config.givenWoolAmount;
        int maxStacks = Config.maxStacks;
        int delayInTicks = Config.woolReplaceDelay;

        //Checks, whether the event's block is specified in the internal array of map-blocks, writes the value of the operation in the boolean blockIsMap.
        for (Location iterBlock : BlockBreakingSystem.getMapBlocks()) {
            if (iterBlock.equals(block.getLocation())) {
                blockIsMap = true;
                break;
            }
        }

        //Checks, whether a modification of the map's blocks, following the action of breaking a block is to be made.
        // If this is not the case, and if the broken block possesses the wool material as it's type, it is replaced
        // after cooldown and an amount of
        if (BlockBreakingSystem.isCollectBrokenBlocks()) {
            ArrayList<Location> mapBlocks = BlockBreakingSystem.getMapBlocks();
            ArrayList<Location> removedBlocks = BlockBreakingSystem.getRemovedBlocks();

            if (mapBlocks.contains(block.getLocation())) {
                mapBlocks.remove(block.getLocation());
                BlockBreakingSystem.setMapBlocks(mapBlocks);

                removedBlocks.add(block.getLocation());
                BlockBreakingSystem.setRemovedBlocks(removedBlocks);
                Bukkit.broadcastMessage("Removed Blocks: " + BlockBreakingSystem.locArrayToString(BlockBreakingSystem.getRemovedBlocks()));
            }

        } else {
            event.setCancelled(true);
            if (type.equals(Material.WOOL)) {

                for (ItemStack is : inventory) {
                    if (is != null) {
                        itemAmount += is.getType().equals(Material.WOOL) ? is.getAmount() : 0;
                    }
                }

                itemStack.setType(type);

                if (itemAmount < maxStacks * 64) {
                    itemStack.setAmount(givenWoolAmount);
                    inventory.addItem(itemStack);
                    Cache.getPassivePerks().values().forEach(perk -> {

                        if (perk.hasPlayer(p)) {
                            perk.functionality(event);
                        }
                    });
                }

                Colorable data = (Colorable) block.getState().getData();
                block.setType(Material.AIR);

                if (blockIsMap) {

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            block.setType(Material.WOOL);
                            block.setData(data.getColor().getWoolData());
                        }
                    }.runTaskLater(Main.getInstance(), delayInTicks);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerItemDamage(PlayerItemDamageEvent event) {
        event.getItem().setDurability(event.getItem().getType().getMaxDurability());
    }

    /**
     * The block, placed, in case a block-scanning-process is occurring, is, if it is contained by either the array of
     * map blocks or the array of removed blocks, purged from the latter and added to the first one.
     *
     * @param event The spigot-api's event class, specifying, to which occasion the method is called and delivering
     *              information, concerning these circumstances.
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {

        if (BlockBreakingSystem.isCollectBrokenBlocks()) {
            ArrayList<Location> mapBlocks = BlockBreakingSystem.getMapBlocks();
            mapBlocks.add(event.getBlockPlaced().getLocation());

            BlockBreakingSystem.setMapBlocks(mapBlocks);
            ArrayList<Location> removedBlocks = BlockBreakingSystem.getRemovedBlocks();
            removedBlocks.remove(event.getBlock().getLocation());
            Bukkit.broadcastMessage(BlockBreakingSystem.locArrayToString(mapBlocks) + "\n" + BlockBreakingSystem.locArrayToString(removedBlocks));
        }
    }

    /**
     * Method, called in case a player tries to toggle to or out of a flying state of presence. If the player, the
     * method is called upon possesses a gamemode, naturally not granting the permission to fly, their velocity is set
     * to the direction, their facing to, multiplied by a previously specified factor,additionally to this incremented
     * by a given value in the y dimension. To prevent them from flying, as they are granted the permission to do so, if
     * the method is called, their state of movement at first is set to a non-flying state, after which the previously
     * specified edit of their velocity is exerted.
     * After a delay, specified by the config.json file, this permission however is once again granted to them, to allow
     * for another double-jump after this period of time.
     *
     * @param event The spigot-api's event class, specifying, to which occasion the method is called and delivering
     *              information, concerning these circumstances.
     * @author Servaturus
     */
    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player p = event.getPlayer();

        if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR || p.isFlying()) {
            return;
        }

        event.setCancelled(true);

        p.setFlying(false);
        p.setAllowFlight(false);
        p.setVelocity(event.getPlayer().getLocation().getDirection().multiply(1.1).setY(1));

        long jumpCooldown;

        try {
            jumpCooldown = Config.jumpCooldown;
        } catch (ExceptionInInitializerError e) {
            jumpCooldown = 40;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                p.setAllowFlight(true);
            }
        }.runTaskLater(Main.getInstance(), jumpCooldown);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().setFlying(false);
        event.getPlayer().setAllowFlight(true);
    }

    /**
     * Method, called when a player condones a modification to their gamemode. If the gamemode to be switched to by
     * default does not guarantee for the player to be capable of using the movement-option of flying, this permission,
     * in order to allow for the onPlayerToggleFlight method to be called, is given to them.
     *
     * @param event The spigot-api's event class, specifying, to which occasion the method is called and delivering
     *              information, concerning these circumstances.
     * @author Servaturus
     */

    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        Player p = event.getPlayer();
        int delay = 5; //TODO: Introduce customizability to the delay value.

        p.setAllowFlight(true);

        new BukkitRunnable() {
            @Override
            public void run() {
                p.setAllowFlight(true);
            }

        }.runTaskLater(Main.getInstance(), delay);
    }
}
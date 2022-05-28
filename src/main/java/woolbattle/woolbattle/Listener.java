package woolbattle.woolbattle;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.Colorable;
import org.bukkit.scheduler.BukkitRunnable;
import woolbattle.woolbattle.itemsystem.ItemSystem;
import woolbattle.woolbattle.perks.AllPassivePerks;
import woolbattle.woolbattle.perks.PassivePerk;
import woolbattle.woolbattle.woolsystem.BlockBreakingSystem;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import static woolbattle.woolbattle.team.TeamSystem.findTeamDyeColor;

public class Listener implements org.bukkit.event.Listener {

    /**
     * @param event  of the spigot-api's event class, specifying, to which occasion the method is called and delivering
     * information, concerning these circumstances.
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();


        //Checks, whether the player, having broken the event's block is in the creative, or spectator mode, returns if
        //this is the case
        if(p.getGameMode().equals(GameMode.CREATIVE) || p.getGameMode().equals(GameMode.SPECTATOR)){
            return;
        }

        //Internal variables of the plugin, not meant to be modifiable by the end-user

        DyeColor teamColor = findTeamDyeColor(p);//Is to be implemented in the team-system, being created
        Inventory inventory = p.getInventory();
        Block block = event.getBlock();
        ItemStack itemStack = new ItemStack(Material.WOOL, 0, teamColor.getWoolData()){};
        Material type = block.getType();
        boolean blockIsMap = false;
        int itemAmount = 0;

        int givenWoolAmount = 2;
        int maxStacks = 3;
        int delayInTicks = 5;

        //Checks, whether the event's block is specified in the internal array of map-blocks, writes the value of the operation in the boolean blockIsMap.
        for(Location iterBlock : BlockBreakingSystem.getMapBlocks()){
            if(iterBlock.equals(block.getLocation())){
                blockIsMap = true;
                break;
            }
        }

        //Checks, whether a modification of the map's blocks, following the action of breaking a block is to be made.
        // If this is not the case, and if the broken block possesses the wool material as it's type, it is replaced
        // after cooldown and an amount of
        if(BlockBreakingSystem.isCollectBrokenBlocks()){
            ArrayList<Location> mapBlocks = BlockBreakingSystem.getMapBlocks();
            ArrayList<Location> removedBlocks = BlockBreakingSystem.getRemovedBlocks();

            if(mapBlocks.contains(block.getLocation())){
                mapBlocks.remove(block.getLocation());
                BlockBreakingSystem.setMapBlocks(mapBlocks);

                removedBlocks.add(block.getLocation());
                BlockBreakingSystem.setRemovedBlocks(removedBlocks);
                Bukkit.broadcastMessage("Removed Blocks: " + BlockBreakingSystem.locArrayToString(BlockBreakingSystem.getRemovedBlocks()));
            }

        }else{
            event.setCancelled(true);
            if(type.equals(Material.WOOL)){

                for(ItemStack is : inventory){
                    if(is != null){
                        itemAmount += is.getType().equals(Material.WOOL)? is.getAmount() : 0;
                    }
                }

                itemStack.setType(type);

                if(itemAmount < maxStacks*64){
                    itemStack.setAmount(givenWoolAmount);
                    inventory.addItem(itemStack);
                }

                Colorable data = (Colorable) block.getState().getData();
                block.setType(Material.AIR);

                if(blockIsMap){

                    new BukkitRunnable(){
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

        event.getItem().setDurability( event.getItem().getType().getMaxDurability());
    }

    /**
     *  The block, placed, in case a block-scanning-process is occurring, is, if it is contained by either the array of
     *  map blocks or the array of removed blocks, purged from the latter and added to the first one.
     *@param event An instance of a class implementing an implementation  of the spigot-api's event class, specifying, to which occasion the method is called and delivering
     *              information, concerning these circumstances.
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {

        if(BlockBreakingSystem.isCollectBrokenBlocks()){
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
     * @param event An instance of a class implementing an implementation  of the spigot-api's event class, specifying, to which occasion the method is called and delivering
     *              information, concerning these circumstances.
     * @author Servaturus
     */
    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {

        Player p = event.getPlayer();
        p.setFlying(false);
        event.setCancelled(true);

        if(p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR || p.isFlying()){

            event.setCancelled(false);

            p.setFlying(!p.isFlying());
            p.setAllowFlight(true);
            return;
        }
        long jumpCooldown;
        try{
            jumpCooldown = Config.jumpCooldown;
        }catch(ExceptionInInitializerError e){
            jumpCooldown = 40;
        }
        event.setCancelled(true);
        p.setFlying(false);
        p.setAllowFlight(false);
        p.setVelocity(event.getPlayer().getLocation().getDirection().multiply(1.1).setY(1));
        new BukkitRunnable(){
            @Override
            public void run() {
                p.setAllowFlight(true);
            }
        }.runTaskLater(Main.getInstance(), jumpCooldown);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().setAllowFlight(true);
        event.getPlayer().setFlying(false);
    }

    /**
     * Method, called when a player condones a modification to their gamemode. If the gamemode to be switched to by
     * default does not guarantee for the player to be capable of using the movement-option of flying, this permission,
     * in order to allow for the onPlayerToggleFlight method to be called, is given to them.
     *
     * @param event An instance of a class implementing an implementation  of the spigot-api's event class, specifying, to which occasion the method is called and delivering
     *              information, concerning these circumstances.
     * @author Servaturus
     *
     */

    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        Player p = event.getPlayer();
        p.setAllowFlight(true);
        p.setFlying(false);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        //Checking, whether the registered click portraits a right click
        if(event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)){
            event.setCancelled(false);
            return;
        }

        ItemStack is = event.getItem();


        if(is == null){
            event.setCancelled(false);
            return;
        }
        Player player = event.getPlayer();
        PlayerInventory inv = player.getInventory();

        int slot = inv.first(is);
        ArrayList<String> itemNames = new ArrayList<String>() {
            {
                //add(ChatColor.DARK_PURPLE + "Shears");
                add(ChatColor.DARK_PURPLE + "Bow");
                add(ChatColor.DARK_PURPLE + "EnderPearl");
            }
        };

        //Checks, whether the item used is one of the game items, by comparing its name to a list of the game items' names

        if(!itemNames.contains(is.getItemMeta().getDisplayName())){
            event.setCancelled(false);
            return;
        }
        int woolAmount = 0;
        int bowWoolCost = 1;
        for(ItemStack iterStack : inv.getContents()){
            if(iterStack != null && iterStack.getType().equals(Material.WOOL)){
                woolAmount += iterStack.getAmount();
            }
        }
        if(woolAmount-bowWoolCost < 0){
            player.playNote(player.getLocation(), Instrument.PIANO, Note.flat(1, Note.Tone.C));
            player.playNote(player.getLocation(), Instrument.PIANO, Note.flat(1, Note.Tone.B));

            player.getInventory().setItem(slot, is);
            event.setCancelled(true);
        }
        else if(is.getType().equals(Material.ENDER_PEARL)){

            player.getInventory().setItem(slot, is);
            woolAmount = 0;
            int enderPearlWoolCost = 8;
            for(ItemStack iterStack : inv.getContents()){
                if(iterStack != null && iterStack.getType().equals(Material.WOOL)){
                    woolAmount += iterStack.getAmount();
                }
            }
            if(woolAmount-enderPearlWoolCost < 0){

                player.playNote(player.getLocation(), Instrument.PIANO, Note.flat(1, Note.Tone.C));
                player.playNote(player.getLocation(), Instrument.PIANO, Note.flat(1, Note.Tone.B));
                player.getInventory().setItem(slot, is);
                event.setCancelled(true);
                return;
            }

            HashMap<UUID, Long> enderPearlCooldowns = Cache.getEnderPearlCooldowns();

            if(enderPearlCooldowns.get(player.getUniqueId()) == null){
                enderPearlCooldowns.put(player.getUniqueId(), new Date().getTime());
            }else if(new Date().getTime()-enderPearlCooldowns.get(player.getUniqueId())<enderPearlWoolCost){
                player.getInventory().setItem(slot, is);
                event.setCancelled(true);
                return;
            }
            ItemSystem.setItemCooldown(player, slot, is, enderPearlWoolCost);
            player.getInventory().setItem(slot, is);
            enderPearlCooldowns.replace(player.getUniqueId(), new Date().getTime());
            Cache.setEnderPearlCooldowns(enderPearlCooldowns);
            ItemSystem.subtractWool(player, enderPearlWoolCost);

        }else if(is.getType().equals(Material.BOW)) {

            woolAmount = 0;
            for(ItemStack iterStack : inv.getContents()){
                if(iterStack != null && iterStack.getType().equals(Material.WOOL)){
                    woolAmount += iterStack.getAmount();
                }
            }
            if(woolAmount-bowWoolCost < 0){
                player.playNote(player.getLocation(), Instrument.PIANO, Note.flat(1, Note.Tone.C));
                player.playNote(player.getLocation(), Instrument.PIANO, Note.flat(1, Note.Tone.B));

                player.getInventory().setItem(slot, is);
                event.setCancelled(true);

                return;
            }
            HashMap<UUID, Boolean> bowFlags = Cache.getBowFlags();
            if(!bowFlags.containsKey(player.getUniqueId())){

                bowFlags.put(player.getUniqueId(), false);
                Cache.setBowFlags(bowFlags);
            }
            if(bowFlags.get(player.getUniqueId())){
                return;
            }

            bowFlags = Cache.getBowFlags();
            bowFlags.replace(player.getUniqueId(), true);
            Cache.setBowFlags(bowFlags);

        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        int bowWoolCost = 1;
        Projectile projectile = event.getEntity();
        Player player;
        if(!(event.getEntity().getShooter() instanceof Player)){
            return;
        }
        player = (Player) projectile.getShooter();
        if(!projectile.getType().equals(EntityType.ARROW)){
            return;
        }
        HashMap<UUID, Boolean> bowFlags = Cache.getBowFlags();
        if(bowFlags.containsKey(player.getUniqueId())){
            bowFlags.put(player.getUniqueId(), false);
        }else{
            bowFlags.replace(player.getUniqueId(), false);
        }
        Cache.setBowFlags(bowFlags);
        ItemSystem.subtractWool(player, bowWoolCost);
    }

    /**
     * Meant to remove potential entities having hit a block
     *
     * @author Servaturus
     * @param event An instance of a class implementing an implementation of the spigot-api's event class, specifying, to which occasion the method is called and delivering
     *              information, concerning these circumstances
     */
    @EventHandler(ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile proj = event.getEntity();
        if(!proj.getType().equals(EntityType.ARROW)){
            return;
        }
        proj.remove();
    }
}
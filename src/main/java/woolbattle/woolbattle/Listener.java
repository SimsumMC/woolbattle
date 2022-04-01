package woolbattle.woolbattle;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Colorable;
import org.bukkit.scheduler.BukkitRunnable;
import woolbattle.woolbattle.woolsystem.BlockBreakingSystem;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class Listener implements org.bukkit.event.Listener {

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();
        if(player.getGameMode().equals(GameMode.CREATIVE) || player.getGameMode().equals(GameMode.SPECTATOR)){

        }
        ItemStack mainHand = player.getItemInHand();
        if(mainHand.getType().equals(Material.SHEARS)){
            mainHand.setDurability((short) (mainHand.getDurability() -1));
        }else{}

        //Internal variables of the plugin, not meant to be modifiable by the end-user
        DyeColor teamColor = DyeColor.GREEN;//Is to be implemented in the team-system, being created
        Inventory inventory = player.getInventory();
        Block block = event.getBlock();
        ItemStack itemStack = new ItemStack(Material.WOOL, 0, (byte) teamColor.getWoolData()){};
        Material type = block.getType();
        int itemAmount = 0;

        //Variables that are about to be able to be modified by the end-user
        int givenWoolAmount = 1; //Is to be changed, to be modifiable by the user
        int maxStacks = 3;//Too has to be embedded in a command to database system, to be able to customize it through mere minecraft chat
        int delayInTicks= 10;
        boolean blockIsMap = false;

        //Checks
        for(Location iterBlock : BlockBreakingSystem.getMapBlocks()){
            if(iterBlock.equals(block.getLocation())){
                blockIsMap = true;
                break;
            }
        }


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
            /*for(Location iterBlock : BlockBreakingSystem.getMapBlocks()){
                if(event.getBlock().getLocation().equals(iterBlock)){
                    mapBlocks.remove(iterBlock);
                }
            }*/
        }else{
            event.setCancelled(true);
            if(type.equals(Material.WOOL)){

                for(ItemStack is : inventory){
                    if(is != null){
                        itemAmount += is.getType().equals(Material.WOOL)? is.getAmount() : 0;
                    }
                }

                itemStack.setType(type); // The wool, additionally to this, is to be coloured according to the team colour of the player, breaking the block

                if(itemAmount < maxStacks*64){
                    itemStack.setAmount(givenWoolAmount);
                    inventory.addItem(itemStack);
                }else{}

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
    public void onBlockPlace(BlockPlaceEvent event) {

        if(BlockBreakingSystem.isCollectBrokenBlocks()){
            ArrayList<Location> mapBlocks = BlockBreakingSystem.getMapBlocks();
            mapBlocks.add(event.getBlockPlaced().getLocation());

            BlockBreakingSystem.setMapBlocks(mapBlocks);
            ArrayList<Location> removedBlocks = BlockBreakingSystem.getRemovedBlocks();
            if(removedBlocks.contains(event.getBlock().getLocation())){
                removedBlocks.remove(event.getBlock().getLocation());
            }
            Bukkit.broadcastMessage(BlockBreakingSystem.locArrayToString(mapBlocks) + "\n" + BlockBreakingSystem.locArrayToString(removedBlocks));
        }
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {

        int defaultJumpDelay = 2000;
        Player p = event.getPlayer();
        Bukkit.broadcastMessage("Listener is called");
        if(p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR || p.isFlying()){
            event.setCancelled(false);
            return;

        } else {
            event.setCancelled(true);
            p.setFlying(false);

            if(Cache.getJumpCooldown().containsKey(p.getUniqueId())){
                if(new Date().getTime()-Cache.getJumpCooldown().get(p.getUniqueId())<defaultJumpDelay){
                    p.sendMessage(ChatColor.RED + "You are not allowed to use the double jump yet...");
                    p.setAllowFlight(true);
                    p.setFlying(false);
                    return;
                }
                else{

                }
            }else {

            }
            Bukkit.broadcastMessage("Actual double jump fragment");
            p.setVelocity(event.getPlayer().getLocation().getDirection().multiply(1.1).setY(1));
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        p.setAllowFlight(true);
                    }
                }.runTaskLater(Main.getInstance(), 20);
                HashMap<UUID, Long> jumpCooldown = Cache.getJumpCooldown();


                if(jumpCooldown.containsKey(p.getUniqueId())){
                    jumpCooldown.remove(p.getUniqueId());

                }else{

                }
                jumpCooldown.put(p.getUniqueId(), new Date().getTime());
                Cache.setJumpCooldown(jumpCooldown);
            //p.setAllowFlight(true);
            //p.setFlying(false);
            }

        }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().setAllowFlight(true);
        event.getPlayer().setFlying(false);
    }
}
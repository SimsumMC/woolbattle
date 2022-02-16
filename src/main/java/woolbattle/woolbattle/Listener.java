package woolbattle.woolbattle;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Colorable;
import org.bukkit.scheduler.BukkitRunnable;
import woolbattle.woolbattle.woolsystem.BlockBreakingSystem;

import java.util.ArrayList;

public class Listener implements org.bukkit.event.Listener {
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHand = player.getItemInHand();
        if(mainHand.getType().equals(Material.SHEARS)){
            //Bukkit.broadcastMessage(mainHand.getType().toString());
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
                        itemAmount = is.getType().equals(Material.WOOL)? itemAmount+is.getAmount() : itemAmount;
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
        }
    }
}
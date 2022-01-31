package woolbattle.woolbattle;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Colorable;
import org.bukkit.scheduler.BukkitRunnable;

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

        DyeColor teamColor = DyeColor.GREEN;
        Inventory inventory = player.getInventory();
        Block block = event.getBlock();
        ItemStack itemStack = new ItemStack(Material.WOOL, 0, (byte) teamColor.getWoolData()){};
        Material type = block.getType();

        int amount = 1; //Is to be changed, to be modifiable by the user
        int itemAmount = 0;
        int maxStacks = 3;//Too has to be embedded in a command to database system, to be able to customize it through mere minecraft chat
        int delayInTicks = 10;
        boolean blockIsMap = false; //Is going to be changed, to actually check, whether the block broken is part of the map

        for(Block iterBlock : BlockBreakingSystem.getMapBlocks()){
            if(iterBlock.getLocation().equals(block.getLocation())){
                blockIsMap = true;
                break;
            }
        }
        event.setCancelled(true);

        if(type.equals(Material.WOOL)){

            for(ItemStack is : inventory){
                if(is != null){
                    itemAmount = is.getType().equals(Material.WOOL)? itemAmount+is.getAmount() : itemAmount;
                }
            }

            itemStack.setType(type); // The wool, additionally to this, is to be coloured according to the team colour of the player, breaking the block

            if(itemAmount < maxStacks*64){
                itemStack.setAmount(amount);
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
                }.runTaskLater(Main.getInstance(), 10);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {

        if(BlockBreakingSystem.isCollectBrokeBlocks()){
            ArrayList<Block> mapBlocks = BlockBreakingSystem.getMapBlocks();
            mapBlocks.add(event.getBlockPlaced());
            BlockBreakingSystem.setMapBlocks(mapBlocks);
        }
    }
}
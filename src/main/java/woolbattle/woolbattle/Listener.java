package woolbattle.woolbattle;


import org.bukkit.Bukkit;


import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import org.bukkit.scheduler.BukkitRunnable;


public class Listener implements org.bukkit.event.Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        boolean blockIsMap = true; //Is going to be changed, to actually check, whether the block broken is part of the map
        ItemStack itemStack = new ItemStack(){};
        int amount = 1; //Is to be changed, to be modifiable by the user
        int itemAmount = 0;

        short dura = 238;



        if(blockIsMap){

            Material type = event.getBlock().getType();

            if(type.equals(Material.WOOL)){
                event.setCancelled(true);
                /*if(event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.SHEARS)){
                    event.getPlayer().getInventory().getItemInMainHand().setDurability((short) 238);
                }else{}*/




                for(ItemStack is : event.getPlayer().getInventory()){
                    if(is != null){
                        itemAmount = is.getType().equals(Material.WOOL)? itemAmount+is.getAmount() : itemAmount;
                        if(is.getType().equals(Material.SHEARS)){
                            is.setDurability(is.getType().getMaxDurability());
                        }
                    }
                }

                for(ItemStack is : event.getPlayer().getInventory()){
                    if(is != null){
                        itemAmount = is.getType().equals(Material.WOOL)? itemAmount+is.getAmount() : itemAmount;
                    }
                }

                itemStack.setType(type); // The wool, additionally to this, is to be coloured according to the team colour of the player, breaking the block
                if(itemAmount < 192){
                    itemStack.setAmount(amount);
                    event.getPlayer().getInventory().addItem(itemStack);
                }else{}

                event.getBlock().setType(Material.AIR);



                new BukkitRunnable(){
                @Override
                public void run() {
                    event.getBlock().setType(type);
                }
                }.runTaskLater(Main.getInstance(), 10);

                event.getBlock().setType(type);

                /**new BukkitRunnable(){
                    @Override
                    public void run() {

                    }
                }.runTaskLater(, 10);**/


            }
        }
    }
}
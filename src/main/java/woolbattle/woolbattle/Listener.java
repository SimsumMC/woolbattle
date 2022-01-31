package woolbattle.woolbattle;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Colorable;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;

public class Listener implements org.bukkit.event.Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        DyeColor teamColor = DyeColor.BLUE;
        boolean blockIsMap = true; //Is going to be changed, to actually check, whether the block broken is part of the map
        ItemStack itemStack = new ItemStack(){};
        int amount = 1; //Is to be changed, to be modifiable by the user
        int itemAmount = 0;
        int maxStacks = 3;//Too has to be embedded in a command to database system, to be able to customize it through mere minecraft chat
        Player player = event.getPlayer();
        Inventory inventory = player.getInventory();
        Block block = event.getBlock();
        if(blockIsMap){

            Material type = block.getType();
            MaterialData itemData = event.getBlock().getState().getData();
            itemData.setData();
            itemStack.setData(itemData);

            if(type.equals(Material.WOOL)){
                event.setCancelled(true);
                ItemStack mainHand = player.getItemInHand();
                if(mainHand instanceof Damageable){
                    mainHand.setDurability((short) (mainHand.getDurability() -1));
                }else{}

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
}

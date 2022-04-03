package woolbattle.woolbattle;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.HashMap;

public class ItemSystem {
    //Creates the item-meta-specific values that are known, before a potential player is passed into the giveItems
    // method
    private static ItemMeta shearsMeta = new ItemStack(Material.SHEARS).getItemMeta();
    private static ItemMeta bowMeta = new ItemStack(Material.BOW).getItemMeta();

    private static HashMap<Integer, ItemMeta> armorMetas = new HashMap(){
        {
            put(100, new ItemStack(Material.LEATHER_BOOTS).getItemMeta());
            put(101, new ItemStack(Material.LEATHER_LEGGINGS).getItemMeta());
            put(102, new ItemStack(Material.LEATHER_CHESTPLATE).getItemMeta());
            put(103, new ItemStack(Material.LEATHER_HELMET).getItemMeta());
        }
    };

    private static int shearSlot = 0; //Is to be made modifiable by the user.
    private static int bowSlot = 1; //As above.

    //Modifies these values further, once again according to information known befor giveItems is called
    static{
        shearsMeta.addEnchant(Enchantment.KNOCKBACK, 2, true);
        shearsMeta.addEnchant(Enchantment.DURABILITY, 10, true);
        shearsMeta.setUnbreakable(true);

        bowMeta.addEnchant(Enchantment.KNOCKBACK, 2, true);
        bowMeta.addEnchant(Enchantment.DURABILITY, 10, true);
        bowMeta.addEnchant(Enchantment.ARROW_KNOCKBACK, 2, true);
        bowMeta.setUnbreakable(true);

        for(ItemMeta meta : armorMetas.values()){
            meta.addEnchant(Enchantment.DURABILITY, 10, true);
            meta.setUnbreakable(true);
        }
    }

    /**
     * Method to add the game's base items, additionally to this the individual player's perk-items into their
     * inventory, colours colour-able fragments of this equipment according to the player's team-colour.
     * @param p The player to be given the items specified above
     * @author Servaturus
     */
    public static void giveItems(Player p){
        DyeColor color = Cache.findTeamColor(p);
        Inventory playerInv = p.getInventory();

        ItemStack shears = new ItemStack(Material.SHEARS){
            {
                this.setItemMeta(shearsMeta);
            }
        };
        playerInv.setItem(shearSlot, shears);

        ItemStack bow = new ItemStack(Material.SHEARS){
            {
                this.setItemMeta(bowMeta);
            }
        };
        playerInv.setItem(bowSlot, bow);

        for(Integer index : armorMetas.keySet()){
            ItemMeta meta = armorMetas.get(index);
            ItemStack armorPiece = new ItemStack(){
                {
                    setItemMeta(armorMetas.get(index));
                }
            };
            armorPiece.setData(new MaterialData(armorPiece.getType(), color.getDyeData()));
            playerInv.setItem(index, armorPiece);
        }

        addPerkItems(playerInv);
        p.getInventory().setContents(playerInv.getContents());
    }
    //Method, in future to act as an augmentation to the giveItems method. The perks, to be added then should be fetched
    //the plugin's db and added to the player's inventory's slots, specified there respectively.
    private static void addPerkItems(Inventory inv){

    }
    public static void subtractWool(Player p, int amount){
        Inventory inv = p.getInventory();
        int woolAmount = 0;
        ArrayList<ItemStack> woolStacks = new ArrayList<>();
        ItemStack lowestStack = null;
        for(ItemStack iterStack : inv.getContents()){
                if(iterStack.getType().equals(Material.WOOL)){
                    woolStacks.add(iterStack);
                }
        }
        for(ItemStack woolStack : woolStacks){
            if(lowestStack == null){
                lowestStack = woolStack;
            }else if(lowestStack.getAmount()>woolStack.getAmount()){
                lowestStack = woolStack;
            }
        }
    }
}

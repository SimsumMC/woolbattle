package woolbattle.woolbattle;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.HashMap;

import static com.mongodb.client.model.Filters.eq;

public class ItemSystem {
    //Creates the item-meta-specific values that are known, before a potential player is passed into the giveItems
    // method
    private static ItemMeta shearsMeta = new ItemStack(Material.SHEARS).getItemMeta();
    private static ItemMeta bowMeta = new ItemStack(Material.BOW).getItemMeta();

    private static HashMap<Integer, LeatherArmorMeta> armorStacks = new HashMap(){
        {
            put(0, new ItemStack(Material.LEATHER_BOOTS).getItemMeta());
            put(1, new ItemStack(Material.LEATHER_LEGGINGS).getItemMeta());
            put(2, new ItemStack(Material.LEATHER_CHESTPLATE).getItemMeta());
            put(3, new ItemStack(Material.LEATHER_HELMET).getItemMeta());
        }
    };
    private static HashMap<String, Integer> defaultSlots = new HashMap(){
        {
            put("shears", 0);
            put("bow", 1);
            put("enderpearl", 2);
            put("perk1", 3);
            put("perk2", 4);
        }
    };
    private static int shearSlotDefault = 0; //Is to be made modifiable by the user.
    private static int bowSlotDefault = 1; //As above.
    private static int enderpearlSlotDefault = 5;
    private static int perk1SlotDefault = 3;
    private static int perk2SlotDefault = 4;
    //Modifies these values further, once again according to information known befor giveItems is called
    static{
        shearsMeta.addEnchant(Enchantment.KNOCKBACK, 2, true);
        shearsMeta.addEnchant(Enchantment.DURABILITY, 10, true);
        shearsMeta.spigot().setUnbreakable(true);
        shearsMeta.setDisplayName(ChatColor.DARK_PURPLE + "Shears");

        bowMeta.addEnchant(Enchantment.KNOCKBACK, 2, true);
        bowMeta.addEnchant(Enchantment.DURABILITY, 10, true);
        bowMeta.addEnchant(Enchantment.ARROW_KNOCKBACK, 2, true);
        bowMeta.spigot().setUnbreakable(true);
        bowMeta.setDisplayName(ChatColor.DARK_PURPLE + "Bow");
        for(LeatherArmorMeta meta : armorStacks.values()){
            meta.addEnchant(Enchantment.DURABILITY, 10, true);
            meta.spigot().setUnbreakable(true);
        }
    }

    /**
     * Method to add the game's base items, additionally to this the individual player's perk-items into their
     * inventory, colours colour-able fragments of this equipment according to the player's team-colour.
     * @param p The player to be given the items specified above
     * @author Servaturus
     */
    public static void giveItems(Player p){
        Color color = Cache.findTeamColor(p);
        PlayerInventory playerInv = p.getInventory();
        //()playerInv

        MongoCollection collection = Main.getMongoClient().getDatabase("woolbattle").getCollection("playerInventories");
        Document found = (Document) collection.find(eq("_id", "mapBlocks")).first();
        int shearSlot; //Is to be made modifiable by the user.
        int bowSlot ; //As above.
        int enderpearlSlot;
        int perk1Slot;
        int perk2Slot;
        if(found == null){
            shearSlot = defaultSlots.get("shears"); //Is to be made modifiable by the user.
            bowSlot = defaultSlots.get("bow"); //As above.
            enderpearlSlot = defaultSlots.get("enderpearl");
            perk1Slot = defaultSlots.get("perk1");
            perk2Slot = defaultSlots.get("perk2");

        }else{
            shearSlot = (found.get("shear") != null)? (Integer) found.get("shear") : defaultSlots.get("shear");
            bowSlot = (found.get("bow") != null)? (Integer) found.get("bow") : defaultSlots.get("bow");
            enderpearlSlot = (found.get("enderpearl") != null)? (Integer) found.get("enderpearl") : defaultSlots.get("enderpearl");
            perk1Slot = (found.get("perk1") != null)? (Integer) found.get("perk1Slot") : defaultSlots.get("perk1");
            perk2Slot = (found.get("perk") != null)? (Integer) found.get("perk2Slot") : defaultSlots.get("perk2");
        }
        ItemStack shears = new ItemStack(Material.SHEARS){
            {
                this.setItemMeta(shearsMeta);
            }
        };
        playerInv.setItem(shearSlotDefault, shears);

        ItemStack bow = new ItemStack(Material.BOW){
            {
                this.setItemMeta(bowMeta);
            }
        };
        playerInv.setItem(bowSlotDefault, bow);

        ItemStack enderpearl = new ItemStack(Material.ENDER_PEARL){
            {
                ItemMeta meta = getItemMeta();
                meta.setDisplayName(ChatColor.DARK_PURPLE + "EnderPearl");
                setItemMeta(meta);
            }
        };
        playerInv.setItem(enderpearlSlot, enderpearl);
        for(Integer index : armorStacks.keySet()){
            LeatherArmorMeta meta = (LeatherArmorMeta) armorStacks.get(index);
            meta.setColor(color/*color*/);

            ItemStack armorPiece = new ItemStack(){
                {
                    switch(index){
                        case 0:
                            this.setType(Material.LEATHER_BOOTS);

                            break;
                        case 1:
                            this.setType(Material.LEATHER_LEGGINGS);
                            break;
                        case 2:
                            this.setType(Material.LEATHER_CHESTPLATE);
                            break;
                        case 3:
                            this.setType(Material.LEATHER_HELMET);
                            break;

                    }
                    setItemMeta(meta);
                    ((LeatherArmorMeta) getItemMeta()).setColor(color);
                    meta.setDisplayName(ChatColor.DARK_PURPLE + getType().toString());
                    setItemMeta(meta);
                    setAmount(1);
                    //setType(armorStacks.get(index));

                }
            };

            //armorPiece.setData(new MaterialData(armorPiece.getType(), color));
            armorPiece.setAmount(1);
            switch(index){
            case 0:
                playerInv.setBoots(armorPiece);

                break;
            case 1:
                playerInv.setLeggings(armorPiece);
                break;
            case 2:
                playerInv.setChestplate(armorPiece);
                break;
            case 3:
                playerInv.setHelmet(armorPiece);
                break;

            //playerInv.setItem(index, armorPiece);//setItem(index, armorPiece);
            }
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

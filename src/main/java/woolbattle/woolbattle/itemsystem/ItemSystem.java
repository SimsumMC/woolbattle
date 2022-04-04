package woolbattle.woolbattle.itemsystem;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;
import woolbattle.woolbattle.Cache;
import woolbattle.woolbattle.Main;

import java.util.ArrayList;
import java.util.HashMap;

import static com.mongodb.client.model.Filters.eq;
import static woolbattle.woolbattle.team.TeamSystem.findTeamColor;
import static woolbattle.woolbattle.team.TeamSystem.findTeamDyeColor;

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
        bowMeta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
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
        Color color = findTeamColor(p);
        PlayerInventory playerInv = p.getInventory();
        //()playerInv
        playerInv.clear();
        MongoCollection collection = Main.getMongoClient().getDatabase("woolbattle").getCollection("playerInventories");
        Document found = (Document) collection.find(eq("_id", "mapBlocks")).first();
        int shearSlot;
        int bowSlot ;
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
            meta.setColor(color);

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
                }
            };
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
        playerInv.setItem(9, new ItemStack(Material.ARROW));
        addPerkItems(playerInv);
        p.getInventory().setContents(playerInv.getContents());
    }
    //Method, in future to act as an augmentation to the giveItems method. The perks, to be added then should be fetched
    //the plugin's db and added to the player's inventory's slots, specified there respectively.
    private static void addPerkItems(Inventory inv){

    }
    /**
     * Method that subtracts wool from a players inventory.
     * @author Servaturus
     * @param p The player, to remove the wool from
     * @param amount The wool amount to subtract
     */
    public static void subtractWool(Player p, int amount){

        PlayerInventory inv = p.getInventory();
        int woolAmount = 0;
        ArrayList<ItemStack> woolStacks = new ArrayList<>();
        ArrayList<Integer> slots = new ArrayList<>();
        DyeColor color = findTeamDyeColor(p);

        int maximumStacks = 3;
        for(ItemStack is : inv.getContents()){
            if(is != null && is.getType().equals(Material.WOOL)){
                woolAmount += is.getAmount();
            }
        }
        if(woolAmount-amount <=0){
            p.getInventory().remove(Material.WOOL);
            return;
        }
        woolAmount = woolAmount-amount;
        int modulo = woolAmount%64;
        inv.remove(Material.WOOL);
        //int amountToDistribute = woolAmount-amount;
        ItemStack woolInstance =  new ItemStack(Material.WOOL);
        MaterialData materialData = new MaterialData(Material.WOOL);
        materialData.setData(color.getWoolData());
        woolInstance.setData(materialData);
        int iterator =0;
        if(woolAmount%64 !=0){
            ItemStack wool = new ItemStack(woolInstance);
            wool.setAmount(modulo);
            woolStacks.add(new ItemStack(wool));
            woolAmount-=(woolAmount%64);

        }
        for(int i = 0; i<woolAmount/64;i++){
            ItemStack wool = new ItemStack(woolInstance);
            wool.setAmount(64);
            woolStacks.add(wool);
            iterator++;
        }
        p.sendMessage(woolStacks.toString());

        for(ItemStack woolStack : woolStacks){
            inv.addItem(woolStack);
        }

    }
    /**
     * Method that replaces the specified itemSlot with a gunpowder-itemstack, lowering it's amount every second by one,
     * until the specified cooldown has run out, which makes it replace said slot with the original item, illustrated by
     * itemStack passed in.
     * @param p The player, to add a cooldown to one of their items.
     * @param slot The slot, to condone the cooldown in.
     * @param cooldownInSeconds The cooldown's duration.
     * @param item The item, to replace the specified slot with after the cooldown.
     *
     * @author Servaturus
     */
    public static void setItemCooldown(Player p, int slot, ItemStack item, int cooldownInSeconds){
        new BukkitRunnable(){
            @Override
            public void run() {

                int maxLoops = cooldownInSeconds;
                ItemStack expiredItem = new ItemStack(Material.SULPHUR){
                    {
                        ItemMeta meta = getItemMeta();
                        meta.setDisplayName(ChatColor.RED + "Item on Cooldown");
                        setItemMeta(meta);
                    }
                };

                new BukkitRunnable(){

                    volatile int loops = 0;

                    @Override
                    public void run() {
                        if(loops == maxLoops){
                            if(item.getAmount()<1){
                                item.setAmount(item.getAmount() +1);
                                p.getInventory().setItem(slot, item);
                            }
                        }else{
                            expiredItem.setAmount((maxLoops-loops));
                            p.getInventory().setItem(slot,expiredItem);
                            loops++;

                        }
                    }
                }.runTaskTimer(Main.getInstance(), 0, 20);
            }
        }.runTaskAsynchronously(Main.getInstance());
    }

}

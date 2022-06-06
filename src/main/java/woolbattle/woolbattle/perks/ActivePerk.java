package woolbattle.woolbattle.perks;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import woolbattle.woolbattle.Cache;
import woolbattle.woolbattle.Main;
import woolbattle.woolbattle.stats.StatsSystem;

import java.util.HashMap;

import static com.mongodb.client.model.Filters.eq;
import static woolbattle.woolbattle.itemsystem.ItemSystem.*;

public class ActivePerk {
    private ItemStack itemStack;
    private String itemName;
    private String description = "No description provided.";
    private final boolean useOnExecute;
    private final boolean selectable;
    private final int cooldown;
    private final int woolCost;

    public ActivePerk(ItemStack itemStack, int cooldown, int woolCost, boolean useOnExecute, boolean selectable) {
        this.itemStack = itemStack;
        this.itemName = itemStack.getItemMeta().getDisplayName();
        this.useOnExecute = useOnExecute;
        this.cooldown = cooldown;
        this.woolCost = woolCost;
        this.selectable = selectable;
    }

    public ActivePerk(ItemStack itemStack, int cooldown, int woolCost, boolean useOnExecute) {
        this.itemStack = itemStack;
        this.itemName = itemStack.getItemMeta().getDisplayName();
        this.useOnExecute = useOnExecute;
        this.cooldown = cooldown;
        this.woolCost = woolCost;
        this.selectable = true;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack newItemStack) {
        itemStack = newItemStack;
    }

    public int getWoolCost() {
        return woolCost;
    }

    public int getCooldown() {
        return cooldown;
    }

    public boolean getSelectableStatus(){
        return selectable;
    }

    public String getDescription() {
        return description;
    }

    public ActivePerk setDescription(String description) {
        this.description = description;
        return this;
    }

    public ActivePerk setItemName(String name){
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        itemStack.setItemMeta(itemMeta);
        itemName = name;
        return this;
    }

    public ActivePerk addEnchantment(Enchantment enchantment, int level, boolean invisible){
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.addEnchant(enchantment, level, true);

        if(invisible){
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        itemStack.setItemMeta(itemMeta);

        return this;
    }

    public ActivePerk addEnchantment(Enchantment enchantment, boolean invisible){
        return addEnchantment(enchantment,1, invisible);
    }

    public void register(){
        HashMap<String, ActivePerk> activePerks = Cache.getActivePerks();
        activePerks.put(itemName.substring(2), this);
        Cache.setActivePerks(activePerks);
    }

    public void execute(PlayerInteractEvent event, Player player){
        if(!useOnExecute){
            return;
        }

        Inventory inventory = player.getInventory();
        int slot = inventory.first(event.getItem());

        if(!subtractWool(player, woolCost)){
            event.setCancelled(true);
            player.playNote(player.getLocation(), Instrument.PIANO, Note.flat(1, Note.Tone.C));
            player.playNote(player.getLocation(), Instrument.PIANO, Note.flat(1, Note.Tone.B));
            return;
        }

        if(cooldown != 0){
            setItemCooldown(player, slot, itemStack, cooldown);
        }

        onExecute(event, player);

        StatsSystem.addActivePerkUsage(player);
    }

    public void onExecute(PlayerInteractEvent event, Player player) {}

    /**
     * A Method that returns the Slot of the Active Perk.
     * @param player - the player of the perk
     * @author SimsumMC
     */
    public int getSlot(Player player) {
        String activePerkName = this.itemName.substring(2);

        MongoDatabase database = Main.getMongoDatabase();

        MongoCollection<Document> collection = database.getCollection("playerInventories");

        Document foundDocument = collection.find(eq("_id", player.getUniqueId().toString())).first();

        int perk1Slot;
        int perk2Slot;

        if(foundDocument == null){
            perk1Slot = defaultSlots.get("perk1");
            perk2Slot = defaultSlots.get("perk2");
        }
        else{
            perk1Slot = (int) foundDocument.get("active_perk1");
            perk2Slot = (int) foundDocument.get("active_perk2");
        }

        MongoCollection<Document> perksCollection = database.getCollection("playerPerks");

        Document perksDocument = perksCollection.find(eq("_id", player.getUniqueId().toString())).first();

        if(perksDocument != null) {

            String activePerk1String = null;
            String activePerk2String = null;


            if (perksDocument.get("first_active") != null){
                activePerk1String = (String) perksDocument.get("first_active");
            }

            if (perksDocument.get("second_active") != null){
                activePerk2String = (String) perksDocument.get("second_active");
            }

            if(activePerk1String != null && activePerk1String.equals(activePerkName)) {
                return perk1Slot;
            }

            if(activePerk2String != null && activePerk2String.equals(activePerkName)) {
                return perk2Slot;
            }

            return perk1Slot;

        }
        return perk1Slot;
    }


}

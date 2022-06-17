package woolbattle.woolbattle.perks;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import woolbattle.woolbattle.Cache;
import woolbattle.woolbattle.Main;
import woolbattle.woolbattle.stats.StatsSystem;

import java.util.Collection;
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

        int slot = getSlotCache(player);

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
     * A Method that returns the Slot of the Active Perk. -> normally from the cache
     * @param player - the player of the perk
     * @author SimsumMC
     */
    public int getSlotCache(Player player) {
        String activePerkName = this.itemName.substring(2);

        HashMap<Player, HashMap<String, Integer>> activePerkSlots = Cache.getActivePerkSlots();

        if(!activePerkSlots.containsKey(player) || !activePerkSlots.get(player).containsKey(activePerkName)){
            return getSlotDB(player);
        }

        return activePerkSlots.get(player).get(activePerkName);
    }

    /**
     * A Method that returns the Slot of the Active Perk. -> directly from the database
     * @param player - the player of the perk
     * @author SimsumMC
     */
    private int getSlotDB(Player player){
        String activePerkName = this.itemName.substring(2);

        MongoDatabase database = Main.getMongoDatabase();

        MongoCollection<Document> collection = database.getCollection("playerInventories");

        Document foundDocument = collection.find(eq("_id", player.getUniqueId().toString())).first();

        int shearsSlot;
        int bowSlot;
        int enderPearlSlot;
        int perk1Slot;
        int perk2Slot;

        if(foundDocument == null){
            shearsSlot = defaultSlots.get("shears");
            bowSlot = defaultSlots.get("bow");
            enderPearlSlot = defaultSlots.get("enderpearl");
            perk1Slot = defaultSlots.get("perk1");
            perk2Slot = defaultSlots.get("perk2");
        }
        else{
            if(foundDocument.get("active_perk1") instanceof Integer){
                perk1Slot = (int) foundDocument.get("active_perk1");
            }
            else {
                perk1Slot = defaultSlots.get("perk1");
            }

            if(foundDocument.get("active_perk2") instanceof Integer){
                perk2Slot = (int) foundDocument.get("active_perk2");
            }
            else {
                perk2Slot = defaultSlots.get("perk2");
            }

            shearsSlot = (int) foundDocument.get("shears");
            bowSlot = (int) foundDocument.get("bow");
            enderPearlSlot = (int) foundDocument.get("ender_pearl");
        }

        if(!this.selectable){
            if(activePerkName.equals("Shears")){
                return shearsSlot;
            }
            if(activePerkName.equals("Bow")){
                return bowSlot;
            }
            if(activePerkName.equals("Ender Pearl")){
                return enderPearlSlot;
            }
        }

        MongoCollection<Document> perksCollection = database.getCollection("playerPerks");

        Document perksDocument = perksCollection.find(eq("_id", player.getUniqueId().toString())).first();

        if(perksDocument != null) {

            String activePerk1String;
            String activePerk2String;

            if (perksDocument.get("first_active") != null){
                activePerk1String = (String) perksDocument.get("first_active");
                if(activePerk1String.equals(activePerkName)) {
                    return perk1Slot;
                }
            }

            if (perksDocument.get("second_active") != null){
                activePerk2String = (String) perksDocument.get("second_active");
                if(activePerk2String.equals(activePerkName)) {
                    return perk2Slot;
                }
            }
        }
        return perk1Slot;
    }

    /**
     * A Method that puts all the slots from all active perks for every player in a HashMap, in the so-called "Cache"
     * to reduce database calls.
     * @author SimsumMC
     */
    public static void loadActivePerkSlots(){
        MongoDatabase database = Main.getMongoDatabase();

        MongoCollection<Document> collection = database.getCollection("playerInventories");
        MongoCollection<Document> perksCollection = database.getCollection("playerPerks");

        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();

        for(Player player : onlinePlayers){
            Document foundDocument = collection.find(eq("_id", player.getUniqueId().toString())).first();

            int shearsSlot;
            int bowSlot;
            int enderPearlSlot;
            int perk1Slot;
            int perk2Slot;

            if(foundDocument == null){
                shearsSlot = defaultSlots.get("shears");
                bowSlot = defaultSlots.get("bow");
                enderPearlSlot = defaultSlots.get("enderpearl");
                perk1Slot = defaultSlots.get("perk1");
                perk2Slot = defaultSlots.get("perk2");
            }
            else{
                if(foundDocument.get("active_perk1") instanceof Integer){
                    perk1Slot = (int) foundDocument.get("active_perk1");
                }
                else {
                    perk1Slot = defaultSlots.get("perk1");
                }

                if(foundDocument.get("active_perk2") instanceof Integer){
                    perk2Slot = (int) foundDocument.get("active_perk2");
                }
                else {
                    perk2Slot = defaultSlots.get("perk2");
                }

                shearsSlot = (int) foundDocument.get("shears");
                bowSlot = (int) foundDocument.get("bow");
                enderPearlSlot = (int) foundDocument.get("ender_pearl");
            }

            HashMap<String, Integer> playerSlots = new HashMap<>();

            playerSlots.put("Shears", shearsSlot);
            playerSlots.put("Bow", bowSlot);
            playerSlots.put("Ender Pearl", enderPearlSlot);

            Document perksDocument = perksCollection.find(eq("_id", player.getUniqueId().toString())).first();

            if(perksDocument != null) {

                String activePerk1String;
                String activePerk2String;


                if (perksDocument.get("first_active") != null){
                    activePerk1String = (String) perksDocument.get("first_active");
                    playerSlots.put(activePerk1String, perk1Slot);
                }

                if (perksDocument.get("second_active") != null){
                    activePerk2String = (String) perksDocument.get("second_active");
                    playerSlots.put(activePerk2String, perk2Slot);
                }

            }

            HashMap<Player, HashMap<String, Integer>> activePerkSlots = Cache.getActivePerkSlots();
            activePerkSlots.put(player, playerSlots);
            Cache.setActivePerkSlots(activePerkSlots);
        }
    }


}

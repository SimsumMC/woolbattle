package woolbattle.woolbattle.perks;

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

import java.util.HashMap;

import static woolbattle.woolbattle.itemsystem.ItemSystem.setItemCooldown;
import static woolbattle.woolbattle.itemsystem.ItemSystem.subtractWool;

public class ActivePerk {
    private ItemStack itemStack;
    private String itemName;
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

    public ItemStack getItemStack(){
        return itemStack;
    }

    public void setItemStack(ItemStack newItemStack){
        itemStack = newItemStack;
    }

    public int getWoolCost(){
        return woolCost;
    }

    public int getCooldown(){
        return cooldown;
    }

    public boolean getSelectableStatus(){
        return selectable;
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
            player.sendMessage(ChatColor.RED +  "You don't have enough wool to use this item!");
            return;
        }
        if(cooldown != 0){
            setItemCooldown(player, slot, itemStack, cooldown);
        }

        onExecute(event, player);
    }

    public void onExecute(PlayerInteractEvent event, Player player) {}


}

package woolbattle.woolbattle.perks;

import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import woolbattle.woolbattle.Cache;

import java.util.ArrayList;
import java.util.HashMap;

import static woolbattle.woolbattle.itemsystem.ItemSystem.setItemCooldown;
import static woolbattle.woolbattle.itemsystem.ItemSystem.subtractWool;

public class ActivePerk {
    private final ItemStack itemStack;
    private String itemName;
    private final int cooldown;
    private final int woolCost;
    private ArrayList<Action> triggerActions = new ArrayList<>();

    public ActivePerk(ItemStack itemStack, int cooldown, int woolCost) {
        this.itemStack = itemStack;
        this.itemName = itemStack.getItemMeta().getDisplayName();
        this.cooldown = cooldown;
        this.woolCost = woolCost;
    }

    public ItemStack getItemStack(){
        return itemStack;
    }

    public ActivePerk setItemName(String name){
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        itemStack.setItemMeta(itemMeta);
        itemName = name;
        return this;
    }

    public ActivePerk setTriggerActions(ArrayList<Action> newTriggerActions) {
        triggerActions = newTriggerActions;
        return this;
    }

    public ActivePerk addEnchantment(Enchantment enchantment, boolean invisible){
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.addEnchant(enchantment, 1, true);

        if(invisible){
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        itemStack.setItemMeta(itemMeta);

        return this;
    }

    public ActivePerk addEnchantment(Enchantment enchantment){
        return addEnchantment(enchantment, false);
    }

    public void register(){
        HashMap<String, ActivePerk> activePerks = Cache.getActivePerks();
        activePerks.put(itemName.substring(2), this);
        Cache.setActivePerks(activePerks);
    }

    public void execute(PlayerInteractEvent event, Player player){
        event.setCancelled(true);
        System.out.println(event.isCancelled());
        if(!triggerActions.isEmpty() && !triggerActions.contains(event.getAction())){
            return;
        }
        if(!subtractWool(player, woolCost)){
            player.sendMessage(ChatColor.RED +  "You don't have enough wool to use this item!");
            return;
        }

        Inventory inventory = player.getInventory();
        int slot = inventory.first(event.getItem());

        setItemCooldown(player, slot, itemStack, cooldown);

        onExecute(event, player);
    }

    public void onExecute(PlayerInteractEvent event, Player player) {}


}

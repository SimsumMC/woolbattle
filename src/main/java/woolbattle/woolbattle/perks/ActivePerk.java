package woolbattle.woolbattle.perks;

import org.bukkit.ChatColor;
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
    private final ItemStack itemStack;
    private final String itemName;
    private final int cooldown;
    private final int woolCost;
    private final HashMap<Player, Long> playerCooldowns = new HashMap<>();

    public ActivePerk(ItemStack itemStack, int cooldown, int woolCost) {
        this.itemStack = itemStack;
        this.itemName = itemStack.getItemMeta().getDisplayName();
        this.cooldown = cooldown;
        this.woolCost = woolCost;
    }

    public ActivePerk setItemName(String name){
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        itemStack.setItemMeta(itemMeta);
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
        activePerks.put(itemName, this);
        Cache.setActivePerks(activePerks);
    }

    public void execute(PlayerInteractEvent event, Player player){
        long unixTime = System.currentTimeMillis() / 1000L;
        Object playerCooldown = playerCooldowns.get(player);
        if(playerCooldown != null && ((long) playerCooldown - unixTime) < cooldown){
            // item under cooldown
            return;
        }
        if(!subtractWool(player, woolCost)){
            // not enough wool
            return;
        }

        playerCooldowns.put(player, unixTime);

        Inventory inventory = player.getInventory();
        int slot = inventory.first(event.getItem());

        setItemCooldown(player, slot, itemStack, cooldown);

        onExecute(event, player);
    }

    public void onExecute(PlayerInteractEvent event, Player player){
        // default execute
        player.sendMessage(ChatColor.RED + "This Perk is currently disabled!");
    }

}

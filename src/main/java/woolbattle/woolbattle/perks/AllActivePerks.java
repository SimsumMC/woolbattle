package woolbattle.woolbattle.perks;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class AllActivePerks {

    static ArrayList<ActivePerk> allActivePerks = new ArrayList<>();

    public static void load(){

        allActivePerks = new ArrayList<>();

        ActivePerk rescuePlatform = new ActivePerk(new ItemStack(Material.BLAZE_ROD), 15, 20){
            @Override
            public void onExecute(PlayerInteractEvent event, Player player){
                // TODO: place rescue platform
                player.getLocation();
                player.sendMessage(ChatColor.RED + "test");
            }
        }.setItemName("§cRescue Platform").addEnchantment(Enchantment.KNOCKBACK, true);

        allActivePerks.add(rescuePlatform);

        // register all perks
        for(ActivePerk perk : allActivePerks){
            perk.register();
        }
    }

}

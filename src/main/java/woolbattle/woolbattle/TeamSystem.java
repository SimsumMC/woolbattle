package woolbattle.woolbattle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import java.util.ArrayList;
import java.util.Objects;

public class TeamSystem implements Listener, CommandExecutor {

    public int teamLimit = 2;
    public ArrayList<HumanEntity> membersRed = new ArrayList<>();

    @EventHandler
    public void clickEvent(InventoryClickEvent click){
        if (click.getCurrentItem() == null || !Objects.equals(click.getCurrentItem().getItemMeta().getDisplayName(), ChatColor.RED + "Vote for team Red")) return;
        else{
            if (click.getView().getTitle().equals(ChatColor.RED + "Team Voting")) {
                HumanEntity player1 = click.getWhoClicked();
                if (membersRed.contains(click.getWhoClicked())) {
                    click.setCancelled(true);
                    click.getWhoClicked().sendMessage(ChatColor.DARK_RED + "You have already entered this Team!");
                    click.setCancelled(true);
                } else {
                    if (membersRed.size() >= teamLimit) {
                        click.getWhoClicked().sendMessage(ChatColor.RED + "The Team already has " + teamLimit + " Members.");

                    } else {
                        membersRed.add(player1);
                        click.getWhoClicked().sendMessage(String.valueOf(membersRed));
                        click.getWhoClicked().sendMessage(ChatColor.RED + "You have entered Team Red");
                    }
                    click.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void DamageProtection(EntityDamageByEntityEvent damage){
        if (damage.getEntity() instanceof Player && damage.getDamager() instanceof Zombie) {
            Vector velocity;
            velocity = damage.getEntity().getVelocity();
            damage.getEntity().setVelocity(velocity);
            damage.setCancelled(true);
        }
        else return;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){

        if(sender instanceof Player){
            Player player = (Player) sender;
            Inventory voting = Bukkit.createInventory(null, 27, ChatColor.RED + "Team Voting");

            // Adding the "Vote for red" item
            ItemStack voteRed = new ItemStack(Material.WOOL, 1, (byte)14);
            ArrayList<String> voteRedLore = new ArrayList<>();
            ItemMeta voteRedMeta = voteRed.getItemMeta();

            voteRedMeta.setDisplayName(ChatColor.RED + "Vote for team Red");
            voteRedMeta.setLore(voteRedLore);
            voteRed.setItemMeta(voteRedMeta);
            voting.setItem(10, voteRed);

            // Adding the "Vote for blue" item
            ItemStack voteBlue = new ItemStack(Material.WOOL, 1, (byte)11);
            ArrayList<String> voteBlueLore = new ArrayList<>();
            ItemMeta voteBlueMeta = voteRed.getItemMeta();

            voteBlueMeta.setDisplayName(ChatColor.RED + "Vote for team Red");
            voteBlueMeta.setLore(voteBluedLore);
            voteBlue.setItemMeta(voteRedMeta);
            voting.setItem(10, voteRed);
            player.openInventory(voting);
        }

        return true;
    }

}
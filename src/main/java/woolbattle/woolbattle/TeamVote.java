package woolbattle.woolbattle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class TeamVote extends Listener implements CommandExecutor {

    public int teamLimit = 2;
    public ArrayList<HumanEntity> membersRed = new ArrayList<>();

    @EventHandler
    public void clickEvent(InventoryClickEvent click){
        if (click.getCurrentItem() == null) return;
        if (click.getView().getTitle().equals(ChatColor.RED + "Team Voting")){
            HumanEntity player1 = click.getWhoClicked();
            if (membersRed.contains(click.getWhoClicked())) {
                click.setCancelled(true);
                click.getWhoClicked().sendMessage(ChatColor.DARK_RED + "You have already entered this Team!");
                click.setCancelled(true);
            }
            else {
                if (membersRed.size() >= teamLimit){
                    click.getWhoClicked().sendMessage(ChatColor.RED + "The Team already has " + String.valueOf(teamLimit)+" Members.");

                }
                else {
                    membersRed.add(player1);
                    click.getWhoClicked().sendMessage(String.valueOf(membersRed));
                    click.getWhoClicked().sendMessage(ChatColor.RED + "You have entered Team Red");

                    /* if(membersRed.size() == 1){
                    /
                    } */
                }
                click.setCancelled(true);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){

        if(sender instanceof Player){
            Player player = (Player) sender;
            ItemStack voteRed = new ItemStack(Material.WOOL, 1, (byte)14);
            ArrayList<String> voteRedLore = new ArrayList<>();
            Inventory voting = Bukkit.createInventory(null, 27, ChatColor.RED + "Team Voting");
            ItemMeta voteRedMeta = voteRed.getItemMeta();

            voteRedMeta.setDisplayName(ChatColor.RED + "Vote for team Red");
            voteRedMeta.setLore(voteRedLore);
            voteRed.setItemMeta(voteRedMeta);
            voting.setItem(15, voteRed);
            player.openInventory(voting);
        }

        return true;
    }
}

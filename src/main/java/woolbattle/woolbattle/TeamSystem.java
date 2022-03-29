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
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class TeamSystem implements Listener, CommandExecutor {

    public int teamLimit = 2; //temporary

    public ArrayList<HumanEntity> membersRed = new ArrayList<>();
    public ArrayList<HumanEntity> membersBlue = new ArrayList<>();
    public ArrayList<HumanEntity> membersGreen = new ArrayList<>();
    public ArrayList<HumanEntity> membersYellow = new ArrayList<>();

    public HashMap<String, ArrayList> teams = new HashMap<String, ArrayList>(){{
        put("Blue", membersBlue);
        put("Green", membersGreen);
        put("Red", membersRed);
        put("Green", membersYellow);
    }};

    //Event which handles the team selecting
    @EventHandler
    public void clickEvent(InventoryClickEvent click){
        if (click.getCurrentItem() == null) return;
        else if (click.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.RED + "Select team Red")){
            if (click.getView().getTitle().equals(ChatColor.RED + "Team Select")) {
                HumanEntity playerRed = click.getWhoClicked();
                if (membersRed.contains(playerRed)) {
                    click.setCancelled(true);
                    playerRed.sendMessage(ChatColor.DARK_RED + "You have already entered this Team!");
                }
                else if (membersRed.size() >= teamLimit){
                    playerRed.sendMessage(ChatColor.RED + "The Team already has " + teamLimit + " Members.");
                }
                else if(membersBlue.contains(playerRed) || membersGreen.contains(playerRed) || membersYellow.contains(playerRed)) {
                    membersRed.add(playerRed);
                    if (membersBlue.contains(playerRed)) membersBlue.remove(playerRed);
                    else if (membersYellow.contains(playerRed)) membersYellow.remove(playerRed);
                    else if (membersGreen.contains(playerRed)) membersGreen.remove(playerRed);

                    playerRed.sendMessage(ChatColor.RED + "You have entered Team Red");
                }
                else {

                    membersRed.add(playerRed);
                    playerRed.sendMessage(ChatColor.RED + "You have entered Team Red");
                }
                click.setCancelled(true);

            }
        }
        else if (click.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.BLUE + "Select team Blue")){
            if (click.getView().getTitle().equals(ChatColor.RED + "Team Selecting")) {
                HumanEntity playerBlue = click.getWhoClicked();
                if (membersBlue.contains(playerBlue)) {
                    click.setCancelled(true);
                    playerBlue.sendMessage(ChatColor.DARK_RED + "You have already entered this Team!");
                }
                else if (membersBlue.size() >= teamLimit) {
                    playerBlue.sendMessage(ChatColor.RED + "The Team already has " + teamLimit + " Members.");
                }
                else if(membersRed.contains(playerBlue) || membersGreen.contains(playerBlue) || membersYellow.contains(playerBlue)) {
                    membersBlue.add(playerBlue);
                    if (membersRed.contains(playerBlue)) membersRed.remove(playerBlue);
                    else if (membersYellow.contains(playerBlue)) membersYellow.remove(playerBlue);
                    else if (membersGreen.contains(playerBlue)) membersGreen.remove(playerBlue);

                    playerBlue.sendMessage(ChatColor.BLUE + "You have entered Team Blue");
                }
                else {
                    membersBlue.add(playerBlue);
                    playerBlue.sendMessage(ChatColor.BLUE + "You have entered Team Blue");
                }
                click.setCancelled(true);
            }
        }
        else if (click.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Select team Green")) {
            if (click.getView().getTitle().equals(ChatColor.RED + "Team Selecting")) {
                HumanEntity playerGreen = click.getWhoClicked();
                if (membersGreen.contains(playerGreen)) {
                    click.setCancelled(true);
                    playerGreen.sendMessage(ChatColor.DARK_RED + "You have already entered this Team!");
                }
                else if (membersGreen.size() >= teamLimit) {
                    playerGreen.sendMessage(ChatColor.RED + "The Team already has " + teamLimit + " Members.");
                }
                else if(membersRed.contains(playerGreen) || membersBlue.contains(playerGreen) || membersYellow.contains(playerGreen)) {
                    membersGreen.add(playerGreen);
                    if (membersRed.contains(playerGreen)) membersRed.remove(playerGreen);
                    else if (membersYellow.contains(playerGreen)) membersYellow.remove(playerGreen);
                    else if (membersBlue.contains(playerGreen)) membersBlue.remove(playerGreen);

                    playerGreen.sendMessage(ChatColor.GREEN + "You have entered Team Green");
                }
                else {
                    membersGreen.add(playerGreen);
                    playerGreen.sendMessage(ChatColor.GREEN + "You have entered Team Green");
                }
                click.setCancelled(true);
            }
        }
        else if (click.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Select team Yellow")){
            if (click.getView().getTitle().equals(ChatColor.RED + "Team Selecting")) {
                HumanEntity playerYellow = click.getWhoClicked();
                if (membersYellow.contains(playerYellow)) {
                    click.setCancelled(true);
                    playerYellow.sendMessage(ChatColor.DARK_RED + "You have already entered this Team!");
                }
                else if (membersYellow.size() >= teamLimit) {
                    playerYellow.sendMessage(ChatColor.RED + "The Team already has " + teamLimit + " Members.");
                }
                else if(membersRed.contains(playerYellow) || membersGreen.contains(playerYellow) || membersBlue.contains(playerYellow)) {
                    membersYellow.add(playerYellow);
                    if (membersRed.contains(playerYellow)) membersRed.remove(playerYellow);
                    else if (membersBlue.contains(playerYellow)) membersBlue.remove(playerYellow);
                    else if (membersGreen.contains(playerYellow)) membersGreen.remove(playerYellow);

                    playerYellow.sendMessage(ChatColor.YELLOW + "You have entered Team Yellow");
                }
                else {
                    membersYellow.add(playerYellow);
                    playerYellow.sendMessage(ChatColor.YELLOW + "You have entered Team Yellow");
                }
                click.setCancelled(true);
            }
        }
        else if (Objects.equals(click.getCurrentItem().getItemMeta().getDisplayName()," " ) && click.getView().getTitle().equals(ChatColor.RED + "Team Selecting")) {
            click.setCancelled(true);
        }

    }

    //Event which Opens Team Selection GUI upon clicking item
    @EventHandler
    public void onClick (PlayerInteractEvent click){
        if (Objects.equals(click.getItem().getItemMeta().getDisplayName(),ChatColor.DARK_PURPLE + "Select Team" )){

            HumanEntity player = click.getPlayer();
            Inventory voting = Bukkit.createInventory(null, 27, ChatColor.RED + "Team Selecting");

            //adding glass
            ItemStack Glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte)15);
            ItemMeta GlassMeta = Glass.getItemMeta();
            GlassMeta.setDisplayName(" ");
            Glass.setItemMeta(GlassMeta);

            for (int i = 0; i<= 26; i++) {
                voting.setItem(i, Glass);
            }
            // Adding the "Vote for red" item
            ItemStack voteRed = new ItemStack(Material.WOOL, 1, (byte)14);
            ArrayList<String> voteRedLore = new ArrayList<>();
            ItemMeta voteRedMeta = voteRed.getItemMeta();

            voteRedMeta.setDisplayName(ChatColor.RED + "Select team Red");
            voteRedMeta.setLore(voteRedLore);
            voteRed.setItemMeta(voteRedMeta);
            voting.setItem(11, voteRed);

            // Adding the "Vote for blue" item
            ItemStack voteBlue = new ItemStack(Material.WOOL, 1, (byte)11);
            ArrayList<String> voteBlueLore = new ArrayList<>();
            ItemMeta voteBlueMeta = voteBlue.getItemMeta();

            voteBlueMeta.setDisplayName(ChatColor.BLUE + "Select team Blue");
            voteBlueMeta.setLore(voteBlueLore);
            voteBlue.setItemMeta(voteBlueMeta);
            voting.setItem(12, voteBlue);

            // Adding the "Vote for Green" item
            ItemStack voteGreen = new ItemStack(Material.WOOL, 1, (byte)5);
            ArrayList<String> voteGreenLore = new ArrayList<>();
            ItemMeta voteGreenMeta = voteGreen.getItemMeta();

            voteGreenMeta.setDisplayName(ChatColor.GREEN + "Select team Green");
            if (membersGreen.size() == 1){
                String hello = String.valueOf(membersGreen.get(1));
                voteGreenLore.add(hello);
            }
            voteGreenMeta.setLore(voteGreenLore);
            voteGreen.setItemMeta(voteGreenMeta);
            voting.setItem(14, voteGreen);

            // Adding the "Vote for Yellow" item
            ItemStack voteYellow = new ItemStack(Material.WOOL, 1, (byte)4);
            ArrayList<String> voteYellowLore = new ArrayList<>();
            ItemMeta voteYellowMeta = voteYellow.getItemMeta();

            voteYellowMeta.setDisplayName(ChatColor.YELLOW + "Select team Yellow");
            voteYellowMeta.setLore(voteYellowLore);
            voteYellow.setItemMeta(voteYellowMeta);
            voting.setItem(15, voteYellow);

            player.openInventory(voting);
        }
    }

    //Event which cancels team Damage
    @EventHandler
    public void DamageProtection(EntityDamageByEntityEvent damage){
        if (damage.getEntity() instanceof Player && damage.getDamager() instanceof Player) {
            HumanEntity damager = (HumanEntity) damage.getDamager();
            HumanEntity damaged = (HumanEntity) damage.getEntity();
            if (membersRed.contains(damager) && membersRed.contains(damaged) || membersGreen.contains(damager) && membersGreen.contains(damaged) || membersYellow.contains(damager) && membersYellow.contains(damaged) || membersBlue.contains(damager) && membersBlue.contains(damaged)) {
                Vector velocity;
                velocity = damage.getEntity().getVelocity();
                damage.getEntity().setVelocity(velocity);
                damage.setCancelled(true);
            }
        }
        else return;
    }

    //temporary event which adds item to inventory upon typing a command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(sender instanceof Player){
            Player player = (Player) sender;

            ItemStack teamSelect = new ItemStack(Material.COMPASS, 1);
            ArrayList<String> teamSelectLore = new ArrayList<>();
            ItemMeta teamSelectMeta = teamSelect.getItemMeta();

            teamSelectMeta.setDisplayName(ChatColor.DARK_PURPLE + "Select Team");
            teamSelectMeta.setLore(teamSelectLore);
            teamSelect.setItemMeta(teamSelectMeta);


            player.getInventory().setItem(8, teamSelect);
        }
        return true;
    }
}
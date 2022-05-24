package woolbattle.woolbattle.perks;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import woolbattle.woolbattle.Cache;

import java.util.ArrayList;
import java.util.HashMap;

import static woolbattle.woolbattle.itemsystem.ItemSystem.setItemCooldown;
import static woolbattle.woolbattle.itemsystem.ItemSystem.subtractWool;
import static woolbattle.woolbattle.team.TeamSystem.findTeamDyeColor;

public class AllActivePerks implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        Player player;
        if(!(event.getEntity().getShooter() instanceof Player)){
            return;
        }

        player = (Player) projectile.getShooter();

        if(projectile.getType() == EntityType.SNOWBALL || projectile.getType() == EntityType.ENDER_PEARL ||  projectile.getType() == EntityType.ARROW){
            String perkName;
            if(projectile.getType() ==  EntityType.SNOWBALL){
                perkName = "Exchanger";
            }
            else if(projectile.getType() == EntityType.ARROW){
                perkName = "Bow";
            }
            else{
                perkName = "Ender Pearl";
            }
            ActivePerk perk = Cache.getActivePerks().get(perkName);
            ItemStack itemStack = perk.getItemStack();
            itemStack.setAmount(1);
            int woolCost = perk.getWoolCost();

            int cooldown = perk.getCooldown();
            int perkSlot = 0;

            if(!(projectile.getType() == EntityType.ARROW)){
                player.getInventory().addItem(itemStack);
                perkSlot = player.getInventory().first(perk.getItemStack());
                player.getInventory().removeItem(itemStack);
            }

            if(!(projectile.getType() == EntityType.ARROW)) {
                player.getInventory().setItem(perkSlot, itemStack);
            }

            if(!subtractWool(player, woolCost)){
                event.setCancelled(true);
                projectile.remove();
                player.playNote(player.getLocation(), Instrument.PIANO, Note.flat(1, Note.Tone.C));
                player.playNote(player.getLocation(), Instrument.PIANO, Note.flat(1, Note.Tone.B));
                player.sendMessage(ChatColor.RED +  "You don't have enough wool to use this item!");
            }
            else{
                if(cooldown != 0) {
                    setItemCooldown(player, perkSlot, itemStack, cooldown);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if(!(event.getDamager() instanceof Projectile)){
            return;
        }
        Projectile projectile = (Projectile) event.getDamager();
        if(projectile.getType() == EntityType.SNOWBALL) {
            event.setCancelled(true);
            Player shooterPlayer;
            if (!(projectile.getShooter() instanceof Player)) {
                return;
            }
            shooterPlayer = (Player) projectile.getShooter();

            Player hitPlayer;

            if(!(event.getEntity() instanceof Player)){
                return;
            }
            hitPlayer = (Player) event.getEntity();

            Location hitPlayerLocation = hitPlayer.getLocation();
            Location shooterPlayerLocation = shooterPlayer.getLocation();

            shooterPlayer.teleport(hitPlayerLocation);
            hitPlayer.teleport(shooterPlayerLocation);

            projectile.remove();
        }

    }

    /**
     * The PlayerMoveEvent Event is duplicated here for the jump Platform perk.
     * @param event the PlayerMoveEvent event
     * @author SimsumMC
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        Location playerLocation = player.getLocation();

        Location location = playerLocation.clone().subtract(0, 1, 0);

        Block block = location.getBlock();
        ArrayList<Block> nearbyWoolBlocks = new ArrayList<>();

        nearbyWoolBlocks.add(block);

        if(block.getType() != Material.WOOL){

            World world = playerLocation.getWorld();
            double x = location.getX();
            double y = location.getY();
            double z = location.getZ();

            nearbyWoolBlocks.add(new Location(world,x-1, y, z).getBlock());
            nearbyWoolBlocks.add(new Location(world,x-1, y, z).getBlock());
            nearbyWoolBlocks.add(new Location(world,x-1, y, z-1).getBlock());
            nearbyWoolBlocks.add(new Location(world, x, y, z+1).getBlock());
            nearbyWoolBlocks.add(new Location(world, x, y, z).getBlock());
            nearbyWoolBlocks.add(new Location(world, x,y, z-1).getBlock());
            nearbyWoolBlocks.add(new Location(world,x+1, y, z+1).getBlock());
            nearbyWoolBlocks.add(new Location(world,x+1, y, z).getBlock());
            nearbyWoolBlocks.add(new Location(world,x+1, y, z-1).getBlock());
            nearbyWoolBlocks.removeIf(nearbyBlock -> nearbyBlock.getType() != Material.WOOL);
        }

        HashMap<Player, ArrayList<ArrayList<Block>>> jumpPlatformBlocks = Cache.getJumpPlatformBlocks();

        if(!jumpPlatformBlocks.containsKey(player)){
            return;
        }

        ArrayList<ArrayList<Block>> playerBlocks = jumpPlatformBlocks.get(player);

        if(playerBlocks.isEmpty()){
            return;
        }

        for(ArrayList<Block> array : playerBlocks){
            for(Block nearbyBlock : nearbyWoolBlocks) {
                if (array.contains(nearbyBlock)) {
                    for (Block existingJumpBlock : array) {
                        existingJumpBlock.setType(Material.AIR);
                    }

                    player.setVelocity(location.getDirection().multiply(1).setY(3));

                }
            }

        }

    }

    public static void load(){

        ActivePerk shears = new ActivePerk(new ItemStack(Material.SHEARS), 0, 0, false, false)
                .setItemName(ChatColor.AQUA + "Shears")
                .addEnchantment(Enchantment.DIG_SPEED, 5, false)
                .addEnchantment(Enchantment.DURABILITY, 10, false)
                .addEnchantment(Enchantment.KNOCKBACK, 5, false);

        ItemStack shearsItemStack = shears.getItemStack();
        ItemMeta shearsItemMeta = shearsItemStack.getItemMeta();
        shearsItemMeta.spigot().setUnbreakable(true);
        shearsItemStack.setItemMeta(shearsItemMeta);
        shears.setItemStack(shearsItemStack);

        shears.register();

        ActivePerk bow = new ActivePerk(new ItemStack(Material.BOW), 0, 1, false, false)
                .setItemName(ChatColor.AQUA + "Bow")
                .addEnchantment(Enchantment.DURABILITY, 10, false)
                .addEnchantment(Enchantment.KNOCKBACK, 5, false)
                .addEnchantment(Enchantment.ARROW_KNOCKBACK, 5, false)
                .addEnchantment(Enchantment.ARROW_INFINITE, 1, false);

        ItemStack bowItemStack = bow.getItemStack();
        ItemMeta bowItemMeta = bowItemStack.getItemMeta();
        bowItemMeta.spigot().setUnbreakable(true);
        bowItemStack.setItemMeta(bowItemMeta);
        bow.setItemStack(bowItemStack);

        bow.register();

        ActivePerk enderPearl = new ActivePerk(new ItemStack(Material.ENDER_PEARL), 5, 5, false, false)
                .setItemName(ChatColor.AQUA + "Ender Pearl").addEnchantment(Enchantment.DURABILITY, true);

        enderPearl.register();

        ActivePerk rescuePlatform = new ActivePerk(new ItemStack(Material.BLAZE_ROD), 15, 15, true){
            @Override
            public void onExecute(PlayerInteractEvent event, Player player){
                Location playerLocation = player.getLocation();
                DyeColor teamColor = findTeamDyeColor(player);

                World world = playerLocation.getWorld();
                double x = playerLocation.getX();
                double y = playerLocation.getY();
                double z = playerLocation.getZ();

                ArrayList<Location> locations = new ArrayList<Location>(){{
                    add(new Location(world, x, y -5, z));
                    add(new Location(world, x, y -5, z+1));
                    add(new Location(world, x, y -5, z+2));
                    add(new Location(world, x, y -5, z-1));
                    add(new Location(world, x, y -5, z-2));
                    add(new Location(world, x+1, y -5, z));
                    add(new Location(world, x+1, y -5, z+1));
                    add(new Location(world, x+1, y -5, z+2));
                    add(new Location(world, x+1, y -5, z-1));
                    add(new Location(world, x+1, y -5, z-2));
                    add(new Location(world, x+2, y -5, z));
                    add(new Location(world, x+2, y -5, z+1));
                    add(new Location(world, x+2, y -5, z-1));
                    add(new Location(world, x-1, y -5, z));
                    add(new Location(world, x-1, y -5, z+1));
                    add(new Location(world, x-1, y -5, z+2));
                    add(new Location(world, x-1, y -5, z-1));
                    add(new Location(world, x-1, y -5, z-2));
                    add(new Location(world, x-2, y -5, z));
                    add(new Location(world, x-2, y -5, z+1));
                    add(new Location(world, x-2, y -5, z-1));
                }};

                for(Location location : locations){
                    Block block = location.getBlock();
                    Material material = block.getType();
                    if(material != Material.AIR){
                        continue;
                    }
                    block.setType(Material.WOOL);
                    block.setData(teamColor.getWoolData());
                }
            }
        }.setItemName(ChatColor.AQUA + "Rescue Platform").addEnchantment(Enchantment.DURABILITY, true);

        rescuePlatform.register();

        ActivePerk exchanger = new ActivePerk(new ItemStack(Material.SNOW_BALL), 15, 10, false)
                .setItemName(ChatColor.AQUA + "Exchanger").addEnchantment(Enchantment.DURABILITY, true);
        //no onExecute method here, see onProjectileLaunch event

        exchanger.register();

        ActivePerk knockbackStick = new ActivePerk(new ItemStack(Material.STICK), 0, 0, false)
                .setItemName(ChatColor.AQUA + "Knockback Stick")
                .addEnchantment(Enchantment.KNOCKBACK,100, false);

        knockbackStick.register();

        ActivePerk jumpPlatform = new ActivePerk(new ItemStack(Material.SLIME_BALL), 15, 25, true){
            @Override
            public void onExecute(PlayerInteractEvent event, Player player){
                Location playerLocation = player.getLocation();
                DyeColor teamColor = findTeamDyeColor(player);

                World world = playerLocation.getWorld();
                double x = playerLocation.getX();
                double y = playerLocation.getY();
                double z = playerLocation.getZ();

                ArrayList<Location> locations = new ArrayList<Location>(){{
                    add(new Location(world, x, y -5, z));
                    add(new Location(world, x, y -5, z+1));
                    add(new Location(world, x, y -5, z-1));
                    add(new Location(world, x+1, y -5, z));
                    add(new Location(world, x-1, y -5, z));
                }};

                HashMap<Player, ArrayList<ArrayList<Block>>> jumpPlatformBlocks = Cache.getJumpPlatformBlocks();

                jumpPlatformBlocks.put(player, null);

                ArrayList<ArrayList<Block>> playerBlocks = new ArrayList<>();

                ArrayList<Block> newPlayerBlocks = new ArrayList<>();

                for(Location location : locations){
                    Block block = location.getBlock();
                    Material material = block.getType();

                    if(material != Material.AIR){
                        continue;
                    }

                    newPlayerBlocks.add(block);

                    block.setType(Material.WOOL);
                    block.setData(teamColor.getWoolData());
                }

                playerBlocks.add(newPlayerBlocks);

                jumpPlatformBlocks.put(player, playerBlocks);

                Cache.setJumpPlatformBlocks(jumpPlatformBlocks);

            }
        }.setItemName(ChatColor.AQUA + "Jump Platform").addEnchantment(Enchantment.DURABILITY, true);

        jumpPlatform.register();
    }

}

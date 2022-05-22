package woolbattle.woolbattle.perks;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;
import woolbattle.woolbattle.Cache;
import woolbattle.woolbattle.lobby.LobbySystem;

import java.util.ArrayList;
import java.util.Collection;

import static woolbattle.woolbattle.itemsystem.ItemSystem.setItemCooldown;
import static woolbattle.woolbattle.itemsystem.ItemSystem.subtractWool;
import static woolbattle.woolbattle.lobby.LobbySystem.getActivePerkSlot;
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

        if(projectile.getType() == EntityType.SNOWBALL){
            ActivePerk exchanger = Cache.getActivePerks().get("Exchanger");

            ItemStack itemStack = exchanger.getItemStack();
            itemStack.setAmount(1);
            int woolCost = exchanger.getWoolCost();
            int cooldown = exchanger.getCooldown();
            int exchangerSlot = getActivePerkSlot(player, "Exchanger");

            if(!subtractWool(player, woolCost)){
                projectile.remove();
                player.getInventory().addItem(itemStack);
                player.sendMessage(ChatColor.RED +  "You don't have enough wool to use this item!");
            }
            else{
                setItemCooldown(player, exchangerSlot, itemStack, cooldown);
            }

        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if(projectile.getType() == EntityType.SNOWBALL) {
            // Exchanger
            Player shooterPlayer;
            if (!(projectile.getShooter() instanceof Player)) {
                return;
            }
            shooterPlayer = (Player) projectile.getShooter();

            Entity hitEntity;

            try{
                hitEntity = event.getHitEntity();
            }catch (NoSuchMethodError error){
                Collection<Entity> nearbyEntities = Bukkit.getWorlds().get(0).getNearbyEntities(shooterPlayer.getLocation(), 0.25, 0.25, 0.25);
                if(nearbyEntities.isEmpty()){
                    return;
                }
                hitEntity = nearbyEntities.iterator().next();
            }


            Player hitPlayer;

            if(!(hitEntity instanceof Player)){
                return;
            }
            hitPlayer = (Player) hitEntity;

            Location hitPlayerLocation = hitPlayer.getLocation();
            Location shooterPlayerLocation = shooterPlayer.getLocation();

            shooterPlayer.teleport(hitPlayerLocation);
            hitPlayer.teleport(shooterPlayerLocation);

            projectile.remove();
        }

    }

    public static void load(){

        ActivePerk rescuePlatform = new ActivePerk(new ItemStack(Material.BLAZE_ROD), 15, 20, true){
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
        }.setItemName(ChatColor.RED + "Rescue Platform").addEnchantment(Enchantment.KNOCKBACK, true);

        rescuePlatform.register();

        ActivePerk exchanger = new ActivePerk(new ItemStack(Material.SNOW_BALL), 10, 10, false)
                .setItemName(ChatColor.WHITE + "Exchanger").addEnchantment(Enchantment.KNOCKBACK, true);
        //no onExecute method here, see onProjectileLaunch event

        exchanger.register();
    }

}

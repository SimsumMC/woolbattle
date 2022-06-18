package woolbattle.woolbattle.perks;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import woolbattle.woolbattle.Cache;
import woolbattle.woolbattle.stats.StatsSystem;

import java.util.ArrayList;
import java.util.HashMap;

import static woolbattle.woolbattle.base.Base.addEnderPearl;
import static woolbattle.woolbattle.itemsystem.ItemSystem.setItemCooldown;
import static woolbattle.woolbattle.itemsystem.ItemSystem.subtractWool;
import static woolbattle.woolbattle.lives.LivesSystem.teleportPlayerTeamSpawn;
import static woolbattle.woolbattle.team.TeamSystem.*;

public class AllActivePerks implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        if(!(event.getEntity().getShooter() instanceof Player)){
            return;
        }

        Player player = (Player) projectile.getShooter();

        if(projectile.getType() == EntityType.SNOWBALL || projectile.getType() == EntityType.ENDER_PEARL ||
                projectile.getType() == EntityType.ARROW || projectile.getType() == EntityType.EGG){
            String perkName;
            if(projectile.getType() ==  EntityType.SNOWBALL){
                perkName = "Exchanger";
            }
            else if(projectile.getType() == EntityType.ARROW){
                perkName = "Bow";
            }
            else if(projectile.getType() == EntityType.EGG){
                perkName = "Egg";
            }
            else{
                perkName = "Ender Pearl";
            }

            ActivePerk perk = Cache.getActivePerks().get(perkName);
            ItemStack itemStack = perk.getItemStack();
            itemStack.setAmount(1);

            int woolCost = perk.getWoolCost();
            int cooldown = perk.getCooldown();
            int perkSlot = perk.getSlotCache(player);

            if(!(projectile.getType() == EntityType.ARROW)) {
                player.getInventory().setItem(perkSlot, itemStack);
            }

            if(!subtractWool(player, woolCost)){
                event.setCancelled(true);
                projectile.remove();
                player.playNote(player.getLocation(), Instrument.PIANO, Note.flat(1, Note.Tone.C));
                player.playNote(player.getLocation(), Instrument.PIANO, Note.flat(1, Note.Tone.B));
            }
            else{
                if(cooldown != 0) {
                    setItemCooldown(player, perkSlot, itemStack, cooldown);
                    StatsSystem.addActivePerkUsage(player);
                }

                if(perkName.equals("Ender Pearl")){
                    addEnderPearl((EnderPearl) projectile);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if(!(event.getEntity() instanceof Player)){
            return;
        }
        if(event.getDamager() instanceof Player){
            Player player = (Player) event.getDamager();
            Player damagedPlayer = (Player) event.getEntity();
            if(player.getItemInHand().hasItemMeta() && player.getItemInHand().getItemMeta().getDisplayName().substring(2).equals("Duel")){
                event.setCancelled(true);
                ActivePerk perk = Cache.getActivePerks().get("Duel");

                ItemStack itemStack = perk.getItemStack();
                itemStack.setAmount(1);
                int perkSlot = perk.getSlotCache(player);
                player.getInventory().setItem(perkSlot, itemStack);

                HashMap<Player, Player> playerDuels = Cache.getPlayerDuels();

                if(playerDuels.containsKey(damagedPlayer)){
                    String damagedPlayerDuelName = getTeamColour(getPlayerTeam(playerDuels.get(damagedPlayer), true)) + damagedPlayer.getDisplayName();
                    player.sendMessage(
                            ChatColor.RED +  "This player is already in a duel with " + damagedPlayerDuelName + ChatColor.RED + "!");
                    return;
                }

                if(playerDuels.containsKey(player)){
                    String playerDuelName = getTeamColour(getPlayerTeam(playerDuels.get(player), true)) + player.getDisplayName();
                    player.sendMessage(
                            ChatColor.RED +  "You are already in a duel with " + playerDuelName + ChatColor.RED + "!");
                    return;
                }

                int woolCost = perk.getWoolCost();
                int cooldown = perk.getCooldown();

                if(!subtractWool(player, woolCost)){
                    event.setCancelled(true);
                    player.playNote(player.getLocation(), Instrument.PIANO, Note.flat(1, Note.Tone.C));
                    player.playNote(player.getLocation(), Instrument.PIANO, Note.flat(1, Note.Tone.B));
                    return;
                }
                else{
                    setItemCooldown(player, perkSlot, itemStack, cooldown);

                    playerDuels.put(damagedPlayer, player);
                    playerDuels.put(player, damagedPlayer);

                    Cache.setPlayerDuels(playerDuels);

                    String playerName = getTeamColour(getPlayerTeam(player, true)) + player.getDisplayName();
                    String damagedPlayerName = getTeamColour(getPlayerTeam(damagedPlayer, true)) + damagedPlayer.getDisplayName();

                    player.sendMessage(ChatColor.GOLD + "You are now in a duel with " + damagedPlayerName + ChatColor.GOLD + "!");
                    damagedPlayer.sendMessage(ChatColor.GOLD + "You are now in a duel with " + playerName + ChatColor.GOLD + "!");

                    StatsSystem.addActivePerkUsage(player);
                }

            }
        }
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

            hitPlayer = (Player) event.getEntity();

            Location hitPlayerLocation = hitPlayer.getLocation();
            Location shooterPlayerLocation = shooterPlayer.getLocation();

            shooterPlayer.teleport(hitPlayerLocation);
            hitPlayer.teleport(shooterPlayerLocation);

            projectile.remove();
        }
        if(projectile.getType() == EntityType.EGG) {
            Player player = (Player) event.getEntity();
            Vector velocity = player.getVelocity().multiply(2);
            player.setVelocity(velocity);
        }

    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        if (event.getState() == PlayerFishEvent.State.FAILED_ATTEMPT || event.getState() == PlayerFishEvent.State.IN_GROUND) {
            FishHook hook = event.getHook();
            ActivePerk perk = Cache.getActivePerks().get("Grappling Hook");
            ItemStack itemStack = perk.getItemStack();

            int woolCost = perk.getWoolCost();
            int cooldown = perk.getCooldown();
            int perkSlot;

            perkSlot = perk.getSlotCache(player);

            if(!subtractWool(player, woolCost)){
                event.setCancelled(true);
                hook.remove();
                player.playNote(player.getLocation(), Instrument.PIANO, Note.flat(1, Note.Tone.C));
                player.playNote(player.getLocation(), Instrument.PIANO, Note.flat(1, Note.Tone.B));
                return;
            }
            else{
                if(cooldown != 0) {
                    setItemCooldown(player, perkSlot, itemStack, cooldown);
                }
            }

            Location playerLocation = player.getLocation();
            Location hookLocation = hook.getLocation();
            Vector vector = new Vector(hookLocation.getX() - playerLocation.getX(), 1.0, hookLocation.getZ() - playerLocation.getZ());
            player.setVelocity(vector);

            StatsSystem.addActivePerkUsage(player);
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

    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if(event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.EGG){
            event.setCancelled(true);
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

        ActivePerk rescuePlatform = new ActivePerk(new ItemStack(Material.BLAZE_ROD), 15, 25, true){
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
        }.setItemName(ChatColor.AQUA + "Rescue Platform").addEnchantment(Enchantment.DURABILITY, true)
         .setDescription("Places blocks under you.");

        rescuePlatform.register();

        ActivePerk exchanger = new ActivePerk(new ItemStack(Material.SNOW_BALL), 15, 10, false)
                .setItemName(ChatColor.AQUA + "Exchanger").addEnchantment(Enchantment.DURABILITY, true)
                .setDescription("Swap your Location with another player.");
        //no onExecute method here, see onProjectileLaunch event

        exchanger.register();

        ActivePerk knockbackStick = new ActivePerk(new ItemStack(Material.STICK), 0, 0, false)
                .setItemName(ChatColor.AQUA + "Knockback Stick")
                .addEnchantment(Enchantment.KNOCKBACK,100, false)
                .setDescription("Best weapon in the game.");

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
        }.setItemName(ChatColor.AQUA + "Jump Platform").addEnchantment(Enchantment.DURABILITY, true)
         .setDescription("Boosts yourself up.");

        jumpPlatform.register();

        ActivePerk grapplingHook = new ActivePerk(new ItemStack(Material.FISHING_ROD), 5, 5, false)
                .setItemName(ChatColor.AQUA + "Grappling Hook").addEnchantment(Enchantment.DURABILITY, true)
                .setDescription("Helps you go fast from one point to another.");

        grapplingHook.register();

        ActivePerk homeTeleport = new ActivePerk(new ItemStack(Material.WATCH), 30, 25, true){
            @Override
            public void onExecute(PlayerInteractEvent event, Player player) {
                teleportPlayerTeamSpawn(player);
            }
        }.setItemName(ChatColor.AQUA + "Home Teleport").addEnchantment(Enchantment.DURABILITY, true)
         .setDescription("Teleports you home.");

        homeTeleport.register();

        ActivePerk rescuePod = new ActivePerk(new ItemStack(Material.FEATHER), 15, 30, true){
            @Override
            public void onExecute(PlayerInteractEvent event, Player player){
                Location playerLocation = player.getLocation();
                DyeColor teamColor = findTeamDyeColor(player);

                World world = playerLocation.getWorld();
                double x = playerLocation.getX();
                double y = playerLocation.getY();
                double z = playerLocation.getZ();

                ArrayList<Location> locations = new ArrayList<Location>(){{
                    add(new Location(world, x, y -1, z));
                    add(new Location(world, x, y+2 , z));
                    add(new Location(world, x, y , z+1));
                    add(new Location(world, x, y , z-1));
                    add(new Location(world, x+1, y , z));
                    add(new Location(world, x-1, y , z));
                    add(new Location(world, x, y+1, z+1));
                    add(new Location(world, x, y+1, z-1));
                    add(new Location(world, x+1, y+1, z));
                    add(new Location(world, x-1, y+1, z));
                }};

                player.teleport(new Location(world, x, y, z));

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
        }.setItemName(ChatColor.AQUA + "Rescue Pod").addEnchantment(Enchantment.DURABILITY, true)
         .setDescription("Places blocks around you.");

        rescuePod.register();

        ActivePerk duel = new ActivePerk(new ItemStack(Material.WOOD_SWORD), 30, 10, false)
                .setItemName(ChatColor.AQUA + "Duel").addEnchantment(Enchantment.DURABILITY, true)
                .setDescription("Puts you in a 1V1 with the hit player.");

        duel.register();

        ActivePerk Egg = new ActivePerk(new ItemStack(Material.EGG), 0, 1, false)
                .setItemName(ChatColor.AQUA + "Egg").addEnchantment(Enchantment.DURABILITY, true)
                .setDescription("Basically a Bow for Eggs.");

        Egg.register();

    }

}

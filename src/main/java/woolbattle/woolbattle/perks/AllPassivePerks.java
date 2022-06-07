package woolbattle.woolbattle.perks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.IllegalPluginAccessException;
import woolbattle.woolbattle.Config;
import woolbattle.woolbattle.Main;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static woolbattle.woolbattle.team.TeamSystem.findTeamDyeColor;

public class AllPassivePerks {
    public static ArrayList<PassivePerk<? extends Event, ?>> passivePerks = new ArrayList<>();

    public static void load(){

        PassivePerk<BlockEvent, BlockBreakEvent> woolMultiplication = new PassivePerk<BlockEvent, BlockBreakEvent>(
                new ItemStack(Material.EMERALD),
                ChatColor.AQUA + "Wool Duplication",
                false
        ){
            int factor = (4)-1; //TODO: Implement in the config (one nth of the amount to give to the receiving player has allready been given)

            ItemStack temp = new ItemStack(Material.WOOL, Config.givenWoolAmount*(factor));
            @Override
            public <S extends Event, H extends S> void functionality(H event) {
                assert event instanceof BlockBreakEvent;
                Player p = ((BlockBreakEvent) event).getPlayer();
                ItemStack wool = temp;
                wool.setData(new MaterialData(findTeamDyeColor(p).getWoolData()));
                AtomicInteger amount = new AtomicInteger();

                p.getInventory().spliterator().tryAdvance(itemStack -> amount.addAndGet(itemStack.getAmount()));
                if((amount.get() + Config.givenWoolAmount*(factor) >= Config.maxStacks *64)){
                    wool.setAmount((Config.maxStacks*64 - amount.get()));
                }
                p.getInventory().addItem(wool);
            }
        };
        woolMultiplication.addPlayer(Bukkit.getPlayer("Serva7urus"));
        woolMultiplication.register();

        for(PassivePerk<? extends Event, ?> perk : passivePerks){
            try{
                Bukkit.getPluginManager().registerEvents(perk, Main.getInstance());
            }catch(IllegalPluginAccessException ignored){

            }
        }
    }

    public static ArrayList<PassivePerk<? extends Event, ?>> getPassivePerks() {return passivePerks;}
    public static void setPassivePerks(ArrayList<PassivePerk<? extends Event, ?>> passivePerks) {AllPassivePerks.passivePerks = passivePerks;}

    public static ArrayList<PassivePerk<? extends Event, ?>> getPassivePerkByClass(Class<? extends Event> clazz){
        ArrayList<PassivePerk<? extends Event, ?>> result = new ArrayList<>();

        for(PassivePerk<? extends Event, ?> passivePerk : passivePerks){
            if(passivePerk.getType().equals(clazz)){
                continue;
            }
            result.add(passivePerk);
        }
        return result;
    }
}

package woolbattle.woolbattle.perks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import woolbattle.woolbattle.Main;

import java.util.ArrayList;

public abstract class PassivePerk<G extends Event, E extends G> implements Listener {
    private int delay = 0;
    private final ItemStack item;
    private final String name;
    private boolean overwriteEvent = false;
    G g = (G) new Event() {
        @Override
        public HandlerList getHandlers() {
            return null;
        }
    };
    E e = (E) g;
    private final Class<E> type =  (Class<E>) e.getClass();

    private ArrayList<Player> players = new ArrayList<>();



    public PassivePerk(ItemStack item, String name, int delayInTicks){
        this.delay = delayInTicks;
        this.item = item;
        this.name = name;
    }

    public PassivePerk (ItemStack item, String name, boolean overwriteEvent){
        this.name = name;
        this.item = item;
        this.overwriteEvent = overwriteEvent;
    }

    public void functionality(){

    }

    @EventHandler
    public  <H extends Event, S extends H> void functionality(S event){

    }

    public final void loop(){
        if(delay == 0){
            return;
        }

        new BukkitRunnable(){
            @Override
            public void run() {
                functionality();
            }
        }.runTaskTimer(Main.getInstance(), delay, 0);
    }

    public final void register(){
        ArrayList<PassivePerk<? extends Event, ?>> passivePerks = AllPassivePerks.getPassivePerks();
        if(passivePerks.contains(this)){
            return;
        }
        passivePerks.add(this);
        AllPassivePerks.setPassivePerks(passivePerks);
    }



    public Class<E> getType() {
        return type;
    }

    public String getName() {return name;}
    public ItemStack getItem() {return item;}
    public int getDelay() {return delay;}
    public boolean isOverwriteEvent() {return overwriteEvent;}

    public ArrayList<Player> getPlayers(){return players;}
    public void addPlayer(Player p){players.add(p);}
    public void removePlayer(Player p){players.remove(p);}
    public boolean hasPlayer(Player p){return players.contains(p);}
}
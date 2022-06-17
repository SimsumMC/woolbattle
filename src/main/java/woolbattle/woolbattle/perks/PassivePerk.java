package woolbattle.woolbattle.perks;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import woolbattle.woolbattle.Cache;
import woolbattle.woolbattle.Main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class PassivePerk<G extends Event, E extends G> implements Listener {
    private int delay = 0;
    private final ItemStack item;
    private final String name;
    private boolean overwriteEvent = false;
    private final String description;
    G g = (G) new Event() {
        @Override
        public HandlerList getHandlers() {
            return null;
        }
    };
    E e = (E) g;
    private final Class<E> type =  (Class<E>) e.getClass();

    private ArrayList<Player> players = new ArrayList<>();

    public PassivePerk(ItemStack item, String name, int delayInTicks, String description){
        this.delay = delayInTicks;
        this.name = name;
        this.item = item;
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        this.description = description;

        MongoDatabase db = Main.getMongoDatabase();
        MongoCollection<Document> collection = db.getCollection("playerPerks");
        collection.listIndexes().forEach((Consumer) document -> {
            assert document instanceof Document;

            if(((Document) document).get("passive").equals(name)){
                players.add(Bukkit.getPlayer((UUID) ((Document) document).get("_id")));
            }
        });
    }

    public PassivePerk (ItemStack item, String name, boolean overwriteEvent, String description){
        this.name = name;
        this.item = item;
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        this.overwriteEvent = overwriteEvent;
        this.description = description;

        MongoDatabase db = Main.getMongoDatabase();
        MongoCollection<Document> collection = db.getCollection("playerPerks");
        collection.listIndexes().forEach((Consumer) document -> {
            assert document instanceof Document;

            if(((Document) document).get("passive") != null && ((Document) document).get("passive").equals(name)){
                players.add(Bukkit.getPlayer((UUID) ((Document) document).get("_id")));
            }
        });
    }

    public void functionality(){

    }

    public  <H extends Event, S extends H> void functionality(S event){}

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

    public final void register() {
        HashMap<String, PassivePerk<? extends Event, ?>> passivePerks = Cache.getPassivePerks();
        if (passivePerks.containsKey(this.name)) {
            passivePerks.replace(this.name.substring(2), this);
            return;
        }
        passivePerks.put(this.name.substring(2), this);
        Cache.setPassivePerks(passivePerks);
    }


    public Class<E> getType() {
        return type;
    }

    public String getName() {return name;}
    public ItemStack getItem() {return item;}
    public int getDelay() {return delay;}
    public boolean isOverwriteEvent() {return overwriteEvent;}

    public ArrayList<Player> getPlayers(){return players;}
    public void setPlayers(ArrayList<Player> players) {this.players = players;}

    public boolean hasPlayer(Player p){return players.contains(p);}

    public String getDescription(){
        return description;
    }
}
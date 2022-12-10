/**
 MIT License

 Copyright (c) 2022-present SimsumMC, Servaturus and Flashtube

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

package woolbattle.woolbattle.perks;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.IllegalPluginAccessException;
import woolbattle.woolbattle.Cache;
import woolbattle.woolbattle.Config;
import woolbattle.woolbattle.Main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static woolbattle.woolbattle.team.TeamSystem.findTeamDyeColor;

public class AllPassivePerks {
    private static final PassivePerk<BlockEvent, BlockBreakEvent> woolMultiplication = new PassivePerk<BlockEvent, BlockBreakEvent>(
            new ItemStack(Material.EMERALD),
            ChatColor.AQUA + "Wool Duplication",
            false,
            "Increases the amount of wool you get"
    ){
        final int factor = (2)-1;

        @Override
        public <S extends Event, H extends S> void functionality(H event) {
            assert event instanceof BlockBreakEvent;
            Player p = ((BlockBreakEvent) event).getPlayer();
            ItemStack wool = new ItemStack(Material.WOOL, Config.givenWoolAmount*factor, findTeamDyeColor(p).getWoolData());
            AtomicInteger amount = new AtomicInteger();

            p.getInventory().spliterator().tryAdvance(itemStack -> amount.addAndGet(itemStack.getAmount()));

            if(amount.get() == (Config.maxStacks * 64)){
                return;
            }
            else if (amount.get() + (Config.givenWoolAmount * (factor)) >(Config.maxStacks * 64)) {
                wool.setAmount(((Config.maxStacks * 64)-amount.get()));
            }

            p.getInventory().addItem(wool);
        }
    };

    /**Method setting up the system of passive perks. Over the course of the method, instances of the passive perk are added to the HashMap of
     * passive perks in Cache.java and assigned to potential owners through a query
     * toward the db respectively
     * @author Servaturus
     */

    public static void load(){
        woolMultiplication.register();
        assignPlayersToPerks();
    }

    public static void assignPlayersToPerks(){
        MongoDatabase db = Main.getMongoDatabase();
        MongoCollection<Document> collection = db.getCollection("playerPerks");
        HashMap<String, PassivePerk<? extends Event, ?>> passivePerks = Cache.getPassivePerks();;
        HashMap<String, PassivePerk<? extends Event, ?>> newPassivePerks = new HashMap<>();
        FindIterable<Document> iterable = collection.find();
        MongoCursor<Document> cursor  = iterable.iterator();

        for(PassivePerk<? extends Event, ?> perk : passivePerks.values()){
            ArrayList<Player> players = new ArrayList<>();
            //iterates over the elements of the fetched document, creates a Player object through the stored information
            //, additionally to that stores players in possession of the perk to its respective instance's array of players.
            try{
                while(cursor.hasNext()){
                    Document document = cursor.next();

                    if(document.get("passive") == null || !document.get("passive").equals(perk.getName().substring(2))){
                        continue;
                    }

                    Player player = Bukkit.getPlayer(UUID.fromString((String) document.get("_id")));

                    if(document.get("passive").equals(perk.getName().substring(2)) && player != null){
                        players.add(player);
                    }
                }
            }finally{
                cursor.close();
            }

            perk.setPlayers(players);

            newPassivePerks.put(perk.getName().substring(2), perk);

            if(perk.isOverwriteEvent()){
                try{
                    Bukkit.getPluginManager().registerEvents(perk, Main.getInstance());

                }catch(IllegalPluginAccessException ignored){}
            }
        }

        Cache.setPassivePerks(newPassivePerks);
    }
}
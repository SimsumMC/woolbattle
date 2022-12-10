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

package woolbattle.woolbattle.achievements;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import woolbattle.woolbattle.Main;

import java.util.ArrayList;

import static com.mongodb.client.model.Filters.eq;

public class AchievementUI {

    /** A function which creates the GUI which contains the Achievements.
     * @param player - A handle to the player object which is passed by a function in LobbySystem. The parameter is the player which has clicked the item in order to open the GUI.
     * @author Beelzebub
     */
    public static void showAchievementGUI(Player player) {
        MongoDatabase db = Main.getMongoDatabase();
        MongoCollection<Document> collection = db.getCollection("playerAchievements");
        ArrayList<String> arrayList = (ArrayList<String>) collection.find(eq("_id", player.getUniqueId().toString())).first().get("achievements");
        Inventory achievements = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Achievements");

        //adding glass
        ItemStack Glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 15);
        ItemMeta GlassMeta = Glass.getItemMeta();
        GlassMeta.setDisplayName(" ");
        Glass.setItemMeta(GlassMeta);
        for (int i = 0; i <= 26; i++) {
            achievements.setItem(i, Glass);
        }

        //adding fullwool achievement
        Material fullwoolMat = Material.COAL;
        if (arrayList.contains("fullwool")) {fullwoolMat = Material.DIAMOND;}
        ItemStack fullwool = new ItemStack(fullwoolMat);
        ItemMeta fullwoolmeta = fullwool.getItemMeta();
        ArrayList<String> fullwoolLore = new ArrayList<>();

        fullwoolLore.add(ChatColor.WHITE + "Have the maximum amount of wool in your inventory");
        fullwoolLore.add(" ");
        if (fullwoolMat == Material.COAL) {
            fullwoolLore.add(ChatColor.RED + "Not Completed");
        }
        else {
            fullwoolLore.add(ChatColor.GREEN + "Completed");
        }
        fullwoolmeta.setDisplayName("§6Strategist");
        fullwoolmeta.setLore(fullwoolLore);
        fullwool.setItemMeta(fullwoolmeta);
        achievements.setItem(13, fullwool);

        //adding Killstreak 5 achievement
        Material killstreak5Mat = Material.COAL;
        if (arrayList.contains("killstreak5")) {killstreak5Mat = Material.DIAMOND;}
        ItemStack Killstreak5 = new ItemStack(killstreak5Mat, 1, (byte)1);
        ItemMeta Killstreak5meta = Killstreak5.getItemMeta();
        ArrayList<String> Killstreak5Lore = new ArrayList<>();

        Killstreak5Lore.add(ChatColor.WHITE + "Get a Killstreak of 5 in one game");
        Killstreak5Lore.add(" ");
        if (killstreak5Mat == Material.COAL) {
            Killstreak5Lore.add(ChatColor.RED + "Not Completed");
        }
        else {
            Killstreak5Lore.add(ChatColor.GREEN + "Completed");
        }
        Killstreak5meta.setDisplayName("§6Dominator");
        Killstreak5meta.setLore(Killstreak5Lore);
        Killstreak5.setItemMeta(Killstreak5meta);
        achievements.setItem(14, Killstreak5);

        //adding closeCall achievement
        Material closeCallMat = Material.COAL;
        if (arrayList.contains("closeCall")) {closeCallMat = Material.DIAMOND;}
        ItemStack closeCall = new ItemStack(closeCallMat, 1, (byte)1);
        ItemMeta closeCallMeta = closeCall.getItemMeta();
        ArrayList<String> closeCallLore = new ArrayList<>();

        closeCallLore.add(ChatColor.WHITE + "Win a game of Woolbattle while only having a single life left");
        closeCallLore.add(" ");
        if (closeCallMat == Material.COAL) {
            closeCallLore.add(ChatColor.RED + "Not Completed");
        }
        else {
            closeCallLore.add(ChatColor.GREEN + "Completed");
        }
        closeCallMeta.setDisplayName("§6Close Call");
        closeCallMeta.setLore(closeCallLore);
        closeCall.setItemMeta(closeCallMeta);
        achievements.setItem(12, closeCall);

        //adding losing achievement
        Material losingMat = Material.COAL;
        if (arrayList.contains("losing")) {losingMat = Material.DIAMOND;}
        ItemStack losing = new ItemStack(losingMat, 1, (byte)1);
        ItemMeta losingMeta = losing.getItemMeta();
        ArrayList<String> losingLore = new ArrayList<>();

        losingLore.add(ChatColor.WHITE + "Lose a game of Woolbattle without having a single Kill");
        losingLore.add(" ");
        if (losingMat == Material.COAL) {
            losingLore.add(ChatColor.RED + "Not Completed");
        }
        else {
            losingLore.add(ChatColor.GREEN + "Completed");
        }
        losingMeta.setDisplayName("§6Losing is the new winning");
        losingMeta.setLore(losingLore);
        losing.setItemMeta(losingMeta);
        achievements.setItem(15, losing);

        //adding carried achievement
        Material carriedMat = Material.COAL;
        if (arrayList.contains("carried")) {carriedMat = Material.DIAMOND;}
        ItemStack carried = new ItemStack(carriedMat, 1, (byte)1);
        ItemMeta carriedMeta = carried.getItemMeta();
        ArrayList<String> carriedLore = new ArrayList<>();

        carriedLore.add(ChatColor.WHITE + "Have someone else fight the battle for you - in other words,");
        carriedLore.add(ChatColor.WHITE + "win a game of Woolbattle without having a single kill");
        carriedLore.add(" ");
        if (carriedMat == Material.COAL) {
            carriedLore.add(ChatColor.RED + "Not Completed");
        }
        else {
            carriedLore.add(ChatColor.GREEN + "Completed");
        }
        carriedMeta.setDisplayName("§6The British way");
        carriedMeta.setLore(carriedLore);
        carried.setItemMeta(carriedMeta);
        achievements.setItem(11, carried);

        player.openInventory(achievements);
    }
}

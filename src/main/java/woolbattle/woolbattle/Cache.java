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

package woolbattle.woolbattle;

import org.bukkit.block.Block;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import woolbattle.woolbattle.perks.ActivePerk;
import woolbattle.woolbattle.perks.PassivePerk;

import java.util.ArrayList;
import java.util.HashMap;

public class Cache {

    /**
     * A Class that contains a few HashMaps with setters and getters to cache different things easily.
     * @author SimsumMC
     */

    private static HashMap<String, ActivePerk> activePerks = new HashMap<>();

    private static HashMap<String, PassivePerk<? extends Event, ?>> passivePerks = new HashMap<>();

    private static HashMap<Player, Player> playerDuels = new HashMap<>();

    private static HashMap<Player, Long> lastDamage = new HashMap<>();

    private static HashMap<Player, Long> spawnProtection = new HashMap<>();

    private static HashMap<Player, ArrayList<EnderPearl>> enderPearls = new HashMap<>();

    private static HashMap<Player, ArrayList<ArrayList<Block>>> jumpPlatformBlocks = new HashMap<>();

    private static HashMap<Player, HashMap<String, Integer>> activePerkSlots = new HashMap<>();

    private static HashMap<String, HashMap<Player, Integer>> killStreaks = new HashMap<String, HashMap<Player, Integer>>(){{
        put("Blue", new HashMap<>());
        put("Red", new HashMap<>());
        put("Green", new HashMap<>());
        put("Yellow", new HashMap<>());
    }};

    private static HashMap<String, ArrayList<Player>> teamMembers = new HashMap<String, ArrayList<Player>>(){{
        put("Blue", new ArrayList<>());
        put("Red", new ArrayList<>());
        put("Green", new ArrayList<>());
        put("Yellow", new ArrayList<>());
    }};

    private static HashMap<String, Integer> teamLives = new HashMap<String, Integer>(){{
        put("Blue", 0);
        put("Red", 0);
        put("Green", 0);
        put("Yellow", 0);
    }};

    private static HashMap<Integer, ArrayList<Player>> lifeVoting = new HashMap<Integer, ArrayList<Player>>(){{
        put(5, new ArrayList<>());
        put(10, new ArrayList<>());
        put(15, new ArrayList<>());
    }};

    private static HashMap<Player, HashMap<String, Integer>> playerStats = new HashMap<>();

    public static HashMap<String, ActivePerk> getActivePerks() {return activePerks;}
    public static void setActivePerks(HashMap<String, ActivePerk> activePerks) {Cache.activePerks = activePerks;}

    public static HashMap<String, PassivePerk<? extends Event,?>> getPassivePerks() {return passivePerks;}
    public static void setPassivePerks(HashMap<String, PassivePerk<? extends Event,?>> passivePerks) {Cache.passivePerks = passivePerks;}

    public static HashMap<Player, Player> getPlayerDuels() {return playerDuels;}
    public static void setPlayerDuels(HashMap<Player, Player> playerDuels) {Cache.playerDuels = playerDuels;}

    public static HashMap<Player, Long> getLastDamage() {return lastDamage;}
    public static void setLastDamage(HashMap<Player, Long> lastDamage) {Cache.lastDamage = lastDamage;}

    public static HashMap<Player, Long> getSpawnProtection() {return spawnProtection;}
    public static void setSpawnProtection(HashMap<Player, Long> spawnProtection) {Cache.spawnProtection = spawnProtection;}

    public static HashMap<Player, ArrayList<EnderPearl>> getEnderPearls() {return enderPearls;}
    public static void setEnderPearls(HashMap<Player, ArrayList<EnderPearl>> enderPearls) {Cache.enderPearls = enderPearls;}

    public static HashMap<Player, ArrayList<ArrayList<Block>>> getJumpPlatformBlocks() {return jumpPlatformBlocks;}
    public static void setJumpPlatformBlocks(HashMap<Player, ArrayList<ArrayList<Block>>> jumpPlatformBlocks) {Cache.jumpPlatformBlocks = jumpPlatformBlocks;}

    public static HashMap<String, HashMap<Player, Integer>> getKillStreaks() {return killStreaks;}
    public static void setKillStreaks(HashMap<String, HashMap<Player, Integer>> killStreaks) {Cache.killStreaks = killStreaks;}

    public static HashMap<String, ArrayList<Player>> getTeamMembers() {return teamMembers;}
    public static void setTeamMembers(HashMap<String, ArrayList<Player>> teamMembers) {Cache.teamMembers = teamMembers;}

    public static HashMap<String, Integer> getTeamLives() {return teamLives;}
    public static void setTeamLives(HashMap<String, Integer> teamLives) {Cache.teamLives = teamLives;}

    public static HashMap<Integer, ArrayList<Player>> getLifeVoting() {return lifeVoting;}
    public static void setLifeVoting(HashMap<Integer, ArrayList<Player>> lifeVoting) {Cache.lifeVoting = lifeVoting;}

    public static HashMap<Player, HashMap<String, Integer>> getPlayerStats() {return playerStats;}
    public static void setPlayerStats(HashMap<Player, HashMap<String, Integer>> playerStats) {Cache.playerStats = playerStats;}

    public static HashMap<Player, HashMap<String, Integer>> getActivePerkSlots() {return activePerkSlots;}
    public static void setActivePerkSlots(HashMap<Player, HashMap<String, Integer>> activePerkSlots) {Cache.activePerkSlots = activePerkSlots;}

    public static void clear(){
        playerDuels = new HashMap<>();

        lastDamage = new HashMap<>();

        spawnProtection = new HashMap<>();

        jumpPlatformBlocks = new HashMap<>();

        enderPearls = new HashMap<>();

        playerStats = new HashMap<>();

        activePerkSlots = new HashMap<>();

        killStreaks = new HashMap<String, HashMap<Player, Integer>>(){{
            put("Blue", new HashMap<>());
            put("Red", new HashMap<>());
            put("Green", new HashMap<>());
            put("Yellow", new HashMap<>());
        }};

        teamMembers = new HashMap<String, ArrayList<Player>>(){{
            put("Blue", new ArrayList<>());
            put("Red", new ArrayList<>());
            put("Green", new ArrayList<>());
            put("Yellow", new ArrayList<>());
        }};

        teamLives = new HashMap<String, Integer>(){{
            put("Blue", 0);
            put("Red", 0);
            put("Green", 0);
            put("Yellow", 0);
        }};

        lifeVoting = new HashMap<Integer, ArrayList<Player>>(){{
            put(5, new ArrayList<>());
            put(10, new ArrayList<>());
            put(15, new ArrayList<>());
        }};
    }
}
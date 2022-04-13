package woolbattle.woolbattle;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Cache {

    /**
     * A Class that contains a few HashMaps with setters and getters to cache different things easily.
     * @author SimsumMC
     */

    private static HashMap<Player, Long> lastDamage = new HashMap<>();

    private static HashMap<String, Integer> jumpCooldown = new HashMap<>();

    private static HashMap<UUID, Long> enderPearlCooldowns = new HashMap<>();

    private static HashMap<UUID, Boolean> bowFlags = new HashMap<>();

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

    public static HashMap<Player, Long> getLastDamage() {return lastDamage;}
    public static void setLastDamage(HashMap<Player, Long> lastDamage) {Cache.lastDamage = lastDamage;}

    public static HashMap<String, Integer> getJumpCooldown() {return jumpCooldown;}
    public static void setJumpCooldown(HashMap<String, Integer> jumpCooldown) {Cache.jumpCooldown = jumpCooldown;}

    public static HashMap<UUID, Long> getEnderPearlCooldowns() {return enderPearlCooldowns;}
    public static void setEnderPearlCooldowns(HashMap<UUID, Long> enderPearlCooldowns) {Cache.enderPearlCooldowns = enderPearlCooldowns;}

    public static HashMap<UUID, Boolean> getBowFlags() {return bowFlags;}
    public static void setBowFlags(HashMap<UUID, Boolean> bowFlags) {Cache.bowFlags = bowFlags;}

    public static HashMap<String, HashMap<Player, Integer>> getKillStreaks() {return killStreaks;}
    public static void setKillStreaks(HashMap<String, HashMap<Player, Integer>> killStreaks) {Cache.killStreaks = killStreaks;}

    public static HashMap<String, ArrayList<Player>> getTeamMembers() {return teamMembers;}
    public static void setTeamMembers(HashMap<String, ArrayList<Player>> teamMembers) {Cache.teamMembers = teamMembers;}

    public static HashMap<String, Integer> getTeamLives() {return teamLives;}
    public static void setTeamLives(HashMap<String, Integer> teamLives) {Cache.teamLives = teamLives;}

    public static HashMap<Integer, ArrayList<Player>> getLifeVoting() {return lifeVoting;}
    public static void setLifeVoting(HashMap<Integer, ArrayList<Player>> lifeVoting) {Cache.lifeVoting = lifeVoting;}

    public static void clear(){

        lastDamage = new HashMap<>();

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

        jumpCooldown = new HashMap<>();

        enderPearlCooldowns = new HashMap<>();

        bowFlags = new HashMap<>();
    }
}
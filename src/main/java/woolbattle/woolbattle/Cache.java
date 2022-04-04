package woolbattle.woolbattle;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Cache {



    /**
     * A Class that contains a few HashMaps with setters and getters to cache different things easily.
     * @author SimsumMC
     */
    private static HashMap<UUID, Boolean> enderPearlFlags = new HashMap<UUID, Boolean>();

    private static HashMap<String, Integer> lastDeath = new HashMap<>();

    private static HashMap<String, Integer> jumpCooldown = new HashMap<>();

    private static HashMap<String, ArrayList<Player>> teamMembers = new HashMap<String, ArrayList<Player>>(){{
        put("Blue", new ArrayList<>());
        put("Red", new ArrayList<>());
        put("Green", new ArrayList<>());
        put("Yellow", new ArrayList<>());
    }};

    private static HashMap<String, Integer> teamLives = new HashMap<String, Integer>(){{
        put("Blue", 10);
        put("Red", 0);
        put("Green", 0);
        put("Yellow", 0);
    }};

    private static HashMap<Integer, ArrayList<Player>> lifeVoting = new HashMap<Integer, ArrayList<Player>>(){{
        put(5, new ArrayList<>());
        put(10, new ArrayList<>());
        put(15, new ArrayList<>());
    }};

    public static HashMap<String, Integer> getLastDeath() {return lastDeath;}
    public static void setLastDeath(HashMap<String, Integer> lastDeath) {Cache.lastDeath = lastDeath;}

    public static HashMap<String, Integer> getJumpCooldown() {return jumpCooldown;}
    public static void setJumpCooldown(HashMap<String, Integer> jumpCooldown) {Cache.jumpCooldown = jumpCooldown;}

    public static HashMap<String, ArrayList<Player>> getTeamMembers() {return teamMembers;}
    public static void setTeamMembers(HashMap<String, ArrayList<Player>> teamMembers) {Cache.teamMembers = teamMembers;}

    public static HashMap<String, Integer> getTeamLives() {return teamLives;}
    public static void setTeamLives(HashMap<String, Integer> teamLives) {Cache.teamLives = teamLives;}

    public static HashMap<Integer, ArrayList<Player>> getLifeVoting() {return lifeVoting;}
    public static void setLifeVoting(HashMap<Integer, ArrayList<Player>> lifeVoting) {Cache.lifeVoting = lifeVoting;}

    public static HashMap<UUID, Boolean> getEnderPearlFlags() {return enderPearlFlags;}

    public static void setEnderPearlFlags(HashMap<UUID, Boolean> enderPearlFlags) {Cache.enderPearlFlags = enderPearlFlags;}

    public static void clear(){

        lastDeath = new HashMap<>();

        jumpCooldown = new HashMap<>();

        teamMembers = new HashMap<String, ArrayList<Player>>(){{
            put("Blue", new ArrayList<>());
            put("Red", new ArrayList<>());
            put("Green", new ArrayList<>());
            put("Yellow", new ArrayList<>());
        }};

        teamLives = new HashMap<String, Integer>(){{
            put("Blue", 10);
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

    public static DyeColor findTeamDyeColor(Player p){
        HashMap<String, ArrayList<Player>> teamMembers = getTeamMembers();
        ArrayList<Player> red = teamMembers.get("red"),
                blue = teamMembers.get("blue"),
                yellow = teamMembers.get("yellow"),
                green = teamMembers.get("green");
        if(red != null && blue.contains(p)){
            return DyeColor.RED;
        }else if(yellow != null && yellow.contains(p)){
            return DyeColor.ORANGE;
        }else if(green != null && green.contains(p)){
            return DyeColor.GREEN;
        }else if(blue != null && blue.contains(p)){
            return DyeColor.BLUE;
        }else{
            return DyeColor.WHITE;
        }
    }
    public static Color findTeamColor(Player p){
        HashMap<String, ArrayList<Player>> teamMembers = getTeamMembers();
        ArrayList<Player> red = teamMembers.get("red"),
                blue = teamMembers.get("blue"),
                yellow = teamMembers.get("yellow"),
                green = teamMembers.get("green");
        if(red != null && blue.contains(p)){
            return Color.RED;
        }else if(yellow != null && yellow.contains(p)){
            return Color.ORANGE;
        }else if(green != null && green.contains(p)){
            return Color.GREEN;
        }else if(blue != null && blue.contains(p)){
            return Color.BLUE;
        }else{
            return Color.WHITE;
        }
    }
}
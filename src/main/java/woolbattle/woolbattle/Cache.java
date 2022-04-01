package woolbattle.woolbattle;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;

public class Cache {

    /**
     * A Class that contains a few HashMaps with setters and getters to cache different things easily.
     * @author SimsumMC
     */

    private static HashMap<String, Integer> jumpCooldown = new HashMap<>();

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

    public static HashMap<String, Integer> getJumpCooldown() {return jumpCooldown;}
    public static void setJumpCooldown(HashMap<String, Integer> jumpCooldown) {Cache.jumpCooldown = jumpCooldown;}

    public static HashMap<String, ArrayList<Player>> getTeamMembers() {return teamMembers;}
    public static void setTeamMembers(HashMap<String, ArrayList<Player>> teamMembers) {Cache.teamMembers = teamMembers;}

    public static HashMap<String, Integer> getTeamLives() {return teamLives;}
    public static void setTeamLives(HashMap<String, Integer> teamLives) {Cache.teamLives = teamLives;}

    public static HashMap<Integer, ArrayList<Player>> getLifeVoting() {return lifeVoting;}
    public static void setLifeVoting(HashMap<Integer, ArrayList<Player>> lifeVoting) {Cache.lifeVoting = lifeVoting;}

    public static void clear(){
        jumpCooldown.clear();
        teamMembers.clear();
        teamLives.clear();
        lifeVoting.clear();
    }

}

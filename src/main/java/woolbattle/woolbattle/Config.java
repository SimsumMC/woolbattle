package woolbattle.woolbattle;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class Config {

    public static final String defaultMap = (String) Config.getValue("mapName");

    public static final int defaultLives = (int) (long) Config.getValue("defaultLives");
    public static final int startCooldown = (int) (long) Config.getValue("startCooldown");
    public static final int minHeight = (int) (long) getValue("minHeight");
    public static final int maxHeight = (int) (long) Config.getValue("maxHeight");
    public static final int teamSize = (int) (long) Config.getValue("teamSize");

    public static final ArrayList<Integer> lobbyCoordinates = (ArrayList<Integer>) Config.getValue("lobbySpawn");
    public static final ArrayList<Integer> midCoordinates = (ArrayList<Integer>) Config.getValue("mapSpawn");
    public static final ArrayList<ArrayList<Integer>> teamCoordinates = (ArrayList<ArrayList<Integer>>) Config.getValue("teamSpawns");

    public static final ArrayList<Integer> blueCoordinates = teamCoordinates.get(0);
    public static final ArrayList<Integer> redCoordinates = teamCoordinates.get(1);
    public static final ArrayList<Integer> greenCoordinates = teamCoordinates.get(2);
    public static final ArrayList<Integer> yellowCoordinates = teamCoordinates.get(3);

    public static final Location lobbyLocation = new Location(
            Bukkit.getServer().getWorlds().get(0),
            (double) lobbyCoordinates.get(1),
            (double) lobbyCoordinates.get(2),
            (double) lobbyCoordinates.get(3)
    );

    public static final Location midLocation = new Location(
            Bukkit.getServer().getWorlds().get(0),
            (double) midCoordinates.get(1),
            (double) midCoordinates.get(2),
            (double) midCoordinates.get(3)
    );

    public static final Location blueLocation = new Location(
            Bukkit.getServer().getWorlds().get(0),
            (double) blueCoordinates.get(1),
            (double) blueCoordinates.get(2),
            (double) blueCoordinates.get(3)
    );
    public static final Location redLocation = new Location(
            Bukkit.getServer().getWorlds().get(0),
            (double) redCoordinates.get(1),
            (double) redCoordinates.get(2),
            (double) redCoordinates.get(3)
    );
    public static final Location greenLocation = new Location(
            Bukkit.getServer().getWorlds().get(0),
            (double) greenCoordinates.get(1),
            (double) greenCoordinates.get(2),
            (double) greenCoordinates.get(3)
    );
    public static final Location yellowLocation = new Location(
            Bukkit.getServer().getWorlds().get(0),
            (double) yellowCoordinates.get(1),
            (double) yellowCoordinates.get(2),
            (double) yellowCoordinates.get(3)
    );

    /**
     * Returns the value of the given key from the config.json.
     * @param key the JSON key as a String<br>
     * @author SimsumMC & Servaturus
     */
    public static Object getValue(String key) {
        JSONParser parser = new JSONParser();
        FileReader fileReader;
        JSONObject jsonObject = new JSONObject();
        try {
            fileReader = new FileReader("plugins/config.json");
            Object obj = parser.parse(fileReader);
            jsonObject = (JSONObject) obj;
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return jsonObject.get(key);
    }

}
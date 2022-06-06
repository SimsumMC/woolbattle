package woolbattle.woolbattle;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;


public class Config {

    /**
     * A Config class that contains all default values from the config.json as variables.
     * @author SimsumMC & Servaturus
     */

    private static boolean fileExisting = false;

    public static final String mongoDatabase = (String) getValue("mongodb");

    static{
        System.out.println(Config.mongoDatabase);
    }

    public static final String defaultMap = (String) getValue("mapName");
    public static final int defaultLives = (int) (long) getValue("defaultLives");
    public static final int spawnProtectionLengthAfterDeath = (int) (long) getValue("spawnProtectionAfterDeath");
    public static final int spawnProtectionLengthAtGameStart = (int) (long) getValue("spawnProtectionAtGameStart");

    public static final int startCooldown = (int) (long) getValue("startCooldown");
    public static final int skipCooldown = (int) (long) getValue("skipCooldown");
    public static final int deathCooldown = (int) (long) getValue("deathCooldown");
    public static final int jumpCooldown = (int) (long) getValue("jumpCooldown");

    public static final int minHeight = (int) (long) getValue("minHeight");
    public static final int maxHeight = (int) (long) getValue("maxHeight");

    public static final int teamSize = (int) (long) getValue("teamSize");

    public static final int givenWoolAmount = (int) (long) getValue("givenWoolAmount");
    public static final int maxStacks = (int) (long) getValue("maxStacks");
    public static final int woolReplaceDelay = (int) (long) getValue("woolReplaceDelay");

    public static final ArrayList<Long> lobbyCoordinates = (ArrayList<Long>) Config.getValue("lobbySpawn");
    public static final ArrayList<Long> midCoordinates = (ArrayList<Long>) Config.getValue("mapSpawn");
    public static final ArrayList<ArrayList<Long>> teamCoordinates = (ArrayList<ArrayList<Long>>) Config.getValue("teamSpawns");

    public static final ArrayList<Long> blueCoordinates = teamCoordinates.get(0);
    public static final ArrayList<Long> redCoordinates = teamCoordinates.get(1);
    public static final ArrayList<Long> greenCoordinates = teamCoordinates.get(2);
    public static final ArrayList<Long> yellowCoordinates = teamCoordinates.get(3);

    public static final Location lobbyLocation = new Location(
            Bukkit.getServer().getWorlds().get(0),
            (double) lobbyCoordinates.get(0),
            (double) lobbyCoordinates.get(1),
            (double) lobbyCoordinates.get(2)
    );

    public static final Location midLocation = new Location(
            Bukkit.getServer().getWorlds().get(0),
            (double) midCoordinates.get(0),
            (double) midCoordinates.get(1),
            (double) midCoordinates.get(2)
    );

    public static final Location blueLocation = new Location(
            Bukkit.getServer().getWorlds().get(0),
            (double) blueCoordinates.get(0),
            (double) blueCoordinates.get(1),
            (double) blueCoordinates.get(2)
    );
    public static final Location redLocation = new Location(
            Bukkit.getServer().getWorlds().get(0),
            (double) redCoordinates.get(0),
            (double) redCoordinates.get(1),
            (double) redCoordinates.get(2)
    );
    public static final Location greenLocation = new Location(
            Bukkit.getServer().getWorlds().get(0),
            (double) greenCoordinates.get(0),
            (double) greenCoordinates.get(1),
            (double) greenCoordinates.get(2)
    );
    public static final Location yellowLocation = new Location(
            Bukkit.getServer().getWorlds().get(0),
            (double) yellowCoordinates.get(0),
            (double) yellowCoordinates.get(1),
            (double) yellowCoordinates.get(2)
    );

    /**
     * Returns the value of the given key from the config.json.
     * @param key the JSON key as a String<br>
     * @author SimsumMC & Servaturus
     */
    public static  Object getValue(String key) {
        if (!fileExisting) {

            // make sure that the directory exists
            File directory = new File("plugins/WoolBattle");
            directory.mkdir();

            File file = new File("plugins/WoolBattle/config.json");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                    Files.write(Paths.get(file.toURI()), Collections.singleton("{\n" +
                            "  \"mongodb\": \"ADD the connection string HERE\",\n" +
                            "  \"mapName\": \"MapName\",\n" +
                            "  \"mapSpawn\": [0, 0, 0],\n" +
                            "  \"lobbySpawn\": [0, 0, 0],\n" +
                            "  \"defaultLives\": 10,\n" +
                            "  \"spawnProtectionAfterDeath\": 5,\n" +
                            "  \"spawnProtectionAtGameStart\": 15,\n" +
                            "  \"startCooldown\": 60,\n" +
                            "  \"skipCooldown\": 30,\n" +
                            "  \"deathCooldown\": 10,\n" +
                            "  \"maxHeight\": 100,\n" +
                            "  \"minHeight\": 0,\n" +
                            "  \"teamSize\": 2,\n" +
                            "  \"teamSpawns\": [[0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0]],\n" +
                            "  \"givenWoolAmount\": 1,\n" +
                            "  \"maxStacks\": 3,\n" +
                            "  \"jumpCooldown\": 60,\n" +
                            "  \"woolReplaceDelay\" : 10\n" +
                            "}"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fileExisting = true;
            }
        }
        JSONParser parser = new JSONParser();
        FileReader fileReader;
        JSONObject jsonObject = new JSONObject();
        try {
            fileReader = new FileReader("plugins/WoolBattle/config.json");
            Object obj = parser.parse(fileReader);
            jsonObject = (JSONObject) obj;
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return jsonObject.get(key);
        }
}
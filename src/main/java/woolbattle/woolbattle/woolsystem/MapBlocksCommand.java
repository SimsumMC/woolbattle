package woolbattle.woolbattle.woolsystem;

import org.bson.BsonValue;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import woolbattle.woolbattle.Main;

import java.util.ArrayList;
import java.util.Locale;

import static com.mongodb.client.model.Filters.eq;

public class MapBlocksCommand implements CommandExecutor {

    private final String syntax = ChatColor.GREEN + "Proper syntax:\n/mapblocks <fetch/push/ls/ || clear> <[] || db/local";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(args.length <1){
            sender.sendMessage(ChatColor.RED
                    +
                    "The arguments, added to the command are not portraying the amount of information, needed in order for the command to work." +
                    syntax
            );
            return false;
        }

        else{
            switch(args[0].toLowerCase(Locale.ROOT)){
                case "fetch":
                    sender.sendMessage(ChatColor.GREEN + "Initiating fetching process...");
                    int previousSize = BlockBreakingSystem.getMapBlocks().size();
                    BlockBreakingSystem.fetchMapBlocks();
                    sender.sendMessage(ChatColor.GREEN + "Concluded the fetch operation: ");
                    sender.sendMessage(ChatColor.GREEN +
                            "Previous mapBlocks:" +
                            ChatColor.BLUE+
                            previousSize+
                            ChatColor.GREEN+
                            " \nCurrent mapBlocks: "+
                            ChatColor.BLUE +
                            BlockBreakingSystem.getMapBlocks().size()+
                            ChatColor.GREEN +
                            "."
                    );

                    break;

                case "push":
                    int previousSizeCached = BlockBreakingSystem.getMapBlocks().size();
                    int previousSizeDb;

                    Document found = Main.getMongoDatabase().getCollection("map").find(eq("_id", "mapBlocks")).first();
                    if(found == null){
                        previousSizeDb = 0;
                    }
                    else{
                        previousSizeDb = ((ArrayList<ArrayList<Double>>) Main.getMongoDatabase().
                                getCollection("map").
                                find(eq("_id", "mapBlocks")).
                                first().
                                get("mapBlocks")).size();
                    }

                    BlockBreakingSystem.pushMapBlocks();

                    int currentSize= ((ArrayList<ArrayList<Double>>) Main.getMongoDatabase().
                            getCollection("map").
                            find(eq("_id", "mapBlocks")).
                            first().
                            get("mapBlocks")).size();

                    sender.sendMessage(ChatColor.GREEN +
                            "The blocks having been pushed are equal to "+
                            ChatColor.BLUE+
                            previousSizeCached+
                            ChatColor.GREEN+
                            ".\nThe blocks, stored in the plugin's database in advance of the pushing process were equal to "+
                            ChatColor.BLUE+
                            previousSizeDb+
                            ChatColor.GREEN+
                            ".\nThe blocks, present in the database, in this moment are equal to "+
                            ChatColor.BLUE+
                            currentSize+
                            ChatColor.GREEN+
                            "."
                    );
                    break;

                case "ls":
                    if(args.length !=2){
                        sender.sendMessage(ChatColor.RED + "The amount of arguments specified is not congruent with the one needed. " + syntax);
                    }
                    switch(args[1].toLowerCase(Locale.ROOT)){
                        case "db":

                                sender.sendMessage(ChatColor.GREEN + "The following array-like string is standing on behalf of the blocks, currently present in the mapBlocks collection of the db:\n" +
                                        BlockBreakingSystem.doubleArrArrToString((ArrayList<ArrayList<Double>>) Main.
                                                getMongoClient().
                                                getDatabase("woolbattle").
                                                getCollection("map").
                                                find(eq(
                                                        "_id",
                                                        "mapBlocks")
                                                ).
                                                first().
                                                get("mapBlocks"))
                                );

                            break;
                        case "local":
                            sender.sendMessage(ChatColor.GREEN + "The following array-like string is standing on behalf of the blocks, currently present in the blockBreakingSystem's Cache:\n" + BlockBreakingSystem.locArrayToString(BlockBreakingSystem.getMapBlocks()));
                            break;
                    }
                    break;

                case "clear":
                    if(args.length < 2) {
                        sender.sendMessage(ChatColor.RED + "The arguments, added to the command are not portraying\n" +
                                "the amount of information, needed in order for the command\n" +
                                "to work.\n"+
                                syntax);
                    }
                    else{
                        switch(args[1].toLowerCase(Locale.ROOT)){
                            case "db":
                                sender.sendMessage(ChatColor.GREEN + "Clearing the mapBlocks, stored in the db...");
                                BlockBreakingSystem.clearDbMapBlocks();
                                break;
                            case "local":
                                sender.sendMessage(ChatColor.GREEN + "Clearing the cached mapBlocks and removedBlocks array...");
                                BlockBreakingSystem.setMapBlocks(new ArrayList<>());
                                BlockBreakingSystem.setRemovedBlocks(new ArrayList<>());
                                break;
                        }
                    }
                    break;
            }
        }
        return false;
    }
}
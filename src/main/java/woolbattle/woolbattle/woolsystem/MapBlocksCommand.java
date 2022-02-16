package woolbattle.woolbattle.woolsystem;

import org.bson.BsonArray;
import org.bson.BsonValue;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import woolbattle.woolbattle.Main;

import java.util.ArrayList;
import java.util.Locale;

import static com.mongodb.client.model.Filters.exists;

public class MapBlocksCommand implements CommandExecutor {
    private final String syntax = ChatColor.GREEN + "Proper syntax:\n/mapblocks <fetch/push/ls/ || clear> <[] || db/local";
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(args.length <1){
            commandSender.sendMessage(ChatColor.RED
                    +
                    "The arguments, added to the command are not portraying\nthe amount of information, needed in order for the command\nto work.\n" +
                    syntax
            );
            return false;
        }

        else if(args.length>2){
            commandSender.sendMessage(ChatColor.RED+
                    "The amount of arguments, sent to use this command\nhas been to high.\n"+
                    syntax
            );
            return false;
        }

        else{
            switch(args[0].toLowerCase(Locale.ROOT)){
                case "fetch":
                    commandSender.sendMessage(ChatColor.GREEN + "Initiating fetching process...");
                    int previousSize = BlockBreakingSystem.getMapBlocks().size();
                    BlockBreakingSystem.fetchMapBlocks();

                    commandSender.sendMessage(ChatColor.GREEN +
                            "In advance of the fetching process, there were " +
                            ChatColor.BLUE+
                            previousSize+
                            ChatColor.GREEN+
                            " mapBlocks.\nThe current amount of them is equal to "+
                            ChatColor.BLUE +
                            BlockBreakingSystem.getMapBlocks().size()+
                            ChatColor.GREEN +
                            "."
                    );
                    break;

                    case "push":
                        int previousSizeCached = BlockBreakingSystem.getMapBlocks().size();
                        int previousSizeDb = ((ArrayList<BsonValue>) Main.getMongoClient().
                                getDatabase("woolbattle").
                                getCollection("blockBreaking").
                                find(exists("mapBlocks")).
                                first().
                                get("mapBlocks"))
                                .size();

                        BlockBreakingSystem.pushMapBlocks();

                        int currentSize= ((ArrayList<BsonValue>) Main.getMongoClient().
                                getDatabase("woolbattle").
                                getCollection("blockBreaking").
                                find(new Document("mapBlocks", new BsonArray())).
                                first().
                                get("mapBlocks")).size();

                        commandSender.sendMessage(ChatColor.GREEN +
                                "The blocks having been pushed are equal to "+
                                ChatColor.BLUE+
                                previousSizeCached+
                                ChatColor.GREEN+
                                " .\nThe blocks, stored in the plugin's database in advance of the pushing process were equal to "+
                                ChatColor.BLUE+
                                previousSizeDb+
                                ChatColor.GREEN+
                                " .\nThe blocks, present in the database, in this moment are equal to "+
                                ChatColor.BLUE+
                                currentSize+
                                ChatColor.GREEN+
                                "."
                        );
                        break;

                    case "ls":
                        commandSender.sendMessage(ChatColor.GREEN + "The following array-like string is standing on behalf of the blocks, currently present in the blockBreakingSystem's Cache:\n" + BlockBreakingSystem.locArrayToString(BlockBreakingSystem.getMapBlocks()));
                        break;

                    case "clear":
                        if(args.length < 2) {
                            commandSender.sendMessage(ChatColor.RED + "The arguments, added to the command are not portraying\n" +
                                    "the amount of information, needed in order for the command\n" +
                                    "to work.\n"+
                                    syntax);
                        }else{
                            switch(args[1].toLowerCase(Locale.ROOT)){
                                case "db":
                                    commandSender.sendMessage(ChatColor.GREEN + "Clearing the mapBlocks, stored in the db...");
                                    BlockBreakingSystem.clearDbMapBlocks();
                                    break;
                                case "local":
                                    commandSender.sendMessage("Clearing the cached mapBlocks array...");
                                    BlockBreakingSystem.setMapBlocks(new ArrayList<Location>());
                                    break;
                            }
                        }
                        break;
            }
        }
        return false;
    }
}
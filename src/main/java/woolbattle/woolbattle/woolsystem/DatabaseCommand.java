package woolbattle.woolbattle.woolsystem;

import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import woolbattle.woolbattle.Main;
import org.bson.*;
import java.util.Collection;

public class DatabaseCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        commandSender.sendMessage(Main.getMongoClient().getDatabase("rildeDB").getCollection("seed").toString());
        Main.getMongoClient().getDatabase("rildeDB").createCollection("new");
        return false;
    }
}

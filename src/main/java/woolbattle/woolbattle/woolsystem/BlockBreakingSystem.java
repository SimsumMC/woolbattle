package woolbattle.woolbattle.woolsystem;

import org.bukkit.block.Block;
import java.util.ArrayList;

public class BlockBreakingSystem {

    private static ArrayList<Block> mapBlocks = new ArrayList<Block>();
    private static boolean collectBrokenBlocks = false;

    public static void fetchMapBlocks() {
        //Is supposed to fetch the Blocks, about to become the map from a database
    }

    public static boolean isCollectBrokeBlocks() {
        return collectBrokenBlocks;
    }
    public static void setCollectBrokenBlocks(boolean collectBrokenBlocks) {
        BlockBreakingSystem.collectBrokenBlocks = collectBrokenBlocks;
    }

    public static void setMapBlocks(ArrayList<Block> mapBlocksArg) {
        mapBlocks = mapBlocksArg;
    }
    public static ArrayList<Block> getMapBlocks() {
        return mapBlocks;
    }
}

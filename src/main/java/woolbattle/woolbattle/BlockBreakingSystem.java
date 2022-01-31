package woolbattle.woolbattle;

import org.bukkit.block.Block;

import java.util.ArrayList;

public class BlockBreakingSystem {

    private static ArrayList<Block> mapBlocks = new ArrayList<Block>();
    private static boolean collectBrokeBlocks = false;

    public static boolean isCollectBrokeBlocks() {
        return collectBrokeBlocks;
    }

    public static void setCollectBrokeBlocks(boolean collectBrokeBlocks) {
        BlockBreakingSystem.collectBrokeBlocks = collectBrokeBlocks;
    }

    public static void setMapBlocks(ArrayList<Block> mapBlocks) {
        mapBlocks = mapBlocks;
    }

    public static ArrayList<Block> getMapBlocks() {
        return mapBlocks;
    }
}

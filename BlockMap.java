import java.util.ArrayList;
import java.util.HashMap;

public class BlockMap {
    private final static HashMap<Integer,Block> Blocks = new HashMap<>();

    private BlockMap(){
    }

    public static HashMap<Integer,Block> getBlockMap(){
        return Blocks;
    }
}

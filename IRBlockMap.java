import java.util.ArrayList;
import java.util.HashMap;

public class IRBlockMap {
    private final static HashMap<Integer,Block> Blocks = new HashMap<>();

    private IRBlockMap(){
    }

    public static HashMap<Integer,Block> getBlockMap(){
        return Blocks;
    }
}
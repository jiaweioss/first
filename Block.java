import java.util.HashMap;

public class Block {
    Integer ID;
    Block Father;
    Integer BlockLevel;
    HashMap<String, Identifier> Identifiers;

    public Block(int ID,Block father,Integer BlockLevel) {
        this.ID = ID;
        this.Father = father;
        this.Identifiers = new HashMap<>();
        this.BlockLevel = BlockLevel;
    }


}

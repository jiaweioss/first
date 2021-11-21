import java.util.ArrayList;

public class whileBlock {
    whileBlock Father;
    int blockPoint;

    ArrayList<Integer> breakLocate;

    public whileBlock(whileBlock Father,int blockPoint){
        this.Father = Father;
        this.blockPoint = blockPoint;
        this.breakLocate = new ArrayList<>();
    }

}

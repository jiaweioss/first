import java.util.ArrayList;

public class Identifier {
    String name;
    Integer value;
    IdentType type;
    Integer globle;

    ArrayList<Integer> Dimension = new ArrayList<>();
    ArrayList<Integer> arrayValue = new ArrayList<>();
    public Identifier(Integer value, String name,IdentType type){
        this.globle = 0;
        this.value = value;
        this.name = name;
        this.type = type;

        this.Dimension.add(0);
    }





}



public class Identifier {
    String name;
    Integer value;
    IdentType type;
    Integer globle;
    public Identifier(Integer value, String name,IdentType type){
        this.globle = 0;
        this.value = value;
        this.name = name;
        this.type = type;
    }
}



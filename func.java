import java.util.ArrayList;

public class func {
    String type;
    String name;

    ArrayList<Params> params;



    public func(String type,String name,ArrayList<Params> params){
        this.name = name;
        this.type = type;
        this.params = params;
    }
}

import java.util.ArrayList;

public class func {
    String type;
    String name;

    ArrayList<Params> params;

    public func(String type, String name, ArrayList<Params> params) {
        this.name = name;
        this.type = type;
        this.params = params;
    }

    public String printParams() {

        StringBuilder s = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            s.append(params.get(i).printSize(i+1));
            if (i != params.size() - 1) {
                s.append(",");
            }
        }
        return s.toString();
    }
}

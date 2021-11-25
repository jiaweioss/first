import java.util.HashMap;

public class funcMap {
    private final static HashMap<String,func> funcs = new HashMap<>();

    private final static HashMap<String,func> IRfuncs = new HashMap<>();

    private funcMap(){
    }

    public static HashMap<String,func> getfuncMap(){
        return funcs;
    }

    public static HashMap<String,func> getIRfuncMap(){
        return IRfuncs;
    }
}

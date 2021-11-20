import java.util.regex.Pattern;

public class regValue {
    String value;
    Boolean type;

    String name;
    //type为true的时候表明为寄存器
    public regValue(String value, boolean type,String name) {
        this.type = type;
        this.value = value;
        this.name = name;
    }

    public String print() {
        String s;
        if (type) {
            if(isInteger(value)){
                s = "%" + value;
            }else {
                s = "@" + value;
            }

        } else {
            s = value;
        }
        return s;
    }

    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }
}


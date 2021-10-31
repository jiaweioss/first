public class regValue {
    Integer value;
    Boolean type;

    //type为true的时候表明为寄存器
    public regValue(int value, boolean type) {
        this.type = type;
        this.value = value;
    }

    public String print() {
        String s;
        if (type) {
            s = "%" + value.toString();
        } else {
            s = value.toString();
        }
        return s;
    }
}

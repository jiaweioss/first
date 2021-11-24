import java.util.ArrayList;

public class Params {
    String name;
    ArrayList<Integer> dimension;

    public Params(String name, ArrayList<Integer> dimension) {
        this.name = name;
        this.dimension = dimension;
    }


    public String printSize(int regPoint) {
        StringBuilder s = new StringBuilder();

        if (dimension.size() == 1) {
            s.append("i32 %").append(regPoint);
        } else if (dimension.size() == 2) {
            s.append("i32* %").append(regPoint);
        } else {
            for (int i = 2; i < dimension.size(); i++) {
                s.append("[").append(dimension.get(i)).append(" x ");
            }
            s.append("i32");
            s.append("]".repeat(Math.max(0, dimension.size() - 1)));
            s.append("* %").append(regPoint);
        }
        return s.toString();
    }
}

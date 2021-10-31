import java.util.ArrayList;

public class printTree {

    private ArrayList<Integer> col = new ArrayList<>();


    public printTree() {
        for (int i = 0; i <= 50; i++) {
            col.add(0);
        }
    }

    void print(ASTNode AST, int level) {
        col.set(level + 1, 1);
        for (int i = 0; i < level; i++) {
            if (col.get(i) != 0) {
                System.out.print("|  ");
            } else {
                System.out.print("   ");
            }
        }
        System.out.print("|——" + AST.getToken().getValue());
        System.out.println();
        for (ASTNode node : AST.getNodeList()
        ) {
            if (node == AST.getNodeList().get(AST.getNodeList().size() - 1))
                col.set(level + 1, 0);
            print(node, level + 1);
        }

    }
}

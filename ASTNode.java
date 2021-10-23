import java.util.ArrayList;

public class ASTNode {
    private Token token;
    private ArrayList<ASTNode> NodeList;

    public void addNode(ASTNode Node){
        this.NodeList.add(Node);
    }

    public ASTNode(Token token,ArrayList<ASTNode> NodeList){
        this.token = token;
        this.NodeList = NodeList;
    }

    public Token getToken() {
        return token;
    }

    public ArrayList<ASTNode> getNodeList() {
        return NodeList;
    }
}

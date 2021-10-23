import java.util.ArrayList;

public class Semantic {

    private int lineNum;

    public Semantic() {
        this.lineNum = 1;
    }

    public void analyze(ASTNode Node) throws ERR {


        if (Node.getToken().getSymbolType() == SymbolType.INTTK) {
            if (Node.getToken().getLineNum() > this.lineNum) {
                System.out.println();
                this.lineNum++;
            }
            System.out.print("define dso_local i32 ");
        } else if (Node.getToken().getSymbolType() == SymbolType.MAINTK) {
            if (Node.getToken().getLineNum() > this.lineNum) {
                System.out.println();
                this.lineNum++;
            }
            System.out.print("@main ");
        } else if (Node.getToken().getSymbolType() == SymbolType.RETURNTK) {
            if (Node.getToken().getLineNum() > this.lineNum) {
                System.out.println();
                this.lineNum++;
            }
            System.out.print("ret ");
        } else if (Node.getToken().getSymbolType() == SymbolType.LPARENT || Node.getToken().getSymbolType() == SymbolType.RPARENT ||
                Node.getToken().getSymbolType() == SymbolType.RBRACE || Node.getToken().getSymbolType() == SymbolType.LBRACE) {
            if (Node.getToken().getLineNum() > this.lineNum) {
                System.out.println();
                this.lineNum++;
            }
            System.out.print(Node.getToken().getValue() );
        }else if(Node.getToken().getValue().equals("Exp")){
            System.out.print("i32 "+calcuExp(Node));
        }

        for (ASTNode node : Node.getNodeList()
        ) {
            if(!Node.getToken().getValue().equals("Exp"))
            analyze(node);
        }
    }

    public int calcuExp(ASTNode Node) throws ERR {
        return calcuAddExp(Node.getNodeList().get(0));
    }

    public int calcuAddExp(ASTNode Node) throws ERR {
        int ans;
        int p=0;
        ArrayList<ASTNode> List = Node.getNodeList();

        ans = calcuMulExp(List.get(p++));
        while(p<Node.getNodeList().size()){
            if(List.get(p).getToken().getSymbolType() == SymbolType.MINU){
                ans-=calcuMulExp(List.get(++p));
            }else if(List.get(p).getToken().getSymbolType() == SymbolType.PLUS){
                ans+=calcuMulExp(List.get(++p));
            }
            p++;
        }
        return ans;
    }

    public int calcuMulExp(ASTNode Node) throws ERR {
        int ans;
        int p=0;
        ArrayList<ASTNode> List = Node.getNodeList();
        ans = UnaryExp(List.get(p++));
        while(p<Node.getNodeList().size()-1){
            if(List.get(p).getToken().getSymbolType() == SymbolType.MULT){
                ans*=UnaryExp(List.get(++p));
            }
            else if(List.get(p).getToken().getSymbolType() == SymbolType.DIV){
                if(UnaryExp(List.get(++p))==0){
                    throw new ERR("除0");
                }else{
                    ans/=UnaryExp(List.get(p));
                }
            }
            else if(List.get(p).getToken().getSymbolType() == SymbolType.MOD){
                if(UnaryExp(List.get(++p))==0){
                    throw new ERR("除0");
                }else{
                    ans%=UnaryExp(List.get(p));
                }
            }
            p++;
        }
        return ans;
    }

    public int UnaryExp(ASTNode Node) throws ERR{
        int ans=0;
        int op = 1;
        ArrayList<ASTNode> List = Node.getNodeList();
        for (ASTNode node:List
             ) {
            if(node.getToken().getSymbolType() == SymbolType.MINU){
                op*=-1;
            }else if(node.getToken().getValue().equals("PrimaryExp")){
                ans = op*PrimaryExp(node);
            }
        }
        return ans;
    }

    public int PrimaryExp(ASTNode Node) throws ERR {
        int p = 0;
        ArrayList<ASTNode> List = Node.getNodeList();
        if(List.get(0).getToken().getSymbolType()==SymbolType.LPARENT){
            return calcuExp(List.get(p+1));
        }else {
            return Number(List.get(0));
        }
    }

    public int Number(ASTNode Node){
        if(Node.getToken().getSymbolType()==SymbolType.OCTNUM){
            return Integer.parseInt(Node.getToken().getValue(), 8);
        }else if(Node.getToken().getSymbolType()==SymbolType.HEXNUM){
            return Integer.parseInt(Node.getToken().getValue(), 16);
        }else{
            return Integer.parseInt(Node.getToken().getValue());
        }
    }
}

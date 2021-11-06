import java.util.ArrayList;

public class Semantic {

    Block block;
    public Semantic() {
        this.block = new Block(0, null, 0);
        BlockMap.getBlockMap().put(this.block.ID, this.block);
    }

    private boolean checkToken(ASTNode Node, SymbolType type) {
        return Node.getToken().getSymbolType() == type;
    }

    private boolean checkValue(ASTNode Node, String value) {
        return Node.getToken().getValue().equals(value);
    }

    public void analyze(ASTNode Node, int BlockLevel) throws ERR {
        //检查类型
        if (checkValue(Node, "ConstDef")) {
            ConstDef(Node);
        } else if (checkValue(Node, "VarDef")) {
            VarDef(Node);
        } else if (checkValue(Node, "Block")) {
            this.block = new Block(this.block.ID + 1, this.block, BlockLevel);
            BlockMap.getBlockMap().put(this.block.ID, this.block);
        } else if (checkValue(Node, "Stmt")){
            Stmt(Node);
        } else if (checkValue(Node, "Exp")){
            CheckExp(Node);
        }

        for (ASTNode node : Node.getNodeList()
        ) {
            analyze(node, BlockLevel + 1);
        }
    }

    public void CheckExp(ASTNode Node) throws ERR {
        if(Node.getToken().getValue().equals("LVal")){
            if(!checkHas(Node.getNodeList().get(0).getNodeList().get(0))){
                throw new ERR("LVal未定义");
            }
        }
        for (ASTNode node : Node.getNodeList()
        ) {
            CheckExp(node);
        }
    }

    boolean checkHas(ASTNode Node)  {
        Block temp = this.block;
        while(temp != null){
            if(temp.Identifiers.containsKey(Node.getToken().getValue()))
                return true;
            temp = temp.Father;
        }
        return false;
    }


    private void Stmt(ASTNode Node) throws ERR{
        if(Node.getNodeList().get(0).getToken().getValue().equals("LVal")){
            ASTNode node = Node.getNodeList().get(0).getNodeList().get(0).getNodeList().get(0);
            if(!checkHas(node)){
                throw new ERR("Stmt");
            }
        }
    }

    private void ConstDef(ASTNode Node) throws ERR {
        ASTNode Ident = Node.getNodeList().get(0);
        if (!checkHas(Ident.getNodeList().get(0))) {
            String IdentName = Node.getNodeList().get(0).getNodeList().get(0).getToken().getValue();
            this.block.Identifiers.put(IdentName,
                    new Identifier(ConstInitVal(Node.getNodeList().get(2)),
                            IdentName,
                            IdentType.Constant));
        } else {
            throw new ERR("常量定义重复");
        }
    }

    private void VarDef(ASTNode Node) throws ERR {

        ASTNode Ident = Node.getNodeList().get(0);
        if (!checkHas(Ident.getNodeList().get(0))) {
            String IdentName = Node.getNodeList().get(0).getNodeList().get(0).getToken().getValue();
            this.block.Identifiers.put(IdentName,
                    new Identifier(null, IdentName,
                            IdentType.Variable));
        } else {
            throw new ERR("变量定义重复");
        }
    }


    public int ConstInitVal(ASTNode Node) throws ERR {
        return ConstExp(Node.getNodeList().get(0));
    }

    public int ConstExp(ASTNode Node) throws ERR {
        return calcuAddExp(Node.getNodeList().get(0));
    }

    public int calcuExp(ASTNode Node) throws ERR {
        return calcuAddExp(Node.getNodeList().get(0));
    }

    public int calcuAddExp(ASTNode Node) throws ERR {
        int ans;
        int p = 0;
        ArrayList<ASTNode> List = Node.getNodeList();

        ans = calcuMulExp(List.get(p++));
        while (p < Node.getNodeList().size()) {
            if (List.get(p).getToken().getSymbolType() == SymbolType.MINU) {
                ans -= calcuMulExp(List.get(++p));
            } else if (List.get(p).getToken().getSymbolType() == SymbolType.PLUS) {
                ans += calcuMulExp(List.get(++p));
            }
            p++;
        }
        return ans;
    }

    public int calcuMulExp(ASTNode Node) throws ERR {
        int ans;
        int p = 0;
        ArrayList<ASTNode> List = Node.getNodeList();
        ans = UnaryExp(List.get(p++));
        while (p < Node.getNodeList().size() - 1) {
            if (List.get(p).getToken().getSymbolType() == SymbolType.MULT) {
                ans *= UnaryExp(List.get(++p));
            } else if (List.get(p).getToken().getSymbolType() == SymbolType.DIV) {
                if (UnaryExp(List.get(++p)) == 0) {
                    throw new ERR("除0");
                } else {
                    ans /= UnaryExp(List.get(p));
                }
            } else if (List.get(p).getToken().getSymbolType() == SymbolType.MOD) {
                if (UnaryExp(List.get(++p)) == 0) {
                    throw new ERR("除0");
                } else {
                    ans %= UnaryExp(List.get(p));
                }
            }
            p++;
        }
        return ans;
    }

    public int UnaryExp(ASTNode Node) throws ERR {
        int ans = 0;
        int op = 1;
        ArrayList<ASTNode> List = Node.getNodeList();
        for (ASTNode node : List
        ) {
            if (node.getToken().getSymbolType() == SymbolType.MINU) {
                op *= -1;
            } else if (node.getToken().getValue().equals("PrimaryExp")) {
                ans = op * PrimaryExp(node);
            }
        }
        return ans;
    }

    public int PrimaryExp(ASTNode Node) throws ERR {
        int p = 0;
        ArrayList<ASTNode> List = Node.getNodeList();
        if (List.get(0).getToken().getSymbolType() == SymbolType.LPARENT) {
            return calcuExp(List.get(p + 1));
        } else if (List.get(0).getToken().getValue().equals("LVal")) {
            ASTNode Ident = List.get(0).getNodeList().get(0).getNodeList().get(0);
            if (checkHas(Ident)){
                return this.block.Identifiers.get(Ident.getToken().getValue()).value;
            }else {
                throw new ERR("PrimaryExp");
            }

        } else {
            return Number(List.get(0));
        }
    }

    public static int Number(ASTNode Node) {
        if (Node.getToken().getSymbolType() == SymbolType.OCTNUM) {
            return Integer.parseInt(Node.getToken().getValue(), 8);
        } else if (Node.getToken().getSymbolType() == SymbolType.HEXNUM) {
            return Integer.parseInt(Node.getToken().getValue(), 16);
        } else {
            return Integer.parseInt(Node.getToken().getValue());
        }
    }
}

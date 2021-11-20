import java.util.ArrayList;

public class Semantic {

    int BID;

    public Semantic() {
        BlockMap.getBlockMap().put(0, new Block(0, null, 0));
        this.BID = 0;
    }

    private boolean checkToken(ASTNode Node, SymbolType type) {
        return Node.getToken().getSymbolType() == type;
    }

    private boolean checkValue(ASTNode Node, String value) {
        return Node.getToken().getValue().equals(value);
    }

    public void analyze(ASTNode Node, int blockID) throws ERR {
        //检查类型
        if (checkValue(Node, "ConstDef")) {
            ConstDef(Node, blockID);
        } else if (checkValue(Node, "VarDef")) {
            VarDef(Node, blockID);
        } else if (checkValue(Node, "Block")) {
            BlockMap.getBlockMap().put(++BID, new Block(BID, BlockMap.getBlockMap().get(blockID), 0));
            blockID = BID;
        } else if (checkValue(Node, "Stmt")) {
            Stmt(Node, blockID);
        } else if (checkValue(Node, "Exp")) {
            CheckExp(Node, blockID);
        }

        for (ASTNode node : Node.getNodeList()
        ) {
            analyze(node, blockID);
        }
    }

    public void CheckExp(ASTNode Node, int blockID) throws ERR {
        if (Node.getToken().getValue().equals("LVal")) {
            if (!checkHas(Node.getNodeList().get(0).getNodeList().get(0), blockID)) {
                throw new ERR("LVal未定义");
            }
        }
        for (ASTNode node : Node.getNodeList()
        ) {
            CheckExp(node, blockID);
        }
    }

    boolean checkHas(ASTNode Node, int blockID) {
        Block temp = BlockMap.getBlockMap().get(blockID);

        while (temp != null) {

            if (temp.Identifiers.containsKey(Node.getToken().getValue()))
                return true;
            temp = temp.Father;
        }
        return false;
    }


    private void Stmt(ASTNode Node, int blockID) throws ERR {
        if (Node.getNodeList().get(0).getToken().getValue().equals("LVal")) {
            ASTNode node = Node.getNodeList().get(0).getNodeList().get(0).getNodeList().get(0);
            if (!checkHas(node, blockID)) {
                throw new ERR("Stmt");
            }
        }
    }

    private void ConstDef(ASTNode Node, int blockID) throws ERR {
        if (!BlockMap.getBlockMap().get(blockID).Identifiers.containsKey(Node.getNodeList().get(0).getNodeList().get(0).getToken().getValue())) {
            String IdentName = Node.getNodeList().get(0).getNodeList().get(0).getToken().getValue();
            BlockMap.getBlockMap().get(blockID).Identifiers.put(IdentName,
                    new Identifier(ConstInitVal(Node.getNodeList().get(2), blockID),
                            IdentName,
                            IdentType.Constant));
        } else {
            throw new ERR("常量定义重复");
        }
    }

    private void VarDef(ASTNode Node, int blockID) throws ERR {

        if (!BlockMap.getBlockMap().get(blockID).Identifiers.containsKey(Node.getNodeList().get(0).getNodeList().get(0).getToken().getValue())) {
            String IdentName = Node.getNodeList().get(0).getNodeList().get(0).getToken().getValue();

            if (blockID == 0) {
                int value = 0;
                for (ASTNode node : Node.getNodeList()) {
                    if (node.getToken().getValue().equals("InitVal")) {
                        value = ConstInitVal(node, blockID);
                    }

                }
                BlockMap.getBlockMap().get(blockID).Identifiers.put(IdentName,
                        new Identifier(value, IdentName,
                                IdentType.Variable));
            } else {
                BlockMap.getBlockMap().get(blockID).Identifiers.put(IdentName,
                        new Identifier(null, IdentName,
                                IdentType.Variable));
            }

        } else {
            throw new ERR("变量定义重复");
        }
    }


    public int ConstInitVal(ASTNode Node, int blockID) throws ERR {
        return ConstExp(Node.getNodeList().get(0), blockID);
    }

    public int ConstExp(ASTNode Node, int blockID) throws ERR {
        return calcuAddExp(Node.getNodeList().get(0), blockID);
    }

    public int calcuExp(ASTNode Node, int blockID) throws ERR {
        return calcuAddExp(Node.getNodeList().get(0), blockID);
    }

    public int calcuAddExp(ASTNode Node, int blockID) throws ERR {
        int ans;
        int p = 0;
        ArrayList<ASTNode> List = Node.getNodeList();

        ans = calcuMulExp(List.get(p++), blockID);
        while (p < Node.getNodeList().size()) {
            if (List.get(p).getToken().getSymbolType() == SymbolType.MINU) {
                ans -= calcuMulExp(List.get(++p), blockID);
            } else if (List.get(p).getToken().getSymbolType() == SymbolType.PLUS) {
                ans += calcuMulExp(List.get(++p), blockID);
            }
            p++;
        }
        return ans;
    }

    public int calcuMulExp(ASTNode Node, int blockID) throws ERR {
        int ans;
        int p = 0;
        ArrayList<ASTNode> List = Node.getNodeList();
        ans = UnaryExp(List.get(p++), blockID);
        while (p < Node.getNodeList().size() - 1) {
            if (List.get(p).getToken().getSymbolType() == SymbolType.MULT) {
                ans *= UnaryExp(List.get(++p), blockID);
            } else if (List.get(p).getToken().getSymbolType() == SymbolType.DIV) {
                if (UnaryExp(List.get(++p), blockID) == 0) {
                    throw new ERR("除0");
                } else {
                    ans /= UnaryExp(List.get(p), blockID);
                }
            } else if (List.get(p).getToken().getSymbolType() == SymbolType.MOD) {
                if (UnaryExp(List.get(++p), blockID) == 0) {
                    throw new ERR("除0");
                } else {
                    ans %= UnaryExp(List.get(p), blockID);
                }
            }
            p++;
        }
        return ans;
    }

    public int UnaryExp(ASTNode Node, int blockID) throws ERR {
        int ans = 0;
        int op = 1;
        ArrayList<ASTNode> List = Node.getNodeList();
        for (ASTNode node : List
        ) {
            if (node.getToken().getSymbolType() == SymbolType.MINU) {
                op *= -1;
            } else if (node.getToken().getValue().equals("PrimaryExp")) {
                ans = op * PrimaryExp(node, blockID);
            }
        }
        return ans;
    }

    public int PrimaryExp(ASTNode Node, int blockID) throws ERR {
        int p = 0;
        ArrayList<ASTNode> List = Node.getNodeList();
        if (List.get(0).getToken().getSymbolType() == SymbolType.LPARENT) {
            return calcuExp(List.get(p + 1), blockID);
        } else if (List.get(0).getToken().getValue().equals("LVal")) {
            ASTNode Ident = List.get(0).getNodeList().get(0).getNodeList().get(0);
            if (checkHas(Ident, blockID)) {
                if (BlockMap.getBlockMap().get(blockID).Identifiers.get(Ident.getToken().getValue()).type == IdentType.Constant) {
                    return BlockMap.getBlockMap().get(blockID).Identifiers.get(Ident.getToken().getValue()).value;
                } else {
                    throw new ERR("PrimaryExp");
                }
            } else {
                throw new ERR("PrimaryExp");
            }

        } else {
            return Number(List.get(0));
        }
    }

    public static Integer Number(ASTNode Node) {
        if (Node.getToken().getSymbolType() == SymbolType.OCTNUM) {
            return Integer.parseInt(Node.getToken().getValue(), 8);
        } else if (Node.getToken().getSymbolType() == SymbolType.HEXNUM) {
            return Integer.parseInt(Node.getToken().getValue(), 16);
        } else {
            return Integer.parseInt(Node.getToken().getValue());
        }
    }
}

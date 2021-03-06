import java.util.ArrayList;
import java.util.List;

public class Semantic {

    int BID;
    ArrayList<Params> holdParam;

    public Semantic() {
        BlockMap.getBlockMap().put(0, new Block(0, null, 0));
        this.BID = 0;
        this.holdParam = null;
    }

    private boolean checkValue(ASTNode Node, String value) {
        return Node.getToken().getValue().equals(value);
    }

    public void analyze(ASTNode Node, int blockID) throws ERR {
        //检查类型
        if (checkValue(Node, "ConstDef")) {
            ConstDef(Node, blockID);
        } else if (checkValue(Node, "funcDef")) {
            FuncDef(Node, blockID);
        } else if (checkValue(Node, "VarDef")) {
            VarDef(Node, blockID);
        } else if (checkValue(Node, "Block")) {
            BlockMap.getBlockMap().put(++BID, new Block(BID, BlockMap.getBlockMap().get(blockID), 0));
            blockID = BID;
            if (this.holdParam != null) {
                for (Params p : holdParam) {
                    Identifier i = new Identifier(null, p.name, IdentType.Variable);
                    i.Dimension = p.dimension;
                    BlockMap.getBlockMap().get(blockID).Identifiers.put(p.name, i);
                }
                this.holdParam = null;
            }
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

    private void FuncDef(ASTNode Node, int blockID) throws ERR {
        String type = Node.getNodeList().get(0).getNodeList().get(0).getToken().getValue();
        String name = Node.getNodeList().get(1).getNodeList().get(0).getToken().getValue();
        ArrayList<Params> param = new ArrayList<>();
        if (Node.getNodeList().get(3).getToken().getValue().equals("FuncFParams")) {
            for (ASTNode node : Node.getNodeList().get(3).getNodeList()) {
                if (node.getToken().getValue().equals("FuncFParam")) {
                    String pname = FuncFParam(node, blockID).name;
                    for (Params p : param) {
                        if (p.name.equals(pname)) {
                            System.out.println(p.name);
                            throw new ERR("定义重名啦");
                        }
                    }
                    param.add(FuncFParam(node, blockID));

                }
            }
            this.holdParam = param;
        }
        if (!funcMap.getfuncMap().containsKey(name)) {
            funcMap.getfuncMap().put(name, new func(type, name, param));
        } else {
            throw new ERR("函数定义重复");
        }

    }

    private Params FuncFParam(ASTNode Node, int blockID) throws ERR {
        ArrayList<Integer> dimen = new ArrayList<>();
        dimen.add(0);
        if (Node.getNodeList().size() >= 3) {
            dimen.add(0);
        }
        String name = Node.getNodeList().get(1).getNodeList().get(0).getToken().getValue();
        for (ASTNode node : Node.getNodeList()) {
            if (node.getToken().getValue().equals("ConstExp")) {
                dimen.add(ConstExp(node, blockID));
            }
        }
        return new Params(name, dimen);

    }


    private void ConstDef(ASTNode Node, int blockID) throws ERR {

        if (!BlockMap.getBlockMap().get(blockID).Identifiers.containsKey(Node.getNodeList().get(0).getNodeList().get(0).getToken().getValue())) {

            String IdentName = Node.getNodeList().get(0).getNodeList().get(0).getToken().getValue();
            ArrayList<ASTNode> List = Node.getNodeList();
            if (List.get(1).getToken().getValue().equals("[")) {
                Identifier arrayName = new Identifier(0,
                        IdentName,
                        IdentType.Constant);
                int temp = 1;
                int dimen = 0;
                while (temp < List.size() && List.get(temp).getToken().getValue().equals("[")) {
                    arrayName.Dimension.add(ConstExp(List.get(temp + 1), blockID));
                    temp += 3;
                    dimen++;
                }

                if (!List.get(temp).getToken().getValue().equals("=")) {
                    throw new ERR("ConstDef");
                } else {
                    temp += 1;
                }

                arrayName.arrayValue = ConstInitValArray(List.get(temp), arrayName.Dimension, blockID, "ConstInitVal");
                BlockMap.getBlockMap().get(blockID).Identifiers.put(IdentName, arrayName);
            } else {
                BlockMap.getBlockMap().get(blockID).Identifiers.put(IdentName,
                        new Identifier(ConstInitVal(Node.getNodeList().get(2), blockID),
                                IdentName,
                                IdentType.Constant));
            }


        } else {
            throw new ERR("常量定义重复");
        }
    }

    public ArrayList<Integer> ConstInitValArray(ASTNode Node, ArrayList<Integer> dimen, Integer blockId, String type) throws ERR {
        ArrayList<Integer> answer = new ArrayList<>();
        ArrayList<Integer> newDimen = new ArrayList<>();
        Integer childDimen = 1;


        for (int i = 1; i < dimen.size(); i++) {
            newDimen.add(dimen.get(i));
            childDimen *= dimen.get(i);
        }
        childDimen /= dimen.get(1);


        int temp = 0;
        if (dimen.size() <= 2) {
            for (ASTNode node : Node.getNodeList()) {
                if (node.getToken().getValue().equals(type)) {
                    answer.add(ConstInitVal(node, blockId));
                    temp++;
                }

            }
            for (int i = temp; i < dimen.get(1); i++) {
                answer.add(0);
            }
        } else {
            for (ASTNode node : Node.getNodeList()) {
                if (node.getToken().getValue().equals(type)) {
                    answer.addAll(ConstInitValArray(node, newDimen, blockId, type));
                    temp++;
                }
            }

            for (int i = temp; i < dimen.get(1); i++) {
                for (int k = 0; k < childDimen; k++)
                    answer.add(0);
            }
        }
        return answer;
    }


    private void VarDef(ASTNode Node, int blockID) throws ERR {

        if (!BlockMap.getBlockMap().get(blockID).Identifiers.containsKey(Node.getNodeList().get(0).getNodeList().get(0).getToken().getValue())) {
            String IdentName = Node.getNodeList().get(0).getNodeList().get(0).getToken().getValue();
            ArrayList<ASTNode> List = Node.getNodeList();

            if (List.size() > 1 && List.get(1).getToken().getValue().equals("[")) {
                Identifier arrayName = new Identifier(0,
                        IdentName,
                        IdentType.Variable);
                int temp = 1;
                while (temp < List.size() && List.get(temp).getToken().getValue().equals("[")) {
                    arrayName.Dimension.add(ConstExp(List.get(temp + 1), blockID));
                    temp += 3;
                }
                if (blockID == 0) {
                    if (temp < List.size() && List.get(temp).getToken().getValue().equals("=")) {
                        temp += 1;
                        arrayName.arrayValue = ConstInitValArray(List.get(temp), arrayName.Dimension, blockID, "InitVal");
                    }
                }
                BlockMap.getBlockMap().get(blockID).Identifiers.put(IdentName, arrayName);

            } else {
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

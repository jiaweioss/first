import java.util.ArrayList;
import java.util.HashMap;

public class TargetCodeGenerator {
    private int lineNum;

    HashMap<Identifier, Integer> register;
    private int regPoint;


    public TargetCodeGenerator() {
        this.lineNum = 1;
        this.regPoint = 0;
        register = new HashMap<>();
    }


    public void Generator(ASTNode Node) throws ERR {

        //输出所有的函数定义，包括函数调用
        defFunc();
        defVariable(0);
        printIdentifier(0);
        analyze(Node, 0);


    }

    private boolean checkValue(ASTNode Node, String value) {
        return Node.getToken().getValue().equals(value);
    }

    public void checkLine(Token token) {
        if (token.getLineNum() > this.lineNum) {
//            System.out.println();
            this.lineNum++;
        }
    }

    public void analyze(ASTNode Node, Integer BlockId) throws ERR {

        printTargetCode(Node, BlockId);
        if (checkValue(Node, "Block")) {
            BlockId++;
        }
        for (ASTNode node : Node.getNodeList()
        ) {
            analyze(node, BlockId);
            if (checkValue(Node, "Block") && checkValue(node, "{")) {
                printIdentifier(BlockId);
            }
        }
    }

    public void printTargetCode(ASTNode Node, Integer blockID) throws ERR {

        if (checkValue(Node, "funcDef")) {
            ArrayList<ASTNode> List = Node.getNodeList();
            checkLine(List.get(0).getNodeList().get(0).getToken());
            System.out.print("define dso_local i32 ");
            checkLine(List.get(1).getNodeList().get(0).getToken());
            System.out.print("@" + List.get(1).getNodeList().get(0).getToken().getValue() + "()");
        }

        if (checkValue(Node, "{") || checkValue(Node, "}")) {
            checkLine(Node.getToken());
            System.out.print(Node.getToken().getValue());
        }

        if (checkValue(Node, "Stmt")) {
            if (Node.getNodeList().get(0).getToken().getSymbolType() == SymbolType.RETURNTK) {
                checkLine(Node.getNodeList().get(0).getToken());
                System.out.println("ret i32 " + printExp(Node.getNodeList().get(1), blockID).print());
            } else if (Node.getNodeList().get(0).getToken().getValue().equals("LVal")) {
                Identifier key = BlockMap.getBlockMap().get(blockID).Identifiers.get(Node.getNodeList().get(0).getNodeList().get(0).getNodeList().get(0).getToken().getValue());
                System.out.println("store i32 " + printExp(Node.getNodeList().get(2), blockID).print()
                        + ", i32* %" + register.get(key));
            } else {
                printExp(Node.getNodeList().get(0), blockID);
            }
        }

        if (checkValue(Node, "VarDef")) {
            if (Node.getNodeList().size() == 3) {
                Identifier key = BlockMap.getBlockMap().get(blockID).Identifiers.get(Node.getNodeList().get(0).getNodeList().get(0).getToken().getValue());
                System.out.println("store i32 " + printExp(Node.getNodeList().get(2).getNodeList().get(0), blockID).print() + ", i32* %" + register.get(key));
            }
        }

    }

    public void defFunc() {
        for (func fun : funcMap.getfuncMap().values()
        ) {
            if (fun.name.equals("getint") || fun.name.equals("getch")) {
                System.out.println("declare " + fun.type + " @" + fun.name + "()");
            } else {
                System.out.println("declare " + fun.type + " @" + fun.name + "(i32)");
            }
        }
    }

    public void defVariable(Integer Id) {

    }

    public void printIdentifier(Integer Id) {
        System.out.println();
        Block block = BlockMap.getBlockMap().get(Id);
        for (Identifier ident : block.Identifiers.values()
        ) {
            if (ident.type == IdentType.Variable) {
                this.regPoint++;
                this.register.put(ident, regPoint);
                System.out.println("%" + regPoint + " = alloca i32");

            }
        }
    }

    public regValue printExp(ASTNode Node, Integer blockID) throws ERR {
        return printAddExp(Node.getNodeList().get(0), blockID);
    }

    public regValue printAddExp(ASTNode Node, Integer blockID) throws ERR {
        regValue reg;
        int p = 0;
        ArrayList<ASTNode> List = Node.getNodeList();
        reg = MulExp(List.get(p++), blockID);
        while (p < Node.getNodeList().size()) {
            if (List.get(p).getToken().getSymbolType() == SymbolType.MINU) {
                regValue temp = MulExp(List.get(++p), blockID);
                System.out.println("%" + (++regPoint) + " = sub i32 " + reg.print() + ", " + temp.print());
                reg = new regValue(regPoint, true);
            } else if (List.get(p).getToken().getSymbolType() == SymbolType.PLUS) {
                regValue temp = MulExp(List.get(++p), blockID);
                System.out.println("%" + (++regPoint) + " = add i32 " + reg.print() + ", " + temp.print());
                reg = new regValue(regPoint, true);
            }
            p++;
        }
        return reg;
    }

    public regValue MulExp(ASTNode Node, Integer blockID) throws ERR {
        regValue reg;
        int p = 0;
        ArrayList<ASTNode> List = Node.getNodeList();
        reg = UnaryExp(List.get(p++), blockID);
        while (p < Node.getNodeList().size()) {
            if (List.get(p).getToken().getSymbolType() == SymbolType.MULT) {
                regValue temp = UnaryExp(List.get(++p), blockID);
                System.out.println("%" + (++regPoint) + " = miv i32 " + reg.print() + ", " + temp.print());
                reg = new regValue(regPoint, true);
            } else if (List.get(p).getToken().getSymbolType() == SymbolType.DIV) {
                regValue temp = UnaryExp(List.get(++p), blockID);
                System.out.println("%" + (++regPoint) + " = sdiv i32 " + reg.print() + ", " + temp.print());
                reg = new regValue(regPoint, true);
            } else if (List.get(p).getToken().getSymbolType() == SymbolType.MOD) {
                regValue temp = UnaryExp(List.get(++p), blockID);
                System.out.println("%" + (++regPoint) + " = srem i32 " + reg.print() + ", " + temp.print());
                reg = new regValue(regPoint, true);
            }
            p++;
        }
        return reg;
    }

    public regValue UnaryExp(ASTNode Node, Integer blockID) throws ERR {
        regValue reg;
        ASTNode func = new ASTNode(new Token(SymbolType.NONE, "none", 0), new ArrayList<>());
        int op = 1;
        ArrayList<ASTNode> List = Node.getNodeList();
        for (ASTNode node : List
        ) {
            if (node.getToken().getSymbolType() == SymbolType.MINU) {
                op *= -1;
            }
            if (node.getToken().getSymbolType() == SymbolType.GETINT || node.getToken().getSymbolType() == SymbolType.PUTINT
                    || node.getToken().getSymbolType() == SymbolType.GETCH || node.getToken().getSymbolType() == SymbolType.PUTCH) {
                func = node;
            }
        }
        if (func.getToken().getSymbolType() == SymbolType.GETINT || func.getToken().getSymbolType() == SymbolType.GETCH) {
            regPoint++;
            System.out.println("%" + regPoint + " = call i32 @" + func.getToken().getValue() + "()");
            reg = new regValue(regPoint, true);
        } else if (func.getToken().getSymbolType() == SymbolType.PUTCH || func.getToken().getSymbolType() == SymbolType.PUTINT) {
            System.out.println("call void @" + func.getToken().getValue() + "(i32 " + printExp(List.get(List.size() - 2), blockID).print() + ")");
            reg = new regValue(regPoint, true);
        } else {
            reg = PrimaryExp(List.get(List.size() - 1), blockID);
        }
        if (op == -1) {
            regPoint++;
            System.out.println("%" + regPoint + " = sub i32 0, " + reg.print());
            reg = new regValue(regPoint, true);
        }
        return reg;
    }

    public regValue PrimaryExp(ASTNode Node, Integer blockID) throws ERR {
        int p = 0;
        regValue reg;
        ArrayList<ASTNode> List = Node.getNodeList();
        if (List.get(0).getToken().getSymbolType() == SymbolType.LPARENT) {
            return printExp(List.get(p + 1), blockID);
        } else if (List.get(0).getToken().getValue().equals("LVal")) {
            Identifier key = BlockMap.getBlockMap().get(blockID).Identifiers.get(Node.getNodeList().get(0).getNodeList().get(0).getNodeList().get(0).getToken().getValue());
            if (key.type == IdentType.Variable) {
                reg = new regValue(register.get(key), true);
                regPoint++;
                System.out.println("%" + regPoint + " = load i32, i32* " + reg.print());
                reg = new regValue(regPoint, true);
            } else {
                reg = new regValue(key.value, false);
            }

        } else {
//            System.out.println(List.get(0).getNodeList().get(0).getToken().getValue());
            reg = new regValue(Semantic.Number(List.get(0)), false);
        }
        return reg;
    }

}

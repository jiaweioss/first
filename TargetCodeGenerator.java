import java.util.ArrayList;
import java.util.HashMap;

public class TargetCodeGenerator {
    private int lineNum;

    HashMap<Identifier, Integer> register;
    private int regPoint;

    ArrayList<String> TargetCode;


    public TargetCodeGenerator() {
        this.lineNum = 1;
        this.regPoint = 0;
        register = new HashMap<>();
    }


    public void Generator(ASTNode Node) throws ERR {

        this.TargetCode = new ArrayList<>();

        //输出所有的函数定义，包括函数调用
        defFunc();
        defVariable(0);
        printIdentifier(0);
        analyze(Node, 0);
        for (String s : TargetCode
        ) {
            System.out.println(s);
        }


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

    }

    public void printVarDef(ASTNode Node, Integer blockID) throws ERR {
        if (Node.getNodeList().size() == 3) {
            Identifier key = searchKey(Node.getNodeList().get(0).getNodeList().get(0).getToken().getValue(), blockID);
            TargetCode.add("store i32 " + printExp(Node.getNodeList().get(2).getNodeList().get(0), blockID).print() + ", i32* %" + register.get(key));
        }
    }

    public void printFuncDef(ASTNode Node, Integer blockID) throws ERR {
        ArrayList<ASTNode> List = Node.getNodeList();
        checkLine(List.get(0).getNodeList().get(0).getToken());
        TargetCode.add("define dso_local i32 @" + List.get(1).getNodeList().get(0).getToken().getValue() + "(){");
        printTargetCode(List.get(4), blockID);
        TargetCode.add("}");
    }

    public void printStmt(ASTNode Node, Integer blockID) throws ERR {


        if (Node.getNodeList().get(0).getToken().getSymbolType() == SymbolType.RETURNTK) {
            checkLine(Node.getNodeList().get(0).getToken());
            TargetCode.add("ret i32 " + printExp(Node.getNodeList().get(1), blockID).print());
        } else if (Node.getNodeList().get(0).getToken().getValue().equals("LVal")) {
            Identifier key = searchKey(Node.getNodeList().get(0).getNodeList().get(0).getNodeList().get(0).getToken().getValue(), blockID);
            TargetCode.add("store i32 " + printExp(Node.getNodeList().get(2), blockID).print()
                    + ", i32* %" + register.get(key));
        } else if (Node.getNodeList().get(0).getToken().getValue().equals("if")) {

            ArrayList<String> temp = new ArrayList<>();
            if (Node.getNodeList().size() <= 5) {
                int mark;
                TargetCode.add("br i1 " + printCond(Node.getNodeList().get(2), blockID).print() + ",label %" + (++regPoint) + ",label %");
                mark = TargetCode.size();
                TargetCode.add("");
                TargetCode.add(regPoint + ":");
                printStmt(Node.getNodeList().get(4), blockID);
                TargetCode.set(mark - 1, TargetCode.get(mark - 1) + (++regPoint));
                TargetCode.add("br label %" + (regPoint));
                TargetCode.add("");
                TargetCode.add(regPoint + ":");

            } else {
                int mark1, mark2;
                TargetCode.add("br i1 " + printCond(Node.getNodeList().get(2), blockID).print() + ",label %" + (++regPoint) + ",label %");
                mark1 = TargetCode.size();
                TargetCode.add("");
                TargetCode.add(regPoint + ":");
                printStmt(Node.getNodeList().get(4), blockID);
                TargetCode.add("br label %");
                mark2 = TargetCode.size();
                TargetCode.add("");

                TargetCode.set(mark1 - 1, TargetCode.get(mark1 - 1) + (++regPoint));
                TargetCode.add(regPoint + ":");

                printStmt(Node.getNodeList().get(6), blockID);
                TargetCode.add("br label %" + (++regPoint));
                TargetCode.set(mark2 - 1, TargetCode.get(mark2 - 1) + regPoint);
                TargetCode.add("");
                TargetCode.add(regPoint + ":");
            }


        } else if (Node.getNodeList().get(0).getToken().getValue().equals("Block")) {
            printTargetCode(Node.getNodeList().get(0), blockID++);
        } else {
//                System.out.println(Node.getNodeList().get(0).getToken().getValue());
            printExp(Node.getNodeList().get(0), blockID);
        }
    }


    public void printTargetCode(ASTNode Node, Integer blockID) throws ERR {

        if (checkValue(Node, "funcDef")) {
            printFuncDef(Node, blockID);
        } else if (checkValue(Node, "Stmt")) {
            printStmt(Node, blockID);
        } else if (checkValue(Node, "VarDef")) {
            printVarDef(Node, blockID);
        } else {
            if (checkValue(Node, "Block")) {
                blockID++;
            }
            for (ASTNode node : Node.getNodeList()
            ) {
                printTargetCode(node, blockID);
                if (checkValue(Node, "Block") && checkValue(node, "{")) {
                    printIdentifier(blockID);
                }
            }
        }

    }

    public void defFunc() {
        for (func fun : funcMap.getfuncMap().values()
        ) {
            if (fun.name.equals("getint") || fun.name.equals("getch")) {
                TargetCode.add("declare " + fun.type + " @" + fun.name + "()");
            } else {
                TargetCode.add("declare " + fun.type + " @" + fun.name + "(i32)");
            }
        }
    }

    public void defVariable(Integer Id) {

    }

    public void printIdentifier(Integer Id) {
        Block block = BlockMap.getBlockMap().get(Id);
        for (Identifier ident : block.Identifiers.values()
        ) {
            if (ident.type == IdentType.Variable) {
                this.regPoint++;
                this.register.put(ident, regPoint);
                TargetCode.add("%" + regPoint + " = alloca i32");

            }
        }
    }


    public regValue printCond(ASTNode Node, Integer blockID) throws ERR {
        return printLorExp(Node.getNodeList().get(0), blockID);
    }

    public regValue printLorExp(ASTNode Node, Integer blockID) throws ERR {
        if (Node.getNodeList().size() > 1) {
            regValue reg;
            regValue temp1 = printAndExp(Node.getNodeList().get(0), blockID);
            regValue temp2 = printAndExp(Node.getNodeList().get(2), blockID);
            TargetCode.add("%" + (++regPoint) + " = or i1 " +
                    temp1.print() + "," + temp2.print());
            temp1 = new regValue(regPoint, true);
            for (int i = 4; i < Node.getNodeList().size() - 1; i += 2) {

                temp2 = printAddExp(Node.getNodeList().get(i), blockID);
                TargetCode.add("%" + (++regPoint) + " = or i1 " +
                        temp1.print() + ", " + temp2.print());
                temp1 = new regValue(regPoint, true);
            }

            reg = new regValue(regPoint, true);
            return reg;
        } else {
            return printAndExp(Node.getNodeList().get(0), blockID);
        }

    }

    public regValue printAndExp(ASTNode Node, Integer blockID) throws ERR {
        if (Node.getNodeList().size() > 1) {
            regValue reg;
            regValue temp1 = printEqExp(Node.getNodeList().get(0), blockID);
            regValue temp2 = printEqExp(Node.getNodeList().get(2), blockID);
            TargetCode.add("%" + (++regPoint) + " = and i1 " +
                    temp1.print() + "," + temp2.print());
            temp1 = new regValue(regPoint, true);
            for (int i = 4; i < Node.getNodeList().size() - 1; i += 2) {

                temp2 = printAddExp(Node.getNodeList().get(i), blockID);
                TargetCode.add("%" + (++regPoint) + " = and i1 " +
                        temp1.print() + ", " + temp2.print());
                temp1 = new regValue(regPoint, true);
            }

            reg = new regValue(regPoint, true);
            return reg;
        } else {
            return printEqExp(Node.getNodeList().get(0), blockID);
        }
    }

    public regValue printEqExp(ASTNode Node, Integer blockID) throws ERR {
        if (Node.getNodeList().size() > 1) {
            regValue reg;
            regValue temp1 = printRelExp(Node.getNodeList().get(0), blockID);
            regValue temp2 = printRelExp(Node.getNodeList().get(2), blockID);
            TargetCode.add("%" + (++regPoint) + " = icmp " + ty(Node.getNodeList().get(1)) + " i32 " +
                    temp1.print() + "," + temp2.print());
            temp1 = new regValue(regPoint, true);
            for (int i = 4; i < Node.getNodeList().size() - 1; i += 2) {

                temp2 = printAddExp(Node.getNodeList().get(i), blockID);
                TargetCode.add("%" + (++regPoint) + " = icmp " + ty(Node.getNodeList().get(i - 1)) + " i32 " +
                        temp1.print() + ", " + temp2.print());
                temp1 = new regValue(regPoint, true);
            }

            reg = new regValue(regPoint, true);
            return reg;
        } else if (Node.getNodeList().size() == 1 && Node.getNodeList().get(0).getNodeList().size() > 1) {
            return printRelExp(Node.getNodeList().get(0), blockID);
        } else {
            String s = printAddExp(Node.getNodeList().get(0).getNodeList().get(0), blockID).print();
            TargetCode.add("%" + (++regPoint) + " = icmp eq i32 " +
                    s + ", 0");
            return new regValue(regPoint, true);
        }
    }

    public regValue printRelExp(ASTNode Node, Integer blockID) throws ERR {
        regValue reg;

        if (Node.getNodeList().size() > 1) {
            regValue temp1 = printAddExp(Node.getNodeList().get(0), blockID);
            regValue temp2 = printAddExp(Node.getNodeList().get(2), blockID);
            TargetCode.add("%" + (++regPoint) + " = icmp " + ty(Node.getNodeList().get(1)) + " i32 " +
                    temp1.print() + "," + temp2.print());
            temp1 = new regValue(regPoint, true);
            for (int i = 4; i < Node.getNodeList().size() - 1; i += 2) {

                temp2 = printAddExp(Node.getNodeList().get(i), blockID);
                TargetCode.add("%" + (++regPoint) + " = icmp " + ty(Node.getNodeList().get(i - 1)) + " i32 " +
                        temp1.print() + ", " + temp2.print());
                temp1 = new regValue(regPoint, true);
            }
        } else {
            return printAddExp(Node.getNodeList().get(0), blockID);
        }
        reg = new regValue(regPoint, true);

        return reg;
    }

    public String ty(ASTNode Node) {
        String s = "";
        switch (Node.getToken().getValue()) {
            case ">=":
                s = "seg";
                break;
            case "<=":
                s = "sle";
                break;
            case "==":
                s = "eq";
                break;
            case "!=":
                s = "ne";
                break;
            case ">":
                s = "sgt";
                break;
            case "<":
                s = "slt";
                break;
        }
        return s;
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
                TargetCode.add("%" + (++regPoint) + " = sub i32 " + reg.print() + ", " + temp.print());
                reg = new regValue(regPoint, true);
            } else if (List.get(p).getToken().getSymbolType() == SymbolType.PLUS) {
                regValue temp = MulExp(List.get(++p), blockID);
                TargetCode.add("%" + (++regPoint) + " = add i32 " + reg.print() + ", " + temp.print());
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
                System.out.println("%" + (++regPoint) + " = mul i32 " + reg.print() + ", " + temp.print());
                reg = new regValue(regPoint, true);
            } else if (List.get(p).getToken().getSymbolType() == SymbolType.DIV) {
                regValue temp = UnaryExp(List.get(++p), blockID);
                TargetCode.add("%" + (++regPoint) + " = sdiv i32 " + reg.print() + ", " + temp.print());
                reg = new regValue(regPoint, true);
            } else if (List.get(p).getToken().getSymbolType() == SymbolType.MOD) {
                regValue temp = UnaryExp(List.get(++p), blockID);
                TargetCode.add("%" + (++regPoint) + " = srem i32 " + reg.print() + ", " + temp.print());
                reg = new regValue(regPoint, true);
            }
            p++;
        }
        return reg;
    }

    public regValue UnaryExp(ASTNode Node, Integer blockID) throws ERR {
        regValue reg;
        ASTNode func = new ASTNode(new Token(SymbolType.NONE, "none", 0), new ArrayList<>());
        int op = 1, att = 1;
        ArrayList<ASTNode> List = Node.getNodeList();
        for (ASTNode node : List
        ) {
            if (node.getToken().getSymbolType() == SymbolType.MINU) {
                op *= -1;
            }
            if (node.getToken().getSymbolType() == SymbolType.NOT) {
                att *= -1;
            }
            if (node.getToken().getSymbolType() == SymbolType.GETINT || node.getToken().getSymbolType() == SymbolType.PUTINT
                    || node.getToken().getSymbolType() == SymbolType.GETCH || node.getToken().getSymbolType() == SymbolType.PUTCH) {
                func = node;
            }
        }
        if (func.getToken().getSymbolType() == SymbolType.GETINT || func.getToken().getSymbolType() == SymbolType.GETCH) {
            regPoint++;
            TargetCode.add("%" + regPoint + " = call i32 @" + func.getToken().getValue() + "()");
            reg = new regValue(regPoint, true);
        } else if (func.getToken().getSymbolType() == SymbolType.PUTCH || func.getToken().getSymbolType() == SymbolType.PUTINT) {
            TargetCode.add("call void @" + func.getToken().getValue() + "(i32 " + printExp(List.get(List.size() - 2), blockID).print() + ")");
            reg = new regValue(regPoint, true);
        } else {
            reg = PrimaryExp(List.get(List.size() - 1), blockID);
        }
        if (op == -1) {
            regPoint++;
            TargetCode.add("%" + regPoint + " = sub i32 0, " + reg.print());
            reg = new regValue(regPoint, true);
        }
        if (att == -1) {
            TargetCode.add("%" + (++regPoint) + " = icmp eq i32 " + reg.print() + ", 0");
            TargetCode.add("%" + (regPoint+1) + " = zext i1 %" + regPoint + " to i32");
            regPoint++;
            reg = new regValue(regPoint, true);
        }
        return reg;
    }

    public Identifier searchKey(String name, Integer blockID) {
        Block temp = BlockMap.getBlockMap().get(blockID);

        while (temp.Identifiers.get(name) == null) {
            temp = temp.Father;
        }
        return temp.Identifiers.get(name);
    }

    public regValue PrimaryExp(ASTNode Node, Integer blockID) throws ERR {
        int p = 0;
        regValue reg;
        ArrayList<ASTNode> List = Node.getNodeList();
        if (List.get(0).getToken().getSymbolType() == SymbolType.LPARENT) {
            return printExp(List.get(p + 1), blockID);
        } else if (List.get(0).getToken().getValue().equals("LVal")) {
            //BlockMap.getBlockMap().get(blockID).Identifiers.get(Node.getNodeList().get(0).getNodeList().get(0).getNodeList().get(0).getToken().getValue())

            Identifier key = searchKey(Node.getNodeList().get(0).getNodeList().get(0).getNodeList().get(0).getToken().getValue(), blockID);

            if (key.type == IdentType.Variable) {
                reg = new regValue(register.get(key), true);
                regPoint++;
                TargetCode.add("%" + regPoint + " = load i32, i32* " + reg.print());
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

import java.util.ArrayList;
import java.util.HashMap;

public class TargetCodeGenerator {

    int BID;
    whileBlock whileBlock;
    //虚拟寄存器与变量的对应表
    HashMap<Identifier, String> register;
    //当前的虚拟寄存器编号
    private Integer regPoint;
    //中间代码字符串
    ArrayList<String> TargetCode;


    public TargetCodeGenerator() {
        this.regPoint = 0;
        register = new HashMap<>();
        this.BID = 0;
        this.whileBlock = new whileBlock(null, 0);
    }

    boolean checkHas(ASTNode Node, int blockID) {
        Block temp = IRBlockMap.getBlockMap().get(blockID);

        while (temp != null) {

            if (temp.Identifiers.containsKey(Node.getToken().getValue()))
                return true;
            temp = temp.Father;
        }
        return false;
    }

    public void Generator(ASTNode Node) throws ERR {

        IRBlockMap.getBlockMap().put(0, new Block(0, null, 0));


        this.TargetCode = new ArrayList<>();


        Initial();//block0的变量分配空间


        defFunc();//输出所有的函数定义，包括函数调用


        printTargetCode(Node, 0);//生成中间代码

        for (String s : TargetCode
        ) {
            System.out.println(s);
        }
    }

    public void Initial() {
        Block block = BlockMap.getBlockMap().get(0);
        for (Identifier ident : block.Identifiers.values()
        ) {
            if (ident.Dimension.size() == 1) {
                if (ident.type == IdentType.Variable) {

                    this.register.put(ident, ident.name);
                    IRBlockMap.getBlockMap().get(0).Identifiers.put(ident.name,
                            ident);
                    if (ident.value == null) {
                        ident.value = 0;
                    }
                    TargetCode.add("@" + ident.name + " = dso_local global i32 " + ident.value);
                } else {
                    IRBlockMap.getBlockMap().get(0).Identifiers.put(ident.name,
                            ident);
                }
            } else {
                if (ident.type == IdentType.Variable) {

                    this.register.put(ident, ident.name);
                    IRBlockMap.getBlockMap().get(0).Identifiers.put(ident.name,
                            ident);
                    TargetCode.add("@" + ident.name + " = dso_local global" + arrayInit(ident.Dimension, ident.arrayValue));
                } else {
                    this.register.put(ident, ident.name);
                    IRBlockMap.getBlockMap().get(0).Identifiers.put(ident.name,
                            ident);
                    TargetCode.add("@" + ident.name + " = dso_local constant" + arrayInit(ident.Dimension, ident.arrayValue));
                }
            }

        }
    }

    public StringBuilder printArrayDimen(ArrayList<Integer> Dimension, ArrayList<Integer> arrayValue, Integer point) {
        StringBuilder result = new StringBuilder();

        ArrayList<Integer> newDimen = new ArrayList<>();
        for (int i = 1; i < Dimension.size(); i++) {
            result.append("[").append(Dimension.get(i)).append(" x ");
            newDimen.add(Dimension.get(i));
        }
        result.append("i32");
        result.append("]".repeat(Math.max(0, Dimension.size() - 1)));
        result.append(" [");

        if (Dimension.size() <= 2) {
            result.append("i32 ").append(arrayValue.get(point++));
            for (int i = 1; i < Dimension.get(1); i++) {
                result.append(",").append("i32 ").append(arrayValue.get(point++));
            }
        } else {
            result.append(printArrayDimen(newDimen, arrayValue, point++));
            for (int i = 1; i < Dimension.get(1); i++) {
                result.append(",").append(printArrayDimen(newDimen, arrayValue, point++));
            }
        }
        result.append("]");
        return result;
    }

    public String arrayInit(ArrayList<Integer> Dimension, ArrayList<Integer> arrayValue) {
        StringBuilder result = new StringBuilder();

        if (arrayValue.size() == 0) {
            result.append(" ");
            for (int i = 1; i < Dimension.size(); i++) {
                result.append("[").append(Dimension.get(i)).append(" x ");
            }
            result.append("i32");
            result.append("]".repeat(Math.max(0, Dimension.size() - 1)));
            result.append(" zeroinitializer");
        } else {
            result = printArrayDimen(Dimension, arrayValue, 0);
        }
        return result.toString();
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

    public void printTargetCode(ASTNode Node, Integer blockID) throws ERR {

        if (utils.checkTokenValue(Node, "funcDef")) {
            printFuncDef(Node, blockID);
        } else if (utils.checkTokenValue(Node, "Stmt")) {
            printStmt(Node, blockID);
        } else if (utils.checkTokenValue(Node, "ConstDef")) {
            if (blockID != 0)
                ConstDef(Node, blockID);
        } else if (utils.checkTokenValue(Node, "VarDef")) {
            if (blockID != 0)
                printVarDef(Node, blockID);
        } else {
            if (utils.checkTokenValue(Node, "Block")) {
                IRBlockMap.getBlockMap().put(++BID, new Block(BID, IRBlockMap.getBlockMap().get(blockID), 0));
                blockID = BID;
            }
            for (ASTNode node : Node.getNodeList()
            ) {
                printTargetCode(node, blockID);
                if (utils.checkTokenValue(Node, "Block") && utils.checkTokenValue(node, "{")) {
                    printIdentifier(blockID);
                }
            }
        }
    }

    private void ConstDef(ASTNode Node, int blockID) throws ERR {
        ASTNode Ident = Node.getNodeList().get(0);
        String IdentName = Node.getNodeList().get(0).getNodeList().get(0).getToken().getValue();
        if (!checkHas(Ident.getNodeList().get(0), blockID)) {

            if (Node.getNodeList().get(1).getToken().getValue().equals("[")) {
                IRBlockMap.getBlockMap().get(blockID).Identifiers.put(IdentName, BlockMap.getBlockMap().get(blockID).Identifiers.get(IdentName));

                Identifier ident = BlockMap.getBlockMap().get(blockID).Identifiers.get(IdentName);
                this.regPoint++;

                TargetCode.add("%" + regPoint + " = getelementptr " + printArrayType(ident.Dimension) + " ," + printArrayType(ident.Dimension) +
                        "* %" + register.get(ident) + ", i32 0" + ", i32 0");
                AllocaArray(ArrayCutHead(ident.Dimension), ident.arrayValue, 0);

            } else {
                IRBlockMap.getBlockMap().get(blockID).Identifiers.put(IdentName,
                        new Identifier(ConstInitVal(Node.getNodeList().get(2), blockID),
                                IdentName,
                                IdentType.Constant));
            }
        } else {
            throw new ERR("常量定义重复");
        }
    }

    public ArrayList<Integer> ArrayCutHead(ArrayList<Integer> array) {
        ArrayList<Integer> temp = new ArrayList<>();
        for (int i = 1; i < array.size(); i++) {
            temp.add(array.get(i));
        }
        return temp;
    }

    public void AllocaArray(ArrayList<Integer> Dimension, ArrayList<Integer> arrayValue, int point) {


        if (Dimension.size() <= 2) {
            TargetCode.add("%" + (regPoint + 1) + " = getelementptr " + printArrayType(Dimension) + " ," + printArrayType(Dimension) +
                    "* %" + (regPoint++) + ", i32 0" + ", i32 0");
            TargetCode.add("store i32 " + arrayValue.get(0) + ", i32* %" + regPoint);
            for (int i = 1; i < arrayValue.size(); i++) {
                TargetCode.add("%" + (regPoint + 1) + " = getelementptr i32,i32* %" + (regPoint++) + ", i32 " + (i));
                TargetCode.add("store i32 " + arrayValue.get(i) + ", i32* %" + regPoint);
            }
        } else {
            for (int i = 1; i <= Dimension.get(0); i++) {
                TargetCode.add("%" + (regPoint + 1) + " = getelementptr " + printArrayType(Dimension) + " ," + printArrayType(Dimension) +
                        "* %" + (regPoint++) + ", i32 0" + ", i32 0");
                AllocaArray(ArrayCutHead(Dimension), arrayValue, point);
            }
        }

    }

    //给变量分配空间
    public void printIdentifier(Integer Id) {

        Block block = BlockMap.getBlockMap().get(Id);
        for (Identifier ident : block.Identifiers.values()
        ) {
            if (ident.type == IdentType.Variable) {
                this.regPoint++;
                this.register.put(ident, regPoint.toString());
                TargetCode.add("%" + regPoint + " = alloca " + printArrayType(ident.Dimension));

            } else if (ident.type == IdentType.Constant && ident.Dimension.size() > 1) {
                this.regPoint++;
                this.register.put(ident, regPoint.toString());

                TargetCode.add("%" + regPoint + " = alloca " + printArrayType(ident.Dimension));
            }
        }
    }

    public StringBuilder printArrayType(ArrayList<Integer> dimension) {
        StringBuilder result = new StringBuilder();
        for (int i = 1; i < dimension.size(); i++) {
            result.append("[").append(dimension.get(i)).append(" x ");
        }
        result.append("i32");
        result.append("]".repeat(Math.max(0, dimension.size() - 1)));
        return result;
    }

    //输出变量定义的中间代码(int a=3)
    public void printVarDef(ASTNode Node, Integer blockID) throws ERR {
        String IdentName = Node.getNodeList().get(0).getNodeList().get(0).getToken().getValue();
        if (!IRBlockMap.getBlockMap().get(blockID).Identifiers.containsKey(Node.getNodeList().get(0).getNodeList().get(0).getToken().getValue())) {

            IRBlockMap.getBlockMap().get(blockID).Identifiers.put(IdentName, BlockMap.getBlockMap().get(blockID).Identifiers.get(IdentName));

        } else {
            throw new ERR("变量定义重复");
        }

        if (Node.getNodeList().size() >= 2 && Node.getNodeList().get(Node.getNodeList().size() - 2).getToken().getValue().equals("=")) {
            if (Node.getNodeList().get(1).getToken().getValue().equals("[")) {
                Identifier ident = BlockMap.getBlockMap().get(blockID).Identifiers.get(IdentName);

                ident.arrayValue = ConstInitValArray(Node.getNodeList().get(Node.getNodeList().size() - 1), ident.Dimension, blockID, "InitVal");

                this.regPoint++;
                TargetCode.add("%" + regPoint + " = getelementptr " + printArrayType(ident.Dimension) + " ," + printArrayType(ident.Dimension) +
                        "* %" + register.get(ident) + ", i32 0" + ", i32 0");
                AllocaArray(ArrayCutHead(ident.Dimension), ident.arrayValue, 0);

            } else {
                Identifier key = utils.searchKey(Node.getNodeList().get(0).getNodeList().get(0).getToken().getValue(), blockID);
                if (key.globle == 1) {
                    TargetCode.add("store i32 " + printExp(Node.getNodeList().get(2).getNodeList().get(0), blockID).print() + ", i32* @" + register.get(key));
                } else {
                    TargetCode.add("store i32 " + printExp(Node.getNodeList().get(2).getNodeList().get(0), blockID).print() + ", i32* %" + register.get(key));
                }
            }
        } else {
            Identifier ident = BlockMap.getBlockMap().get(blockID).Identifiers.get(IdentName);

            int temp = 1;
            for (int i = 1; i < ident.Dimension.size(); i++) {
                temp *= ident.Dimension.get(i);
            }
            for (int i = 1; i <= temp; i++)
                ident.arrayValue.add(0);
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

    //输出函数定义的中间代码(int main())
    public void printFuncDef(ASTNode Node, Integer blockID) throws ERR {
        ArrayList<ASTNode> List = Node.getNodeList();
        TargetCode.add("define dso_local i32 @" + List.get(1).getNodeList().get(0).getToken().getValue() + "(){");
        printTargetCode(List.get(4), blockID);
        TargetCode.add("}");
    }

    public void printStmt(ASTNode Node, Integer blockID) throws ERR {
        if (Node.getNodeList().get(0).getToken().getSymbolType() == SymbolType.RETURNTK) {
            TargetCode.add("ret i32 " + printExp(Node.getNodeList().get(1), blockID).print());
            regPoint++;
        } else if (Node.getNodeList().get(0).getToken().getValue().equals("LVal")) {
            Identifier key = utils.searchKey(Node.getNodeList().get(0).getNodeList().get(0).getNodeList().get(0).getToken().getValue(), blockID);
            if (key.Dimension.size() > 1) {
                regValue reg;
                reg = new regValue(register.get(key), true, key.name);
                StringBuilder locate = new StringBuilder();
                locate.append(", i32 0");
                int point = 2;
                for (int i = 1; i < key.Dimension.size(); i++) {
                    locate.append(", i32 ").append(printExp(Node.getNodeList().get(0).getNodeList().get(point), blockID).print());
                    point += 3;
                }
                regPoint++;
                int hold = regPoint;
                TargetCode.add("%" + regPoint + " = getelementptr " + printArrayType(key.Dimension) + ", " + printArrayType(key.Dimension) + "* " + reg.print() + locate.toString());


                TargetCode.add("store i32 " + printExp(Node.getNodeList().get(2), blockID).print()
                        + ", i32* %" + hold);


            } else {
                if (key.globle == 1) {
                    TargetCode.add("store i32 " + printExp(Node.getNodeList().get(2), blockID).print()
                            + ", i32* @" + register.get(key));
                } else {
                    TargetCode.add("store i32 " + printExp(Node.getNodeList().get(2), blockID).print()
                            + ", i32* %" + register.get(key));
                }
            }


        } else if (Node.getNodeList().get(0).getToken().getValue().equals("if")) {

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


        } else if (Node.getNodeList().get(0).getToken().getValue().equals("while")) {

            int mark;
            TargetCode.add("br label %" + (++regPoint));
            TargetCode.add("");
            TargetCode.add(regPoint + ":");
            int hold = regPoint;
            TargetCode.add("br i1 " + printCond(Node.getNodeList().get(2), blockID).print() + ",label %" + (++regPoint) + ",label %");


            this.whileBlock = new whileBlock(this.whileBlock, regPoint);


            mark = TargetCode.size();
            TargetCode.add("");
            TargetCode.add(regPoint + ":");
            printStmt(Node.getNodeList().get(4), blockID);
            TargetCode.set(mark - 1, TargetCode.get(mark - 1) + (++regPoint));

            for (int i : whileBlock.breakLocate) {
                TargetCode.set(i - 1, TargetCode.get(i - 1) + (regPoint));
            }

            TargetCode.add("br label %" + (hold));
            TargetCode.add("");
            TargetCode.add(regPoint + ":");

            this.whileBlock = whileBlock.Father;
        } else if (Node.getNodeList().get(0).getToken().getValue().equals("Block")) {
            printTargetCode(Node.getNodeList().get(0), blockID++);
        } else if (Node.getNodeList().get(0).getToken().getValue().equals("continue")) {
            TargetCode.add("br label %" + this.whileBlock.blockPoint);
            regPoint++;
        } else if (Node.getNodeList().get(0).getToken().getValue().equals("break")) {
            TargetCode.add("br label %");
            regPoint++;
            this.whileBlock.breakLocate.add(TargetCode.size());
        } else if (Node.getNodeList().get(0).getToken().getValue().equals(";")) {

        } else {
            printExp(Node.getNodeList().get(0), blockID);
        }
    }

    public String ty(ASTNode Node) {
        String s = "";
        switch (Node.getToken().getValue()) {
            case ">=":
                s = "sge";
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
            temp1 = new regValue(regPoint.toString(), true, null);
            for (int i = 4; i < Node.getNodeList().size() - 1; i += 2) {

                temp2 = printAddExp(Node.getNodeList().get(i), blockID);
                TargetCode.add("%" + (++regPoint) + " = or i1 " +
                        temp1.print() + ", " + temp2.print());
                temp1 = new regValue(regPoint.toString(), true, null);
            }

            reg = new regValue(regPoint.toString(), true, null);
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
            temp1 = new regValue(regPoint.toString(), true, null);
            for (int i = 4; i < Node.getNodeList().size() - 1; i += 2) {

                temp2 = printAddExp(Node.getNodeList().get(i), blockID);
                TargetCode.add("%" + (++regPoint) + " = and i1 " +
                        temp1.print() + ", " + temp2.print());
                temp1 = new regValue(regPoint.toString(), true, null);
            }

            reg = new regValue(regPoint.toString(), true, null);
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
            temp1 = new regValue(regPoint.toString(), true, null);
            for (int i = 4; i < Node.getNodeList().size() - 1; i += 2) {

                temp2 = printAddExp(Node.getNodeList().get(i), blockID);
                TargetCode.add("%" + (++regPoint) + " = icmp " + ty(Node.getNodeList().get(i - 1)) + " i32 " +
                        temp1.print() + ", " + temp2.print());
                temp1 = new regValue(regPoint.toString(), true, null);
            }

            reg = new regValue(regPoint.toString(), true, null);
            return reg;
        } else if (Node.getNodeList().size() == 1 && Node.getNodeList().get(0).getNodeList().size() > 1) {
            return printRelExp(Node.getNodeList().get(0), blockID);
        } else {
            String s = printAddExp(Node.getNodeList().get(0).getNodeList().get(0), blockID).print();
            TargetCode.add("%" + (++regPoint) + " = icmp ne i32 " +
                    s + ", 0");
            return new regValue(regPoint.toString(), true, null);
        }
    }

    public regValue printRelExp(ASTNode Node, Integer blockID) throws ERR {
        regValue reg;

        if (Node.getNodeList().size() > 1) {
            regValue temp1 = printAddExp(Node.getNodeList().get(0), blockID);
            regValue temp2 = printAddExp(Node.getNodeList().get(2), blockID);
            TargetCode.add("%" + (++regPoint) + " = icmp " + ty(Node.getNodeList().get(1)) + " i32 " +
                    temp1.print() + "," + temp2.print());
            temp1 = new regValue(regPoint.toString(), true, null);
            for (int i = 4; i < Node.getNodeList().size() - 1; i += 2) {

                temp2 = printAddExp(Node.getNodeList().get(i), blockID);
                TargetCode.add("%" + (++regPoint) + " = icmp " + ty(Node.getNodeList().get(i - 1)) + " i32 " +
                        temp1.print() + ", " + temp2.print());
                temp1 = new regValue(regPoint.toString(), true, null);
            }
        } else {
            return printAddExp(Node.getNodeList().get(0), blockID);
        }
        reg = new regValue(regPoint.toString(), true, null);

        return reg;
    }

    public regValue printExp(ASTNode Node, Integer blockID) throws ERR {
        return printAddExp(Node.getNodeList().get(0), blockID);
    }

    public regValue printAddExp(ASTNode Node, Integer blockID) throws ERR {
        regValue reg;
        int p = 0;
        ArrayList<ASTNode> List = Node.getNodeList();
        reg = printMulExp(List.get(p++), blockID);

        while (p < Node.getNodeList().size()) {
            if (List.get(p).getToken().getSymbolType() == SymbolType.MINU) {
                regValue temp = printMulExp(List.get(++p), blockID);
                TargetCode.add("%" + (++regPoint) + " = sub i32 " + reg.print() + ", " + temp.print());
                reg = new regValue(regPoint.toString(), true, null);
            } else if (List.get(p).getToken().getSymbolType() == SymbolType.PLUS) {
                regValue temp = printMulExp(List.get(++p), blockID);
                TargetCode.add("%" + (++regPoint) + " = add i32 " + reg.print() + ", " + temp.print());
                reg = new regValue(regPoint.toString(), true, null);
            }
            p++;
        }
        return reg;
    }

    public regValue printMulExp(ASTNode Node, Integer blockID) throws ERR {
        regValue reg;
        int p = 0;
        ArrayList<ASTNode> List = Node.getNodeList();
        reg = printUnaryExp(List.get(p++), blockID);
        while (p < Node.getNodeList().size()) {
            if (List.get(p).getToken().getSymbolType() == SymbolType.MULT) {
                regValue temp = printUnaryExp(List.get(++p), blockID);
                TargetCode.add("%" + (++regPoint) + " = mul i32 " + reg.print() + ", " + temp.print());
                reg = new regValue(regPoint.toString(), true, null);
            } else if (List.get(p).getToken().getSymbolType() == SymbolType.DIV) {
                regValue temp = printUnaryExp(List.get(++p), blockID);
                TargetCode.add("%" + (++regPoint) + " = sdiv i32 " + reg.print() + ", " + temp.print());
                reg = new regValue(regPoint.toString(), true, null);
            } else if (List.get(p).getToken().getSymbolType() == SymbolType.MOD) {
                regValue temp = printUnaryExp(List.get(++p), blockID);
                TargetCode.add("%" + (++regPoint) + " = srem i32 " + reg.print() + ", " + temp.print());
                reg = new regValue(regPoint.toString(), true, null);
            }
            p++;
        }
        return reg;
    }

    public regValue printUnaryExp(ASTNode Node, Integer blockID) throws ERR {
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
            reg = new regValue(regPoint.toString(), true, null);
        } else if (func.getToken().getSymbolType() == SymbolType.PUTCH || func.getToken().getSymbolType() == SymbolType.PUTINT) {
            TargetCode.add("call void @" + func.getToken().getValue() + "(i32 " + printExp(List.get(List.size() - 2), blockID).print() + ")");
            reg = new regValue(regPoint.toString(), true, null);
        } else {
            reg = printPrimaryExp(List.get(List.size() - 1), blockID);
        }
        if (op == -1) {
            regPoint++;
            TargetCode.add("%" + regPoint + " = sub i32 0, " + reg.print());
            reg = new regValue(regPoint.toString(), true, null);
        }
        if (att == -1) {
            TargetCode.add("%" + (++regPoint) + " = icmp eq i32 " + reg.print() + ", 0");
            TargetCode.add("%" + (regPoint + 1) + " = zext i1 %" + regPoint + " to i32");
            regPoint++;
            reg = new regValue(regPoint.toString(), true, null);
        }
        return reg;
    }

    public regValue printPrimaryExp(ASTNode Node, Integer blockID) throws ERR {

        int p = 0;
        regValue reg;
        ArrayList<ASTNode> List = Node.getNodeList();
        if (List.get(0).getToken().getSymbolType() == SymbolType.LPARENT) {
            return printExp(List.get(p + 1), blockID);
        } else if (List.get(0).getToken().getValue().equals("LVal")) {
            Identifier key = utils.searchKey(Node.getNodeList().get(0).getNodeList().get(0).getNodeList().get(0).getToken().getValue(), blockID);
            if (key.Dimension.size() > 1) {

                reg = new regValue(register.get(key), true, key.name);
                StringBuilder locate = new StringBuilder();

                int point = 2;
                locate.append(", i32 0");
                for (int i = 1; i < key.Dimension.size(); i++) {
                    locate.append(", i32 ").append(printExp(List.get(0).getNodeList().get(point), blockID).print());
                    point += 3;
                }

                regPoint++;

                TargetCode.add("%" + regPoint + " = getelementptr " + printArrayType(key.Dimension) + ", " + printArrayType(key.Dimension) + "* " + reg.print() + locate.toString());
                TargetCode.add("%" + (regPoint + 1) + " = load i32, i32* " + "%" + (regPoint++));
                reg = new regValue(regPoint.toString(), true, null);


            } else {
                if (key.type == IdentType.Variable) {
                    reg = new regValue(register.get(key), true, null);
                    regPoint++;
                    TargetCode.add("%" + regPoint + " = load i32, i32* " + reg.print());
                    reg = new regValue(regPoint.toString(), true, null);
                } else {
                    reg = new regValue(key.value.toString(), false, null);
                }
            }
        } else {
            reg = new regValue(Semantic.Number(List.get(0)).toString(), false, null);
        }
        return reg;
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
                if (List.get(0).getNodeList().size() > 1 && List.get(0).getNodeList().get(1).getToken().getValue().equals("[")) {
                    Identifier ident = utils.searchKey(Ident.getToken().getValue(), blockID);
                    int locate = 0;
                    for (int i = 1; i < ident.Dimension.size(); i++) {
                        locate += calcuExp(List.get(0).getNodeList().get(3 * i - 1), blockID) * calcuDimen(i, ident.Dimension);
                    }

                    return ident.arrayValue.get(locate);
                } else {
                    return utils.searchKey(Ident.getToken().getValue(), blockID).value;
                }

            } else {
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

    public int calcuDimen(int i, ArrayList<Integer> dimension) {
        int answer = 1;
        for (int k = i + 1; k < dimension.size(); k++) {
            answer *= dimension.get(k);
        }
        return answer;
    }
}




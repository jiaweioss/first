
import java.util.ArrayList;

public class Gramma {
    private int Point;
    private Token currentToken;
    private ArrayList<Token> Tokens;
    private ASTNode AST;

    private int hasNextToken() {
        if (this.Point > this.Tokens.size()) {
            return -1;
        }
        return 0;
    }

    private void nextToken() {
        if (this.Point < this.Tokens.size()) {
            this.currentToken = this.Tokens.get(Point);
            this.Point++;
        } else {
            this.Point++;
            this.currentToken = null;
        }
    }

    public Gramma(ArrayList<Token> Tokens) {
        this.Tokens = Tokens;
        this.Point = 0;
        nextToken();
    }

    public ASTNode analyze() throws ERR {
        AST = compUnit();
        return this.AST;
    }

    private ASTNode compUnit() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "compUnit", 0), new ArrayList<ASTNode>());
        Node.addNode(funcDef());
        return Node;
    }

    private ASTNode funcDef() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "funcDef", 0), new ArrayList<ASTNode>());
        Node.addNode(funcType());
        Node.addNode(Ident());
        if (currentToken.getSymbolType() == SymbolType.LPARENT) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
        } else {
            throw new ERR("main的左括号没了");
        }
        if (currentToken.getSymbolType() == SymbolType.RPARENT) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
        } else {
            throw new ERR("main的右括号没了");
        }
        Node.addNode(Block());
        return Node;
    }

    private ASTNode funcType() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "funcType", 0), new ArrayList<ASTNode>());
        if (currentToken.getSymbolType() == SymbolType.INTTK) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
        } else {
            throw new ERR("Int没了");
        }
        return Node;
    }

    private ASTNode Ident() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "Ident", 0), new ArrayList<ASTNode>());
        if (currentToken.getSymbolType() == SymbolType.MAINTK) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
        } else {
            throw new ERR("main没了");
        }
        return Node;
    }

    private ASTNode Block() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "Block", 0), new ArrayList<ASTNode>());
        if (currentToken.getSymbolType() == SymbolType.LBRACE) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
        } else {
            throw new ERR("{没了");
        }
        Node.addNode(Stmt());
        if (currentToken.getSymbolType() == SymbolType.RBRACE) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
        } else {
            throw new ERR("}没了");
        }
        return Node;
    }

    private ASTNode Stmt() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "Stmt", 0), new ArrayList<ASTNode>());
        if (currentToken.getSymbolType() == SymbolType.RETURNTK) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
        } else {
            throw new ERR("return没了");
        }
        Node.addNode(Exp());
        if (currentToken.getSymbolType() == SymbolType.SEMICN) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
        } else {
            throw new ERR(";没了");
        }
        return Node;
    }

    //以下是处理表达式运算的部分
    private ASTNode Exp() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "Exp", 0), new ArrayList<ASTNode>());
        Node.addNode(AddExp());
        return Node;
    }

    private ASTNode AddExp() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "AddExp", 0), new ArrayList<ASTNode>());
        //
        Node.addNode(MulExp());
        while (currentToken.getSymbolType() == SymbolType.PLUS
                || currentToken.getSymbolType() == SymbolType.MINU) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
            Node.addNode(MulExp());
        }

        return Node;
    }

    private ASTNode MulExp() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "MulExp", 0), new ArrayList<ASTNode>());
        //
        Node.addNode(UnaryExp());
        while (currentToken.getSymbolType() == SymbolType.MULT
                || currentToken.getSymbolType() == SymbolType.DIV
                || currentToken.getSymbolType() == SymbolType.MOD) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
            Node.addNode(UnaryExp());
        }
        return Node;
    }

    private ASTNode UnaryExp() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "UnaryExp", 0), new ArrayList<ASTNode>());
        //

        while (currentToken.getSymbolType() == SymbolType.PLUS
                || currentToken.getSymbolType() == SymbolType.MINU) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
        }
        Node.addNode(PrimaryExp());
        return Node;
    }

    private ASTNode PrimaryExp() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "PrimaryExp", 0), new ArrayList<ASTNode>());
        //
        if (currentToken.getSymbolType() == SymbolType.LPARENT) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();

            Node.addNode(Exp());

            if (currentToken.getSymbolType() == SymbolType.RPARENT) {
                Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                nextToken();
            } else {
                throw new ERR("没有右括号");
            }
        } else if (currentToken.getSymbolType() == SymbolType.OCTNUM ||
                currentToken.getSymbolType() == SymbolType.DECNUM || currentToken.getSymbolType() == SymbolType.HEXNUM) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
        } else {
            throw new ERR("没有number");
        }
        return Node;
    }

    private ASTNode UnaryOp() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "UnaryOp", 0), new ArrayList<ASTNode>());
        //
        if (currentToken.getSymbolType() == SymbolType.PLUS
                || currentToken.getSymbolType() == SymbolType.MINU) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
        } else {
            throw new ERR("+/-错误");
        }
        return Node;
    }
}

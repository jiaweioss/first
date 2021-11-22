
import java.util.ArrayList;

public class Grammar {
    private int Point;
    private Token currentToken;
    private final ArrayList<Token> Tokens;
    
    private void nextToken() {
        if (this.Point < this.Tokens.size()) {
            this.currentToken = this.Tokens.get(Point);
            this.Point++;
        } else {
            this.Point++;
            this.currentToken = null;
        }
    }

    private Token peek(int i) {
        return this.Tokens.get(Point + i - 1);
    }

    public Grammar(ArrayList<Token> Tokens) {
        this.Tokens = Tokens;
        this.Point = 0;
        nextToken();
    }

    public ASTNode analyze() throws ERR {
        return compUnit();
    }

    private ASTNode compUnit() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "compUnit", 0), new ArrayList<>());

        do {
            switch (currentToken.getSymbolType()) {
                case CONSTTK: // 常量定义
                    Node.addNode(Decl());
                    break;
                case INTTK:  // 变量定义, 函数定义, 主函数定义
                    Token tempToken = peek(1);
                    Token tempToken2 = peek(2);

                    if (tempToken.getSymbolType() == SymbolType.IDENT) {
                        if (tempToken2.getSymbolType() == SymbolType.LPARENT) {
                            Node.addNode(funcDef());
                        } else {
                            Node.addNode(Decl());
                        }
                    } else if (tempToken.getSymbolType() == SymbolType.MAINTK) {
                        Node.addNode(funcDef());
                    }
                    break;
                case VOIDTK: // 函数定义
                    Node.addNode(funcDef());
                    break;
                default:
                    // TODO: ERROR
                    break;
            }
        } while (currentToken.getSymbolType() != SymbolType.CODEEND);
        return Node;
    }

    private ASTNode Decl() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "Decl", 0), new ArrayList<>());
        if (currentToken.getSymbolType() == SymbolType.CONSTTK) {
            Node.addNode(ConstDecl());
        } else if (currentToken.getSymbolType() == SymbolType.INTTK) {
            Node.addNode(VarDecl());
        } else {
            throw new ERR("变量定义有问题");
        }
        return Node;
    }

    private ASTNode ConstDecl() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "ConstDecl", 0), new ArrayList<>());
        Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
        nextToken();
        Node.addNode(Btype());
        Node.addNode(ConstDef());
        while (currentToken.getSymbolType() == SymbolType.COMMA) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
            Node.addNode(ConstDef());
        }
        if (currentToken.getSymbolType() == SymbolType.SEMICN) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
        } else {
            throw new ERR("没有分号");
        }
        return Node;
    }

    private ASTNode Btype() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "Btype", 0), new ArrayList<>());
        if (currentToken.getSymbolType() == SymbolType.INTTK) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
        } else {
            throw new ERR("Btype不正确");
        }
        return Node;
    }

    private ASTNode ConstDef() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "ConstDef", 0), new ArrayList<>());
        Node.addNode(Ident());

        while (currentToken.getSymbolType() == SymbolType.LBRACK) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
            Node.addNode(ConstExp());

            if (currentToken.getSymbolType() == SymbolType.RBRACK) {
                Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                nextToken();
            } else {
                throw new ERR("ConstDef");
            }
        }
        if (currentToken.getSymbolType() == SymbolType.ASSIGN) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
        } else {
            throw new ERR("没有等号");
        }
        Node.addNode(ConstInitVal());
        return Node;
    }

    private ASTNode ConstInitVal() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "ConstInitVal", 0), new ArrayList<>());
        if (currentToken.getSymbolType() == SymbolType.LBRACE) {
            // ConstInitVal → '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
            if (currentToken.getSymbolType() != SymbolType.RBRACE) {
                Node.addNode(ConstInitVal());
                while (currentToken.getSymbolType() == SymbolType.COMMA) {
                    Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                    nextToken();
                    Node.addNode(ConstInitVal());
                }
            }
            if (currentToken.getSymbolType() == SymbolType.RBRACE) {
                Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                nextToken();
            } else {
                throw new ERR("ConstInitVal");
            }
        } else {
            Node.addNode(ConstExp());
        }
        return Node;
    }

    private ASTNode ConstExp() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "ConstExp", 0), new ArrayList<>());
        Node.addNode(AddExp());
        return Node;
    }

    private ASTNode VarDecl() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "VarDecl", 0), new ArrayList<>());
        Node.addNode(Btype());
        Node.addNode(VarDef());
        while (currentToken.getSymbolType() == SymbolType.COMMA) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
            Node.addNode(VarDef());
        }
        if (currentToken.getSymbolType() == SymbolType.SEMICN) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
        } else {
            throw new ERR("没有分号");
        }
        return Node;
    }

    private ASTNode VarDef() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "VarDef", 0), new ArrayList<>());
        Node.addNode(Ident());

        while (currentToken.getSymbolType() == SymbolType.LBRACK) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
            Node.addNode(ConstExp());
            if (currentToken.getSymbolType() == SymbolType.RBRACK) {
                Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                nextToken();
            } else {
                throw new ERR("VarDef");
            }
        }

        if (currentToken.getSymbolType() == SymbolType.ASSIGN) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
            Node.addNode(InitVal());
        }
        return Node;
    }

    private ASTNode InitVal() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "InitVal", 0), new ArrayList<>());
        if (currentToken.getSymbolType() == SymbolType.LBRACE) {
            // ConstInitVal → '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
            if (currentToken.getSymbolType() != SymbolType.RBRACE) {
                Node.addNode(InitVal());
                while (currentToken.getSymbolType() == SymbolType.COMMA) {
                    Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                    nextToken();
                    Node.addNode(InitVal());
                }
            }
            if (currentToken.getSymbolType() == SymbolType.RBRACE) {
                Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                nextToken();
            } else {
                throw new ERR("InitVal");
            }
        } else {
            Node.addNode(Exp());
        }
        return Node;
    }

    private ASTNode funcDef() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "funcDef", 0), new ArrayList<>());
        Node.addNode(funcType());
        Node.addNode(Ident());
        if (currentToken.getSymbolType() == SymbolType.LPARENT) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
        } else {
            throw new ERR("funcDef的左括号没了");
        }


        if (currentToken.getSymbolType() == SymbolType.RPARENT) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
        } else {
            Node.addNode(FuncFParams());
            if (currentToken.getSymbolType() == SymbolType.RPARENT) {
                Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                nextToken();
            } else {
                throw new ERR("funcDef的右括号没了");
            }
        }
        Node.addNode(Block());
        return Node;
    }

    private ASTNode FuncFParams() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "FuncFParams", 0), new ArrayList<>());
        Node.addNode(FuncFParam());

        while (currentToken.getSymbolType() == SymbolType.COMMA) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
            Node.addNode(FuncFParam());
        }

        return Node;
    }

    private ASTNode FuncFParam() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "FuncFParam", 0), new ArrayList<>());
        Node.addNode(Btype());
        Node.addNode(Ident());
        if (currentToken.getSymbolType() == SymbolType.LBRACK) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
            if (currentToken.getSymbolType() == SymbolType.RBRACK) {
                Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                nextToken();
            }
            while (currentToken.getSymbolType() == SymbolType.LBRACK) {
                Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                nextToken();

                //TODO:FuncFParam   -> BType Ident ['[' ']' { '[' Exp ']' }]
                Node.addNode(ConstExp());
                if (currentToken.getSymbolType() == SymbolType.RBRACK) {
                    Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                    nextToken();
                }
            }
        }

        return Node;
    }

    private ASTNode funcType() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "funcType", 0), new ArrayList<>());
        if (currentToken.getSymbolType() == SymbolType.INTTK || currentToken.getSymbolType() == SymbolType.VOIDTK) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
        } else {
            throw new ERR("funcType");
        }
        return Node;
    }

    private ASTNode Ident() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.IDENT, "Ident", 0), new ArrayList<>());
        if (currentToken.getSymbolType() == SymbolType.IDENT) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
        } else {
            System.out.println(currentToken.getValue());
            throw new ERR("main没了");
        }
        return Node;
    }

    private ASTNode Block() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "Block", 0), new ArrayList<>());
        if (currentToken.getSymbolType() == SymbolType.LBRACE) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
        } else {
            throw new ERR("Block:{没了");
        }
        while (currentToken.getSymbolType() != SymbolType.RBRACE) {
            Node.addNode(BlockItem());
        }

        if (currentToken.getSymbolType() == SymbolType.RBRACE) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
        } else {
            throw new ERR("Block:}没了");
        }
        return Node;
    }

    private ASTNode BlockItem() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "BlockItem", 0), new ArrayList<>());
        if (currentToken.getSymbolType() == SymbolType.CONSTTK || currentToken.getSymbolType() == SymbolType.INTTK) {
            Node.addNode(Decl());
        } else {
            Node.addNode(Stmt());
        }
        return Node;
    }

    private ASTNode LVal() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "LVal", 0), new ArrayList<>());
        Node.addNode(Ident());
        while (currentToken.getSymbolType() == SymbolType.LBRACK) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
            Node.addNode(Exp());
            if (currentToken.getSymbolType() == SymbolType.RBRACK) {
                Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                nextToken();
            } else {
                throw new ERR("LVal     ");
            }
        }
        return Node;
    }

    private ASTNode Stmt() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "Stmt", 0), new ArrayList<>());


//            //[Exp] ';' 表示可以有一个Exp，也可以仅有一个分号
//            if (currentToken.getSymbolType() != SymbolType.SEMICN) {
//                Node.addNode(Exp());
//            }

        switch (currentToken.getSymbolType()) {
            case RETURNTK:
                Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                nextToken();
                Node.addNode(Exp());
                if (currentToken.getSymbolType() == SymbolType.SEMICN) {
                    Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                    nextToken();
                } else {
                    throw new ERR("Stmt:;没了");
                }
                break;
            case IDENT:

                if (Tokens.get(Point).getSymbolType() == SymbolType.ASSIGN) {
                    Node.addNode(LVal());
                    if (currentToken.getSymbolType() == SymbolType.ASSIGN) {
                        Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                        nextToken();
                    } else {
                        throw new ERR("Stmt:=");
                    }
                    Node.addNode(Exp());
                    if (currentToken.getSymbolType() == SymbolType.SEMICN) {
                        Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                        nextToken();
                    } else {
                        throw new ERR("Stmt:;没了");
                    }
                } else {
                    Node.addNode(Exp());
                    if (currentToken.getSymbolType() == SymbolType.SEMICN) {
                        Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                        nextToken();
                    } else {
                        throw new ERR("Stmt:;没了");
                    }
                }


                break;
            case SEMICN:
                Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                nextToken();
                break;
            case BREAKTK:
            case CONTINUETK:
                Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                nextToken();
                if (currentToken.getSymbolType() == SymbolType.SEMICN) {
                    Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                    nextToken();
                } else {
                    throw new ERR("Stmt:;没了");
                }
                break;
            case LBRACE:
                Node.addNode(Block());
                break;
            case WHILETK:
                Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                nextToken();
                if (currentToken.getSymbolType() == SymbolType.LPARENT) {
                    Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                    nextToken();
                } else {
                    throw new ERR("Stmt:;没了");
                }
                Node.addNode(Cond());
                if (currentToken.getSymbolType() == SymbolType.RPARENT) {
                    Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                    nextToken();
                } else {
                    System.out.println(currentToken.getValue());
                    throw new ERR("Stmt:;没了");
                }
                Node.addNode(Stmt());
                break;
            case IFTK:
                Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                nextToken();
                if (currentToken.getSymbolType() == SymbolType.LPARENT) {
                    Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                    nextToken();
                } else {
                    throw new ERR("Stmt:;没了");
                }
                Node.addNode(Cond());
                if (currentToken.getSymbolType() == SymbolType.RPARENT) {
                    Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                    nextToken();
                } else {
                    System.out.println(currentToken.getValue());
                    throw new ERR("Stmt:;没了");
                }
                Node.addNode(Stmt());
                if (currentToken.getSymbolType() == SymbolType.ELSETK) {
                    Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                    nextToken();
                    Node.addNode(Stmt());
                }
                break;
            default:
                Node.addNode(Exp());
                if (currentToken.getSymbolType() == SymbolType.SEMICN) {
                    Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                    nextToken();
                } else {
                    throw new ERR("Stmt:;没了");
                }
                break;
        }
        return Node;
    }

    private ASTNode Cond() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "Cond", 0), new ArrayList<>());
        Node.addNode(LOrExp());
        return Node;
    }

    private ASTNode LOrExp() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "LorExp", 0), new ArrayList<>());
        Node.addNode(LAndExp());
        while (currentToken.getValue().equals("||")) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
            Node.addNode(LAndExp());
        }
        return Node;
    }

    private ASTNode LAndExp() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "LAndExp", 0), new ArrayList<>());
        Node.addNode(EqExp());
        while (currentToken.getValue().equals("&&")) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
            Node.addNode(EqExp());
        }
        return Node;
    }

    private ASTNode EqExp() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "EqExp", 0), new ArrayList<>());
        Node.addNode(RelExp());
        while (currentToken.getValue().equals("==") || currentToken.getValue().equals("!=")) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
            Node.addNode(RelExp());
        }
        return Node;
    }

    private ASTNode RelExp() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "RelExp", 0), new ArrayList<>());
        Node.addNode(AddExp());
        while (currentToken.getValue().equals(">") || currentToken.getValue().equals("<") ||
                currentToken.getValue().equals("<=") || currentToken.getValue().equals(">=")) {
            Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
            nextToken();
            Node.addNode(AddExp());
        }
        return Node;
    }

    private ASTNode Exp() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "Exp", 0), new ArrayList<>());
        Node.addNode(AddExp());
        return Node;
    }

    private ASTNode AddExp() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "AddExp", 0), new ArrayList<>());
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
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "MulExp", 0), new ArrayList<>());
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
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "UnaryExp", 0), new ArrayList<>());
        //
        if (currentToken.getSymbolType() == SymbolType.GETINT || currentToken.getSymbolType() == SymbolType.GETCH) {
            if (Tokens.get(Point).getSymbolType() == SymbolType.LPARENT) {
                Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                nextToken();
                Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                nextToken();
                if (currentToken.getSymbolType() == SymbolType.RPARENT) {
                    Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                    nextToken();
                } else {
                    throw new ERR("函数定义)");
                }
            }
        } else if (currentToken.getSymbolType() == SymbolType.PUTINT || currentToken.getSymbolType() == SymbolType.PUTCH) {
            if (Tokens.get(Point).getSymbolType() == SymbolType.LPARENT) {
                Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                nextToken();
                Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                nextToken();

                while (currentToken.getSymbolType() != SymbolType.RPARENT) {
                    Node.addNode(Exp());
                    while (currentToken.getSymbolType() == SymbolType.COMMA) {
                        Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                        nextToken();
                        Node.addNode(Exp());
                    }
                }

                if (currentToken.getSymbolType() == SymbolType.RPARENT) {
                    Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                    nextToken();
                } else {
                    throw new ERR("函数定义)");
                }
            }
        } else {
            while (currentToken.getSymbolType() == SymbolType.PLUS
                    || currentToken.getSymbolType() == SymbolType.MINU || currentToken.getSymbolType() == SymbolType.NOT) {
                Node.addNode(new ASTNode(currentToken, new ArrayList<>()));
                nextToken();
            }
            Node.addNode(PrimaryExp());

        }
        return Node;
    }

    private ASTNode PrimaryExp() throws ERR {
        ASTNode Node = new ASTNode(new Token(SymbolType.NONE, "PrimaryExp", 0), new ArrayList<>());
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
        } else if (currentToken.getSymbolType() == SymbolType.IDENT) {
            Node.addNode(LVal());
        } else {
            throw new ERR("没有number");
        }
        return Node;
    }
}

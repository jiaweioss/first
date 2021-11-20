import java.util.ArrayList;

public class Lexical {

    private final StringBuilder Test;
    private char currentChar;
    private int Point;
    private int lineNum;
    private final ArrayList<Token> Tokens;

    public Lexical(StringBuilder text) {
        this.Tokens = new ArrayList<>();
        this.Test = text;
        this.Point = 0;
        this.lineNum = 1;
        nextChar();
    }

    private int hasNextChar() {
        if (this.Point > this.Test.length()) {
            return -1;
        }
        return 0;
    }

    private void nextChar() {
        if (this.Point < this.Test.length()) {
            this.currentChar = this.Test.charAt(Point);
            this.Point++;
        } else {
            this.Point++;
            this.currentChar = '\0';
        }

    }

    private char preRead() {
        if (this.Point <= this.Test.length() - 2)
            return this.Test.charAt(Point);
        else {
            //#为错误字符
            return '\0';
        }
    }

    private void setToken(String token, SymbolType symbolType) {
        this.Tokens.add(new Token(symbolType, token, this.lineNum));
    }

    private void addLineNum() {
        this.lineNum++;
    }

    public ArrayList<Token> analyze() throws ERR {
        while (hasNextChar() != -1) {
            //IDENT识别
            SymbolType symbolType;
            if (Character.isLetter(currentChar) || currentChar == '_') {
                StringBuilder temp = new StringBuilder();
                while (Character.isDigit(currentChar) || Character.isLetter(currentChar) || currentChar == '_') {
                    temp.append(currentChar);
                    nextChar();
                }
                symbolType = Keyword.keywordMap.getOrDefault(temp.toString(), SymbolType.IDENT);
                if(temp.toString().equals("getint")&&currentChar=='('){
                    if(funcMap.getfuncMap().get("getint")==null)
                    funcMap.getfuncMap().put("getint",new func("i32","getint"));
                    symbolType = SymbolType.GETINT;
                }else if(temp.toString().equals("getch")&&currentChar=='('){
                    if(funcMap.getfuncMap().get("getch")==null)
                        funcMap.getfuncMap().put("getch",new func("i32","getch"));
                    symbolType = SymbolType.GETCH;
                }else if(temp.toString().equals("putint")&&currentChar=='('){
                    if(funcMap.getfuncMap().get("putint")==null)
                        funcMap.getfuncMap().put("putint",new func("void","putint"));
                    symbolType = SymbolType.PUTINT;
                }else if(temp.toString().equals("putch")&&currentChar=='('){
                    if(funcMap.getfuncMap().get("putch")==null)
                        funcMap.getfuncMap().put("putch",new func("void","putch"));
                    symbolType = SymbolType.PUTCH;
                }
                setToken(temp.toString(), symbolType);
            } else if (currentChar == '\n') {
                addLineNum();
                nextChar();
            }
            //NUMBER识别
            else if (Character.isDigit(currentChar)) {
                StringBuilder temp = new StringBuilder();
                if ((currentChar == '0' && preRead() == 'x') || currentChar == '0' && preRead() == 'X') {
                    nextChar();
                    nextChar();
                    while (Character.isDigit(currentChar) || (currentChar >= 'A' && currentChar <= 'F')||(currentChar >= 'a' && currentChar <= 'f')) {
                        temp.append(currentChar);
                        nextChar();
                    }
                    symbolType = SymbolType.HEXNUM;
                    setToken(temp.toString(), symbolType);
                } else if (currentChar == '0' && Character.isDigit(preRead())) {
                    nextChar();
                    while (Character.isDigit(currentChar)) {
                        temp.append(currentChar);
                        nextChar();
                    }
                    symbolType = SymbolType.OCTNUM;
                    setToken(temp.toString(), symbolType);
                } else {
                    while (Character.isDigit(currentChar)) {
                        temp.append(currentChar);
                        nextChar();
                    }
                    symbolType = SymbolType.DECNUM;
                    setToken(temp.toString(), symbolType);
                }
            } else {
                switch (currentChar) {
                    case '!':
                        if (preRead() == '=') {
                            nextChar();
                            symbolType = SymbolType.NEQ;
                            setToken("!=", symbolType);

                        } else {
                            symbolType = SymbolType.NOT;
                            setToken("!", symbolType);

                        }
                        break;
                    case '&':
                        nextChar(); // &&
                        if (currentChar == '&') {
                            symbolType = SymbolType.AND;
                            setToken("&&", symbolType);
                        } else {
                            throw new ERR("&&符号错误");
                        }
                        break;
                    case '|':
                        nextChar(); // &&
                        if (currentChar == '|') {
                            symbolType = SymbolType.OR;
                            setToken("||", symbolType);
                        } else {
                            throw new ERR("||符号错误");
                        }
                        break;
                    case '+':
                        symbolType = SymbolType.PLUS;
                        setToken("+", symbolType);
                        break;
                    case '-':

                        symbolType = SymbolType.MINU;
                        setToken("-", symbolType);
                        break;
                    case '*':
                        symbolType = SymbolType.MULT;
                        setToken("*", symbolType);
                        break;
                    case '/':
                        if (preRead() == '/') {
                            while (preRead() != '\n' && preRead() != '\0') {
                                nextChar();
                            }
                            nextChar();
                            addLineNum();
                        } else if (preRead() == '*') {
                            nextChar();
                            while (!(currentChar == '*' && preRead() == '/')) {
                                if (currentChar == '\n') addLineNum();
                                nextChar();
                            }
                            nextChar();
                        } else {
                            symbolType = SymbolType.DIV;
                            setToken("/", symbolType);
                        }
                        break;
                    case '%':
                        symbolType = SymbolType.MOD;
                        setToken("%", symbolType);
                        break;
                    case '<':
                        if (preRead() == '=') {
                            nextChar();
                            symbolType = SymbolType.LEQ;
                            setToken("<=", symbolType);
                        } else {
                            nextChar();
                            symbolType = SymbolType.LSS;
                            setToken("<", symbolType);
                        }
                        break;
                    case '>':
                        if (preRead() == '=') {
                            nextChar();
                            symbolType = SymbolType.GEQ;
                        } else {
                            symbolType = SymbolType.GRE;
                        }
                        setToken(">=", symbolType);
                        break;
                    case '=':
                        if (preRead() == '=') {
                            nextChar();
                            symbolType = SymbolType.EQL;
                            setToken("==", symbolType);
                        } else {
                            symbolType = SymbolType.ASSIGN;
                            setToken("=", symbolType);
                        }
                        break;
                    case ';':
                        symbolType = SymbolType.SEMICN;
                        setToken(";", symbolType);
                        break;
                    case ',':
                        symbolType = SymbolType.COMMA;
                        setToken(",", symbolType);
                        break;
                    case '(':
                        symbolType = SymbolType.LPARENT;
                        setToken("(", symbolType);
                        break;
                    case ')':
                        symbolType = SymbolType.RPARENT;
                        setToken(")", symbolType);
                        break;
                    case '[':
                        symbolType = SymbolType.LBRACK;
                        setToken("]", symbolType);
                        break;
                    case ']':
                        symbolType = SymbolType.RBRACK;
                        setToken("[", symbolType);
                        break;
                    case '{':
                        symbolType = SymbolType.LBRACE;
                        setToken("{", symbolType);
                        break;
                    case '}':
                        symbolType = SymbolType.RBRACE;
                        setToken("}", symbolType);
                        break;

                    case (char) -1:
//                        symbolType = SymbolType.CODEEND;
                        break;

                    default:
//                        symbolType = SymbolType.ILLEAGAL;
                        break;
                }
                nextChar();
            }
        }
        return Tokens;
    }
}

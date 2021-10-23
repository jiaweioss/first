import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class Lexcical {

    private StringBuilder Test;
    private char currentChar;
    private int Point;
    private int lineNum;
    private SymbolType symbolType;
    private ArrayList<Token> Tokens;

    public Lexcical(StringBuilder text) throws IOException {
        this.Tokens = new ArrayList<Token>();
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
            return this.Test.charAt(Point + 1);
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

    public ArrayList<Token> analyze() throws IOException, ERR {
        while (hasNextChar() != -1) {

            //IDENT识别
            if (Character.isLetter(currentChar) || currentChar == '_') {
                StringBuilder temp = new StringBuilder();
                while (Character.isDigit(currentChar) || Character.isLetter(currentChar) || currentChar == '_') {
                    temp.append(currentChar);
                    nextChar();
                }
                if (Keyword.keywordMap.keySet().contains(temp.toString())) {
                    this.symbolType = Keyword.keywordMap.get(temp.toString());
                } else {
                    symbolType = SymbolType.IDENT;
                }
                setToken(temp.toString(), symbolType);
            }
            else if(currentChar=='\n'){
                addLineNum();
                nextChar();
            }
            //NUMBER识别
            else if (Character.isDigit(currentChar)) {
                StringBuilder temp = new StringBuilder();
                if (currentChar == '0' && preRead() == 'x') {
                    temp.append(currentChar);
                    nextChar();
                    temp.append(currentChar);
                    nextChar();
                    while (Character.isDigit(currentChar) || (currentChar >= 'A' && currentChar <= 'F')) {
                        temp.append(currentChar);
                        nextChar();
                    }
                    symbolType = SymbolType.HEXNUM;
                    setToken(temp.toString(), symbolType);
                } else if (currentChar == '0' && Character.isDigit(preRead())) {
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
                        } else if (currentChar == '*') {
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
                            setToken(">=", symbolType);
                        } else {
                            symbolType = SymbolType.GRE;
                            setToken(">=", symbolType);
                        }
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
                        symbolType = SymbolType.CODEEND;
                        break;

                    default:
                        symbolType = SymbolType.ILLEAGAL;
                        break;
                }
                nextChar();
            }
        }
        return Tokens;
    }
}

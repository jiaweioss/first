public class Token {
    private SymbolType symbolType;
    private String value;
    private int lineNum;

    public Token(SymbolType sym, String v, int l) {
        this.symbolType = sym;
        this.value = v;
        this.lineNum = l;
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    public int getLineNum() {
        return lineNum;
    }

    public String getValue() {
        return value;
    }
}

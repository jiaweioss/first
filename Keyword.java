import java.util.HashMap;

public class Keyword {
    public static HashMap<String, SymbolType> keywordMap = new HashMap<>();
    static {
        keywordMap.put("const", SymbolType.CONSTTK);
        keywordMap.put("int", SymbolType.INTTK);
        keywordMap.put("break", SymbolType.BREAKTK);
        keywordMap.put("continue", SymbolType.CONTINUETK);
        keywordMap.put("if", SymbolType.IFTK);
        keywordMap.put("else", SymbolType.ELSETK);
        keywordMap.put("while", SymbolType.WHILETK);
        keywordMap.put("return", SymbolType.RETURNTK);
        keywordMap.put("void", SymbolType.VOIDTK);
    }
}

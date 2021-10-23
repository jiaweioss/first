public enum SymbolType {
    ILLEAGAL,
    CODEEND,
    ANNOTATION,

    IDENT,             // Ident
    HEXNUM,             // 16进制
    OCTNUM,             // 8进制
    DECNUM,             // 10进制
    STRCON,             // FormatString
    MAINTK,             // main
    CONSTTK,            // const
    INTTK,              // int
    BREAKTK,            // break
    CONTINUETK,         // continue
    IFTK,               // if
    ELSETK,             // else
    NOT,                // !
    AND,                // &&
    OR,                 // ||
    WHILETK,            // while
    GETINTTK,           // getint
    PRINTFTK,           // printf
    RETURNTK,           // return
    PLUS,               // +
    MINU,               // -
    VOIDTK,             // void
    MULT,               // *
    DIV,                // /
    MOD,                // %
    LSS,                // <
    LEQ,                // <=
    GRE,                // >
    GEQ,                // >=
    EQL,                // ==
    NEQ,                // !=
    ASSIGN,             // =
    SEMICN,             // ;
    COMMA,              // ,
    LPARENT,            // (
    RPARENT,            // )
    LBRACK,             // [
    RBRACK,             // ]
    LBRACE,             // {
    RBRACE,             // }
    NONE,
    ;               // nothing

}

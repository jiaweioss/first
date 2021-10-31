public class LLVM {
    private int lineNum;

    public LLVM() {
        this.lineNum = 1;
    }
    public void analyze(ASTNode Node){
        if (Node.getToken().getSymbolType() == SymbolType.INTTK) {
            if (Node.getToken().getLineNum() > this.lineNum) {
                System.out.println();
                this.lineNum++;
            }
            System.out.print("define dso_local i32 ");
        } else if (Node.getToken().getSymbolType() == SymbolType.MAINTK) {
            if (Node.getToken().getLineNum() > this.lineNum) {
                System.out.println();
                this.lineNum++;
            }
            System.out.print("@main ");
        } else if (Node.getToken().getSymbolType() == SymbolType.RETURNTK) {
            if (Node.getToken().getLineNum() > this.lineNum) {
                System.out.println();
                this.lineNum++;
            }
            System.out.print("ret ");
        } else if (Node.getToken().getSymbolType() == SymbolType.LPARENT || Node.getToken().getSymbolType() == SymbolType.RPARENT ||
                Node.getToken().getSymbolType() == SymbolType.RBRACE || Node.getToken().getSymbolType() == SymbolType.LBRACE) {
            if (Node.getToken().getLineNum() > this.lineNum) {
                System.out.println();
                this.lineNum++;
            }
            System.out.print(Node.getToken().getValue() );
        }else if(Node.getToken().getValue().equals("Exp")){

        }
    }
}

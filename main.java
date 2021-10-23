import java.io.*;
import java.util.ArrayList;
import java.util.Objects;


public class main {
    public static String readToString(String fileName) {
        String encoding = "UTF-8";
        File file = new File(fileName);
        long filelength = file.length();
        byte[] filecontent = new byte[(int) filelength];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            System.err.println("The OS does not support " + encoding);
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) throws IOException, ERR {
        ArrayList<Token> TOKEN = new ArrayList<>();
        ASTNode ASTRoot;
//         String pathname = ".\\test.txt";
        String pathname = args[0];

        PrintStream ps = new PrintStream("answer.txt");
        System.setOut(ps);

        StringBuilder Test = new StringBuilder(Objects.requireNonNull(readToString(pathname)));
        //词法分析程序
        TOKEN = new Lexcical(Test).analyze();
//        for (Token token:TOKEN
//        ) {
//            System.out.println(token.getSymbolType()+" "+token.getValue());
//        }
        ASTRoot = new Gramma(TOKEN).analyze();
//        printAST(ASTRoot);
        new Semantic().analyze(ASTRoot);

    }

    public static void printAST(ASTNode AST) {
        System.out.print(AST.getToken().getValue()+" ");
        for (ASTNode node:AST.getNodeList()
        ) {
            System.out.print(node.getToken().getValue()+" ");
        }
        System.out.println();
        for (ASTNode node:AST.getNodeList()
             ) {
            printAST(node);
        }

    }


}

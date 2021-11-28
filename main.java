import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
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
//           String pathname = ".\\test.txt";
     String pathname = args[0];
//
     PrintStream ps = new PrintStream(args[1]);
     System.setOut(ps);

        StringBuilder Test = new StringBuilder(Objects.requireNonNull(readToString(pathname)));
//         System.out.println(Test);
        //词法分析程序
        TOKEN = new Lexical(Test).analyze();
        TOKEN.add(new Token(SymbolType.CODEEND, null, 0));

        ASTRoot = new Grammar(TOKEN).analyze();
//        for (Token token : TOKEN
//        ) {
//            System.out.println(token.getSymbolType() + " " + token.getValue());
//        }
//         printTree p = new printTree();
//         p.print(ASTRoot, 0);



        new Semantic().analyze(ASTRoot, 0);

//                BlockPrint(BlockMap.getBlockMap());


        new TargetCodeGenerator().Generator(ASTRoot);

//                BlockPrint(IRBlockMap.getBlockMap());
    }

    public static void BlockPrint(HashMap blockMap) {
        for (Object block :
                blockMap.values()) {
            Block b = (Block) block;
            System.out.println(b.ID);
            for (Identifier ident : b.Identifiers.values()
            ) {
                System.out.println(ident.name + " " + ident.Dimension.size());
            }

        }
    }


}

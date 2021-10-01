import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class main {

    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    public static void print(StringBuilder word, List<String> operator) {
        List<String> Token = Arrays.asList("if", "else", "break", "continue", "while", "return");
        List<String> Tname = Arrays.asList("If", "Else",  "Break", "Continue","While", "Return");
        List<String> Oname = Arrays.asList("Assign", "Semicolon", "LPar", "RPar", "LBrace", "RBrace", "Plus", "Mult", "Div", "Lt", "Gt", "Eq");
        if (operator.contains(word.toString())) {
            System.out.println(Oname.get(operator.indexOf(word.toString())));
        } else if (Token.contains(word.toString())) {
            System.out.println(Tname.get(Token.indexOf(word.toString())));
        } else {
            if (word.length() != 0) {
                if (isInteger(word.toString())) {
                    System.out.println("Number(" + word + ")");
                } else {
                    System.out.println("Ident(" + word + ")");
                }
            }

        }
    }

    public static int analyze(String line) {
        List<String> operator = Arrays.asList("=", ";", "(", ")", "{", "}", "+", "*", "/", "<", ">", "==");
        StringBuilder word = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            String s = line.substring(i, i + 1);
            if (operator.contains(s)) {
                if (s.equals("=")) {
                    if (word.toString().equals("=")) {
                        word.append("=");
                        print(word, operator);
                        word = new StringBuilder();
                    } else {
                        print(word, operator);
                        word = new StringBuilder("=");
                    }
                } else {
                    print(word, operator);
                    word = new StringBuilder(s);
                }
            } else if ((s.charAt(0) >= 97 && s.charAt(0) <= 122) || (s.charAt(0) >= 48 && s.charAt(0) <= 57) || (s.charAt(0) >= 65 && s.charAt(0) <= 90) || s.equals("_")) {
                if (operator.contains(word.toString())) {
                    print(word, operator);
                    word = new StringBuilder();
                }
                else if(isInteger(word.toString())&&(s.charAt(0) < 48 || s.charAt(0) > 57)){
                    print(word, operator);
                    word = new StringBuilder();
                }
                word.append(s);
            } else if (s.equals(" ") || s.equals("\n")) {
                print(word, operator);
                word = new StringBuilder();
            } else {
                print(word, operator);
                System.out.println("Err");
                return 1;
            }
        }
        return 0;
    }

    public static void main(String[] args) throws FileNotFoundException {
//        String pathname = "D:\\课程\\Java lab\\词法分析\\test.txt";
        String pathname = args[0];
        Scanner sc = new Scanner(new File(pathname));
        int i = 0;
        while (sc.hasNextLine()&&i == 0)//逐行读取文件内容
        {
            String line = sc.nextLine();
            if(if(Integer.valueOf(line.charAt(0))!=9))
            i = analyze(line + " ");
        }
    }
}

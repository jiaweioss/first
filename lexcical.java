import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class lexcical {

    public static void printNum(int num, File ir, FileOutputStream out) {

        System.out.println(num);
    }

    public static void print(StringBuilder word, File ir, FileOutputStream out) {
        if (word.toString().equals("int")) {
            System.out.print("define dso_local i32 ");
        } else if (word.toString().equals("main()")) {
            System.out.print("@main()");
        } else if (word.toString().equals("return")) {
            System.out.print("ret i32 ");
        } else if (word.toString().equals(";")) {

        } else {
            System.out.println(word);
        }


    }

    public static int analyze(String line, File ir) throws FileNotFoundException {
        FileOutputStream out = new FileOutputStream(ir, true);
        String[] moud = {"int", "main()", "{", "return", ";", "}"};
        StringBuilder word = new StringBuilder();
        StringBuilder number = new StringBuilder();
        int i = 0;
        int point = 0;
        while (i < line.length()) {
//            System.out.print(i);
            String s = line.substring(i, i + 1);
            if ((i < line.length() - 2) && line.substring(i, i + 2).equals("//")) {
                while (line.charAt(i) != '\n' && i < line.length()) {

                    i++;
                }
            } else if ((i < line.length() - 2) && line.substring(i, i + 2).equals("/*")) {
                while (!line.substring(i, i + 2).equals("*/") && i < line.length() - 2) {
                    i++;
                }
                i += 2;
            } else if ((line.charAt(i) == ' ' || line.charAt(i) == '\n') && i < line.length()) {
                if (word.length() != 0) {
                    if (word.toString().equals(moud[point])) {
                        print(word, ir, out);
                        word = new StringBuilder();
                        point++;
                        i++;
                        if (point == 6) {
                            break;
                        }
                    } else {
                        System.out.print("   " + word);
                        return 1;
                    }
                } else {
                    i++;
                }

            } else if (line.charAt(i) != ' ' && line.charAt(i) != '\n' && !((line.charAt(i) >= 47 && line.charAt(i) <= 58))) {
                if (word.length() != 0) {
                    if (word.toString().equals(moud[point])) {
                        print(word, ir, out);

                        word = new StringBuilder();
                        point++;
                        if (point == 6) {
                            break;
                        }
                    } else {
                        word.append(line.charAt(i));
                        i++;

                    }
                } else {
                    word.append(line.charAt(i));
                    i++;
                }

            } else if (line.charAt(i) >= 48 && line.charAt(i) <= 57 && i < line.length()) {
                    if (!moud[point - 1].equals("return")) {
                        System.out.println(moud[point]);
                        return 2;
                    } else {
                        int num;
                        if (line.substring(i, i + 2).equals("0x") || line.substring(i, i + 2).equals("0X")) {
                            i += 2;
                            while (line.charAt(i) >= 48 && line.charAt(i) <= 57) {
                                number.append(line.charAt(i));
                                i++;
                            }
                            num = Integer.parseInt(number.toString(), 16);
                        } else if (line.charAt(i) == '0') {
                            while (line.charAt(i) >= 48 && line.charAt(i) <= 57) {
                                number.append(line.charAt(i));
                                i++;
                            }
                            num = Integer.parseInt(number.toString(), 8);
                        } else {
                            while (line.charAt(i) >= 48 && line.charAt(i) <= 57) {
                                number.append(line.charAt(i));
                                i++;
                            }
                            num = Integer.parseInt(number.toString());
                        }
                        printNum(num, ir, out);
                    }
                } else {
                    return 3;
                }
            }
            if (point != 6)
                return 4;
            return 0;
        }
    }

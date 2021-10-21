import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;



public class main {


    public static void main(String[] args) throws IOException {
//        String pathname = ".\\test.txt";
        String pathname = args[0];
        BufferedReader bf = new BufferedReader(new FileReader(pathname));
        File ir = new File(args[1]);
        StringBuilder tx = new StringBuilder();
        String line = "";
        int i = 0;
        while (line!=null)//逐行读取文件内容
        {
            line = bf.readLine();
            if(line==null)
                break;
            line += '\n';
            tx.append(line);
        }
            if(lexcical.analyze(tx.toString(),ir)!=0){
                throw new NullPointerException();
            }
    }

}

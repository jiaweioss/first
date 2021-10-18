import java.io.*;


public class main {


    public static int main(String[] args) throws IOException {
        //String pathname = ".\\test.txt";
        String pathname = args[0];
        BufferedReader bf = new BufferedReader(new FileReader(pathname));
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
            return(lexcical.analyze(tx.toString()));
    }

}

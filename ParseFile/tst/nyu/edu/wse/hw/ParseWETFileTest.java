package nyu.edu.wse.hw;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.logging.Logger;


public class ParseWETFileTest {

    private static final String TEST_FILE_PATH_NAME = "/home/liuchang/Documents/study/wse/homework/hw2/ParseFile/data/test/test.txt";

    public static void main(String[] args) throws FileNotFoundException, IOException{
        String input = "Input text, with words, punctuation, etc. Well, it's rather short.";
        Pattern p = Pattern.compile("[\\w']+");
        Matcher m = p.matcher(input);

//        while ( m.find() ) {
//            System.out.println(input.substring(m.start(), m.end()));
//        }
//        String word = "123asdf;klsjf;好的";
//        System.out.println(word.getBytes().toString());
//        System.out.println(new String(word));
        // System.out.println("\n".getBytes().length);

        // test seek bytes
//        RandomAccessFile file = new RandomAccessFile(TEST_FILE_PATH_NAME, "r");
//        file.seek(2);
//        byte[] temp = new byte[4];
//        file.read(temp);
//        file.close();
//        System.out.println(new String(temp, "UTF-8"));

        // test log
        Logger logger = Logger.getLogger("test");
        logger.setLevel(Level.INFO);
        logger.log(Level.INFO, "do some thing");
        for(int i=0;i<10;i++) {
            logger.info(i+"");
        }

    }
}

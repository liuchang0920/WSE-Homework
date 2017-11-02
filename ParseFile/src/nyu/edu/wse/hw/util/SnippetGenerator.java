package nyu.edu.wse.hw.util;

import java.io.*;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SnippetGenerator {

    private static final String COMMON_CONFIG_FILE = "/home/liuchang/Documents/study/wse/homework/hw3/WSE-Homework/ParseFile/config/common-config";;
    private static final Logger log = Logger.getLogger("QueryExecution");

    private String snapshotPath;

    public static void main(String[] args) {
        SnippetGenerator generator = new SnippetGenerator();
        Set<String> key = new HashSet<>();
        key.add("a");
        key.add("on");
        String snippet = generator.getSnippet(203, key);
        System.out.println("snippet: " + snippet);
    }
    public SnippetGenerator() {

        Properties prop = new Properties();
        InputStream is;
        try {
            is = new FileInputStream(COMMON_CONFIG_FILE);
            prop.load(is);
            this.snapshotPath = prop.getProperty("SNAPHOT_PATH");

        } catch (IOException ioe) {
            System.out.println("error while parsing config file: " + ioe.getMessage());
        }
    }

    public String getSnippet(int docId, Set<String> keywords) {
       String content = fetchContent(docId);
       return generateSnippet(content, keywords);
    }

    private String fetchContent(int docId) {
        try {
            FileReader fr = new FileReader(snapshotPath+Integer.toString(docId));
            System.out.println("file path: " + snapshotPath+Integer.toString(docId));
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            StringBuffer sb = new StringBuffer();
            while(line != null) {
                sb.append(line+"\n");
                line = br.readLine();
            }
            return sb.toString();
        } catch (FileNotFoundException fnfe) {
            log.log(Level.SEVERE, "file not found given docId: " + docId);
        } catch (IOException ioe) {
            log.log(Level.SEVERE, "IOException given docId: " + docId + ", " + ioe.getMessage());
        }
        return null;
    }

    private String generateSnippet(String content, Set<String> keywords) {
        String[] sentences = content.split("\\.");
        for(String keyword: keywords) {
            for(String sentence: sentences) {
                if(sentence.matches(".*?\\bbeautiful\\b.*?")) {
                    return sentence;
                }
            }
        }
        return "....";
    }

}

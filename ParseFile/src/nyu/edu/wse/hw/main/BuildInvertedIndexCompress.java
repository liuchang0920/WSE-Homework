package nyu.edu.wse.hw.main;

import nyu.edu.wse.hw.domain.DocFrequency;
import nyu.edu.wse.hw.domain.Lexicon;
import nyu.edu.wse.hw.domain.LexiconItem;
import nyu.edu.wse.hw.util.VariableByteCode;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BuildInvertedIndexCompress {

    private static Lexicon lexicon = new Lexicon();

    private static final String INTERMEDIATE_FILE="temp-sorted.txt";
    //private static final String INTERMEDIATE_FILE_PATH = "/home/liuchang/Documents/study/wse/homework/hw2/ParseFile/data/inverted-intermediate/";
    private static final String INTERMEDIATE_FILE_PATH = "/media/liuchang/New Volume/study/wse/hw2-data/inverted-intermediate/";

    private static final String INVERTED_FILE="inverted.txt";
    // private static final String INVERTED_FILE_PATH = "/home/liuchang/Documents/study/wse/homework/hw2/ParseFile/data/inverted-index/";
    private static final String INVERTED_FILE_PATH = "/media/liuchang/New Volume/study/wse/hw2-data/inverted-index/";

    private static final String LEXICON_FILE="lexicon.out";
    // private static final String LEXICON_FILE_PATH = "/home/liuchang/Documents/study/wse/homework/hw2/ParseFile/data/lexicon/";
    private static final String LEXICON_FILE_PATH =  "/media/liuchang/New Volume/study/wse/hw2-data/lexicon/";

    // indexes
    private long startIndex = 0;// off set by byte
    private long endIndex = -1;

    // logging
    private static final Logger log = Logger.getLogger("buildInvertedIndex");

    public static void main(String[] args) throws ClassNotFoundException, FileNotFoundException, IOException {

        log.info("start building");
        // build inverted index
        BuildInvertedIndexCompress buildInverted = new BuildInvertedIndexCompress();
        buildInverted.parseSortedFile();

        // build lexicon
        buildInverted.writeLexiconToFile(LEXICON_FILE_PATH, LEXICON_FILE);
        log.info("finish parsing");
    }

    public void parseSortedFile() throws IOException{
        System.out.println("start parsing: " + INTERMEDIATE_FILE);
        FileReader fileReader = new FileReader(INTERMEDIATE_FILE_PATH+INTERMEDIATE_FILE);
        BufferedReader br = new BufferedReader(fileReader);
        String line;

        // current word, and corresponding invert list
        String cur="";
        List<DocFrequency> curList = new ArrayList<>();

        // output file
        FileOutputStream fos = new FileOutputStream(INVERTED_FILE_PATH+INVERTED_FILE, false);
        int count = 0;
        while((line = br.readLine()) != null) {
            String[] split = line.split("\t");
            if(cur.equals(split[0])) {
                curList.add(new DocFrequency(Integer.parseInt(split[1]), Integer.parseInt(split[2])));// 这里转会不会影响性能
            } else {
                count += 1;
                if(count%1000000 == 0) {
                    log.info("counter: "+ count + " current building: " + cur);
                }
                // write to inverted index file
                List<Integer> docIdFreqPair = new ArrayList<>();
                curList.forEach(item -> {

                    docIdFreqPair.add(item.getDocId());
                    docIdFreqPair.add(item.getFequency());

                });
                byte[] compressedPair = VariableByteCode.encode(docIdFreqPair);
                fos.write(compressedPair);
                endIndex += compressedPair.length;

                // System.out.println("finish building: " + cur);
                // update lexicon file
                lexicon.addWord(cur, new LexiconItem(startIndex, endIndex, Lexicon.COUNTER++, curList.size()));

                // update parameters
                cur = split[0];
                curList = new ArrayList<>();
                curList.add(new DocFrequency(Integer.parseInt(split[1]), Integer.parseInt(split[2])));
                // update offset
                startIndex = endIndex+1;

                //endIndex = startIndex;

            }
        }
        // close
        fos.close();
        System.out.println("finish parsing: "+INTERMEDIATE_FILE);
    }

    // write lexicon to file
    public void writeLexiconToFile(String lexiconFilePath, String lexiconFile) throws IOException{
        // 直接写到文件
        // lexicon file
        FileOutputStream fout= new FileOutputStream(lexiconFilePath+lexiconFile, false);
        log.info("lexion size: " + lexicon.getMap().size());

        lexicon.getMap().forEach((k, v) -> {
            StringBuffer sb = new StringBuffer();
            sb.append(k+ "::");
            sb.append(v.getWordId()+"::");
            sb.append(v.getCount()+"::");
            sb.append(v.getStartIndex()+"::");
            sb.append(v.getEndIndex()+"\n");
            try{
                fout.write(sb.toString().getBytes());

            } catch (IOException e) {
                log.log(Level.SEVERE, e.getMessage());
            }
        });
        fout.close();
        System.out.println("finish writing lexicon to file");

    }

    public void readFromLexicion(String word) throws FileNotFoundException, IOException{
        if(lexicon.contains(word)) {
            LexiconItem item = lexicon.getWordInfo(word);
            long startIndex = item.getStartIndex();
            long endIndex = item.getEndIndex();
            int delta = (int) (endIndex - startIndex) + 1;
            byte[] byteFile = new byte[delta];

            System.out.println("start index: " + startIndex);
            System.out.println("end index: " + Long.toString(endIndex));
            // do random access file...
            RandomAccessFile file = new RandomAccessFile(INVERTED_FILE_PATH+INVERTED_FILE, "r");
            file.seek(startIndex);

            file.read(byteFile);
            List<Integer> uncompressed = VariableByteCode.decode(byteFile);

            System.out.println("found reverse: ");
            System.out.println(uncompressed);
            for(Integer integer: uncompressed) {
                System.out.print(integer + " ");
            }
        } else System.out.println("doesn't exist the key word");
    }

}

package nyu.edu.wse.hw.main;

import nyu.edu.wse.hw.domain.DocFrequency;
import nyu.edu.wse.hw.domain.Lexicon;
import nyu.edu.wse.hw.domain.LexiconItem;
import nyu.edu.wse.hw.util.ArrayConverter;
import nyu.edu.wse.hw.util.VariableByteCode;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BuildInvertedIndexCompress {

    private static Lexicon lexicon = new Lexicon();
    private static  String INTERMEDIATE_FILE_SORTED;
    private static  String INVERTED_FILE;
    private static String LEXICON_FILE;
    // indexes
    private long startIndex = 0;// off set by byte
    private long endIndex = 0;
    // number of posting per chunk
    private static final int NUM_OF_POSTING = 500;
    // config file
    private static final String COMMON_CONFIG_FILE = "/home/liuchang/Documents/study/wse/homework/hw3/WSE-Homework/ParseFile/config/common-config";;
    // logging
    private static final Logger log = Logger.getLogger("buildInvertedIndex");

    public static void main(String[] args) throws ClassNotFoundException, FileNotFoundException, IOException {

        BuildInvertedIndexCompress buildInverted = new BuildInvertedIndexCompress();
        buildInverted.main();
    }

    public void main() {
        try{
            log.info("start building");
            parseSortedFile();
            System.out.println("lexicong file: " + LEXICON_FILE);
            writeLexiconToFile(LEXICON_FILE);
            log.info("finish parsing");
        } catch (IOException ioe) {
            System.out.println("io exception");
        }

    }

    public BuildInvertedIndexCompress() {

        Properties prop = new Properties();
        InputStream is;
        try {
            is = new FileInputStream(COMMON_CONFIG_FILE);
            prop.load(is);

            this.INTERMEDIATE_FILE_SORTED = prop.getProperty("INTERMEDIATE_FILE_SORTED");
            this.INVERTED_FILE = prop.getProperty("INVERTED_FILE");
            this.LEXICON_FILE = prop.getProperty("LEXICON_FILE");

        } catch (IOException ioe) {
            System.out.println("error while parsing config file: " + ioe.getMessage());
        }

    }

    public void parseSortedFile() throws IOException{
        FileReader fileReader = new FileReader(INTERMEDIATE_FILE_SORTED);
        BufferedReader br = new BufferedReader(fileReader);
        String line;

        // current word, and corresponding invert list
        String cur="";
        List<DocFrequency> curList = new ArrayList<>();
        // output file
        FileOutputStream fos = new FileOutputStream(INVERTED_FILE, false);

        int count = 0;
        while((line = br.readLine()) != null) {
            String[] split = line.split("\t");
            if(cur.equals(split[0])) {
                curList.add(new DocFrequency(Integer.parseInt(split[1]), Integer.parseInt(split[2])));// performance concern
            } else {
                // write to inverted index file
                if(count%1000000 == 0) {
                    log.info("counter: "+ count + " current building: " + cur);
                }

                count += 1;
                // save inverted index
                saveInvertedIndex(curList, fos, cur);
                // update lexicon file
                lexicon.addWord(cur, new LexiconItem(curList.size(), startIndex, endIndex-1));

                // update parameters
                cur = split[0];
                curList = new ArrayList<>();
                curList.add(new DocFrequency(Integer.parseInt(split[1]), Integer.parseInt(split[2])));

                // update offset
                startIndex = endIndex;
            }
        }
        // close
        fos.close();
    }

    // write lexicon to file
    public void writeLexiconToFile(String lexiconFile) throws IOException{
        // 直接写到文件
        // lexicon file
        FileOutputStream fout= new FileOutputStream(lexiconFile, false);
        log.info("lexion size: " + lexicon.getMap().size());

        lexicon.getMap().forEach((k, v) -> {
            StringBuffer sb = new StringBuffer();
            sb.append(k+ " ");
            //sb.append(v.getWordId()+"::");
            sb.append(v.getCount()+" ");
            sb.append(v.getStartIndex()+" ");
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

    private void saveInvertedIndex(List<DocFrequency> curList, FileOutputStream fos, String cur) {

        List<Integer> auxiliaryList = new ArrayList<>(); // used for building auxiliary, will be convert to byte[]
        List<byte[]> invertedListToSave = new ArrayList<>(); // intermediate file to temporarily save compressed inverted file
        List<Integer> tempListToSave = new ArrayList<>(); // used to store data when building each chunk

        // write auxiliary list
        for(int i=0;i<curList.size();i++) {

            tempListToSave.add(curList.get(i).getDocId());
            tempListToSave.add(curList.get(i).getFrequency());
            // System.out.println("docId: " + curList.get(i).getDocId() + " frequency: " + curList.get(i).getFrequency());
            if(((i+1) % (NUM_OF_POSTING) == 0)) {
                byte[] compressedChunk = VariableByteCode.encode(tempListToSave);
                // record to auxiliary table: (docId, compressed chunk length)
                auxiliaryList.add(curList.get(i).getDocId());
                auxiliaryList.add(compressedChunk.length);
                // save compressed files in this chunk to list
                invertedListToSave.add(compressedChunk);

                tempListToSave = new ArrayList<>();
            }
        }
        // check if tempListToSave is empty, otherwise use another chunk to save rest files
        if(tempListToSave.size()>0) {
            //System.out.println("leftover: " + tempListToSave.size()/2);
            byte[] compressedChunk = VariableByteCode.encode(tempListToSave);
            auxiliaryList.add(curList.get(curList.size()-1).getDocId());// last docid in this chunk
            auxiliaryList.add(compressedChunk.length);
            invertedListToSave.add(compressedChunk);
        }
        if(curList.size()>0) {
            try{
                byte[] auxiliaryBytes = ArrayConverter.toByteArray(auxiliaryList.stream().mapToInt(i -> i).toArray());
                if("google".equals(cur)) {
                    System.out.println("google auxiliary: " + auxiliaryList);
                }

//                System.out.println("auxiliary value: " + auxiliaryList);
//                int[] converted = ArrayConverter.toIntArray(auxiliaryBytes);
//                System.out.print("auxiliary convert: ");
//                for(int x = 0;x<converted.length;x++) {
//                    if(converted[x] != auxiliaryList.get(x)) {
//                        System.out.println("not identical");
//                        return;
//                    }
//                }

                fos.write(auxiliaryBytes);
                endIndex += auxiliaryBytes.length;

                // save invertedList
                for(byte[] tempCompressed: invertedListToSave) {
                    fos.write(tempCompressed);
                    endIndex += tempCompressed.length;
                }
                // endIndex already updated

            } catch (IOException ioe) {
                log.log(Level.SEVERE, "error while writing to inverted file: " + ioe.getMessage());
            }
        }
    }
}

package nyu.edu.wse.hw.main;

import nyu.edu.wse.hw.domain.DocFrequency;
import nyu.edu.wse.hw.domain.Lexicon;
import nyu.edu.wse.hw.domain.LexiconItem;
import nyu.edu.wse.hw.util.ArrayConverter;
import nyu.edu.wse.hw.util.VariableByteCode;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BuildInvertedIndexCompress {

    private static Lexicon lexicon = new Lexicon();

    private static final String INTERMEDIATE_FILE="temp-sort.txt";
    private static final String INTERMEDIATE_FILE_PATH = "/home/liuchang/Documents/study/wse/homework/hw3/WSE-Homework/ParseFile/data/inverted-intermediate/";
    //private static final String INTERMEDIATE_FILE_PATH = "/media/liuchang/New Volume/study/wse/hw2-data/inverted-intermediate/";

    private static final String INVERTED_FILE="inverted.txt";
    private static final String INVERTED_FILE_PATH = "/home/liuchang/Documents/study/wse/homework/hw3/WSE-Homework/ParseFile/data/inverted-index/";
    //private static final String INVERTED_FILE_PATH = "/media/liuchang/New Volume/study/wse/hw2-data/inverted-index/";

    private static final String LEXICON_FILE="lexicon.out";
    private static final String LEXICON_FILE_PATH = "/home/liuchang/Documents/study/wse/homework/hw3/WSE-Homework/ParseFile/data/lexicon/";
    //private static final String LEXICON_FILE_PATH =  "/media/liuchang/New Volume/study/wse/hw2-data/lexicon/";

    // indexes
    private long startIndex = 0;// off set by byte
    private long endIndex = -1;

    // number of posting per chunk
    private static final int NUM_OF_POSTING = 500;

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
                curList.add(new DocFrequency(Integer.parseInt(split[1]), Integer.parseInt(split[2])));// performance concern
            } else {
                // write to inverted index file

                // logging
                if(count%1000000 == 0) {
                    log.info("counter: "+ count + " current building: " + cur);
                }

                count += 1;

                // save inverted index
                saveInvertedIndex(curList, fos);

                // System.out.println("finish building: " + cur);
                // update lexicon file
                lexicon.addWord(cur, new LexiconItem(curList.size(), startIndex, endIndex-1));

                // update parameters
                cur = split[0];
                curList = new ArrayList<>();
                curList.add(new DocFrequency(Integer.parseInt(split[1]), Integer.parseInt(split[2])));

                // update offset
                startIndex = endIndex;// 难道是这里？

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
            //sb.append(v.getWordId()+"::");
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

    private void saveInvertedIndex(List<DocFrequency> curList, FileOutputStream fos) {

        List<Integer> auxiliaryList = new ArrayList<>(); // used for building auxiliary, will be convert to byte[]
        List<byte[]> invertedListToSave = new ArrayList<>(); // intermediate file to temporarily save compressed inverted file
        List<Integer> tempListToSave = new ArrayList<>(); // used to store data when building each chunk

        // write auxiliary list
        for(int i=0;i<curList.size();i++) {

            tempListToSave.add(curList.get(i).getDocId());
            tempListToSave.add(curList.get(i).getFequency());

            // compress each NUM_OF_POSTING of postings
            if(((i+1) % (NUM_OF_POSTING) == 0)) {//save on 499　每５００个存一次, but in pair
//                if(i>500) {
//                    System.out.println("create chunk: " + i);
//                }
                // compress
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

        try{
            byte[] auxiliaryBytes = ArrayConverter.toByteArray(auxiliaryList.stream().mapToInt(i -> i).toArray());
            fos.write(auxiliaryBytes);
            endIndex += auxiliaryBytes.length;
            if(auxiliaryBytes.length>160) {
                System.out.println("auxiliary size: " + auxiliaryBytes.length/8);
            }
            // save invertedList
            for(byte[] tempCompressed: invertedListToSave) {
                fos.write(tempCompressed);
                endIndex += tempCompressed.length;

            }

            // update index
            //log.log(Level.INFO, "update endIndex");


        } catch (IOException ioe) {
            log.log(Level.SEVERE, "error while writing to inverted file: " + ioe.getMessage());
        }
    }

}

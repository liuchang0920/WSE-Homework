package nyu.edu.wse.hw.main;

import nyu.edu.wse.hw.domain.*;
import nyu.edu.wse.hw.util.ArrayConverter;
import nyu.edu.wse.hw.util.VariableByteCode;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QueryExecution {

    private static final String INVERTED_FILE="inverted.txt";
    private static final String INVERTED_FILE_PATH = "/home/liuchang/Documents/study/wse/homework/hw3/WSE-Homework/ParseFile/data/inverted-index/";
    //private static final String INVERTED_FILE_PATH = "/media/liuchang/New Volume/study/wse/hw2-data/inverted-index/";

    private static final String LEXICON_FILE="lexicon.out";
    private static final String LEXICON_FILE_PATH = "/home/liuchang/Documents/study/wse/homework/hw3/WSE-Homework/ParseFile/data/lexicon/";
    //private static final String LEXICON_FILE_PATH =  "/media/liuchang/New Volume/study/wse/hw2-data/lexicon/";

    // lexicon
    private Lexicon lexicon;

    //  URL table
    private URLTable urlTable;
    private static final String URLTable_FILE_NAME = "/home/liuchang/Documents/study/wse/homework/hw3/WSE-Homework/ParseFile/data/urltable/URLTable.txt";

    // number of posting per chunk
    private static final int NUM_OF_POSTING = 500;

    // logging
    private static final Logger log = Logger.getLogger("QueryExecution");


    public static void main(String[] args) {

        QueryExecution execution = new QueryExecution();
        execution.query(null);

        /*
        while(true) {
            // handle request
        }
         */

    }

    public QueryExecution() {

        // init lexicon
        lexicon = new Lexicon();
        loadLexicon();
        loadURLTable();

    }

    private void loadLexicon() {
        try{
            FileReader fr = new FileReader(this.LEXICON_FILE_PATH+this.LEXICON_FILE);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            while(line != null) {
                // build lexicon
                try{
                    String[] split = line.split("::");
                    String word = split[0];
                    int frequency = Integer.parseInt(split[1]);
                    int startIndex = Integer.parseInt(split[2]);
                    int endIndex = Integer.parseInt(split[3]);
                    LexiconItem litem = new LexiconItem(
                            frequency,
                            startIndex,
                            endIndex
                    );
                    lexicon.addWord(word, litem);

                } catch (Exception e) {
                    log.log(Level.SEVERE, "unexpected exception"+e.getMessage());
                    log.log(Level.SEVERE, e.getCause()+"");
                }
                line = br.readLine();
            }

            System.out.println("finish loading lexicon file, lexicon size: " + lexicon.getMap().size());

        } catch (IOException ioe) {
            log.log(Level.SEVERE, "ioexception reading lexicon file");
        }
    }

    private void loadURLTable() {

        FileInputStream fis;
        ObjectInputStream ois;
        try {
            fis = new FileInputStream(URLTable_FILE_NAME);
            ois = new ObjectInputStream(fis);
            urlTable = (URLTable) ois.readObject();
            System.out.println("finsh loading url table, size: " + urlTable.getSize());
        } catch (Exception e) {
          log.log(Level.SEVERE, "error while loading url table: " + e.getMessage());
        }
    }

    public List<QueryResult> query(List<String> keywords) {

        String temp = "build";
        TermInformation termInformation = openList(temp);

//        List<QueryResult> result = new ArrayList<>();
//
//        // implement DAAT
//        List<RandomAccessFile> lp = new ArrayList<>();
//        for(String keyword: keywords) {
//            lp.add(openList(keyword));
//        }
//
//        DocFrequency did = new DocFrequency(-1, 0);
//        List<DocFrequency> frequencies;
//        // document id ??
//        while(did.getDocId() <= Integer.MAX_VALUE) {
//
//            frequencies = new ArrayList<>();
//            did = nextGEQ(lp.get(0), did.getDocId());
//            frequencies.add(did);
//            int tempDoc = did.getDocId();
//            for(int i=0;i<lp.size();i++) {
//                DocFrequency tempDocFreq = nextGEQ(lp.get(i), did.getDocId());
//                frequencies.add(tempDocFreq);
//                tempDoc = Math.max(did.getDocId(), tempDocFreq.getDocId());
//            }
//            if(tempDoc == did.getDocId()) {
//                // BM25 compute frequency
//
//                did.setDocId(did.getDocId()+1);
//            } else {
//                did.setDocId(tempDoc);// docid and frequency not compatible
//            }
//        }
//
//
//        // close list
//        for(RandomAccessFile file: lp) {
//            closeList(file);
//        }
//
//        return result;
          return null;
    }

    // provide interface for the index in the inverted file given a keyword
//    public RandomAccessFile openList(String keyword) {
//            try{
//                LexiconItem lexiconItem = lexicon.getWordInfo(keyword);
//                File file = new File(this.INVERTED_FILE_PATH+this.INVERTED_FILE);
//                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
//                randomAccessFile.seek(lexiconItem.getStartIndex());
//                return randomAccessFile;
//            } catch (IOException ioe) {
//                log.log(Level.SEVERE, "ioexception while searching for keyword: " + keyword);
//                return null;
//
//            }
//    }

    public boolean closeList(RandomAccessFile raf) {
        try{
            raf.close();
            return true;
        } catch (IOException ieo) {
            return false;
        }
    }

    public DocFrequency nextGEQ(RandomAccessFile raf, int docId) {
        // uncompress in here ??
        return null;
    }

    public int getGreq(DocFrequency docFrequency) {
        return docFrequency.getFequency();
    }

    private TermInformation openList(String keyword) {
        // seek its auxiliar table
        if(this.lexicon.contains(keyword)) {
            try{
                File file = new File(this.INVERTED_FILE_PATH+this.INVERTED_FILE);
                RandomAccessFile raf = new RandomAccessFile(file, "r");

                LexiconItem lexiconItem = lexicon.getWordInfo(keyword);
                int frequency = lexiconItem.getCount();
                int chunk = frequency / NUM_OF_POSTING;
                if(frequency % NUM_OF_POSTING != 0) chunk+=1;

                raf.seek(lexiconItem.getStartIndex());
                System.out.println("chunk size: " + chunk);
                byte[] auxiliaryBytes = new byte[chunk*2*4];// 1 int --> 4 byte, 2 for (last docId, chunk size)
                raf.read(auxiliaryBytes);
                int[] auxiliaryTable = ArrayConverter.toIntArray(auxiliaryBytes);
                for(int item: auxiliaryTable) {
                    System.out.println(item);
                }
                return new TermInformation(raf, auxiliaryTable);
            } catch (IOException ioe) {
                log.log(Level.SEVERE, "error retrieving meta for term: " + keyword);
                return null;
            }

        } else {
            log.log(Level.SEVERE, "lexicon doesn't contain: "+ keyword);
            return null;
        }
    }

}

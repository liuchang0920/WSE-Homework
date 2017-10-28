package nyu.edu.wse.hw.main;

import nyu.edu.wse.hw.domain.DocFrequency;
import nyu.edu.wse.hw.domain.Lexicon;
import nyu.edu.wse.hw.domain.LexiconItem;
import nyu.edu.wse.hw.domain.QueryResult;
import nyu.edu.wse.hw.util.VariableByteCode;

import java.io.*;
import java.util.ArrayList;
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

    private Lexicon lexicon;

    // logging
    private static final Logger log = Logger.getLogger("QueryExecution");


    public static void main(String[] args) {

        QueryExecution execution = new QueryExecution();
        execution.query(null);
    }

    public QueryExecution() {

        // init lexicon
        lexicon = new Lexicon();
        initLexicon();
    }

    private void initLexicon() {
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

    public List<QueryResult> query(List<String> keywords) {

        String temp = "build";
        List<QueryResult> result = new ArrayList<>();

        // implement DAAT
        List<RandomAccessFile> lp = new ArrayList<>();
        for(String keyword: keywords) {
            lp.add(openList(keyword));
        }

        DocFrequency did = new DocFrequency(-1, 0);
        List<DocFrequency> frequencies;
        // document id ??
        while(did.getDocId() <= Integer.MAX_VALUE) {

            frequencies = new ArrayList<>();
            did = nextGEQ(lp.get(0), did.getDocId());
            frequencies.add(did);
            int tempDoc = did.getDocId();
            for(int i=0;i<lp.size();i++) {
                DocFrequency tempDocFreq = nextGEQ(lp.get(i), did.getDocId());
                frequencies.add(tempDocFreq);
                tempDoc = Math.max(did.getDocId(), tempDocFreq.getDocId());
            }
            if(tempDoc == did.getDocId()) {
                // BM25 compute frequency

                did.setDocId(did.getDocId()+1);
            } else {
                did.setDocId(tempDoc);// docid and frequency not compatible
            }
        }


        // close list
        for(RandomAccessFile file: lp) {
            closeList(file);
        }

        return result;

    }

    // provide interface for the index in the inverted file given a keyword
    private RandomAccessFile openList(String keyword) {
            try{
                LexiconItem lexiconItem = lexicon.getWordInfo(keyword);
                File file = new File(this.INVERTED_FILE_PATH+this.INVERTED_FILE);
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
                randomAccessFile.seek(lexiconItem.getStartIndex());
                return randomAccessFile;
            } catch (IOException ioe) {
                log.log(Level.SEVERE, "ioexception while searching for keyword: " + keyword);
                return null;

            }
    }

    private boolean closeList(RandomAccessFile raf) {
        try{
            raf.close();
            return true;
        } catch (IOException ieo) {
            return false;
        }
    }

    private DocFrequency nextGEQ(RandomAccessFile raf, int docId) {
        // uncompress in here ??
        return null;
    }

    private int getGreq(DocFrequency docFrequency) {
        return docFrequency.getFequency();
    }



}

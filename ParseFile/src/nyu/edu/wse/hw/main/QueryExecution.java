package nyu.edu.wse.hw.main;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import nyu.edu.wse.hw.domain.*;
import nyu.edu.wse.hw.util.ArrayConverter;
import nyu.edu.wse.hw.util.BM25Calculator;
import nyu.edu.wse.hw.util.VariableByteCode;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QueryExecution {


    // query handler inner class
    private static class QueryHandle implements HttpHandler{

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("testint", 1);
            jsonObject.addProperty("teststring", "200");
            String res = jsonObject.toString();
            httpExchange.sendResponseHeaders(200, res.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(res.getBytes());
            os.close();
        }
    }

    private static final String INVERTED_FILE="inverted.out";
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

    // bm25 calculator
    BM25Calculator calculator = new BM25Calculator();

    // logging
    private static final Logger log = Logger.getLogger("QueryExecution");


    public static void main(String[] args) {

        QueryExecution execution = new QueryExecution();
        List<String> test = new ArrayList<>();
        test.add("fast");
        test.add("food");
        execution.query(test);

        // start backend server
//        try{
//
//            HttpServer server = HttpServer.create(new InetSocketAddress(8888), 0);
//            server.createContext("/query", new QueryHandle());
//            server.setExecutor(null);
//            server.start();
//        } catch (IOException ioe) {
//            log.log(Level.SEVERE, "error creating http server");
//        }
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

//        String temp = "Wstrz";
//        TermInformation termInformation = openList(temp);

        Queue<QueryResult> result = new PriorityQueue<>(new Comparator<QueryResult>() {
            @Override
            public int compare(QueryResult o1, QueryResult o2) {
                return 0;
            }
        });

        // implement DAAT
//        List<TermInformation> lp = new ArrayList<>();
//        for(String keyword: keywords) {
//            lp.add(openList(keyword));
//        }
//
//        DocFrequency did = new DocFrequency(-1, 0);
//        List<DocFrequency> frequencies;
//
//        while(did.getDocId() <= Integer.MAX_VALUE) { // 记录docid最大值
//
//            frequencies = new ArrayList<>();
//            did = nextGEQ(lp.get(0), did.getDocId());
//            frequencies.add(did);
//            int tempDoc = did.getDocId();
//            for(int i=1;i<lp.size();i++) {// start from index=1
//                DocFrequency tempDocFreq = nextGEQ(lp.get(i), did.getDocId());
//                frequencies.add(tempDocFreq);
//                tempDoc = Math.max(did.getDocId(), tempDocFreq.getDocId());
//            }
//
//            if(tempDoc == did.getDocId()) {
//                // BM25Calculator compute frequency
//
//                Query query = new Query(new ArrayList<>(), 10);
//
//                calculator.calculate(query);
//                System.out.println("calculate bm25");
//
//                did.setDocId(did.getDocId()+1);
//            } else {
//                did.setDocId(tempDoc);// docid and frequency not compatible
//            }
//        }
//
//
//        // close list
//        for(TermInformation termInfo: lp) {
//            closeList(termInfo.getRandomAccessFile());
//        }

//        return result;
          return null;
    }

    public boolean closeList(RandomAccessFile raf) {
        try{
            raf.close();
            return true;
        } catch (IOException ieo) {
            return false;
        }
    }

    public DocFrequency nextGEQ(TermInformation termInformation, int docId) {

        int[] aTable = termInformation.getAuxiliaryTable();// (last docId, chunkSize);
        int i=termInformation.getCurIndex();
        int jumpBlock = 0;

        while(aTable[i]<docId && i<aTable.length) {
            i += 2;
            termInformation.setCurIndex(i);
            jumpBlock += aTable[i+1];
        }
        if(i>aTable.length) {
            System.out.println("end of search");
        } else {
            // read block
            int chunkSize = aTable[i+1];
            try{
                termInformation.getRandomAccessFile().seek(termInformation.getStartIndex()+jumpBlock);
                byte[] compressedChunk = new byte[chunkSize];
                termInformation.getRandomAccessFile().read(compressedChunk);
                List<Integer> uncompressedChunk = VariableByteCode.decode(compressedChunk);

                // try to find the exact doc id
                for(int index=0;index<uncompressedChunk.size();index+=2) {
                        if(index == docId) {
                            System.out.println("find docId");
                            return new DocFrequency(uncompressedChunk.get(index), uncompressedChunk.get(i+1));
                        } else {
                            System.out.println("did not found docId: "+ docId);
                            return new DocFrequency(uncompressedChunk.get(index), -1);
                        }
                }
                return null;
            } catch (IOException ioe) {
                log.log(Level.SEVERE, "error while uncompressing chunk");
            }
        }
        System.out.println("error retrieving docId");
        return null;
    }

    public int getGreq(DocFrequency docFrequency) {
        return docFrequency.getFequency();
    }

    private TermInformation openList(String keyword) {
        // seek its auxiliary table
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
//                for(int item: auxiliaryTable) {
//                    System.out.println(item);
//                }
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

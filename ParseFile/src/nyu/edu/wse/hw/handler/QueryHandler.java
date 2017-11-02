package nyu.edu.wse.hw.handler;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import nyu.edu.wse.hw.domain.*;
import nyu.edu.wse.hw.util.ArrayConverter;
import nyu.edu.wse.hw.util.BM25Calculator;
import nyu.edu.wse.hw.util.SnippetGenerator;
import nyu.edu.wse.hw.util.VariableByteCode;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QueryHandler implements HttpHandler {


    private static final String COMMON_CONFIG_FILE = "/home/liuchang/Documents/study/wse/homework/hw3/WSE-Homework/ParseFile/config/common-config";
    private static String INVERTED_FILE;
    private static String LEXICON_FILE;
    private static String URLTable_FILE; // = "/home/liuchang/Documents/study/wse/homework/hw3/WSE-Homework/ParseFile/data/urltable/URLTable.txt";

    // number of posting per chunk
    private static final int NUM_OF_POSTING = 500;

    private Lexicon lexicon;
    private URLTable urlTable;

    // bm25 calculator
    private BM25Calculator calculator = new BM25Calculator();
    // max doc
    int maxDoc;
    // snippet generator
    SnippetGenerator snippetGenerator = new SnippetGenerator();

    // logging
    private static final Logger log = Logger.getLogger("QueryHandler");

    public QueryHandler() {
        // load params
        Properties configProp = new Properties();
        InputStream configIs;
        try {
            configIs = new FileInputStream(COMMON_CONFIG_FILE);
            configProp.load(configIs);

            this.URLTable_FILE = configProp.getProperty("URLTable_FILE");
            this.INVERTED_FILE = configProp.getProperty("INVERTED_FILE");
            this.LEXICON_FILE = configProp.getProperty("LEXICON_FILE");

        } catch (IOException ioe) {
            System.out.println("error while parsing config file: " + ioe.getMessage());
        }

        // init lexicon
        lexicon = new Lexicon();
        loadLexicon();
        loadURLTable();

        // load total document size
        Properties prop = new Properties();
        InputStream is;
        try {

            is = new FileInputStream(BM25Calculator.BM25_CONFIG_FILE);
            prop.load(is);
            maxDoc = Integer.parseInt(prop.getProperty("total"));
            System.out.println("max doc: " + maxDoc);
            is.close();
        } catch (IOException ioe) {
            System.out.println("error while reading config file");
        }
    }

    private void loadLexicon() {
        try{
            FileReader fr = new FileReader(this.LEXICON_FILE);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            while(line != null) {
                // build lexicon
                try{
                    String[] split = line.split(" ");
                    String word = split[0];
                    int frequency = Integer.parseInt(split[1]);
                    long startIndex = Long.parseLong(split[2]);
                    long endIndex = Long.parseLong(split[3]);
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
        System.out.println("loading url table");
        try {
            fis = new FileInputStream(URLTable_FILE);
            ois = new ObjectInputStream(fis);
            urlTable = (URLTable) ois.readObject();
            System.out.println("finish loading url table, size: " + urlTable.getSize());
        } catch (Exception e) {
            System.out.println("error while loading url table");
            log.log(Level.SEVERE, "error while loading url table: " + e.getMessage());
        }
    }

    public Queue<QueryResult> query(Set<String> keywords) {

//        Queue<QueryResult> result = new PriorityQueue<>(new Comparator<QueryResult>() {
//            @Override
//            public int compare(QueryResult o1, QueryResult o2) {
//                return (int)(o2.getBm25Value() - o1.getBm25Value());
//            }
//        });
//
//        // implement DAAT
        List<TermInformation> lp = new ArrayList<>();
        for(String keyword: keywords) {
            lp.add(openList(keyword));
        }
//
//        DocFrequency did = new DocFrequency(-1, 0);
//        List<DocFrequency> frequencies;
//
//        while(did.getDocId() < maxDoc) { // 记录docid最大值
//            System.out.println("current search did: " + did.getDocId());
//            frequencies = new ArrayList<>();
//            did = nextGEQ(lp.get(0), did.getDocId()); // start pair
//            frequencies.add(did);
//            int tempDoc = did.getDocId();
//            for(int i=1;i<lp.size();i++) {// start from index=1
//                DocFrequency tempDocFreq = nextGEQ(lp.get(i), did.getDocId());
//                frequencies.add(tempDocFreq);
//                if(tempDocFreq.getDocId()>did.getDocId()) {
//                    // did.setDocId(tempDocFreq.getDocId());
//                    tempDoc = tempDocFreq.getDocId();
//                    break;
//                }
//            }
//
//            if(tempDoc == did.getDocId()) {
//                if(tempDoc > maxDoc) {
//                     System.out.println("end of search list");
//                } else {
//                    // BM25Calculator compute frequency
//                    // System.out.println("docId: " + tempDoc);
//
//                    // build query to compute bm25
//                    List<QueryItem> items = new ArrayList<>();
//                    for(int i=0;i<keywords.size();i++) {
//                        QueryItem item = new QueryItem(keywords.get(i),
//                                lexicon.getWordInfo(keywords.get(i)).getCount(), frequencies.get(i).getFrequency());    // query, ft, fdt
//                        items.add(item);
//                    }
//                    System.out.println("cur docid: " + did.getDocId());
//                    System.out.println(items.toString());
//
//                    Query query = new Query(items, urlTable.getMap().get(did.getDocId()).getSize());
//
//                    calculator.calculate(query);
//                    // System.out.println("calculate bm25");
//
//                    did.setDocId(did.getDocId()+1);
//                }
//
//            } else {
//                did.setDocId(tempDoc);// docid and frequency not compatible
//            }
//        }
//
//        // close list
//        for(TermInformation termInfo: lp) {
//            closeList(termInfo.getRandomAccessFile());
//        }

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
        // get the first docId that is >= docId, if the max < docID: return maxDoc+1
        int[] auxiliarTable = termInformation.getAuxiliaryTable();

        int curIndex = termInformation.getCurIndex();// first chunk
        int curLastDocId = auxiliarTable[curIndex];// (last docId, chunksize)
        int jumpSize = auxiliarTable.length*8;// skip the size of the auxiliary table (docId, chunksize) pair, skip how many ?
        while(curLastDocId<docId) {
            jumpSize += auxiliarTable[curIndex+1]; // skip compressed chunk

            curIndex+=2;
            termInformation.setCurIndex(curIndex);
            if(curIndex>=auxiliarTable.length-2) break;

            // update curLastDocId
            curLastDocId = auxiliarTable[curIndex];
        }
        if(curIndex>=auxiliarTable.length-1) {
            return new DocFrequency(maxDoc+1, -1);
        }
        // uncompress chunk
        try{

            int chunkSize = auxiliarTable[curIndex+1];
            byte[] compressedChunk = new byte[chunkSize];
            RandomAccessFile randomAccessFile = termInformation.getRandomAccessFile();
            randomAccessFile.seek(termInformation.getStartIndex()+jumpSize); // minus 1 ?
            randomAccessFile.read(compressedChunk);
            List<Integer> uncompressedChunk = VariableByteCode.decode(compressedChunk);

            // try to find the first docId that is larger than docId
            System.out.println("uncompressed chunk: " + uncompressedChunk);
            int curDocId = uncompressedChunk.get(0);

            int tempIndex = 0;
            while(curDocId<docId) {
                // System.out.print("[docId: " + curDocId + ", freq:" + uncompressedChunk.get(tempIndex+1) + "]");
                tempIndex += 2;
                if(tempIndex>=uncompressedChunk.size()-2) break;
                curDocId = uncompressedChunk.get(tempIndex+1);
            }
            System.out.println();

            int findFrequency = uncompressedChunk.get(tempIndex+1);
            //System.out.println("currenct docid is: " + curDocId);
            return new DocFrequency(curDocId, findFrequency);

        } catch (IOException ioe) {
            log.log(Level.SEVERE, "error while uncompressing chunk: " + ioe.getMessage());
            return null;
        }
    }

    public int getFreq(DocFrequency docFrequency) {
        return docFrequency.getFrequency();
    }

    private TermInformation openList(String keyword) {
        // seek its auxiliary table
        if(this.lexicon.contains(keyword)) {
            try{
                File file = new File(this.INVERTED_FILE);
                RandomAccessFile raf = new RandomAccessFile(file, "r");

                LexiconItem lexiconItem = lexicon.getWordInfo(keyword);
                int frequency = lexiconItem.getCount();// how many website contains this keyword
                int chunk = frequency / NUM_OF_POSTING;
                if(frequency % NUM_OF_POSTING != 0) chunk+=1;


                byte[] auxiliaryBytes = new byte[chunk*2*4];// 1 int --> 4 byte, 2 for (last docId, chunk size)
                raf.seek(lexiconItem.getStartIndex()+1);
                raf.read(auxiliaryBytes);
                int[] auxiliaryTable = ArrayConverter.toIntArray(auxiliaryBytes);
                System.out.println("auxiliary table");
                for(int item: auxiliaryTable) {
                    System.out.println(item);
                }
                TermInformation termInformation = new TermInformation(raf, auxiliaryTable);
                termInformation.setStartIndex(lexiconItem.getStartIndex());// set start index
                termInformation.setCurIndex(0);
                return termInformation;

            } catch (IOException ioe) {
                log.log(Level.SEVERE, "error retrieving meta for term: " + keyword);
                return null;
            }

        } else {
            log.log(Level.SEVERE, "lexicon doesn't contain: "+ keyword);
            return null;
        }
    }


    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        System.out.println(" daat.. ");
        // implement DAAT
        List<TermInformation> lp = new ArrayList<>();
        Map<String, String> params = queryToMap(httpExchange.getRequestURI().getQuery());
        String keywords = params.get("keyword");

        Set<String> keywordList = new HashSet<>();
        for(String keyword: keywords.split("\\+")) {
            keywordList.add(keyword);
            //lp.add(openList(keyword));
        }

        Queue<QueryResult> queryResults  = query(keywordList);//= new ArrayList<>(); //
        queryResults.add(new QueryResult(1, 13));
        queryResults.add(new QueryResult(2, 14));

        System.out.println("finish query: " + queryResults.size());
        List<QueryResult> finalResult = new ArrayList<>();
        int i=0;
        while(queryResults.size()>0) {
            System.out.println("fetch result...");
            QueryResult cur = queryResults.poll();
            String snippet = snippetGenerator.getSnippet(cur.getDocId(), keywordList);
            cur.setSnippet(snippet);
            finalResult.add(cur);
            i++;
        }
        System.out.println("finish generating snippets");

        // return json object
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("result", finalResult.toString());
        String res = jsonObject.toString();
        httpExchange.sendResponseHeaders(200, res.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(res.getBytes());
        os.close();

    }

    private Map<String, String> queryToMap(String query){
        Map<String, String> result = new HashMap<String, String>();
        for (String param : query.split("&")) {
            String pair[] = param.split("=");
            if (pair.length>1) {
                result.put(pair[0], pair[1]);
            }else{
                result.put(pair[0], "");
            }
        }
        return result;
    }

}

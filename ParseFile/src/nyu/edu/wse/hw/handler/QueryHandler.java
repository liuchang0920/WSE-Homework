package nyu.edu.wse.hw.handler;

import com.google.gson.Gson;
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


    //private static final String COMMON_CONFIG_FILE = "/home/liuchang/Documents/study/wse/homework/hw3/WSE-Homework/ParseFile/config/common-config";
    private static final String COMMON_CONFIG_FILE = "/home/liuchang/Documents/study/wse/homework/hw3/WSE-Homework/ParseFile/config/common-config-80-files";

    private static String INVERTED_FILE;
    private static String LEXICON_FILE;
    private static String URLTable_FILE; // = "/home/liuchang/Documents/study/wse/homework/hw3/WSE-Homework/ParseFile/data/urltable/URLTable.txt";
    private static int MAX_RESULT = 20;
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

        log.info("loading parameters");
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

            log.info("finish loading lexicon file, lexicon size: " + lexicon.getMap().size());

        } catch (IOException ioe) {
            log.log(Level.SEVERE, "ioexception reading lexicon file");
        }
    }

    private void loadURLTable() {

        FileInputStream fis;
        ObjectInputStream ois;
        log.info("loading url table");
        try {
            fis = new FileInputStream(URLTable_FILE);
            ois = new ObjectInputStream(fis);
            urlTable = (URLTable) ois.readObject();
            log.info("finish loading url table, size: " + urlTable.getSize());
        } catch (Exception e) {
            log.log(Level.SEVERE, "error while loading url table: " + e.getMessage());
        }
    }

    public Queue<QueryResult> query(List<String> keywords) {

        Queue<QueryResult> result = new PriorityQueue<>(new Comparator<QueryResult>() {
            @Override
            public int compare(QueryResult o1, QueryResult o2) {
                return (int)(o1.getBm25Value() - o2.getBm25Value());
            }
        });

        // implement DAAT
        List<TermInformation> lp = new ArrayList<>();
        for(String keyword: keywords) {
            lp.add(openList(keyword));
        }

        DocFrequency did = new DocFrequency(-1, 0);
        List<DocFrequency> frequencies;
        int lastDocId = -2;
        while(did.getDocId() < maxDoc) { // 记录docid最大值

            if(lastDocId == did.getDocId()) {
                System.out.println("same id...");
                break;
            }
            lastDocId = did.getDocId();

            System.out.println("current search did: " + did.getDocId());
            frequencies = new ArrayList<>();
            did = nextGEQ(lp.get(0), did.getDocId()); // start pair
            frequencies.add(did);
            int tempDoc = did.getDocId();
            for(int i=1;i<lp.size();i++) {// start from index=1
                DocFrequency tempDocFreq = nextGEQ(lp.get(i), did.getDocId());
                frequencies.add(tempDocFreq);
                if(tempDocFreq.getDocId()>did.getDocId()) {
                    // did.setDocId(tempDocFreq.getDocId());
                    tempDoc = tempDocFreq.getDocId();
                    break;
                }
            }

            if(tempDoc == did.getDocId()) {
                if(tempDoc > maxDoc) {
                     System.out.println("end of search list");
                     break;
                } else {

                    List<QueryItem> items = new ArrayList<>();
                    for(int i=0;i<keywords.size();i++) {
                        QueryItem item = new QueryItem(keywords.get(i),
                                lexicon.getWordInfo(keywords.get(i)).getCount(), frequencies.get(i).getFrequency());    // query, ft, fdt
                        items.add(item);
                    }

                    Query query = new Query(items, urlTable.getMap().get(did.getDocId()).getSize());
                    double bm25 = calculator.calculate(query);
                    //log.info("docId: " + did.getDocId() + "bm25 value: " + bm25);
                    result.add(new QueryResult(did.getDocId(), bm25));

                    did.setDocId(did.getDocId()+1);
                }

            } else {
                if(tempDoc == did.getDocId()) {
                    System.out.println("break...");
                }
                //System.out.println("update docId:" + tempDoc);
                did.setDocId(tempDoc);// docid and frequency not compatible
            }

        }
        //finish daat

        // close list
        for(TermInformation termInfo: lp) {
            closeList(termInfo.getRandomAccessFile());
        }

        //System.out.println("current result size: " + result.size());
        while(result.size()>MAX_RESULT) {
            result.poll();
        }
        return result;
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
        System.out.println("auxiliary table size: " + auxiliarTable.length);
        for (int i=0;i<auxiliarTable.length;i++) {
            System.out.println(auxiliarTable[i] + ' ');
        }
        System.out.println("auxiliary table: " + auxiliarTable.toString());
        int curIndex = 0;// first chunk
        int curLastDocId = auxiliarTable[curIndex];// (last docId, chunksize)
        int jumpSize = auxiliarTable.length*4;// skip the size of the auxiliary table (docId, chunksize) pair, skip how many ?
        while(curLastDocId<docId) {
            jumpSize += auxiliarTable[curIndex+1]; // skip compressed chunk

            curIndex+=2;
            termInformation.setCurIndex(curIndex);
            if(curIndex>=auxiliarTable.length-2) break;

            // update curLastDocId
            curLastDocId = auxiliarTable[curIndex];
        }
        //System.out.println("auxiliary size: " + auxiliarTable.length);
        //System.out.println("jump size: " + jumpSize);
        if(curIndex>auxiliarTable.length-2) {
            return new DocFrequency(maxDoc+1, -1);
        }
        // uncompress chunk
        try{

            int chunkSize = auxiliarTable[curIndex+1];
            byte[] compressedChunk = new byte[chunkSize];
            RandomAccessFile randomAccessFile = termInformation.getRandomAccessFile();
            //System.out.println("seek: " + (termInformation.getStartIndex()+(long)jumpSize));
            randomAccessFile.seek(termInformation.getStartIndex()+(long)jumpSize); // minus 1 ?
            randomAccessFile.read(compressedChunk);
            List<Integer> uncompressedChunk = VariableByteCode.decode(compressedChunk);

            // try to find the first docId that is larger than docId
            //System.out.println("uncompressed chunk: " + uncompressedChunk);
            int curDocId = uncompressedChunk.get(0);
            //System.out.println("uncompressed chunk: " + uncompressedChunk.toString());
            int tempIndex = 0;
            while(curDocId<docId) {
                // System.out.print("[docId: " + curDocId + ", freq:" + uncompressedChunk.get(tempIndex+1) + "]");
                tempIndex += 2;
                if(tempIndex>=uncompressedChunk.size()-2) break;
                curDocId = uncompressedChunk.get(tempIndex);
            }

            int findFrequency = uncompressedChunk.get(tempIndex+1);
            //System.out.println("currenct docid is: " + curDocId);
            //System.out.println("find doc, frequency pair: " + curDocId + "," + findFrequency);
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
                raf.seek(lexiconItem.getStartIndex());
                raf.read(auxiliaryBytes);
                int[] auxiliaryTable = ArrayConverter.toIntArray(auxiliaryBytes);
                //System.out.println("auxiliary table");
//                for(int item: auxiliaryTable) {
//                    System.out.println(item);
//                }

                TermInformation termInformation = new TermInformation(raf, auxiliaryTable);
                termInformation.setStartIndex(lexiconItem.getStartIndex());// set start index
                //System.out.println("start index: " + lexiconItem.getStartIndex());
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

        log.info("receive request");
        // implement DAAT
        List<TermInformation> lp = new ArrayList<>();
        Map<String, String> params = queryToMap(httpExchange.getRequestURI().getQuery());
        String keywords = params.get("keyword");

        List<String> keywordList = new ArrayList<>();
        for(String keyword: keywords.split("\\+")) {
            keywordList.add(keyword);
            //lp.add(openList(keyword));
        }
        keywordList = new ArrayList<>(new HashSet<>(keywordList)); // delete duplicate words in the input

        Queue<QueryResult> queryResults  = query(keywordList);

        System.out.println("finish query: " + queryResults.size());
        List<QueryResult> finalResult = new ArrayList<>();

        while(queryResults.size()>0) {
            QueryResult cur = queryResults.poll();
            String snippet = snippetGenerator.getSnippet(cur.getDocId(), new HashSet<>(keywordList));
            cur.setSnippet(snippet);
            cur.setUrl(urlTable.getUrl(cur.getDocId()));

            finalResult.add(cur);
        }
        log.info("finish generating snippets");

        // return json object
//        JsonObject jsonObject = new JsonObject();
//        jsonObject.addProperty("result", finalResult.toString());
        //String res = jsonObject.toString();
        String res = new Gson().toJson(finalResult);
        httpExchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        //httpExchange.getRequestHeaders().add("Access-Control-Allow-Origin", "*");//HttpExchange
        httpExchange.sendResponseHeaders(200, res.getBytes().length);
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

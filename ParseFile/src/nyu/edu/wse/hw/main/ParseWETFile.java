package nyu.edu.wse.hw.main;

import nyu.edu.wse.hw.domain.URLTable;
import nyu.edu.wse.hw.domain.URLTableItem;
import nyu.edu.wse.hw.thread.SnapShotWriter;
import nyu.edu.wse.hw.util.GZipReader;

import java.io.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.logging.Logger;

public class ParseWETFile {

    private static final String SEPARATOR = System.getProperty("line.separator");
    private static final String EMPTY_STR = "";
    private static final String URL_PRE_STR = "WARC-Target-URI:";
    private static final Pattern PATTERN = Pattern.compile("[\\w']+");

    //  URL table
    private URLTable urlTable = new URLTable(new HashMap<>());
    private static final String URLTable_FILE_NAME = "/home/liuchang/Documents/study/wse/homework/hw3/WSE-Homework/ParseFile/data/urltable/URLTable.txt";

    // wet file path
    //private static final String WET_FILE_FOLDER = "/media/liuchang/New Volume/study/wse/hw2-data/sample-data/";
    private static final String WET_FILE_FOLDER = "/home/liuchang/Documents/study/wse/homework/hw3/WSE-Homework/ParseFile/data/wet-files/";

    // intermediate file
    private static final String INTERMEDIATE_FILE = "/home/liuchang/Documents/study/wse/homework/hw3/WSE-Homework/ParseFile/data/inverted-intermediate/temp.txt";
    //private static final String INTERMEDIATE_FILE = "/media/liuchang/New Volume/study/wse/hw2-data/inverted-intermediate/temp.txt";

    // multi threading
    private static final int NUM_OF_PARALLEL = 10;
    private static final String SNAPHOT_PATH = "/home/liuchang/Documents/study/wse/homework/hw3/WSE-Homework/ParseFile/data/snapshot/";
    public ExecutorService snapshotService;
    public CountDownLatch countDownLatch;
    private ExecutorService intermediatePostingService;

    // save params for bm25
    private static final String BM25_CONFIG_FILE = "/home/liuchang/Documents/study/wse/homework/hw3/WSE-Homework/ParseFile/data/bm25-config/bm25-config";

    // logging
    private static final Logger log = Logger.getLogger("parseWETFilelogger");

    public static void main(String[] args) throws IOException{

        // set log
        log.setLevel(Level.ALL);
        log.info("start generating intermediate file");

        ParseWETFile parser = new ParseWETFile();

        List<String> fileNames = new ArrayList<>();
        for(File file: new File(WET_FILE_FOLDER).listFiles()) {
            fileNames.add(file.getName());
        }


        FileWriter fw = new FileWriter(INTERMEDIATE_FILE, false);
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter out = new PrintWriter(bw);

        for(String fn: fileNames) {
            System.out.println("parsing: " + fn);
            List<String> pages = parser.readFile(WET_FILE_FOLDER+fn, GZipReader.GZIP_UTF_8);
            for(String page: pages) {
                parser.parseByFile(page, out);
            }
        }

        parser.snapshotService.shutdown();// not safe

        out.close();
        bw.close();
        fw.close();

        // save urltable
        //parser.writeURLTableToDisk();

        // save params for bm25
        double avg = 0;
        for(Map.Entry<Integer, URLTableItem> urlItem: parser.urlTable.getMap().entrySet()) {
            avg += urlItem.getValue().getSize();
        }
        int size = parser.urlTable.getSize();
        Properties prop = new Properties();
        OutputStream os;
        try {
            os = new FileOutputStream(BM25_CONFIG_FILE);
            prop.setProperty("total", Integer.toString(size));
            prop.setProperty("avg", Double.toString(avg/size));// hope works
            prop.store(os, null);
            os.close();
        } catch (IOException ioe) {
            System.out.println("error saving params for bm25");
        }

        log.info("finish generating intermediate file");
    }

    // constructor
    public ParseWETFile() {
        // switch to multi threading
        this.snapshotService = Executors.newFixedThreadPool(NUM_OF_PARALLEL);
        this.intermediatePostingService = Executors.newFixedThreadPool(NUM_OF_PARALLEL);
        this.countDownLatch = new CountDownLatch(1);
    }

    // read input file
    private List<String> readFile(String fileName, String encoding) {
        return GZipReader.gZipReader(fileName, encoding);
    }


    // parse one page
    private void parseByFile(String fileContent, PrintWriter out) {
        HashMap<String, Integer> wordDict = new HashMap<>();
        Set<String> hasVisited = new HashSet<>();

        String [] split = fileContent.split(SEPARATOR);
        String url = "";

        int i=0;
        int wordCounter =0;
        while(!EMPTY_STR.equals(split[i])) {
            // try parse url and all
            if(split[i].indexOf(URL_PRE_STR) != -1) {
                String[] splitLine = split[i].split(" ");
                url = splitLine[splitLine.length-1];
            }
            i++;
        }

        // word count
        StringBuffer sbToSave = new StringBuffer();
        while(i<split.length) {

            Matcher m = PATTERN.matcher(split[i]);
            while(m.find()) {
                // stats on words
                String extractWord = split[i].substring(m.start(), m.end());
                hasVisited.add(extractWord);
                wordCounter++;
                addOne(wordDict, extractWord);
            }

            sbToSave.append(split[i] + '\n');
            i++;

        }

        // write to intermediate file
        for(String word: hasVisited) {
            out.printf("%s\t%d\t%d\n", word, URLTable.getCounter(), wordDict.get(word));
        }
        out.flush();

        // update url table
        urlTable.addURL(url, wordCounter); // 这里存放的大小有bug, 应该存放见到过的全部单词个数，包括重复的

        // generate snapshot
        //this.snapshotService.execute(new SnapShotWriter(SNAPHOT_PATH, this.urlTable.getCounter(), sbToSave.toString()));
    }


    private void writeURLTableToDisk() {
        try{
            FileOutputStream fos = new FileOutputStream(URLTable_FILE_NAME, false);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this.urlTable);
            oos.close();
            fos.close();
            System.out.println(URLTable.getCounter());
            System.out.println("finish writing url table to file");
            System.out.println("url table size: " + urlTable.getSize());
        } catch (IOException ioe) {
            System.out.println("error writing url table to file");
            System.out.println(ioe);
            System.out.println(ioe.getMessage());
        }

    }

    // used in intermediate file
    private void addOne(HashMap<String, Integer> wordDict, String word) {
        if(wordDict.containsKey(word)) {
            wordDict.put(word, wordDict.get(word) + 1);
        } else {
            wordDict.put(word, 1);
        }
    }

}

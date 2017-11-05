package nyu.edu.wse.hw.main;

import nyu.edu.wse.hw.domain.URLTable;
import nyu.edu.wse.hw.domain.URLTableItem;
import nyu.edu.wse.hw.thread.SnapShotWriter;
import nyu.edu.wse.hw.util.GZipReader;

import java.io.*;
import java.util.*;
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
    private static final String PARSE_CONFIG_FILE = "/home/liuchang/Documents/study/wse/homework/hw3/WSE-Homework/ParseFile/config/common-config-80-files";
    //private static final String PARSE_CONFIG_FILE = "/media/liuchang/HardDrive/study/wse/hw2-data/config/bm25-config-30-files";

    private static String URLTable_FILE;
    private static String WET_FILE_FOLDER;
    private static String INTERMEDIATE_FILE;

    // multi threading
    private static final int NUM_OF_PARALLEL = 10;
    private static String SNAPHOT_PATH;
    // save params for bm25
    private static String BM25_CONFIG_FILE;

    //private static final String INTERMEDIATE_FILE = "/media/liuchang/New Volume/study/wse/hw2-data/inverted-intermediate/temp.txt";
    private URLTable urlTable = new URLTable(new HashMap<>());

    public ExecutorService snapshotService;

    // logging
    private static final Logger log = Logger.getLogger("parseWETFilelogger");

    // constructor
    public ParseWETFile() {
        // add config files
        Properties prop = new Properties();
        InputStream is;
        try {
            is = new FileInputStream(PARSE_CONFIG_FILE);
            prop.load(is);
            this.URLTable_FILE = prop.getProperty("URLTable_FILE");
            this.WET_FILE_FOLDER = prop.getProperty("WET_FILE_FOLDER");
            this.INTERMEDIATE_FILE = prop.getProperty("INTERMEDIATE_FILE");
            this.SNAPHOT_PATH = prop.getProperty("SNAPHOT_PATH");
            this.BM25_CONFIG_FILE = prop.getProperty("BM25_CONFIG_FILE");
            System.out.println("bm25 path: " + this.BM25_CONFIG_FILE);
            System.out.println("wet file folder: " + this.WET_FILE_FOLDER);
            System.out.println("finish loading params");
        } catch (IOException ioe) {
            System.out.println("error while parsing config file: " + ioe.getMessage());
        }

        this.snapshotService = Executors.newFixedThreadPool(NUM_OF_PARALLEL);
    }

    public static void main(String[] args) throws IOException{

        ParseWETFile parse = new ParseWETFile();
        parse.main();
    }

    public void main() {

        log.setLevel(Level.ALL);
        log.info("start generating intermediate file");

        List<String> fileNames = new ArrayList<>();
        for(File file: new File(WET_FILE_FOLDER).listFiles()) {
            fileNames.add(file.getName());
        }

        try {

            FileWriter fw = new FileWriter(INTERMEDIATE_FILE+"test", false);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw);

            for(String fn: fileNames) {
                System.out.println("parsing: " + fn);
                List<String> pages = readFile(WET_FILE_FOLDER+fn, GZipReader.GZIP_UTF_8);// list of pages in one wet file
                for(String page: pages) {
                    parseByFile(page, out);
                }
            }

            snapshotService.shutdown();

            out.close();
            bw.close();
            fw.close();

            // save urltable
            writeURLTableToDisk();
            // save bm25 params
            saveBM25Params();

        } catch (IOException ioe) {
            log.log(Level.SEVERE, "error while running main: " + ioe.getMessage());
        }

        log.info("finish generating intermediate file");

    }

    private void saveBM25Params() {
        // save params for bm25
        double avg = 0;
        for(Map.Entry<Integer, URLTableItem> urlItem: urlTable.getMap().entrySet()) {
            avg += urlItem.getValue().getSize();
        }
        int size = urlTable.getSize();
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
//        for(String word: hasVisited) {
//            out.printf("%s\t%d\t%d\n", word, URLTable.getCounter(), wordDict.get(word));
//        }

        // update url table
        urlTable.addURL(url, wordCounter);

        // generate snapshot
        int curDocId = this.urlTable.getCounter()-1;
        //this.snapshotService.execute(new SnapShotWriter(SNAPHOT_PATH, curDocId, sbToSave.toString()));
    }


    private void writeURLTableToDisk() {
        try{
            FileOutputStream fos = new FileOutputStream(URLTable_FILE, false);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this.urlTable);
            oos.close();
            fos.close();
            log.info(URLTable.getCounter()+"");
            log.info("finish writing url table to file");
            log.info("url table size: " + urlTable.getSize());
        } catch (IOException ioe) {
            log.log(Level.SEVERE, "error writing url table to file");
            log.log(Level.SEVERE, ioe+"");
            log.log(Level.SEVERE, ioe.getMessage());
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

//package nyu.edu.wse.hw.thread;
//
//import nyu.edu.wse.hw.domain.URLTable;
//
//import java.io.FileNotFoundException;
//import java.io.PrintWriter;
//import java.io.UnsupportedEncodingException;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Set;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//public class IntermediateFileWriter implements Runnable{
//
//
//    private static final String SEPARATOR = System.getProperty("line.separator");
//    private static final String EMPTY_STR = "";
//    private static final String URL_PRE_STR = "WARC-Target-URI:";
//    private static final Pattern PATTERN = Pattern.compile("[\\w']+");
//    private static final String UTF_8 = "UTF-8";
//    private String intermediatePath;
//    private String intermediateFile;
//    private String content;
//    private int docId;
//    private URLTable urlTable;
//
//    public IntermediateFileWriter(String intermediatePath, String intermediateFile, String content, int docId, URLTable urlTable) {
//        this.intermediatePath = intermediatePath;
//        this.intermediateFile = intermediateFile;
//        this.content = content;
//        this.docId = docId;
//        this.urlTable = urlTable;
//
//    }
//
//    @Override
//    public void run() {
//        try{
//
//            // generate output file
//            PrintWriter pr = new PrintWriter(this.intermediatePath+this.intermediateFile, UTF_8);
//            HashMap<String, Integer> wordDict = new HashMap<>();
//            Set<String> hasVisited = new HashSet<>();
//
//            String [] split = content.split(SEPARATOR);
//            String url = "";
//
//            int i=0;
//            int wordCounter =0;
//            while(!EMPTY_STR.equals(split[i])) {
//                // try parse url and all
//                if(split[i].indexOf(URL_PRE_STR) != -1) {
//                    String[] splitLine = split[i].split(" ");
//                    url = splitLine[splitLine.length-1];
//                }
//                i++;
//            }
//
//            // word count
//            StringBuffer sbToSave = new StringBuffer();
//            while(i<split.length) {
//
//                Matcher m = PATTERN.matcher(split[i]);
//                while(m.find()) {
//                    // stats on words
//                    String extractWord = split[i].substring(m.start(), m.end());
//                    hasVisited.add(extractWord);
//                    wordCounter++;
//                    addOne(wordDict, extractWord);
//                }
//
//                sbToSave.append(split[i] + '\n');
//                i++;
//
//            }
//
//            // write to file
//            // write to intermediate file
//            for(String word: hasVisited) {
//                pr.printf("%s\t%d\t%d\n", word, URLTable.getCounter(), wordDict.get(word));
//            }
//            pr.flush();
//
//            //
//            synchronized (urlTable) {
//                urlTable.addURL(url, wordCounter);
//            }
//
//        } catch (UnsupportedEncodingException uee) {
//            System.out.println(" error encoding using utf-8");
//        } catch (FileNotFoundException fne) {
//            System.out.println(" file not found exception");
//        }
//    }
//
//
//    // used in intermediate file
//    private static void addOne(HashMap<String, Integer> wordDict, String word) {
//        if(wordDict.containsKey(word)) {
//            wordDict.put(word, wordDict.get(word) + 1);
//        } else {
//            wordDict.put(word, 1);
//        }
//    }
//}

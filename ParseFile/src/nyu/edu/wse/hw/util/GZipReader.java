package nyu.edu.wse.hw.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import static com.google.common.base.CharMatcher.ASCII;

public class GZipReader {

    public static final String GZIP_UTF_8 = "UTF-8";
    private static final String STARTER = "WARC/1.0";

    public static List<String> gZipReader(String filename, String encoding) {

        // split into multiple file
        if(filename == null || filename.length() == 0) return null;

        try {
            InputStream is = new FileInputStream(filename);
            InputStream gzipStream = new GZIPInputStream(is);
            Reader decoder = new InputStreamReader(gzipStream, GZIP_UTF_8);
            BufferedReader reader = new BufferedReader(decoder);

            StringBuffer sb = new StringBuffer();
            String line;

            List<String> result = new ArrayList<>();

            // skip first 18 lines
            for(int i=0;i<18;i++) {
                reader.readLine();
            }

            // start parsing
            while ((line = reader.readLine()) != null) {

                if(STARTER.equals(line)) {
                    if(sb.length()>0) {
                        result.add(sb.toString() + "\n");
                        sb = new StringBuffer();
                        sb.append(line+"\n");
                    }
                } else {
                    sb.append(line+"\n");

                }
            }

            System.out.println("result size: " + result.size());
            return result;

        } catch (IOException ioe) {
            System.out.println("error reading gzip file");
            System.out.println(ioe);

        }
        return null;
    }

    public static List<String> filterEngishPages(List<String> pages) {
        return pages.stream().filter(line -> ASCII.matchesAllOf(line)).collect(Collectors.toList());
    }

}

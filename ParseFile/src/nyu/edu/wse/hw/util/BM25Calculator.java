package nyu.edu.wse.hw.util;

import nyu.edu.wse.hw.domain.Query;
import nyu.edu.wse.hw.domain.QueryItem;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BM25Calculator {

    private static final double k1 = 1.2;
    private static final double b = 0.75;
    public static final String BM25_CONFIG_FILE = "/home/liuchang/Documents/study/wse/homework/hw3/WSE-Homework/ParseFile/config/bm25-config";

    private int N;
    private double AVG;

    public BM25Calculator() {
        Properties prop = new Properties();
        InputStream is;
        try {

            is = new FileInputStream(BM25_CONFIG_FILE);
            prop.load(is);
            N = Integer.parseInt(prop.getProperty("total"));
            AVG = Double.parseDouble(prop.getProperty("avg"));
            System.out.println("sucessfully load params, bm25");
        } catch (IOException ioe) {
            System.out.println("error loading params: " + ioe);
        }

    }

    public double calculate(Query query) {
        double result = 0;
        for(QueryItem q: query.getItem()) {
            result += calculate(q.getFt(), q.getFdt(), query.getD());
        }
        System.out.println("bm25　result: " + result);
        return result;
    }

    private double calculate(int ft, int fdt, int d) {
        double result = 0;

        double K = k1 * ((1-b) + b * (d/AVG));
        result = Math.log((N - ft + 0.5) / (ft + 0.5)) * (k1 + 1) * fdt / (K + fdt);
        return result;
    }

}

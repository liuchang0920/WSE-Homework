package nyu.edu.wse.hw.util;

import nyu.edu.wse.hw.domain.Query;

import java.util.List;

public class BM25 {

    private static final double k1 = 1.2;
    private static final double b = 0.75;

    private int N;
    private double davg;

    public BM25(int N, int d, double davg) {

        this.N = N;
        this.davg = davg;

        // K = k1 * (1-b) + b * (d/davg);
    }

    public static double calBM25(int ft, int ftd, List<Query> queryList, int d, double davg) {
        double K = k1 * ((1 - b) + b * (d/davg));

        double result = 0.0;
        for(Query item: queryList) {

        }
    }


}

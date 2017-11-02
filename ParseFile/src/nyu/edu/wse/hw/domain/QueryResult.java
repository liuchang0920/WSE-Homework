package nyu.edu.wse.hw.domain;

public class QueryResult {

    private int docId;
    private double bm25Value;
    private String snippet;

    public QueryResult(int docId, double bm25Value) {
        this.docId = docId;
        this.bm25Value = bm25Value;
    }

    public int getDocId() {
        return docId;
    }

    public double getBm25Value() {
        return bm25Value;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    @Override
    public String toString() {
        return "QueryResult{" +
                "docId=" + docId +
                ", bm25Value=" + bm25Value +
                ", snippet='" + snippet + '\'' +
                '}';
    }
}

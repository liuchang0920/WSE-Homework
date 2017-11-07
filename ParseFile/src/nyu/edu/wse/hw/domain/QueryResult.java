package nyu.edu.wse.hw.domain;

public class QueryResult {

    private int docId;
    private double bm25Value;
    private String snippet;
    private String url;

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "{" +
                "docId=" + docId +
                ", bm25Value=" + bm25Value +
                ", snippet='" + snippet + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}

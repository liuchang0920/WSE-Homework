package nyu.edu.wse.hw.domain;

import java.util.List;

public class Query {

    private List<QueryItem> item;
    private int d;// doc length

    public Query(List<QueryItem> item, int d) {
        this.item = item;
        this.d = d;
    }

    public List<QueryItem> getItem() {
        return item;
    }

    public void setItem(List<QueryItem> item) {
        this.item = item;
    }

    public int getD() {
        return d;
    }

    public void setD(int d) {
        this.d = d;
    }
}

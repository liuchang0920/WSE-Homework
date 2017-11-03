package nyu.edu.wse.hw.domain;

import java.io.Serializable;

public class URLTableItem implements Serializable{

    private static final long serialVersionUID = 3985228888535656576L;

    private int docID;
    private String url;
    private int size;

    public URLTableItem(int docID, String url, int size) {
        this.docID = docID;
        this.url = url;
        this.size = size;
    }

    @Override
    public String toString() {
        return "{" + this.docID + ", " + this.url + ", " + this.size + "}";
    }

    public int getSize() {
        return size;
    }

    public String getUrl() {
        return url;
    }
}

package nyu.edu.wse.hw.domain;

import java.io.Serializable;

public class URLTableItem implements Serializable{

    int docID;
    String url;
    int size;

    public URLTableItem(int docID, String url, int size) {
        this.docID = docID;
        this.url = url;
        this.size = size;
    }

    @Override
    public String toString() {
        return "{" + this.docID + ", " + this.url + ", " + this.size + "}";
    }
}
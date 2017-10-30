package nyu.edu.wse.hw.domain;

import java.io.Serializable;
import java.util.HashMap;

public class URLTable implements Serializable{

    private static final long serialVersionUID = -3786582051180944371L;

    private HashMap<Integer, URLTableItem> map;
    private static int COUNTER; // serves as docID

    public URLTable(HashMap<Integer, URLTableItem> map) {
        this.map = map;
        this.COUNTER = map.size();
    }

    // 更新放到之类来操作
    public boolean containsURL(String url) {
        return map.containsKey(url);
    }

    public void addURL(String url, int size) {
        map.put(COUNTER, new URLTableItem(COUNTER++, url, size));
    }

    public static int getCounter() {
        return COUNTER;
    }

    public int getSize() {
        return map.size();
    }
}

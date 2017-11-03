package nyu.edu.wse.hw.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class URLTable implements Serializable{

    private static final long serialVersionUID = -3786582051180944371L;

    private HashMap<Integer, URLTableItem> map;
    private static AtomicInteger COUNTER; // serves as docID

    public URLTable(HashMap<Integer, URLTableItem> map) {
        this.map = map;
        this.COUNTER = new AtomicInteger(map.size());
    }

    // 更新放到之类来操作
    public boolean containsURL(String url) {
        return map.containsKey(url);
    }

    public void addURL(String url, int size) {
        map.put(COUNTER.get(), new URLTableItem(COUNTER.get(), url, size));
        COUNTER.incrementAndGet();
    }

    public static int getCounter() {
        return COUNTER.get();
    }

    public int getSize() {
        return map.size();
    }

    public HashMap<Integer, URLTableItem> getMap() {
        return map;
    }

    public String getUrl(int docId) {
        return map.get(docId).getUrl();
    }
}

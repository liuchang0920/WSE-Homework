package nyu.edu.wse.hw.domain;

import java.io.Serializable;

public class LexiconItem implements Serializable {

    private static final long serialVersionUID = 111L;
    // indicate postiion in inverted list
    private long startIndex;
    private long endIndex;
    private int count;

    public LexiconItem(int count, long startIndex, long endIndex) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(long startIndex) {
        this.startIndex = startIndex;
    }

    public long getEndIndex() {
        return endIndex;
    }

    @Override
    public String toString() {
        return "LexiconItem{" +
                "startIndex=" + startIndex +
                ", endIndex=" + endIndex +
                ", count=" + count +
                '}';
    }
}

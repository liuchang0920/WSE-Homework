package nyu.edu.wse.hw.domain;

import java.io.Serializable;

public class LexiconItem implements Serializable {

    static final long serialVersionUID = 111L;
    // indicate postiion in inverted list ?? need modification
    long startIndex;
    long endIndex; // off sets ??

    private int wordId;
    private int count;

    public LexiconItem(int wordId, int count) {

        this.wordId = wordId;
        this.count = count;
    }

    public LexiconItem(long startIndex, long endIndex, int wordId, int count) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.wordId = wordId;
        this.count = count;
    }

    public void addOne() {
        this.count++;
    }

    public int getWordId() {
        return wordId;
    }

    public void setWordId(int wordId) {
        this.wordId = wordId;
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

    public void setEndIndex(long endIndex) {
        this.endIndex = endIndex;
    }
}

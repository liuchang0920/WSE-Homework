package nyu.edu.wse.hw.domain;

import java.io.RandomAccessFile;

public class TermInformation {

    RandomAccessFile randomAccessFile;
    int[] auxiliaryTable;
    int curIndex;
    long startIndex;// minus the auxiliary part
    int curJump;

    public TermInformation(RandomAccessFile randomAccessFile, int[] auxiliaryTable) {
        this.randomAccessFile = randomAccessFile;
        this.auxiliaryTable = auxiliaryTable;
        this.curIndex = 0;

    }

    public RandomAccessFile getRandomAccessFile() {
        return randomAccessFile;
    }

    public int[] getAuxiliaryTable() {
        return auxiliaryTable;
    }

    public int getCurIndex() {
        return curIndex;
    }

    public void setCurIndex(int curIndex) {
        this.curIndex = curIndex;
    }

    public long getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(long startIndex) {
        this.startIndex = startIndex;
    }

    public int getCurJump() {
        return curJump;
    }

    public void setCurJump(int curJump) {
        this.curJump = curJump;
    }
}

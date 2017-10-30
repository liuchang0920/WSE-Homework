package nyu.edu.wse.hw.domain;

import java.io.RandomAccessFile;

public class TermInformation {

    RandomAccessFile randomAccessFile;
    int[] auxiliaryTable;

    public TermInformation(RandomAccessFile randomAccessFile, int[] auxiliaryTable) {
        this.randomAccessFile = randomAccessFile;
        this.auxiliaryTable = auxiliaryTable;
    }

    public RandomAccessFile getRandomAccessFile() {
        return randomAccessFile;
    }

    public int[] getAuxiliaryTable() {
        return auxiliaryTable;
    }
}

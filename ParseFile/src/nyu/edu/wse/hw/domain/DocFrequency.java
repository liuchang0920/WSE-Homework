package nyu.edu.wse.hw.domain;

public class DocFrequency {

    int docId;
    int frequency;

    public DocFrequency(int docId, int frequency) {
        this.docId = docId;
        this.frequency = frequency;
    }

    public int getDocId() {
        return docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFequency(int fequency) {
        this.frequency = fequency;
    }
}

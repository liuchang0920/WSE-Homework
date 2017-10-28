package nyu.edu.wse.hw.domain;

public class DocFrequency {

    int docId;
    int fequency;

    public DocFrequency(int docId, int fequency) {
        this.docId = docId;
        this.fequency = fequency;
    }

    public int getDocId() {
        return docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public int getFequency() {
        return fequency;
    }

    public void setFequency(int fequency) {
        this.fequency = fequency;
    }
}

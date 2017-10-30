package nyu.edu.wse.hw.domain;

import java.io.Serializable;
import java.util.HashMap;

public class Lexicon implements Serializable{

    private static final long serialVersionUID = 110L;
    private HashMap<String, LexiconItem> map;
    public static int COUNTER = 0; // serves as wordID

    public Lexicon() {
        map = new HashMap<>();
    }

    public void addWord(String word, LexiconItem item) {
        map.put(word, item);
    }

    public boolean contains(String word) {
        return map.containsKey(word);
    }

    public HashMap<String, LexiconItem> getMap() {
        return map;
    }

    public LexiconItem getWordInfo(String word) {
        return map.get(word);
    }
}

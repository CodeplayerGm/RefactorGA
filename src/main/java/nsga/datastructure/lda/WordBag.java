package nsga.datastructure.lda;

import java.util.Arrays;
import java.util.Vector;

public class WordBag {

    //----------------------------------------------------
    //Instance Variables
    //----------------------------------------------------
    public int[] words;
    public String rawStr;
    public int length;

    //----------------------------------------------------
    //Constructors
    //----------------------------------------------------
    public WordBag() {
        words = null;
        rawStr = "";
        length = 0;
    }

    public WordBag(int length) {
        this.length = length;
        rawStr = "";
        words = new int[length];
    }

    public WordBag(int length, int[] words) {
        this.length = length;
        rawStr = "";

        this.words = new int[length];
        for (int i = 0; i < length; ++i) {
            this.words[i] = words[i];
        }
    }

    public WordBag(int length, int[] words, String rawStr) {
        this.length = length;
        this.rawStr = rawStr;

        this.words = new int[length];
        for (int i = 0; i < length; ++i) {
            this.words[i] = words[i];
        }
    }

    public WordBag(Vector<Integer> doc) {
        this.length = doc.size();
        rawStr = "";
        this.words = new int[length];
        for (int i = 0; i < length; i++) {
            this.words[i] = doc.get(i);
        }
    }

    public WordBag(Vector<Integer> doc, String rawStr) {
        this.length = doc.size();
        this.rawStr = rawStr;
        this.words = new int[length];
        for (int i = 0; i < length; ++i) {
            this.words[i] = doc.get(i);
        }
    }

    @Override
    public String toString() {
        return "Document{" +
                "words=" + Arrays.toString(words) +
                '}';
    }
}

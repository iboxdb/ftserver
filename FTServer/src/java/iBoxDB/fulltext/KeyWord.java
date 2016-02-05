//Free
package iBoxDB.fulltext;

import iBoxDB.LocalServer.DatabaseConfig;
import iBoxDB.LocalServer.NotColumn;

public class KeyWord {

    public final static int MAX_WORD_LENGTH = 16;

    public static void config(DatabaseConfig c) {

        // English Language or Word (max=16)              
        c.EnsureTable(KeyWord.class, "E", "K(" + MAX_WORD_LENGTH + ")", "I");

        // Non-English Language or Character
        c.EnsureTable(KeyWord.class, "N", "K(1)", "I", "P");

    }

    //Key Word
    public String K;

    @NotColumn
    public String getKeyWord() {
        return K;
    }

    @NotColumn
    public void setKeyWord(String k) {
        K = k;
    }

    //Position
    public int P;

    @NotColumn
    public int getPosition() {
        return P;
    }

    @NotColumn
    public void setPosition(int p) {
        P = p;
    }

    //Document ID
    public long I;

    @NotColumn
    public long getID() {
        return I;
    }

    @NotColumn
    public void setID(long i) {
        I = i;
    }

    @NotColumn
    public boolean isWord;
    @NotColumn
    public KeyWord previous;

    @Override
    public String toString() {
        return K + ", Pos=" + P + ", ID=" + I + " " + (isWord ? "1" : "0");
    }

    public String toFullString() {
        return (previous != null ? previous.toFullString() + " -> " : "") + toString();
    }
}

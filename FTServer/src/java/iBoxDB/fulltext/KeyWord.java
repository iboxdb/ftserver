//Free
package iBoxDB.fulltext;

import iBoxDB.LocalServer.DatabaseConfig;
import iBoxDB.LocalServer.NotColumn;

public abstract class KeyWord {

    public final static int MAX_WORD_LENGTH = 16;

    public static void config(DatabaseConfig c) {

        // English Language or Word (max=16)              
        c.EnsureTable(KeyWordE.class, "/E", "K(" + MAX_WORD_LENGTH + ")", "I", "P");

        // Non-English Language or Character
        c.EnsureTable(KeyWordN.class, "/N", "K", "I", "P");

    }

    @NotColumn
    public abstract Object getKeyWord();

    @NotColumn
    public abstract void setKeyWord(Object k);

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
    public KeyWord previous;


    @NotColumn
    public String toFullString() {
        return (previous != null ? previous.toFullString() + " -> " : "") + toString();
    }
}

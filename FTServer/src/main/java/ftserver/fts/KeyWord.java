package ftserver.fts;

import iboxdb.localserver.*;

public abstract class KeyWord {

    public final static int MAX_WORD_LENGTH = 16;

    public static void config(DatabaseConfig c) {

        // English Language or Word (max=16)              
        c.ensureTable(KeyWordE.class, "/E", "K(" + MAX_WORD_LENGTH + ")", "I", "P");

        // Non-English Language or Character
        c.ensureTable(KeyWordN.class, "/N", "K", "I", "P");

    }

    @NotColumn
    public abstract int size();

    //Position
    public int P;

    //Document ID
    public long I;

 

    @NotColumn
    public KeyWord previous;
    @NotColumn
    public boolean isLinked;
    @NotColumn
    public boolean isLinkedEnd;

    @NotColumn
    public String toFullString() {
        return (previous != null ? previous.toFullString() + " -> " : "") + toString();
    }
}

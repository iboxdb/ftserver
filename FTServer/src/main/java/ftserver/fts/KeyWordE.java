package ftserver.fts;

import iboxdb.localserver.*;

public final class KeyWordE extends KeyWord {

    //Key Word
    public String K;

    public void keyWord(String k) {
        if (k.length() > KeyWord.MAX_WORD_LENGTH) {
            return;
        }
        K = k;
    }

    @NotColumn
    @Override
    public int size() {
        return K.length();
    }

    @NotColumn
    @Override
    public String toString() {
        return K + " Pos=" + P + ", ID=" + I + " E";
    }

}

package iBoxDB.fulltext;

import iBoxDB.LocalServer.NotColumn;

public final class KeyWordE extends KeyWord {

    //Key Word
    public String K;

    @NotColumn
    @Override
    public Object getKeyWord() {
        return K;
    }

    @NotColumn
    @Override
    public void setKeyWord(Object k) {
        String t = (String) k;
        if (t.length() > KeyWord.MAX_WORD_LENGTH) {
            return;
        }
        K = t;
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

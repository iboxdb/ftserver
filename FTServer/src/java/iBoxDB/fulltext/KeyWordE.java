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

}

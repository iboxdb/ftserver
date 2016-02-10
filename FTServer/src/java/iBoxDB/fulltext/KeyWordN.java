package iBoxDB.fulltext;

import iBoxDB.LocalServer.NotColumn;

public final class KeyWordN extends KeyWord {

    //Key Word
    public char K;

    @NotColumn
    @Override
    public Object getKeyWord() {
        return K;
    }

    @NotColumn
    @Override
    public void setKeyWord(Object k) {
        K = (Character) k;
    }
}

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
    public KeyWordE getOriginalForm() {
        String of = StringUtil.antetypes.get(K);
        if (of != null) {
            KeyWordE e = new KeyWordE();
            e.I = this.I;
            e.P = this.P;
            e.K = of;
            return e;
        }
        return null;
    }

    @NotColumn
    @Override
    public String toString() {
        return K + " Pos=" + P + ", ID=" + I + " E";
    }

}

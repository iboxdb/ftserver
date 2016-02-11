package iBoxDB.fulltext;

import iBoxDB.LocalServer.NotColumn;
import java.util.HashMap;

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

    public KeyWordE getOriginalForm() {
        String of = antetypes.get(K);
        if (of != null) {
            KeyWordE e = new KeyWordE();
            e.I = this.I;
            e.P = this.P;
            e.K = of;
            return e;
        }
        return null;
    }

    private static HashMap<String, String> antetypes = new HashMap<String, String>() {
        {
            put("dogs", "dog");
            put("houses", "house");
            put("grams", "gram");

            put("kisses", "kiss");
            put("watches", "watch");
            put("boxes", "box");
            put("bushes", "bush");

            put("tomatoes", "tomato");
            put("potatoes", "potato");

            put("babies", "baby");
            put("universities", "university");
            put("flies", "fly");
            put("impurities", "impurity");
        }
    };
}

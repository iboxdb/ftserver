//Free
package iBoxDB.fulltext;

import java.util.*;

class Util {

    final StringUtil sUtil = new StringUtil();

    public LinkedHashMap<Integer, KeyWord> fromString(long id, char[] str) {

        LinkedHashMap<Integer, KeyWord> kws = new LinkedHashMap<Integer, KeyWord>();

        KeyWord k = null;
        for (int i = 0; i < str.length; i++) {
            char c = str[i];
            if (c == ' ') {
                if (k != null) {
                    kws.put(k.getPosition(), k);
                }
                k = null;
            } else if (sUtil.isWord(c)) {
                if (k == null && c != '-' && c != '#') {
                    k = new KeyWord();
                    k.isWord = true;
                    k.setID(id);
                    k.setKeyWord("");
                    k.setPosition(i);
                }
                if (k != null) {
                    k.setKeyWord(k.getKeyWord() + Character.toString(c));
                }
            } else {
                if (k != null) {
                    kws.put(k.getPosition(), k);
                }
                k = new KeyWord();
                k.isWord = false;
                k.setID(id);
                k.setKeyWord(Character.toString(c));
                k.setPosition(i);
                kws.put(k.getPosition(), k);
                k = null;
            }
        }

        return kws;
    }

}

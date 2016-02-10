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
                    k = new KeyWordE();
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
                k = new KeyWordN();
                k.setID(id);
                k.setKeyWord(c);
                k.setPosition(i);
                kws.put(k.getPosition(), k);
                k = null;
            }
        }

        return kws;
    }

}

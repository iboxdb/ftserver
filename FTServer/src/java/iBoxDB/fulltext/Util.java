//Free
package iBoxDB.fulltext;

import java.util.*;

class Util {

    final StringUtil sUtil = new StringUtil();

    public ArrayList<KeyWord> fromString(long id, char[] str, boolean includeOF) {

        ArrayList<KeyWord> kws = new ArrayList<KeyWord>();

        KeyWordE k = null;
        for (int i = 0; i < str.length; i++) {
            char c = str[i];
            if (c == ' ') {
                if (k != null) {
                    kws.add(k);
                    if (includeOF) {
                        k = k.getOriginalForm();
                        if (k != null) {
                            kws.add(k);
                        }
                    }
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
                    kws.add(k);
                    if (includeOF) {
                        k = k.getOriginalForm();
                        if (k != null) {
                            kws.add(k);
                        }
                    }
                }
                k = null;
                KeyWordN n = new KeyWordN();
                n.setID(id);
                n.setKeyWord(c);
                n.setPosition(i);
                kws.add(n);
            }
        }

        return kws;
    }

}

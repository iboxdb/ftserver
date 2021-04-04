package ftserver.fts;

import java.util.*;

public class StringUtil {

    HashSet<Character> set;

    public StringUtil() {
        String s = "!\"@$%&'()*+,./:;<=>?[\\]^_`{|}~\r\n"; //@-
        s += "， 　，《。》、？；：‘’“”【｛】｝——=+、｜·～！￥%……&*（）"; //@-#
        s += "｀～！＠￥％……—×（）——＋－＝【】｛｝：；’＇”＂，．／＜＞？’‘”“";//＃
        s += "� ★☆,。？,　！";
        s += "©»¥「」";
        s += "[¡, !, \", ', (, ), -, °, :, ;, ?]-\"#";

        set = new HashSet<Character>();
        for (char c : s.toCharArray()) {
            if (isWord(c)) {
                continue;
            }
            set.add(c);
        }
        set.add((char) 0);
        set.add((char) 8203);// 0x200B
        // http://www.unicode-symbol.com/block/Punctuation.html
        for (int i = 0x2000; i <= 0x206F; i++) {
            set.add((char) i);
        }

    }

    //Chinese  [\u2E80-\u9fa5]
    //Japanese [\u0800-\u4e00]|
    //Korean   [\uAC00-\uD7A3] [\u3130-\u318F] 
    public final boolean isWord(char c) {
        //English
        if (c >= 'a' && c <= 'z') {
            return true;
        }
        if (c >= '0' && c <= '9') {
            return true;
        }
        //Russian
        // https://unicode-table.com/en/blocks/cyrillic/
        // https://unicode-table.com/en/blocks/cyrillic-supplement/
        if (c >= 0x0400 && c <= 0x052f) {
            return true;
        }
        //Germen
        if (c >= 0xc0 && c <= 0xff) {
            return true;
        }
        if (isWordRight2Left(c)) {
            return true;
        }
        //special
        return c == '-' || c == '#';
    }

    private final boolean isWordRight2Left(char c) {
        // https://unicode-table.com/en/blocks/hebrew/
        // https://www.compart.com/en/unicode/block/U+0590
        if (c >= 0x05D0 && c <= 0x05EE) {
            //not implemented yet            
            //return true;
        }
        // https://unicode-table.com/en/blocks/arabic/
        // https://www.compart.com/en/unicode/bidiclass/AL

        if (c >= 0x0617 && c <= 0x061A) {
            return true;
        }

        if (c >= 0x0620 && c <= 0x065F) {
            return true;
        }
        if (c >= 0x0660 && c <= 0x0669) {
            return true;
        }

        if (c >= 0x066E && c <= 0x06D3) {
            return true;
        }
        if (c >= 0x06D5 && c <= 0x06D5) {
            return true;
        }
        if (c >= 0x06EE && c <= 0x06FC) {
            return true;
        }
        if (c >= 0x06FF && c <= 0x06FF) {
            return true;
        }

        // https://unicode-table.com/en/blocks/arabic-supplement/
        if (c >= 0x0750 && c <= 0x077F) {
            return true;
        }
        // https://unicode-table.com/en/blocks/arabic-extended-a/
        if (c >= 0x08A0 && c <= 0x08FF) {
            return true;
        }

        return false;
    }

    public char[] clear(String str) {
        char[] cs = (str + "   ").toLowerCase().toCharArray();
        for (int i = 0; i < cs.length; i++) {
            if (cs[i] == '"') {
                continue;
            }
            if (set.contains(cs[i])) {
                cs[i] = ' ';
            }
        }
        return cs;
    }

    public ArrayList<KeyWord> fromString(long id, char[] str, boolean forIndex) {

        ArrayList<KeyWord> kws = new ArrayList<KeyWord>();

        KeyWordE k = null;
        int linkedCount = 0;
        int lastNPos = -2;
        for (int i = 0; i < str.length; i++) {
            char c = str[i];
            if (c == ' ') {
                if (k != null) {
                    kws.add(k);
                }
                k = null;

            } else if (c == '"') {
                if (k != null) {
                    kws.add(k);
                }
                k = null;

                if (linkedCount > 0) {
                    linkedCount = 0;
                    setLinkEnd(kws);
                } else {
                    linkedCount = 1;
                }
            } else if (isWord(c)) {
                if (k == null && c != '-' && c != '#') {
                    k = new KeyWordE();
                    k.I = id;
                    k.keyWord("");
                    k.P = i;
                    if (linkedCount > 0) {
                        linkedCount++;
                    }
                    if (linkedCount > 2) {
                        k.isLinked = true;
                    }
                }
                if (k != null) {
                    k.keyWord(k.K + Character.toString(c));
                }
            } else {
                if (k != null) {
                    kws.add(k);
                }
                k = null;

                KeyWordN n = new KeyWordN();
                n.I = id;
                n.P = i;
                n.longKeyWord(c, (char) 0, (char) 0);
                n.isLinked = i == (lastNPos + 1);
                kws.add(n);

                char c1 = str[i + 1];
                if ((c1 != ' ' && c1 != '"') && (!isWord(c1))) {
                    n = new KeyWordN();
                    n.I = id;
                    n.P = i;
                    n.longKeyWord(c, c1, (char) 0);
                    n.isLinked = i == (lastNPos + 1);
                    kws.add(n);
                    if (!forIndex) {
                        kws.remove(kws.size() - 2);
                        i++;
                    }
                }

                if (c1 == ' ' || c1 == '"') {
                    setLinkEnd(kws);
                }

                lastNPos = i;

            }
        }
        setLinkEnd(kws);
        return kws;
    }

    private void setLinkEnd(ArrayList<KeyWord> kws) {
        if (kws.size() > 1) {
            KeyWord last = kws.get(kws.size() - 1);
            if (last.isLinked) {
                last.isLinkedEnd = true;
            }
        }
    }

    public String getDesc(String str, KeyWord kw, int length) {
        ArrayList<KeyWord> list = new ArrayList<KeyWord>();
        while (kw != null) {
            list.add(kw);
            kw = kw.previous;
        }
        KeyWord[] ps = list.toArray(new KeyWord[0]);
        Arrays.sort(ps,
                new Comparator<KeyWord>() {
            @Override
            public int compare(KeyWord o1, KeyWord o2) {
                return o1.P - o2.P;
            }
        }
        );

        int start = -1;
        int end = -1;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ps.length; i++) {
            int len = ps[i].size();

            start = ps[i].P;
            if ((start + len) <= end) {
                continue;
            }
            if ((start + len) >= str.length()) {
                continue;
            }

            end = start + length;
            if (end > str.length()) {
                end = str.length();
            }
            sb.append(str.substring(start, end))
                    .append("...");
        }
        return sb.toString();

    }

}

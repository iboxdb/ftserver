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
        set.add((char) 0x09);
        set.add((char) 8203);// 0x200B
        // http://www.unicode-symbol.com/block/Punctuation.html
        for (int i = 0x2000; i <= 0x206F; i++) {
            set.add((char) i);
        }

    }

    //https://unicode-table.com/en/blocks/cjk-unified-ideographs/
    //Chinese  Range: 4E00—9FFF Quantity of characters: 20992 
    // https://unicode-table.com/en/blocks/cjk-unified-ideographs-extension-a/
    // CJK Unified Ideographs Extension A
    // Range: 3400—4DBF Quantity of characters: 6592 
//https://unicode-table.com/en/blocks/hiragana/    
//Japanese Range: 3040—309F Quantity of characters: 96 
//https://unicode-table.com/en/blocks/katakana/
// Range: 30A0—30FF Quantity of characters: 96 
//https://unicode-table.com/en/blocks/bopomofo/
// Range: 3100—312F Quantity of characters: 48
//https://unicode-table.com/en/blocks/hangul-syllables/
//Korean Range: AC00—D7AF Quantity of characters: 11184     
//[\uAC00-\uD7A3] [\u3130-\u318F] 
    public final boolean isWord(char c) {
        // https://unicode-table.com/en/blocks/basic-latin/
        // 0-9
        if (c >= 0x30 && c <= 0x39) {
            return true;
        }
        // A - Z
        if (c >= 0x41 && c <= 0x5A) {
            return true;
        }
        // a - z
        if (c >= 0x61 && c <= 0x7A) {
            return true;
        }

        // https://unicode-table.com/en/blocks/latin-1-supplement/
        if (c >= 0xC0 && c <= 0xFF) {
            return true;
        }

        // https://unicode-table.com/en/blocks/latin-extended-a/
        if (c >= 0x0100 && c <= 0x017F) {
            return true;
        }

        // https://unicode-table.com/en/blocks/latin-extended-b/
        if (c >= 0x0180 && c <= 0x024F) {
            return true;
        }

        // https://unicode-table.com/en/blocks/ipa-extensions/
        if (c >= 0x0250 && c <= 0x02AF) {
            return true;
        }

        // https://unicode-table.com/en/blocks/greek-coptic/
        if (c >= 0x0370 && c <= 0x03FF) {
            return true;
        }

        //Russian
        // https://unicode-table.com/en/blocks/cyrillic/
        // https://unicode-table.com/en/blocks/cyrillic-supplement/
        if (c >= 0x0400 && c <= 0x052f) {
            return true;
        }

        // https://unicode-table.com/en/blocks/armenian/
        if (c >= 0x0530 && c <= 0x058F) {
            return true;
        }

        if (isWordRight2Left(c)) {
            return true;
        }

        // https://unicode-table.com/en/blocks/hangul-jamo/
        if (c >= 0x1100 && c <= 0x11FF) {
            return true;
        }
        //https://unicode-table.com/en/blocks/hangul-jamo-extended-b/
        if (c >= 0xD7B0 && c <= 0xD7FF) {
            return true;
        }
        //Japanese
        /*
        if (c >= 0x3040 && c <= 0x312F) {
            return true;
        }
         */

        // https://unicode-table.com/en/blocks/latin-extended-additional/
        if (c >= 0x1E00 && c <= 0x1EFF) {
            return true;
        }
        // https://unicode-table.com/en/blocks/greek-extended/
        if (c >= 0x1F00 && c <= 0x1FFF) {
            return true;
        }

        //special
        return c == '-' || c == '#';
    }

    private final boolean isWordRight2Left(char c) {
        // https://unicode-table.com/en/blocks/hebrew/
        // https://www.compart.com/en/unicode/block/U+0590
        if (c >= 0x0590 && c <= 0x05FF) {
            return true;
        }
        // https://unicode-table.com/en/blocks/arabic/
        // https://www.compart.com/en/unicode/bidiclass/AL
        if (c >= 0x0600 && c <= 0x06FF) {
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

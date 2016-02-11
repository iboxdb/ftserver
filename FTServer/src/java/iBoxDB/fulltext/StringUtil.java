package iBoxDB.fulltext;

import java.util.*;

class StringUtil {

    HashSet<Character> set;

    public StringUtil() {
        String s = "!\"@$%&'()*+,./:;<=>?[\\]^_`{|}~\r\n"; //@-
        s += "， 　，《。》、？；：‘’“”【｛】｝——=+、｜·～！￥%……&*（）"; //@-#
        s += "｀～！＠￥％……—×（）——＋－＝【】｛｝：；’＇”＂，．／＜＞？’‘”“";//＃
        set = new HashSet<Character>();
        for (char c : s.toCharArray()) {
            set.add(c);
        }
        set.add((char) 0);
    }

    public boolean isWord(char c) {
        //English
        if (c >= 'a' && c <= 'z') {
            return true;
        }
        if (c >= '0' && c <= '9') {
            return true;
        }
        //Russian
        if (c >= 0x0400 && c <= 0x052f) {
            return true;
        }
        //Germen
        if (c >= 0xc0 && c <= 0xff) {
            return true;
        }
        //Korean [uAC00-uD7A3]
        return c == '-' || c == '#';
    }

    public char[] clear(String str) {
        char[] cs = (str + " ").toLowerCase().toCharArray();
        for (int i = 0; i < cs.length; i++) {
            if (set.contains(cs[i])) {
                cs[i] = ' ';
            } 
        }
        return cs;
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
                return o1.getPosition() - o2.getPosition();
            }
        }
        );

        int start = -1;
        int end = -1;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ps.length; i++) {
            if ((ps[i].getPosition() + ps[i].getKeyWord()
                    .toString().length()) < end) {
                continue;
            }
            start = ps[i].getPosition();
            end = ps[i].getPosition() + length;
            if (end > str.length()) {
                end = str.length();
            }
            sb.append(str.substring(start, end))
                    .append("...");
        }
        return sb.toString();

    }

}

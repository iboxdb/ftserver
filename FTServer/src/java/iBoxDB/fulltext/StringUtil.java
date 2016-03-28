package iBoxDB.fulltext;

import java.util.*;

public class StringUtil {

    protected static HashMap<String, String> correctKW = new HashMap<String, String>() {
        {
            put("databae", "database");
            put("beby", "baby");
            put("androd", "android");
            put("canguan", "餐馆");
            put("meishi", "美食");
        }
    };

    protected static HashMap<String, String> antetypes = new HashMap<String, String>() {
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

    HashSet<Character> set;
    public HashSet<String> mvends;

    public StringUtil() {
        String s = "!\"@$%&'()*+,./:;<=>?[\\]^_`{|}~\r\n"; //@-
        s += "， 　，《。》、？；：‘’“”【｛】｝——=+、｜·～！￥%……&*（）"; //@-#
        s += "｀～！＠￥％……—×（）——＋－＝【】｛｝：；’＇”＂，．／＜＞？’‘”“";//＃
        s += "� ★☆,。？,　！";
        s += "©»¥「」";

        set = new HashSet<Character>();
        for (char c : s.toCharArray()) {
            set.add(c);
        }
        set.add((char) 0);

        String[] ms = new String[]{
            "are", "were", "have", "has", "had",
            "you", "she", "her", "him", "like", "will", "would", "should",
            "when", "than", "then", "that", "this", "there", "who", "those", "these",
            "with", "which", "where", "they", "them", "one",
            "does", "doesn", "did", "gave", "give",
            "something", "someone", "about", "come"
        };
        mvends = new HashSet<String>();
        for (String c : ms) {
            mvends.add(c);
        }
    }

    //Chinese  [\u2E80-\u9fa5]
    //Japanese [\u0800-\u4e00]|
    //Korean   [\uAC00-\uD7A3] [\u3130-\u318F] 
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
        return c == '-' || c == '#';
    }

    public char[] clear(String str) {
        char[] cs = (str + "   ").toLowerCase().toCharArray();
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
            int len = ps[i] instanceof KeyWordE ? ps[i].getKeyWord()
                    .toString().length() : ((KeyWordN) ps[i]).size();
            if ((ps[i].getPosition() + len) <= end) {
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

    public void correctInput(ArrayList<KeyWord> kws) {
        for (int i = 0; i < kws.size(); i++) {
            KeyWord kw = (KeyWord) kws.get(i);
            if (kw instanceof KeyWordE) {
                String str = kw.getKeyWord().toString();
                str = correctKW.get(str);
                if (str != null) {
                    if (isWord(str.charAt(0))) {
                        kw.setKeyWord(str);
                    } else {
                        KeyWordN kwn = new KeyWordN();
                        kwn.I = kw.I;
                        kwn.P = kw.P + 1;
                        switch (str.length()) {
                            case 1:
                                kwn.longKeyWord(str.charAt(0), (char) 0, (char) 0);
                                break;
                            case 2:
                                kwn.longKeyWord(str.charAt(0), str.charAt(1), (char) 0);
                                break;
                            default:
                                continue;
                        }
                        kws.set(i, kwn);
                    }
                }
            }
        }
    }

}

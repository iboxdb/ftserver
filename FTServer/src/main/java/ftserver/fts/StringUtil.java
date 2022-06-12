package ftserver.fts;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    public static StringUtil Instance = new StringUtil();

    private StringUtil() {

    }

    public final boolean isWord(char c) {
        return Lang.Instance.isWord(c);
    }

    public char[] clear(String str) {
        char[] cs = (str + "   ").toLowerCase().toCharArray();
        for (int i = 0; i < cs.length; i++) {
            if (cs[i] == '"') {
                continue;
            }
            if (Lang.Instance.isPunctuation(cs[i])) {
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
            if (start >= str.length()) {
                continue;
            }

            end = start + length;
            if (end > str.length()) {
                end = str.length();
            }
            sb.append(str.substring(start, end))
                    .append("... ");
        }
        return sb.toString();

    }

    public String fromatFrenchInput(String str) {
        if (str == null) {
            return "";
        }
        if (str.contains("\"")) {
            return str;
        }
        Pattern p = Pattern.compile("\\s(\\w+)([’'])(\\w+)", Pattern.UNICODE_CHARACTER_CLASS);
        Matcher m = p.matcher(" " + str.trim() + " ");

        return m.replaceAll(" \"$1$2$3\"").trim();

    }

    public static void main(String[] arg) {
        String str = "l’étranger ls’étranger S’inscrire S'Étatà d'étranger wouldn't I'm l'Europe l’Europe";
        System.out.println(StringUtil.Instance.fromatFrenchInput(str));

    }
}

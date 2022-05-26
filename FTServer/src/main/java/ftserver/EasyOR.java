package ftserver;

import ftserver.fts.StringUtil;
import java.util.*;

// the easy way convert to OR search,by removing one word
public class EasyOR {
    
    static String[] removedWords;
    
    static {
        removedWords = new String[]{"\"", "and", "with", "how", "of", "the", "的", "吗"};
    }
    
    public static ArrayList<String> toOrCondition(String str) {
        if (str == null || str.length() == 0) {
            return new ArrayList<String>();
        }
        for (String s : removedWords) {
            str = str.replaceAll(s, " ");
        }
        char[] encs = StringUtil.Instance.clear(str);
        char[] cncs = Arrays.copyOf(encs, encs.length);
        
        for (int i = 0; i < encs.length; i++) {
            if (StringUtil.Instance.isWord(encs[i])) {
                
            } else {
                encs[i] = ' ';
            }
        }
        
        for (int i = 0; i < cncs.length; i++) {
            if (StringUtil.Instance.isWord(cncs[i])) {
                cncs[i] = ' ';
            }
        }
        
        String en = compress(encs);
        String cn = compress(cncs);
        
        ArrayList<String> result = new ArrayList<String>();
        if (en.length() > 0 && cn.length() > 0) {
            result.add(en);
            result.add(cn);
        } else if (en.length() > 0) {
            result.addAll(removeOneEN(en));
        } else if (cn.contains(" ")) {
            result.addAll(removeOneEN(cn));
        } else {
            result.addAll(removeOneCN(cn));
        }
        
        return filter(result);
    }
    
    private static String compress(char[] cs) {
        StringBuilder r = new StringBuilder();
        for (char c : cs) {
            if (r.length() > 0 && r.charAt(r.length() - 1) == ' ' && c == ' ') {
                continue;
            }
            r.append(c);
        }
        return r.toString().trim();
    }
    
    private static ArrayList<String> removeOneCN(String str) {
        ArrayList<String> r = new ArrayList<String>();
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length() - 2; i += 2) {
            sb.append(str.substring(i, i + 2) + " ");
        }
        r.add(sb.toString().trim());
        
        sb = new StringBuilder();
        for (int i = str.length(); i > 2; i -= 2) {
            sb.append(str.substring(i - 2, i) + " ");
        }
        r.add(sb.toString().trim());
        
        return r;
    }
    
    private static ArrayList<String> removeOneEN(String str) {
        ArrayList<String> r = new ArrayList<String>();
        String[] sps = str.split(" ");
        if (sps.length <= 1) {
            return r;
        } else if (sps.length == 2) {
            r.add(sps[0]);
            r.add(sps[1]);
        } else {
            for (int i = 0; i < sps.length; i++) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < sps.length; j++) {
                    if (i == j) {
                        continue;
                    }
                    if (sps[j].length() < 2) {
                        continue;
                    }
                    sb.append(" " + sps[j]);
                }
                r.add(sb.toString().trim());
            }
        }
        return r;
    }
    
    private static ArrayList<String> filter(ArrayList<String> src) {
        ArrayList<String> r = new ArrayList<String>();
        for (String s : src) {
            if (s != null) {
                String s2 = s.trim();
                if (s2.length() > 1 && (!r.contains(s2))) {
                    r.add(s2);
                }
            }
        }
        return r;
    }
    
    private static String link(ArrayList<String> aas) {
        StringBuilder sb = new StringBuilder();
        for (String s : aas) {
            sb.append(s + " ");
        }
        return sb.toString().trim();
    }
    
}

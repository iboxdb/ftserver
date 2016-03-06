package iBoxDB.fulltext;

import iBoxDB.LocalServer.NotColumn;

public final class KeyWordN extends KeyWord {

    //Key Word
    public long K;

    @NotColumn
    @Override
    public Object getKeyWord() {
        return KtoString(K);
    }

    @NotColumn
    @Override
    public void setKeyWord(Object k) {
        if (k instanceof Long) {
            K = (long) k;
        } else {
            K = StringtoK((String) k);
        }
    }

    @NotColumn
    public byte size() {
        if ((K & CMASK) != 0L) {
            return 3;
        }
        if ((K & (CMASK << 16)) != 0L) {
            return 2;
        }
        return 1;
    }

    static final long CMASK = 0xFFFF;

    @NotColumn
    private static String KtoString(long k) {
        char c0 = (char) ((k & (CMASK << 32)) >> 32);
        char c1 = (char) ((k & (CMASK << 16)) >> 16);
        char c2 = (char) (k & CMASK);

        if (c2 != 0) {
            return new String(new char[]{c0, c1, c2});
        }
        if (c1 != 0) {
            return new String(new char[]{c0, c1});
        }
        return Character.toString(c0);
    }

    @NotColumn
    private static long StringtoK(String str) {
        long k = (0L | str.charAt(0)) << 32;
        if (str.length() > 1) {
            k |= ((0L | str.charAt(1)) << 16);
        }
        if (str.length() > 2) {
            k |= (0L | str.charAt(2));
        }
        return k;
    }
}

package iBoxDB.fulltext;

import iBoxDB.LocalServer.NotColumn;

public final class KeyWordN extends KeyWord {

    //Key Word
    public long K;

    @NotColumn
    @Override
    public Object getKeyWord() {
        return K;
    }

    @NotColumn
    @Override
    public void setKeyWord(Object k) {
        K = (long) k;
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
    public void longKeyWord(char c0, char c1, char c2) {
        long k = (0L | c0) << 32;
        if (c1 != 0) {
            k |= ((0L | c1) << 16);
            if (c2 != 0) {
                k |= (0L | c2);
            }
        }
        K = k;
    }

    @NotColumn
    @Override
    public String toString() {
        return KtoString(K) + " Pos=" + P + ", ID=" + I + " N";
    }

}

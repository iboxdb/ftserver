package ftserver;

import iBoxDB.LocalServer.NotColumn;
import ftserver.fts.KeyWord;
import java.util.Date;
import java.util.Random;

public class Page {

    public final static int MAX_URL_LENGTH = 150;

    public long id;
    public String url;

    public String title;
    public String description;

    public String content;

    public Date createTime = new Date();

    public long rankUpPlus = 0; // only support (1L << 59), or modify 'rankDownId()' 

    @NotColumn
    public boolean isAnd = true;

    @NotColumn
    public long rankUpId() {
        if (rankUpPlus != 0 && rankUpPlus != (1L << 59)) {
            throw new RuntimeException("");
        }
        return (id | (1L << 60)) + rankUpPlus;
    }

    @NotColumn
    public static long rankDownId(long id) {
        long up = 3L;
        return id & (~(up << 59));
    }

    @NotColumn
    public String rankUpDescription() {
        return description + " " + title;
    }

    private static final Random RAN = new Random();

    @NotColumn
    public String getRandomContent() {
        int len = content.length() - 100;
        if (len <= 20) {
            return content;
        }
        int s = RAN.nextInt(len);
        if (s < 0) {
            s = 0;
        }
        if (s > len) {
            s = len;
        }

        int end = s + 200;
        if (end > content.length()) {
            end = content.length();
        }

        return content.substring(s, end);
    }

    @NotColumn
    public KeyWord keyWord;
}

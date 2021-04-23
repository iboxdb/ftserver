package ftserver;

import iboxdb.localserver.*;
import java.util.Date;
import java.util.Random;

public class Page {

    public final static int MAX_URL_LENGTH = 512;

    public String url;

    public long textOrder;

    // too too big this html
    //public String html;
    public String text;

    public Date createTime;
    public boolean isKeyPage = false;

    public String title;
    public String keywords;
    public String description;

    public String userDescription;

    public boolean show = true;

    private static final Random RAN = new Random();

    @NotColumn
    public String getRandomContent(int length) {
        int len = text.length() - 100;
        if (len <= 20) {
            return text;
        }
        int s = RAN.nextInt(len);
        if (s < 0) {
            s = 0;
        }
        if (s > len) {
            s = len;
        }

        int end = s + length;
        if (end > text.length()) {
            end = text.length();
        }

        return text.substring(s, end);
    }

}

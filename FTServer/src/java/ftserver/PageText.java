package ftserver;

import ftserver.fts.KeyWord;
import iBoxDB.LocalServer.NotColumn;
import java.util.Date;

public class PageText {

    public static final int max_text_length = 2100;

    public static final long userPriority = 12;

    public static final long descriptionKeyPriority = 11;

    //this is the center of Priorities, under is Body.Text, upper is user's input
    public static final long descriptionPriority = 10;

    private static final long priorityOffset = 50;

    public long id() {
        return textOrder | (priority << priorityOffset);
    }

    public void id(long id) {
        //ignore set

    }

    public long textOrder;
    public long priority;

    public String url;

    public String title;

    public String text;

    //keywords
    public String keywords;

    public Date createTime = new Date();

    @NotColumn
    public String indexedText() {
        if (priority >= descriptionPriority) {
            return text + " " + title;
        }
        return text + " " + url;
    }

    @NotColumn
    public boolean isAndSearch = true;

    @NotColumn
    public KeyWord keyWord;
}

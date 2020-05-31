package ftserver;

import ftserver.fts.KeyWord;
import iBoxDB.LocalServer.NotColumn;
import java.util.Date;

public class PageText {

    public static final int max_text_length = 1100;

    public static final long userPriority = 12;

    public static final long descriptionKeyPriority = 11;

    //this is the center of Priorities, under is Body.Text, upper is user's input
    public static final long descriptionPriority = 10;

    private static final long priorityOffset = 50;

    public static PageText fromId(long id) {
        PageText pt = new PageText();
        pt.priority = id >> priorityOffset;
        pt.textOrder = id - (pt.priority << priorityOffset);
        return pt;
    }

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

        if (priority == (descriptionPriority - 1)) {
            return text + " " + url;
        }

        return text;
    }

    @NotColumn
    public boolean isAndSearch = true;

    @NotColumn
    public KeyWord keyWord;
}

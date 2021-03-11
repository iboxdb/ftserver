package ftserver;

import iboxdb.localserver.*;
import ftserver.fts.KeyWord;
import java.util.Date;

public class PageText {

    public static final long userPriority = 12;

    public static final long descriptionKeyPriority = 11;

    //this is the center of Priorities, under is Body.Text, upper is user's input
    public static final long descriptionPriority = 10;

    public static final long contextPriority = 9;

    private static final long priorityOffset = 50;

    private static String StringEmpty = "";

    public static PageText fromId(long id) {
        PageText pt = new PageText();
        pt.priority = id >> priorityOffset;
        pt.textOrder = id - (pt.priority << priorityOffset);

        pt.text = StringEmpty;
        pt.keywords = StringEmpty;
        pt.url = StringEmpty;
        pt.title = StringEmpty;
        return pt;
    }

    public static long toId(long textOrder, long priority) {
        return textOrder | (priority << priorityOffset);
    }

    public long id() {
        return toId(textOrder, priority);
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

        if (priority == contextPriority) {
            return text + " " + url;
        }

        return text;
    }

    @NotColumn
    public boolean isAndSearch = true;

    @NotColumn
    public KeyWord keyWord;

    @NotColumn
    public Page page;

    @NotColumn
    public long dbOrder = -1;
}

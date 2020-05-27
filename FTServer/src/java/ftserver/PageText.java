package ftserver;

import ftserver.fts.KeyWord;
import iBoxDB.LocalServer.NotColumn;
import java.util.Date;

public class PageText {

    public static final int max_text_length = 1100;

    //this is the center of Priorities, under is Body.Text, upper is user's input
    public static final long descriptionPriority = 10;

    private final long priorityOffset = 50;

    public long Id() {
        return pageId & (priorityId << priorityOffset);
    }

    public void Id(long id) {
        //ignore set

    }

    public long pageId;
    public long priorityId;

    public String url;

    public String title;

    public String text;

    //keywords
    public String keywords;

    public Date createTime = new Date();

    @NotColumn
    public String indexedText() {
        return text + " " + title;
    }

    @NotColumn
    public boolean isAndSearch = true;

    @NotColumn
    public KeyWord keyWord;
}

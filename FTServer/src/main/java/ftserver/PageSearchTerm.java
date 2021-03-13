package ftserver;

import java.util.Date;
import java.util.UUID;

public class PageSearchTerm {

    public final static int MAX_TERM_LENGTH = 24;

    public Date time;
    public String keywords;
    public UUID uid;
}

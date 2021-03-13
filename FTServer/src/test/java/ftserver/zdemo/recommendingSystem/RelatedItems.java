package ftserver.zdemo.recommendingSystem;
 
import iboxdb.localserver.*;

public class RelatedItems {

    private static final Object[] defaultItems = new Object[0];

    public long id;
    public Object[] items = defaultItems;

    @NotColumn
    public String text = "";
}

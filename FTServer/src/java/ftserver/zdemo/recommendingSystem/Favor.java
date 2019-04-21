package ftserver.zdemo.recommendingSystem;
 
public class Favor {

    public Favor() {
    }

    public Favor(long uid, long iid) {
        userId = uid;
        itemId = iid;
    }
    public long userId;
    public long itemId;
}

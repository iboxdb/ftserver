package ftserver;

import iboxdb.localserver.*;

public class App {

    //for Application
    public static AutoBox Item;

    //for New Index
    public static AutoBox Index;

    //for Readonly PageIndex
    public static final ReadonlyList Indices = new ReadonlyList();
    
    
    public static boolean IsAndroid;

    public static void log(String msg) {
        System.out.println(msg);
    }
}

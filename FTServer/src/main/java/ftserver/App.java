package ftserver;

import iboxdb.localserver.*;
import java.util.ArrayList;

public class App {

    //for Application
    public static AutoBox Item;

    //for New Index
    public static AutoBox Index;

    //for Readonly PageIndex
    public static ArrayList<AutoBox> Indices;

    public static void log(String msg) {
        System.out.println(msg);
    }
}

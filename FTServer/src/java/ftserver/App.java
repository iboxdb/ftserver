package ftserver;

import iBoxDB.LocalServer.*;

public class App {

    //for Application
    public static AutoBox Item;

    //for PageIndex
    public static AutoBox Auto;

    public static Box cube() {
        return Auto.cube();
    }

    public static void log(String msg) {
        System.out.println(msg);
    }
}

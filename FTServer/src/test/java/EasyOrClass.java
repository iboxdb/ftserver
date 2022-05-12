
import ftserver.EasyOR;
import java.util.ArrayList;

public class EasyOrClass {

    public static void main(String[] arg) {

        System.out.println("EasyOr");

        String msg;

        print(null);
        print("");
        print(" ");
        print("Java .NET NoSQL数据库 iBoxDB");
        print("Java .NET NoSQL 数据库");

        print("Java");
        print("Java .NET");
        print("Java .NET NoSQL");
        print("Java .NET Embedded NoSQL");

        print("数");
        print("数据");
        print("数据库");
        print("数据库空");
        
        print("数据库空间");
        print("数据库，空间");
        
        print("数据库空间最");
        print("数据库空间最佳");
        print("数据库空间最佳性");
        print("数据库空间最佳性能");
        print("数据库空间 最佳性能");        
        print("数据库空间　最佳性能");
    }

    private static void print(String msg) {
        ArrayList<String> al = EasyOR.toOrCondition(msg);
        System.out.println(msg + "  " + al.size() + " : ");
        for (String a : al) {
            System.out.println(a);
        }
        System.out.println();

    }
}

package ftserver.zdemo;

import iBoxDB.LocalServer.*;
import ftserver.fts.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.Buffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class MainClass {

    public static void main(String[] args) throws Exception {

        System.out.println(java.lang.Runtime.getRuntime().maxMemory());
        DB.root("/tmp/");
        //DB.root("/home/user/ftsdb/err");
        test1();
        
        //-Xmx=4G
        //test_big_n();
        //test_big_e();

        //test_order();
    }

    public static void test_order() {
        iBoxDB.LocalServer.BoxSystem.DBDebug.DeleteDBFiles(3);
        DB db = new DB(3);
        Engine engine = new Engine();
        engine.Config(db.getConfig());

        AutoBox auto = db.open();
        int count = 100;
        final String[] ts = new String[count];
        for (int i = 0; i < count; i++) {
            ts[i] = "test " + i;
        }
        for (int i = 0; i < ts.length; i++) {
            try (Box box = auto.cube()) {
                engine.indexText(box, i, ts[i], false);
                box.commit().Assert();
            }
        }

        boolean doagain = true;
        long startId = Long.MAX_VALUE;
        long tcount = 0;
        while (doagain && (startId >= 0)) {
            doagain = false;
            try (Box box = auto.cube()) {
                for (KeyWord kw : engine.searchDistinct(box, "test", startId, 9)) {
                    System.out.println(engine.getDesc(ts[(int) kw.I], kw, 20));
                    tcount++;
                    doagain = true;
                    startId = kw.I - 1;
                }
            }
            System.out.println();
            System.out.println(startId);
        }
        System.out.println(count + " == " + tcount);
        auto.getDatabase().close();
    }

    public static void test1() {

        final String[] ts = new String[]{
            //ID=0
            "Setting up Git\n"
            + "\n"
            + "Download and install the latest version of GitHub Desktop. "
            + "This will automatically install Git and keep it up-to-date for you.\n"
            + "On your computer, open the Git Shell application.\n"
            + "Tell Git your name so your commits will be properly labeled. Type everything after the $ here:\n"
            + "\n babies "
            + "git config --global user.name \"YOUR NAME\"\n"
            + "Tell Git the email address that will be associated with your Git commits. "
            + "The email you specify should be the same one found in your email settings. "
            + "To keep your email address hidden, 電腦上任意型態 "
            + " 关于 see \"Keeping your C# Java NoSQL email address abc@global.com private\".",
            //ID=1
            "关于版本控制\n"
            + "什么是“版本控制”？我为什么要关心它呢？ 版本控制 android 是一种记录一个或若干文件内容变化，database"
            + "以便将来查阅特定版本修订情况的系统。 在本书所展示的例子中，我们对保存着软件源代码的文件作版本控制，"
            + "但实际上，C lang IT 美食 你可以对任何类型的文件进行版本控制。",
            //ID=2
            "バージョン管理に関して\n"
            + "\n"
            + "「バージョン管理」とは何でしょうか。また、なぜそれを気にする必要があるのでしょうか。 "
            + "バージョン管理とは、一つのファイルやファイルの集合に対して時間とともに加えられていく変更を記録するシステムで、"
            + "後で特定バージョンを呼び出すことができるようにするためのものです。"
            + " 本書の例では、バージョン管理されるファイルとしてソフトウェアのソースコードを用いていますが、"
            + "実際にはコンピューター上のあらゆる種類のファイルをバージョン管理のもとに置くことができます。",
            //ID=3
            "關於版本控制\n"
            + "什麼是版本控制？ 以及為什麼讀者會在意它？ "
            + "版本控制是一個能夠記錄一個或一組檔案在某一段時間的變更，"
            + "使得讀者以後能取回特定版本的系統。has NoSQL 1234567890ABCDEFGH java"
            + "在本書的範例中，讀者會學到如何對軟體的原始碼做版本控制。"
            + " 即使實際上讀者幾乎可以針對電腦上任意型態的檔案做版本控制。",
            //ID=4
            "Git 简史\n"
            + "同生活中的许多伟大事物一样，Git 诞生于一个极富纷争大举创新的年代。nosql \n"
            + "\n"
            + "Linux 内核开源项目有着为数众广的参与者。 绝大多数的 Linux 内核维护工作都花在了提交补丁和保存归档的"
            + "繁琐事务上（1991－2002年间）。 到 2002 年，"
            + "整个项目组开始启用一个专有的分布式 version 版本控制系统 BitKeeper 来管理和维护代码。\n"
            + "\n"
            + "到了 2005 年，开发 BitKeeper 的商业公司同 Linux 内核开源社区的合作关系结束，"
            + "他们收回了 Linux 内核社区免费使用 BitKeeper 的权力。"
            + " 这就迫使 Linux 开源社区（特别是 Linux 的缔造者 Linux Torvalds）基于使用 BitKcheper 时的"
            + "经验教训，开发出自己的版本系统　。 他们对新的系统制订了若干目标：",
            //ID=5
            "버전 관리란?\n\n버전 관리는 무엇이고 우리는 왜 이것을 알아야 할까? 버전 관리 시스템은 파일 변화를 시간에 따라 "
            + "기록했다가 나중에 특정 시점의 버전을 다시 꺼내올 수 있는 시스템이다. 이 책에서는 버전 관리하는 예제로 소프트웨어 "
            + "소스 코드만 보여주지만, 실제로 거의 모든 컴퓨터 파일의 버전을 관리할 수 있다.\n\n그래픽 디자이너나"
            + "웹 디자이너도 버전 관리 시스템(VCS - Version Control System)을 사용할 수 있다. VCS로 이미지나 레이아웃의"
            + "버전(변경 이력 혹은 수정 내용)을 관리하는 것은 매우 현명하다. VCS를 사용하면 각 파일을 이전 상태로 되돌릴 수 있고,"
            + "프로젝트를 통째로 이전 is 상태로 되돌릴 수 있고, 시간에 따라 수정 내용을 비교해 볼 수 있고,"
            + "누가 문제를 일으켰는지도 추적할 수 있고, 누가 언제 만들어낸 이슈인지도 알 수 있다. VCS를 사용하면 파일을 잃어버리거나"
            + "잘못 고쳤을 때도 쉽게 복구할 수 있다. HAS GIT 이런 모든 장점을 큰 노력 없이 이용할 수 있다.",
            //ID=6
            "  and yet  he thought  as they joined the queue lining up outside snape s classroom door  "
            + "she had chosen to come and talk to him  hadn t she  she had been cedric s girlfriend  "
            + "she could easily have hated harry for coming out of the triwizard maze alive when cedric "
            + "had died  yet she was talking to him in a perfectly friendly way  not as though "
            + "she thought him mad  or a liar  or in some horrible way responsible for cedric s death      \n",
            //ID = 7
            "  \"if these things are important enough to pass on right under the nose of the   ministry  "
            + "you'd think he'd have left us know why ­ unless he thought it was obvious \"     \"thought "
            + "wrong  then  didn t he \" said ron     \n",
            //ID = 8
            "as he drove toward town he thought of nothing except a large order of drills "
            + "he was hoping to get that day  "

        };

        for (int tran = 0; tran < 2; tran++) {
            iBoxDB.LocalServer.BoxSystem.DBDebug.DeleteDBFiles(3);
            DB db = new DB(3);
            Engine engine = new Engine();
            engine.Config(db.getConfig());

            AutoBox auto = db.open();

            for (int i = 0; i < ts.length; i++) {
                if (tran == 0) {
                    try (Box box = auto.cube()) {
                        engine.indexText(box, i, ts[i], false);
                        box.commit().Assert();
                    }
                } else {
                    engine.indexTextNoTran(auto, 3, i, ts[i], false);
                }
            }
            try (Box box = auto.cube()) {
                //engine.indexText(box, 4, ts[4], true);
                box.commit().Assert();
            }

            try (Box box = auto.cube()) {
                for (KeyWord kw : engine.search(box, "\"Java NoSQL\" \"上任意\"")) {
                    System.out.println(kw.toFullString());
                    System.out.println(engine.getDesc(ts[(int) kw.I], kw, 20));
                    System.out.println();
                }
                for (String skw : engine.discover(box,
                        'n', 's', 2,
                        '\u2E80', '\u9fa5', 2)) {
                    System.out.println(skw);
                }
            }
            db.close();
            System.out.println("----------------------");
        }
    }

    // 150 seconds test
    public static void test_big_n() throws FileNotFoundException, IOException, InterruptedException {
        String book = "/github/hero.txt";  //UTF-8
        long dbid = 1;
        boolean rebuild = false;
        int istran = 0;
        String split = "10000"; // "。";
        String strkw = "黄蓉 郭靖 洪七公";
        //strkw = "洪七公 黄蓉 郭靖";
        //strkw = "黄蓉 郭靖 公";
        //strkw = "郭靖 黄蓉";
        
        //strkw = "黄蓉";
        //strkw = "时察";
        //strkw = "的";
        //strkw = "七十二路";
        //strkw = "十八掌";
        //strkw = "日日夜夜无穷无尽的";
        //strkw = "牛家村边绕 日日夜夜无穷无尽的";
        //strkw = "这几天";
        //strkw = "有 这几天";
        //strkw = "这几天 有";
        //strkw = "牛家村边绕";
        test_big(book, dbid, rebuild, split, strkw, istran);
    }

    // 30 seconds test
    public static void test_big_e() throws FileNotFoundException, IOException, InterruptedException {
        String book = "/phoenix.txt"; //UTF-8
        long dbid = 2;
        boolean rebuild = false;
        int istran = 0;//10;
        String split = "\\.";
        String strkw = "Harry";
        //strkw = "Harry Philosopher";
        //strkw = "Philosopher";
        //strkw = "\"Harry Philosopher\"";
        //strkw = "\"He looks\"";
        //strkw = "He looks";
        //strkw = "\"he drove toward town he thought\"";
        //strkw = "\"he drove toward\"";
        //strkw = "\"he thought\"";
        //strkw = "\"he thought\" toward";
        //strkw = "toward \"he thought\"";
        //strkw = "he thought";
        //strkw = "he thought toward";
        //strkw = "He";
        test_big(book, dbid, rebuild, split, strkw, istran);
    }

    private static void test_big(String book, long dbid, boolean rebuild,
            String split, String strkw, final int istran) throws FileNotFoundException, IOException, InterruptedException {
        String path = System.getProperty("user.home") + book;

        RandomAccessFile rf = new RandomAccessFile(path, "r");
        byte[] bs = new byte[(int) rf.length()];
        rf.readFully(bs);

        if (rebuild) {
            iBoxDB.LocalServer.BoxSystem.DBDebug.DeleteDBFiles(dbid);
        }
        
        DB db = new DB(dbid);

        rf.close();

        String[] tstmp;
        int splitNum = 0;
        try {
            splitNum = Integer.parseInt(split);
        } catch (Throwable te) {

        }

        if (splitNum == 0) {
            tstmp = new String(bs).split(split);
        } else {
            String text = new String(bs);
            int leng = text.length() / splitNum;
            tstmp = new String[leng + 1];
            for (int i = 0; i < leng; i++) {
                tstmp[i] = text.substring(i * splitNum, (i + 1) * splitNum);
            }
            tstmp[leng] = text.substring(leng * splitNum);
        }

        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < 3; i++) {
            for (String str : tstmp) {
                list.add(str);
            }
        }
        final String[] ts = list.toArray(new String[0]);

        final Engine engine = new Engine();
        engine.Config(db.getConfig());
        
        DatabaseConfig dbcfg = db.getConfig();
        dbcfg.CacheLength = dbcfg.mb(1300); 

        final AutoBox auto = db.open();

        long begin;
        if (rebuild) {
            ExecutorService pool = Executors.newFixedThreadPool(8);
            begin = System.currentTimeMillis();
            final AtomicLong rbcount = new AtomicLong(0);
            for (int i = 0; i < ts.length; i++) {
                final int tsi = i;
                pool.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (istran < 1) {
                            try (Box box = auto.cube()) {
                                long t_id = box.newId(); //tsi;
                                rbcount.addAndGet(engine.indexText(box, t_id, ts[tsi], false));
                                box.commit().Assert();
                            }
                        } else {
                            rbcount.addAndGet(engine.indexTextNoTran(auto, istran, tsi, ts[tsi], false));
                        }
                    }
                });
            }
            pool.shutdown();
            pool.awaitTermination(1, TimeUnit.DAYS);
            System.out.println("Index " + ((System.currentTimeMillis() - begin) / 1000.0) + " -" + rbcount.get());
        }

        HashSet<Long> items = new HashSet<Long>();
        int c;
        for (int i = 0; i < 20; i++) {
            begin = System.currentTimeMillis();
            c = 0;
            try (Box box = auto.cube()) {
                //search  searchDistinct
                for (KeyWord kw : engine.searchDistinct(box, strkw)) {
                    c++;
                    //System.out.println(engine.getDesc(ts[0], kw, 15));
                    //System.out.println(kw.toFullString());
                    //items.add(kw.I);
                }
            }
            System.out.println("FTS Count: " + c + " ,Time: " + ((System.currentTimeMillis() - begin) / 1000.0));
        }

        StringUtil sutil = new StringUtil();
        for (int i = 0; i < ts.length; i++) {
            ts[i] = ts[i].toLowerCase() + " ";
            ts[i] = " " + new String(sutil.clear(ts[i])) + " ";
        }
        strkw = strkw.toLowerCase();

        String[] kws = strkw.split(" ");
        String tmp_kws = null;
        for (int i = 0; i < kws.length; i++) {
            if (kws[i].length() < 1) {
                kws[i] = null;
                continue;
            }
            if (tmp_kws == null) {
                if (kws[i].startsWith("\"")) {
                    tmp_kws = kws[i];
                    kws[i] = null;
                }
            } else if (tmp_kws != null) {
                tmp_kws += (" " + kws[i]);

                if (kws[i].endsWith("\"")) {
                    kws[i] = tmp_kws.substring(1, tmp_kws.length() - 1);
                    tmp_kws = null;
                } else {
                    kws[i] = null;
                }
            }
        }

        System.out.println("WORD: " + strkw + " " + items.size());
        begin = System.currentTimeMillis();
        c = 0;

        Test:
        for (int i = 0; i < ts.length; i++) {

            for (int j = 0; j < kws.length; j++) {
                if (kws[j] == null) {
                    continue;
                }
                int p = 0;
                Test_P:
                while (p >= 0) {
                    p = ts[i].indexOf(kws[j], p + 1);
                    if (p < 0) {
                        continue Test;
                    }
                    if (onlyPart(ts[i], kws[j], p)) {
                        continue Test_P;
                    }
                    break;
                }
            }
            c++;

            //if (!items.contains((long) i)) {
            /*
            if (!items.remove((long) i)) {
                System.out.println(ts[i]);
                System.out.println();
            }*/
        }/*
        for (long l : items) {
            System.out.println(ts[(int) l]);
        }*/
        System.out.println("MEM Count: " + c + " ,Time: " + ((System.currentTimeMillis() - begin) / 1000.0) + " Lines:" + ts.length);

    }

    private static boolean onlyPart(String str, String wd, int p) {
        char last = wd.charAt(wd.length() - 1);
        if (last > 256) {
            return false;
        }
        char pc = str.charAt(p + wd.length());
        if (pc >= 'a' && pc <= 'z') {
            return true;
        }
        if (pc == '-') {
            return true;
        }

        int bef = p;
        Test:
        while (bef > 0) {
            pc = str.charAt(bef - 1);
            if (pc >= 'a' && pc <= 'z') {
                return true;
            }
            if (pc == '-') {
                bef--;
                continue Test;
            }
            break;
        }

        return false;
    }

}

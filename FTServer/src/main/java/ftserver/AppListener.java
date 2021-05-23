package ftserver;

import iboxdb.localserver.*;
import java.io.File;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import static ftserver.App.*;
import java.util.ArrayList;

@WebListener
public class AppListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        App.IsAndroid = false;
        try {
            App.IsAndroid = Class.forName("dalvik.system.DexClassLoader") != null;
        } catch (Throwable e) {

        }
        log("IsAndroid = " + App.IsAndroid);
        long tm = java.lang.Runtime.getRuntime().maxMemory();
        log("Xmx = " + (tm / 1024 / 1024) + " MB");

        File mvnConfig = new File(".mvn/jvm.config");
        if (mvnConfig.exists()) {
            log("Maven 3 -Xmx setting " + mvnConfig.getAbsolutePath());
        }

        //Path
        String dir = "DATA_FTS_JAVA_161";

        String path = System.getProperty("user.home") + File.separatorChar + dir + File.separatorChar;

        new File(path).mkdirs();
        log(System.getProperty("java.version"));
        log(String.format("DB Path=%s ", new File(path).getAbsolutePath()));
        DB.root(path);

        //Config
        App.Item = new IndexServer().getInstance(IndexServer.ItemDB).get();

        long start = IndexServer.IndexDBStart;
        for (File f : new File(path).listFiles()) {

            String fn = f.getName().replace("db", "")
                    .replace(".ibx", "");
            try {
                long r = Long.parseLong(fn);
                if (r > start) {
                    start = r;
                }
            } catch (Throwable e) {
            }
        }

        App.Indices = new ArrayList<AutoBox>();
        for (long l = IndexServer.IndexDBStart; l < start; l++) {
            App.Indices.add(new ReadonlyIndexServer().getInstance(l).get());
        }
        App.Indices.add(new IndexServer().getInstance(start).get());
        log("Current Index DB (" + start + ")");
        App.Index = App.Indices.get(App.Indices.size() - 1);

        log("DB Started...");
        IndexPage.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        IndexPage.shutdown();
        IndexPage.addSearchTerm("SystemShutdown", true);
        if (App.Item != null) {
            App.Item.getDatabase().close();
            App.Item = null;
        }
        if (App.Indices != null) {
            for (AutoBox d : App.Indices) {
                d.getDatabase().close();
            }
            App.Indices = null;
        }
        log("DB Closed");
    }
}

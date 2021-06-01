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

    public AppListener() {
        App.log("AppListener Flag: " + 3);
        App.log("AppListener ClassLoader: " + getClass().getClassLoader().getClass().getName());
        App.log("Thread ContextClassLoader: " + Thread.currentThread().getContextClassLoader().getClass().getName());
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        App.IsAndroid = false;
        try {
            App.IsAndroid = Class.forName("dalvik.system.DexClassLoader") != null;
            sce.getServletContext().setAttribute(App.class.getName(), App.class);
        } catch (Throwable e) {

        }
        log("IsAndroid = " + App.IsAndroid);
        System.setProperty("fts.isAndroid", Boolean.toString(App.IsAndroid));
        long tm = java.lang.Runtime.getRuntime().maxMemory();
        tm = (tm / 1024L / 1024L);
        log("-Xmx " + tm + " MB");// Test on 4G setting.
        if (tm < 2000L) {
            log("Low Memory System, Reset Config");
            Config.SwitchToReadonlyIndexLength = Config.mb(tm / 4) + 1;
            Config.Readonly_CacheLength = Config.mb(tm / 150) + 1;
            Config.ItemConfig_CacheLength = Config.mb(tm / 30) + 1;
            Config.ItemConfig_SwapFileBuffer = (int) Config.mb(tm / 150) + 1;

            if (Config.Readonly_CacheLength < Config.mb(8)) {
                Config.Readonly_CacheLength = 1;
            }
            log("ReadOnly CacheLength = " + (Config.Readonly_CacheLength / 1024L / 1024L) + " MB (" + Config.Readonly_CacheLength + ")");
        }

        File mvnConfig = new File(".mvn/jvm.config");
        if (mvnConfig.exists()) {
            log("Maven 3 -Xmx setting " + mvnConfig.getAbsolutePath());
        }

        //Path
        String dir = "DATA_FTS_JAVA_161";

        String path = System.getProperty("user.home") + File.separatorChar + dir + File.separatorChar;

        new File(path).mkdirs();
        log("java.version = " + System.getProperty("java.version"));
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
            App.Indices.add(ReadonlyIndexServer.GetReadonly(l));
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

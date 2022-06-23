package ftserver;

import iboxdb.localserver.*;
import javax.servlet.*;
import javax.servlet.annotation.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

import static ftserver.App.*;
import ftserver.fts.Engine;

@WebListener
public class AppListener implements ServletContextListener {

    public AppListener() {
        App.log("AppListener Flag: " + 34);
        App.log("AppListener ClassLoader: " + getClass().getClassLoader().getClass().getName());
        App.log("Thread ContextClassLoader: " + Thread.currentThread().getContextClassLoader().getClass().getName());
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        App.log("=====FTServer=====");

        log("Java Version: " + System.getProperty("java.version"));
        long tm = java.lang.Runtime.getRuntime().maxMemory();
        tm = (tm / 1024L / 1024L);
        log("Max Memory(-Xmx): " + tm + " MB");

        App.IsAndroid = false;
        try {
            App.IsAndroid = Class.forName("dalvik.system.DexClassLoader") != null;
            if (App.IsAndroid) {
                System.setProperty("fts.isAndroid", Boolean.toString(App.IsAndroid));
                sce.getServletContext().setAttribute(App.class.getName(), App.class);
            }
        } catch (Throwable e) {

        }
        log("IsAndroid: " + App.IsAndroid);

        App.log("Current Web Path: " + new File("./").getAbsolutePath());

        File mvnConfig = new File(".mvn/jvm.config");
        if (mvnConfig.exists()) {
            log("Maven 3 (-Xmx Setting): " + mvnConfig.getAbsolutePath());
        }

        //DB Path
        String dir = "DATA_FTS_JAVA_161";
        String path = System.getProperty("user.home") + File.separatorChar + dir + File.separatorChar;

        if (getPomFile().exists() && (!App.IsAndroid)) {
            path = getPomFile().getAbsolutePath()
                    .replace(POMXML, ".." + File.separatorChar + dir + File.separatorChar);
        }

        new File(path).mkdirs();
        log(String.format("Current DB Path: %s ", getFilePath(new File(path))));
        DB.root(path);

        if (tm < 3600L) {
            // Test on 4000MB(4GB) setting.
            log("Low Memory System(" + tm + "MB), Reset Config");
            Config.SwitchToReadonlyIndexLength = Config.mb(tm / 4) / Config.DSize + 1;
            Config.Index_CacheLength = Config.SwitchToReadonlyIndexLength;

            Config.Readonly_CacheLength = Config.mb(tm / 150) + 1;
            Config.Readonly_MaxDBCount = Config.mb(tm / 7) / Config.mb(tm / 150) / Config.DSize + 1;

            Config.ItemConfig_CacheLength = Config.mb(tm / 30) + 1;
            Config.ItemConfig_SwapFileBuffer = (int) Config.mb(tm / 150) + 1;

        }

        log("ReadOnly CacheLength = " + (Config.Readonly_CacheLength / 1024L / 1024L) + " MB (" + Config.Readonly_CacheLength + ")");
        log("ReadOnly Max DB Count = " + Config.Readonly_MaxDBCount);

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

        for (long l = IndexServer.IndexDBStart; l < start; l++) {
            App.Indices.add(l, true);
        }
        App.Indices.add(start, false);
        App.Index = App.Indices.get(App.Indices.length() - 1);

        log("ReadOnly Index DB (" + start + "), start from " + IndexServer.IndexDBStart);
        log("MinCache = " + (Config.minCache() / 1024L / 1024L) + " MB");

        //bigger will more accurate, smaller faster will jump some pages, 
        Engine.KeyWordMaxScan = 2000;
        log("KeyWordMaxScan = " + Engine.KeyWordMaxScan);

        IndexPage.HttpGet_SleepTime = 0;
        log("HttpGet_SleepTime = " + IndexPage.HttpGet_SleepTime + "ms");
        IndexPage.start();
        log("=====DB Started=====");

        try {
            int httpPort = 8080;
            File pomConfig = getPomFile();
            if (pomConfig.exists()) {

                FileInputStream fs = new FileInputStream(pomConfig);
                byte[] bs = new byte[fs.available()];
                fs.read(bs);
                String str = new String(bs, "UTF-8");
                fs.close();

                Pattern p = Pattern.compile("<cargo.servlet.port>(\\d+)</cargo.servlet.port>");
                Matcher m = p.matcher(str);

                if (m.find()) {
                    App.log("HTTP Port: " + m.group(1));
                    httpPort = Integer.parseInt(m.group(1));
                    App.log("-");
                }

                for (Enumeration<NetworkInterface> en = NetworkInterface
                        .getNetworkInterfaces(); en.hasMoreElements();) {
                    NetworkInterface intf = en.nextElement();
                    for (Enumeration<InetAddress> enumIpAddr = intf
                            .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        String ip = inetAddress.getHostAddress();

                        if (ip.contains(":") || ip.contains("%")) {
                            // IP V6 can't access from Browser, Just for
                            // showing.

                        } else {
                            App.log("http://" + ip + ":" + httpPort);
                        }

                    }
                }

                //java.awt.Desktop.getDesktop().browse(new URI("http://127.0.0.1:" + httpPort));
                URI url = new URI("http://127.0.0.1:" + httpPort);
                if (!App.IsAndroid)
                try {
                    Class desktop = Class.forName("java.awt.Desktop");
                    Object deskA = desktop.getMethod("getDesktop").invoke(null);
                    deskA.getClass().getMethod("browse", URI.class).invoke(deskA, url);
                } catch (Throwable de) {
                    log("Browser " + url.toString());
                }

            }
        } catch (Throwable e) {

        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        IndexPage.shutdown();
        IndexPage.addSearchTerm(IndexPage.SystemShutdown);
        if (App.Item != null) {
            App.Item.getDatabase().close();
            App.Item = null;
        }
        if (App.Indices != null) {
            App.Indices.close();
        }
        log("DB Closed");
    }

    private String POMXML = "pom.xml";

    private File getPomFile() {
        File f = new File(POMXML);
        if (f.exists()) {
            return f;
        }
        if (f.getAbsolutePath().contains("target")) {
            f = new File("../../../../" + POMXML);
        }
        return f;
    }

    private String getFilePath(File f) {
        try {
            return f.getCanonicalPath();
        } catch (IOException ex) {
            return f.getAbsolutePath();
        }
    }
}

package ftserver;

import iBoxDB.LocalServer.*;
import java.io.File;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import static ftserver.App.*;

/*
Turn off virtual memory for 8G+ RAM Machine
use DatabaseConfig.CacheLength and PageText.max_text_length to Control Memory

Linux:
 # free -h
 # sudo swapoff -a
 # free -h 

Windows:
System Properties(Win+Pause) - Advanced system settings - Advanced
- Performance Settings - Advanced - Virtual Memory Change -
uncheck Automatically manage paging file - select No paging file - 
click Set - OK restart
 */
@WebListener
public class AppListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        //Path
        String path = System.getProperty("user.home") + File.separatorChar + "ftsdata130" + File.separatorChar;
        new File(path).mkdirs();

        if (!new File(path).exists()) {

            String tmpPath = sce.getServletContext().getRealPath("/")
                    + "WEB-INF" + File.separatorChar + "DB" + File.separatorChar;

            path = tmpPath;
            (new File(path)).mkdirs();
        }

        log(System.getProperty("java.version"));
        log(String.format("DB Path=%s ", path));

        DB.root(path);

        //Config
        IndexServer db = new IndexServer();
        App.Auto = db.getInstance(1).get();
        App.Item = db.getInstance(2).get();
        log("DB Started...");

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        IndexPage.shutdown();
        if (App.Auto != null) {
            App.Auto.getDatabase().close();
        }
        if (App.Item != null) {
            App.Item.getDatabase().close();
        }
        App.Auto = null;
        App.Item = null;
        log("DB Closed");
    }
}

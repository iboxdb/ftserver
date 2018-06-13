package FTServer;

import FTServer.FTS.Engine;
import iBoxDB.LocalServer.*;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        App.IsVM = false;
        String path = System.getProperty("user.home") + File.separatorChar + "ftsdata100" + File.separatorChar;
        new File(path).mkdirs();

        String tmpPath = sce.getServletContext().getRealPath("/")
                + "WEB-INF" + File.separatorChar + "DB" + File.separatorChar;

        if (!new File(path).exists()) {
            App.IsVM = true;
            path = tmpPath;
            (new File(path)).mkdirs();
        }

        Logger.getLogger(App.class.getName()).log(Level.INFO,
                String.format("DBPath=%s VM=" + App.IsVM, path));

        DB db = new DB(1);
        DatabaseConfig cfg = db.getConfig().DBConfig;

        cfg.CacheLength
                = cfg.mb(App.IsVM ? 16 : 512);

        cfg.FileIncSize
                = (int) cfg.mb(16);
        new Engine().Config(cfg);

        cfg.EnsureTable(Page.class, "Page", "id");
        cfg.EnsureIndex(Page.class, "Page", true, "url(" + Page.MAX_URL_LENGTH + ")");
        cfg.EnsureTable(Page.Lock.class, "PageLock", "url(" + Page.MAX_URL_LENGTH + ")");
        App.Auto = db.open();

        Logger.getLogger(App.class.getName()).log(Level.INFO, "DB Started...");

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (App.Auto != null) {
            App.Auto.getDatabase().close();
        }
        App.Auto = null;
    }
}

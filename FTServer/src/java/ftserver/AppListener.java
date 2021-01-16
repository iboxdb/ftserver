package ftserver;

import iboxdb.localserver.*;
import java.io.File;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import static ftserver.App.*;

@WebListener
public class AppListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        //Path
        //String path = System.getProperty("user.home") + File.separatorChar + "DATA_FTS_J_140" + File.separatorChar;
        String path = "../DATA_FTS_J_140";

        new File(path).mkdirs();
        log(System.getProperty("java.version"));
        log(String.format("DB Path=%s ", new File(path).getAbsolutePath()));
        DB.root(path);

        //Config
        IndexServer db = new IndexServer();
        App.Auto = db.getInstance(1).get();
        App.Item = db.getInstance(2).get();
        log("DB Started...");
        IndexPage.start();
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

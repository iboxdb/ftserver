package iBoxDB.fts;

import java.io.File;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class SListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        String path = System.getProperty("user.home") + "/ftsdata4/";
        new File(path).mkdirs();

        String tmpPath = sce.getServletContext().getRealPath("/")
                + "WEB-INF" + File.separatorChar + "DB" + File.separatorChar;

        if (!new File(path).exists()) {
            path = tmpPath;
            (new File(path)).mkdirs();
        }

        SDB.init(path);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        SDB.close();
    }
}

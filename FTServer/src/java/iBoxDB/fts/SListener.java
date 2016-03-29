package iBoxDB.fts;

import java.io.File;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class SListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        String path = null;
        boolean isVM = false;
        String VCAP_SERVICES = System.getenv("VCAP_SERVICES");
        if (VCAP_SERVICES != null && VCAP_SERVICES.length() > 0) {
            String key = "\"host_path\":\"/";
            int pos = VCAP_SERVICES.lastIndexOf(key);
            if (pos > 0) {
                int p2 = VCAP_SERVICES.indexOf("\"", pos + key.length());
                path = VCAP_SERVICES.substring(pos + key.length() - 1, p2);
                isVM = true;
            }
        }

        if (path == null) {
            path = System.getProperty("user.home") + "/ftsdata6/";
            new File(path).mkdirs();

            String tmpPath = sce.getServletContext().getRealPath("/")
                    + "WEB-INF" + File.separatorChar + "DB" + File.separatorChar;

            if (!new File(path).exists()) {
                path = tmpPath;
                (new File(path)).mkdirs();
            }
        }

        SDB.init(path, isVM);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        SDB.close();
    }
}

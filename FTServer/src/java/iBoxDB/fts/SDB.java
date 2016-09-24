package iBoxDB.fts;

import iBoxDB.LocalServer.*;
import iBoxDB.fulltext.Engine;
import java.io.File;
import java.io.IOException;
import java.util.logging.*;

public class SDB {

    public static DB.AutoBox search_db;

    private static String lockFile;

    public static void init(String path, boolean isVM) {

        Logger.getLogger(SDB.class.getName()).log(Level.INFO,
                String.format("DBPath=%s VM=" + isVM, path));

        lockFile = DB.root(path) + "running_";
        if (isVM) {
            // when JVM on VM or Using NFS, to prevent multiple VM Instances reading/writing same file.
            // example
            // JAVA_OPTS = $JAVA_OPTS -XX:+UseConcMarkSweepGC -XX:+UseCompressedOops -XX:+UseCMSCompactAtFullCollection -Xmx356m -Xms128m
            // lockFile = 7
            String str = System.getenv("lockFile");
            if (str == null) {
                return;
            }
            lockFile += str;
            try {
                if (!new File(lockFile).createNewFile()) {
                    Logger.getLogger(SDB.class.getName()).log(Level.INFO, "System Running, ResetName and Restart");
                    return;
                }
            } catch (IOException ex) {
                Logger.getLogger(SDB.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
            for (File f : new File(path).listFiles()) {
                if (!f.getAbsolutePath().equalsIgnoreCase(lockFile)) {
                    if (f.getName().startsWith("running")) {
                        f.delete();
                    }
                }
            }
        }

        try {

            DB server = new DB(1);
            if (isVM) {
                server.getConfig().DBConfig.CacheLength
                        = server.getConfig().DBConfig.mb(16);
            } else {
                server.getConfig().DBConfig.CacheLength
                        = server.getConfig().DBConfig.mb(512);
            }
            server.getConfig().DBConfig.SwapFileBuffer
                    = (int) server.getConfig().DBConfig.mb(4);
            server.getConfig().DBConfig.FileIncSize
                    = (int) server.getConfig().DBConfig.mb(16);
            new Engine().Config(server.getConfig().DBConfig);

            server.getConfig().ensureTable(BPage.class, "Page", "id");
            server.getConfig().ensureIndex(BPage.class, "Page", true, "url(" + BPage.MAX_URL_LENGTH + ")");

            search_db = server.open();

        } catch (Throwable ex) {
            if (search_db != null) {
                search_db.getDatabase().close();
            }
            search_db = null;
            Logger.getLogger(SDB.class.getName()).log(Level.INFO,
                    ex.getClass().getName(), ex);
            return;
        }

        Logger.getLogger(SDB.class.getName()).log(Level.INFO, "DB Started...");
    }

    public static void close() {
        if (search_db != null) {
            search_db.getDatabase().close();
        }
        search_db = null;
        Logger.getLogger(SDB.class.getName()).log(Level.INFO, "DBClosed");
    }

}

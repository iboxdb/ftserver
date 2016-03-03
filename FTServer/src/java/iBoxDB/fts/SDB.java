package iBoxDB.fts;

import iBoxDB.LocalServer.*;
import iBoxDB.fulltext.Engine;
import java.util.ArrayList;
import java.util.logging.*;

public class SDB {

    public static DB.AutoBox search_db;
    private static boolean isVM;

    public static void init(String path, boolean isVM) {

        Logger.getLogger(SDB.class.getName()).log(Level.INFO, String.format("DBPath=%s", path));
        SDB.isVM = isVM;
        DB.root(path);

        DB server = new DB(1);
        if (isVM) {
            server.getConfig().DBConfig.CacheLength
                    = server.getConfig().DBConfig.mb(8);
        }
        server.getConfig().DBConfig.SwapFileBuffer
                = (int) server.getConfig().DBConfig.mb(2);
        new Engine().Config(server.getConfig().DBConfig);

        server.getConfig().ensureTable(BURL.class, "URL", "id");
        server.getConfig().ensureTable(BPage.class, "Page", "id");
        server.getConfig().ensureIndex(BPage.class, "Page", true, "url(" + BPage.MAX_URL_LENGTH + ")");

        search_db = server.open();

        try (Box box = search_db.cube()) {
            ArrayList<BURL> list = new ArrayList<BURL>();
            for (BURL burl : box.select(BURL.class, "from URL")) {
                list.add(burl);
            }
            for (BURL burl : list) {
                box.d("URL", burl.id).delete();
            }
            box.commit().Assert();
        }
    }

    public static void close() {
        if (search_db != null) {
            search_db.getDatabase().close();
        }
        search_db = null;
        Logger.getLogger(SDB.class.getName()).log(Level.INFO, "DBClosed");
    }

    public static void vmGC() {
        if (isVM) {
            System.runFinalization();
            System.gc();
        }
    }
}

package iBoxDB.fts;

import iBoxDB.LocalServer.*;
import iBoxDB.fulltext.Engine;
import java.util.logging.*;

public class SDB {

    public static DB.AutoBox search_db;

    public static void init(String path) {

        Logger.getLogger(SDB.class.getName()).log(Level.INFO, String.format("DBPath=%s", path));

        DB.root(path);

        DB server = new DB(1);
        /*
        server.getConfig().DBConfig.CacheLength
                = server.getConfig().DBConfig.mb(8);
         */
        server.getConfig().DBConfig.SwapFileBuffer
                = (int) server.getConfig().DBConfig.mb(2);
        new Engine().Config(server.getConfig().DBConfig);
        server.getConfig().ensureTable(Page.class, "Page", "id");
        server.getConfig().ensureIndex(Page.class, "Page", true, "url(" + Page.MAX_URL_LENGTH + ")");

        search_db = server.open();

    }

    public static void close() {
        if (search_db != null) {
            search_db.getDatabase().close();
        }
        search_db = null;
        Logger.getLogger(SDB.class.getName()).log(Level.INFO, "DBClosed");
    }
}

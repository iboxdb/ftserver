package FTServer;

import FTServer.FTS.Engine;
import iBoxDB.LocalServer.*;
import java.util.Date;
import java.util.HashSet;

public class SearchResource {

    private final static int BATCH_COMMIT = 200;
    public final static Engine engine = new Engine();

    public static String indexText(String url, boolean isDelete, HashSet<String> subUrls) {

        url = Page.getUrl(url);
        try (Box box = App.Auto.cube()) {
            Page.Lock pl = box.d("PageLock", url).select(Page.Lock.class);
            if (pl == null) {
                pl = new Page.Lock();
                pl.url = url;
                pl.time = new Date();
            } else if ((new Date().getTime() - pl.time.getTime()) > 1000 * 60 * 5) {
                pl.time = new Date();
            } else {
                return "Running";
            }
            box.d("PageLock").replace(pl);
            if (box.commit() != CommitResult.OK) {
                return "Running";
            }
        }
        try {
            for (Page p : App.Auto.select(Page.class, "from Page where url==?", url)) {
                engine.indexTextNoTran(App.Auto, BATCH_COMMIT, p.id, p.content.toString(), true);
                engine.indexTextNoTran(App.Auto, BATCH_COMMIT, p.rankUpId(), p.rankUpDescription(), true);
                App.Auto.delete("Page", p.id);
            }

            if (isDelete) {
                return "deleted";
            }

            Page p = Page.get(url, subUrls);
            if (p == null) {
                return "temporarily unreachable";
            } else {
                p.id = App.Auto.newId();
                App.Auto.insert("Page", p);
                engine.indexTextNoTran(App.Auto, BATCH_COMMIT, p.id, p.content.toString(), false);
                engine.indexTextNoTran(App.Auto, BATCH_COMMIT, p.rankUpId(), p.rankUpDescription(), false);
                return p.url;
            }

        } finally {
            App.Auto.delete("PageLock", url);
        }
    }
}

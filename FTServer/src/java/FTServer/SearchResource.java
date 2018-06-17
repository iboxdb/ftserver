package FTServer;

import FTServer.FTS.Engine;
import iBoxDB.LocalServer.*;
import java.util.Date;
import java.util.HashSet;

public class SearchResource {

    private final static int BATCH_COMMIT = 200;
    public final static Engine engine = new Engine();

    public static String indexText(String url, boolean deleteOnly, HashSet<String> subUrls) {

        url = Page.getUrl(url);
        try (Box box = App.Auto.cube()) {
            PageLock pl = box.d("PageLock", url).select(PageLock.class);
            if (pl == null) {
                pl = new PageLock();
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
            Page defaultPage = null;
            for (Page p : App.Auto.select(Page.class, "from Page where url==?", url)) {
                engine.indexTextNoTran(App.Auto, BATCH_COMMIT, p.id, p.content, true);
                engine.indexTextNoTran(App.Auto, BATCH_COMMIT, p.rankUpId(), p.rankUpDescription(), true);
                App.Auto.delete("Page", p.id);
                defaultPage = p;
            }

            if (deleteOnly) {
                return "deleted";
            }

            Page p = Page.get(url, subUrls);
            if (p == null) {
                p = defaultPage;
            }
            if (p == null) {
                return "temporarily unreachable";
            } else {
                if (p.id == 0) {
                    p.id = App.Auto.newId();
                }
                App.Auto.insert("Page", p);
                engine.indexTextNoTran(App.Auto, BATCH_COMMIT, p.id, p.content, false);
                engine.indexTextNoTran(App.Auto, BATCH_COMMIT, p.rankUpId(), p.rankUpDescription(), false);
                return p.url;
            }

        } finally {
            App.Auto.delete("PageLock", url);
        }
    }
}

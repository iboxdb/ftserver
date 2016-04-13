package iBoxDB.fts;

import iBoxDB.LocalServer.*;
import iBoxDB.fulltext.Engine;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedDeque;

public class SearchResource {

    public static ConcurrentLinkedDeque<String> searchList
            = new ConcurrentLinkedDeque<String>();

    public static ConcurrentLinkedDeque<String> urlList
            = new ConcurrentLinkedDeque<String>();

    public final static Engine engine = new Engine();

    public static String indexText(String url, boolean isDelete, HashSet<String> subUrls) {

        for (BPage p : SDB.search_db.select(BPage.class, "from Page where url==?", url)) {
            engine.indexTextNoTran(SDB.search_db, 10, p.id, p.content.toString(), true);
            engine.indexTextNoTran(SDB.search_db, 10, p.rankUpId(), p.rankUpDescription(), true);
            SDB.search_db.delete("Page", p.id);
        }

        if (isDelete) {
            return "deleted";
        }

        BPage p = BPage.get(url, subUrls);
        if (p == null) {
            return "temporarily unreachable";
        } else {
            p.id = SDB.search_db.newId();
            engine.indexTextNoTran(SDB.search_db, 10, p.id, p.content.toString(), false);
            engine.indexTextNoTran(SDB.search_db, 10, p.rankUpId(), p.rankUpDescription(), false);
            SDB.search_db.insert("Page", p);

            urlList.add(p.url);
            while (urlList.size() > 3) {
                urlList.remove();
            }
            return p.url;
        }

    }

}

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
        try (Box box = SDB.search_db.cube()) {
            for (BPage p : box.select(BPage.class, "from Page where url==?", url)) {
                engine.indexText(box, p.id, p.content.toString(), true);
                engine.indexText(box, p.rankUpId(), p.rankUpDescription(), true);
                box.d("Page", p.id).delete();
                break;
            }
            box.commit().Assert();
        }
        if (isDelete) {
            return "deleted";
        }

        BPage p = BPage.get(url, subUrls);
        if (p == null) {
            return "temporarily unreachable";
        } else {
            try (Box box = SDB.search_db.cube()) {
                p.id = box.newId();
                box.d("Page").insert(p);
                engine.indexText(box, p.id, p.content.toString(), false);
                engine.indexText(box, p.rankUpId(), p.rankUpDescription(), false);
                box.commit().Assert();
            }
            urlList.add(p.url);
            while (urlList.size() > 3) {
                urlList.remove();
            }
            return p.url;
        }

    }

}

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

    public static String indexText(final String name, final boolean isDelete) {

        String url = getUrl(name);

        try (Box box = SDB.search_db.cube()) {
            for (Page p : box.select(Page.class, "from Page where url==?", url)) {
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
        Page p = Page.get(url);
        if (p == null) {
            return "temporarily unreachable";
        } else {
            try (Box box = SDB.search_db.cube()) {
                p.id = box.newId();
                box.d("Page").insert(p);
                engine.indexText(box, p.id, p.content.toString(), false);
                engine.indexText(box, p.rankUpId(), p.rankUpDescription(), false);
                CommitResult cr = box.commit();
                cr.Assert(cr.getErrorMsg(box));
            }
            urlList.add(p.url);
            while (urlList.size() > 3) {
                urlList.remove();
            }
            return p.url;
        }

    }

    private static String getUrl(String name) {
        int p = name.indexOf("http://");
        if (p < 0) {
            p = name.indexOf("https://");
        }
        if (p >= 0) {
            return name.substring(p).trim();
        }
        return "";
    }

}

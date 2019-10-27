package ftserver;

import ftserver.fts.Engine;
import ftserver.fts.KeyWord;
import iBoxDB.LocalServer.*;
import java.util.Date;
import java.util.*;
import java.util.concurrent.Callable;

public class IndexAPI {

    final static Engine ENGINE = new Engine();

    public static long[] Search(ArrayList<Page> outputPages,
            String name, long[] startId, long pageCount) {
        name = name.trim();

        if (name.length() > 100) {
            return new long[]{-1};
        }
        //And
        if (startId[0] > 0) {
            startId[0] = Search(outputPages, name, startId[0], pageCount);
            if (outputPages.size() >= pageCount && startId[0] > 0) {
                return startId;
            }
        }

//Or
        String orName = new String(ENGINE.sUtil.clear(name));
        orName = orName.replaceAll("\"", " ").trim();

        ArrayList<StringBuilder> ors = new ArrayList();
        ors.add(new StringBuilder());
        for (int i = 0; i < orName.length(); i++) {
            char c = orName.charAt(i);
            StringBuilder last = ors.get(ors.size() - 1);

            if (c == ' ') {
                if (last.length() > 0) {
                    ors.add(new StringBuilder());
                }
            } else if (last.length() == 0) {
                last.append(c);
            } else if (!ENGINE.sUtil.isWord(c)) {
                if (!ENGINE.sUtil.isWord(last.charAt(last.length() - 1))) {
                    last.append(c);
                    ors.add(new StringBuilder());
                } else {
                    last = new StringBuilder();
                    last.append(c);
                    ors.add(last);
                }
            } else {
                if (!ENGINE.sUtil.isWord(last.charAt(last.length() - 1))) {
                    last = new StringBuilder();
                    last.append(c);
                    ors.add(last);
                } else {
                    last.append(c);
                }
            }
        }

        ors.add(0, null);
        ors.add(1, new StringBuilder(name));

        if (startId.length < ors.size()) {
            startId = new long[ors.size()];
            startId[0] = -1;
            for (int i = 1; i < startId.length; i++) {
                startId[i] = Long.MAX_VALUE;
            }
        }

        if (ors.size() > 16 || stringEqual(ors.get(1).toString(), ors.get(2).toString())) {
            for (int i = 1; i < startId.length; i++) {
                startId[i] = -1;
            }
            return startId;
        }

        try (Box box = App.cube()) {

            Iterator<KeyWord>[] iters = new Iterator[ors.size()];

            for (int i = 0; i < ors.size(); i++) {
                StringBuilder sbkw = ors.get(i);
                if (startId[i] <= 0 || sbkw == null || sbkw.length() < 2) {
                    iters[i] = null;
                    startId[i] = -1;
                    continue;
                }
                //never set Long.MAX
                long subCount = pageCount * 10;
                iters[i] = ENGINE.searchDistinct(box, sbkw.toString(), startId[i], subCount).iterator();
            }

            KeyWord[] kws = new KeyWord[iters.length];

            int mPos = maxPos(startId);
            while (mPos > 0) {

                for (int i = 0; i < iters.length; i++) {
                    if (kws[i] == null) {
                        if (iters[i] != null && iters[i].hasNext()) {
                            kws[i] = iters[i].next();
                            startId[i] = kws[i].I;
                        } else {
                            iters[i] = null;
                            kws[i] = null;
                            startId[i] = -1;
                        }
                    }
                }
                if (outputPages.size() >= pageCount) {
                    break;
                }
                mPos = maxPos(startId);

                if (mPos > 1) {
                    KeyWord kw = kws[mPos];

                    long id = kw.I;
                    id = Page.rankDownId(id);
                    Page p = box.d("Page", id).select(Page.class);
                    p.keyWord = kw;
                    p.isAnd = false;
                    outputPages.add(p);

                }

                long maxId = startId[mPos];
                for (int i = 0; i < startId.length; i++) {
                    if (startId[i] == maxId) {
                        kws[i] = null;
                    }
                }

            }

        }
        return startId;
    }

    private static int maxPos(long[] ids) {
        int pos = 0;
        for (int i = 0; i < ids.length; i++) {
            if (ids[i] > ids[pos]) {
                pos = i;
            }
        }
        return pos;
    }

    private static boolean stringEqual(String a, String b) {
        if (a.equals(b)) {
            return true;
        }
        if (a.equals("\"" + b + "\"")) {
            return true;
        }
        if (b.equals("\"" + a + "\"")) {
            return true;
        }
        return false;
    }

    public static long Search(ArrayList<Page> outputPages,
            String name, long startId, long pageCount) {
        name = name.trim();
        try (Box box = App.cube()) {

            for (KeyWord kw : ENGINE.searchDistinct(box, name, startId, pageCount)) {

                startId = kw.I - 1;
                long id = kw.I;
                id = Page.rankDownId(id);
                Page p = box.d("Page", id).select(Page.class);
                p.keyWord = kw;
                outputPages.add(p);
                pageCount--;
            }
        }

        return pageCount == 0 ? startId : -1;
    }

    public static String getDesc(String str, KeyWord kw, int length) {
        if (kw.I == -1) {
            return str;
        }
        return ENGINE.getDesc(str, kw, length);
    }

    public static ArrayList<String> discover() {
        ArrayList<String> discoveries = new ArrayList<>();

        try (Box box = App.Auto.cube()) {
            for (String skw : IndexAPI.ENGINE.discover(box, 'a', 'z', 2,
                    '\u2E80', '\u9fa5', 1)) {
                discoveries.add(skw);
            }
        }
        return discoveries;
    }

    public static String indexText(final String url, final boolean deleteOnly, final HashSet<String> subUrls) {
        boolean tran = true;
        if (tran) {
            return indexTextWithTran(Html.getUrl(url), deleteOnly, subUrls,
                    subUrls != null ? 1L << 59 : 0);
        }
        return indexTextNoTran(Html.getUrl(url), deleteOnly, subUrls);
    }

    private static String indexTextWithTran(final String url, final boolean deleteOnly, final HashSet<String> subUrls,
            final long rankUpPlus) {
        return pageLock(url, new Callable<String>() {

            @Override
            public String call() {
                try (Box box = App.Auto.cube()) {
                    Page defaultPage = null;

                    for (Page p : DB.toList(box.select(Page.class, "from Page where url==?", url))) {
                        ENGINE.indexText(box, p.id, p.content, true);
                        ENGINE.indexText(box, p.rankUpId(), p.rankUpDescription(), true);
                        box.d("Page", p.id).delete();
                        defaultPage = p;
                    }

                    if (deleteOnly) {
                        return box.commit() == CommitResult.OK ? "deleted" : "not deleted";
                    }

                    Page p = Html.get(url, subUrls);
                    if (p == null) {
                        //p = defaultPage;
                    }
                    if (p == null) {
                        return "temporarily unreachable";
                    } else {
                        if (p.id == 0) {
                            p.id = box.newId();
                            p.rankUpPlus = rankUpPlus;
                        }
                        box.d("Page").insert(p);
                        ENGINE.indexText(box, p.id, p.content, false);
                        ENGINE.indexText(box, p.rankUpId(), p.rankUpDescription(), false);
                        CommitResult cr = box.commit();
                        if (cr != CommitResult.OK) {
                            return cr.getErrorMsg(box);
                        }
                        return p.url;
                    }

                }
            }
        });
    }

    //No transaction, less memory
    private static String indexTextNoTran(final String url, final boolean deleteOnly, final HashSet<String> subUrls) {

        //url = Page.getUrl(url);
        final int BATCH_COMMIT = 200;
        return pageLock(url, new Callable<String>() {

            @Override
            public String call() {

                Page defaultPage = null;
                for (Page p : App.Auto.select(Page.class, "from Page where url==?", url)) {
                    ENGINE.indexTextNoTran(App.Auto, BATCH_COMMIT, p.id, p.content, true);
                    ENGINE.indexTextNoTran(App.Auto, BATCH_COMMIT, p.rankUpId(), p.rankUpDescription(), true);
                    App.Auto.delete("Page", p.id);
                    defaultPage = p;
                }

                if (deleteOnly) {
                    return "deleted";
                }

                Page p = Html.get(url, subUrls);
                if (p == null) {
                    p = defaultPage;
                }
                if (p == null) {
                    return "temporarily unreachable";
                } else {
                    if (p.id == 0) {
                        p.id = App.Auto.newId();
                        //   p.rankUpPlus = rankUpPlus;
                    }
                    App.Auto.insert("Page", p);
                    ENGINE.indexTextNoTran(App.Auto, BATCH_COMMIT, p.id, p.content, false);
                    ENGINE.indexTextNoTran(App.Auto, BATCH_COMMIT, p.rankUpId(), p.rankUpDescription(), false);
                    return p.url;
                }
            }
        });

    }

    private static String pageLock(final String url, final Callable<String> run) {
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
            return run.call();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            App.Auto.delete("PageLock", url);
        }
    }
}

package ftserver;

import ftserver.fts.Engine;
import ftserver.fts.KeyWord;
import iBoxDB.LocalServer.*;
import java.util.Date;
import java.util.*;
import java.util.concurrent.Callable;

public class IndexAPI {

    final static Engine ENGINE = new Engine();

    public static long[] Search(ArrayList<PageText> outputPages,
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
                    PageText p = box.d("PageText", id).select(PageText.class);
                    p.keyWord = kw;
                    p.isAndSearch = false;
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

    public static long Search(ArrayList<PageText> outputPages,
            String name, long startId, long pageCount) {
        name = name.trim();
        try (Box box = App.cube()) {

            for (KeyWord kw : ENGINE.searchDistinct(box, name, startId, pageCount)) {

                startId = kw.I - 1;
                long id = kw.I;
                PageText p = box.d("PageText", id).select(PageText.class);
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

    public static Boolean addPage(Page page) {

        if (App.Auto.get(Object.class, "Page", page.url) != null) {
            //call removePage first
            return null;
        }

        page.createTime = new Date();
        page.textOrder = App.Auto.newId();
        return App.Auto.insert("Page", page);
    }

    public static boolean addPageIndex(final String url) {

        Page page = App.Auto.get(Page.class, "Page", url);
        if (page == null) {
            return false;
        }

        ArrayList<PageText> ptlist = Html.getDefaultTexts(page);

        for (PageText pt : ptlist) {
            addPageTextIndex(pt);
        }
        return true;
    }

    public static void addPageTextIndex(PageText pt) {
        try (Box box = App.Auto.cube()) {
            if (box.d("PageText", pt.id()).select(Object.class) != null) {
                return;
            }
            box.d("PageText").insert(pt);
            ENGINE.indexText(box, pt.id(), pt.indexedText(), false);
            box.commit();
        }
    }

    public static void removePage(String url) {

        Page page = App.Auto.get(Page.class, "Page", url);
        if (page == null) {
            return;
        }

        ArrayList<PageText> ptlist = App.Auto.select(PageText.class, "from PageText where textOrder==?", page.textOrder);

        for (PageText pt : ptlist) {
            try (Box box = App.Auto.cube()) {
                ENGINE.indexText(box, pt.id(), pt.indexedText(), true);
                box.d("PageText", pt.id()).delete();
                box.commit();
            }
        }

        App.Auto.delete("Page", url);
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

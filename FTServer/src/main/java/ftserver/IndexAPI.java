/* iBoxDB FTServer Bruce Yang CL-N */
package ftserver;

import iboxdb.localserver.*;
import ftserver.fts.*;
import java.util.*;
import static ftserver.App.*;
import java.text.NumberFormat;

public class IndexAPI {

    private static class StartIdParam {
        //andBox,orBox, ids...

        public long[] startId;

        public StartIdParam(long[] id) {
            if (id.length == 1) {
                startId = new long[]{App.Indices.length() - 1, -1, id[0]};
            } else {
                startId = id;
            }
        }

        public boolean isAnd() {
            if (startId[0] >= 0) {
                if (startId[2] >= 0) {
                    return true;
                }
                startId[0]--;
                startId[2] = Long.MAX_VALUE;
                return isAnd();
            }
            return false;
        }

        protected ArrayList<String> ToOrCondition(String name) {

            ArrayList<String> ors = EasyOR.toOrCondition(name);

            //----SET----//
            ors.add(0, null); //and box
            ors.add(1, null); //or box
            ors.add(2, null); //and startId
            //full search
            ors.add(3, name);

            if (startId.length != ors.size()) {
                startId = new long[ors.size()];
                startId[0] = -1;
                startId[1] = App.Indices.length() - 1;//or box
                startId[2] = -1;
                for (int i = 3; i < startId.length; i++) {
                    startId[i] = Long.MAX_VALUE;
                }
            }

            if (ors.size() > 15 || ors.size() < 5 || stringEqual(ors.get(3).toString(), ors.get(4).toString())) {
                for (int i = 0; i < startId.length; i++) {
                    startId[i] = -1;
                }
            }

            return ors;
        }

        public boolean isOr() {
            if (startId[1] >= 0) {
                for (int i = 3; i < startId.length; i++) {
                    if (startId[i] >= 0) {
                        return true;
                    }
                }
                startId[1]--;
                for (int i = 3; i < startId.length; i++) {
                    startId[i] = Long.MAX_VALUE;
                }
                return isOr();
            }
            return false;
        }
    }

    public static long[] Search(List<PageText> outputPages,
            String name, long[] t_startId, long pageCount) {
        name = StringUtil.Instance.fromatFrenchInput(name);
        if (name.length() == 0 || name.length() > 150
                || name.equals(IndexPage.SystemShutdown)) {
            return new long[]{-1, -1, -1};
        }
        long maxTime = 1000 * 2;
        if (pageCount == 1) {
            maxTime = 500;
        }
        StartIdParam startId = new StartIdParam(t_startId);
        long beginTime = System.currentTimeMillis();

        //And
        while (startId.isAnd()) {
            DelayService.delayIndex();
            AutoBox auto = App.Indices.get((int) startId.startId[0]);
            startId.startId[2] = SearchAnd(auto, outputPages, name, startId.startId[2], pageCount - outputPages.size());
            App.Indices.tryCloseOutOfCache(auto);

            for (PageText pt : outputPages) {
                if (pt.dbOrder < 0) {
                    pt.dbOrder = startId.startId[0] + IndexServer.IndexDBStart;
                }
            }
            if (outputPages.size() >= pageCount) {
                return startId.startId;
            }
            if ((System.currentTimeMillis() - beginTime) > maxTime) {
                return startId.startId;
            }
        }

        //OR            
        ArrayList<String> ors = startId.ToOrCondition(name);
        while (startId.isOr()) {
            DelayService.delayIndex();
            AutoBox auto = App.Indices.get((int) startId.startId[1]);
            SearchOr(auto, outputPages, ors, startId.startId, pageCount);
            App.Indices.tryCloseOutOfCache(auto);
            for (PageText pt : outputPages) {
                if (pt.dbOrder < 0) {
                    pt.dbOrder = startId.startId[1] + IndexServer.IndexDBStart;
                }
            }
            if (outputPages.size() >= pageCount) {
                break;
            }
            if ((System.currentTimeMillis() - beginTime) > maxTime) {
                break;
            }
        }
        return startId.startId;
    }

    private static long SearchAnd(AutoBox auto, List<PageText> pages,
            String name, long startId, long pageCount) {
        name = name.trim();

        try (Box box = auto.cube()) {
            for (KeyWord kw : Engine.Instance.searchDistinct(box, name, startId, pageCount)) {
                pageCount--;
                startId = kw.I - 1;

                long id = kw.I;
                PageText pt = PageText.fromId(id);
                Page p = getPage(pt.textOrder);
                if (p.show) {
                    pt = Html.getDefaultText(p, id);
                    pt.keyWord = kw;
                    pt.page = p;
                    pt.isAndSearch = true;
                    pages.add(pt);
                } else {
                    //App.log("Old Version Page Not Showing: " + p.url + " (" + name + ") AND");
                }
            }

            return pageCount == 0 ? startId : -1;
        }
    }

    private static void SearchOr(AutoBox auto, List<PageText> outputPages,
            ArrayList<String> ors, long[] startId, long pageCount) {

        try (Box box = auto.cube()) {

            Iterator<KeyWord>[] iters = new Iterator[ors.size()];

            for (int i = 0; i < ors.size(); i++) {
                String sbkw = ors.get(i);
                if (sbkw == null || sbkw.length() < 1) {
                    iters[i] = null;
                    continue;
                }
                //never set Long.MAX 
                long subCount = pageCount * 10;
                iters[i] = Engine.Instance.searchDistinct(box, sbkw, startId[i], subCount).iterator();
            }

            int orStartPos = 3;
            KeyWord[] kws = new KeyWord[iters.length];

            int mPos = maxPos(startId);
            while (mPos >= orStartPos) {

                for (int i = orStartPos; i < iters.length; i++) {
                    if (kws[i] == null) {
                        if (iters[i] != null && iters[i].hasNext()) {
                            kws[i] = iters[i].next();
                            startId[i] = kws[i].I;
                        } else {
                            iters[i] = null;
                            startId[i] = -1;
                        }
                    }
                }

                if (outputPages.size() >= pageCount) {
                    break;
                }

                mPos = maxPos(startId);

                if (mPos > orStartPos) {
                    KeyWord kw = kws[mPos];

                    long id = kw.I;

                    PageText pt = PageText.fromId(id);
                    Page p = getPage(pt.textOrder);
                    if (p.show) {
                        pt = Html.getDefaultText(p, id);
                        pt.keyWord = kw;
                        pt.page = p;
                        pt.isAndSearch = false;
                        outputPages.add(pt);
                    } else {
                        //App.log("Old Version Page Not Showing: " + p.url + " OR ");
                    }
                }

                long maxId = startId[mPos];
                for (int i = orStartPos; i < startId.length; i++) {
                    if (startId[i] == maxId) {
                        kws[i] = null;
                    }
                }

            }

        }

    }

    private static int maxPos(long[] ids) {
        int orStartPos = 3;
        orStartPos--;
        for (int i = orStartPos; i < ids.length; i++) {
            if (ids[i] > ids[orStartPos]) {
                orStartPos = i;
            }
        }
        return orStartPos;
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

    public static Page getPage(long textOrder) {
        return App.Item.get(Page.class, "Page", textOrder);
    }

    public static long addPage(Page page) {
        /*
        Page oldPage = GetOldPage(page.url);
        if (oldPage != null && oldPage.show) {
            if (oldPage.text.equals(page.text)) {
                log("Page is not changed. " + page.url);
                return -1L;
            } else {
                log("Page is changed. " + page.url);
            }
        }
         */
        try (Box box = App.Item.cube()) {
            page.createTime = new Date();
            page.textOrder = box.newId();
            box.d("Page").insert(page, 1);
            if (box.commit() == CommitResult.OK) {
                return page.textOrder;
            }
            return -1L;
        }
    }

    public static boolean addPageIndex(final long textOrder) {

        Page page = getPage(textOrder);
        if (page == null) {
            return false;
        }
        long HuggersMemory = Integer.MAX_VALUE / 2;
        if (App.IsAndroid) {
            //App.log("Android.addPageIndex NoHuggersMemory");
            HuggersMemory = 0;
        }
        ArrayList<PageText> ptlist = Html.getDefaultTexts(page);
        int count = 0;

        if (ptlist.size() == 0) {
            log("[No Description]");
        }
        for (PageText pt : ptlist) {
            count++;
            addPageTextIndex(pt, count == ptlist.size() ? 0 : HuggersMemory);
        }
        return true;
    }

    public static void addPageTextIndex(PageText pt, long huggers) {
        try (Box box = App.Index.cube()) {

            Engine.Instance.indexText(box, pt.id(), pt.indexedText(), false,
                    new Runnable() {
                @Override
                public void run() {
                    DelayService.delay();
                }
            });
            CommitResult cr = box.commit(huggers);
            log("MEM:  " + NumberFormat.getInstance().format(cr.getMemoryLength(box)));
        }
    }

    public static void DisableOldPage(String url) {
        try (Box box = App.Item.cube()) {
            ArrayList<Page> page = new ArrayList<Page>();

            for (Page p : box.scale(Page.class, "from Page where url==? limit 1,10", url)) {
                if (!p.show) {
                    break;
                }
                page.add(p);
            }
            for (Page p : page) {
                p.show = false;
                box.d("Page").update(p);
            }
            box.commit().Assert();
        }
    }

}

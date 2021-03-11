package ftserver;

import java.util.*;
import java.util.concurrent.*;
import static ftserver.App.*;
import ftserver.fts.KeyWord;
import iboxdb.localserver.*;

public class IndexPage {

    public static void addSearchTerm(String keywords) {
        addSearchTerm(keywords, false);
    }

    public static void addSearchTerm(String keywords, boolean isShutdown) {
        if (keywords.length() < PageSearchTerm.MAX_TERM_LENGTH) {
            PageSearchTerm pst = new PageSearchTerm();
            pst.time = new Date();
            pst.keywords = keywords;
            pst.uid = UUID.randomUUID();

            long huggersMem = 1024L * 1024L * 10L;
            if (isShutdown) {
                huggersMem = 0;
            }
            try ( Box box = App.Item.cube()) {
                box.d("/PageSearchTerm").insert(pst);
                box.commit(huggersMem);
            }
        }
    }

    public static ArrayList<PageSearchTerm> getSearchTerm(int len) {
        return App.Item.select(PageSearchTerm.class, "from /PageSearchTerm limit 0 , ?", len);
    }

    public static String getDesc(String str, KeyWord kw, int length) {
        if (kw.I == -1) {
            return str;
        }
        return IndexAPI.ENGINE.getDesc(str, kw, length);
    }

    public static ArrayList<String> discover() {
        ArrayList<String> discoveries = new ArrayList<>();

        try ( Box box = App.Index.cube()) {
            for (String skw : IndexAPI.ENGINE.discover(box, 'a', 'z', 2,
                    '\u2E80', '\u9fa5', 2)) {
                discoveries.add(skw);
            }
        }
        return discoveries;
    }

    public static String addPage(String url, String userDescription, boolean isKeyPage) {

        if (!isKeyPage) {
            if (App.Item.count("from Page where url==? limit 0,1", url) > 0) {
                return null;
            }
        }

        HashSet<String> subUrls = new HashSet<>();

        long begin = System.currentTimeMillis();
        Page p = Html.get(url, subUrls);
        long ioend = System.currentTimeMillis();

        if (p == null) {
            return "temporarily unreachable";
        } else {
            p.userDescription = userDescription;
            p.show = true;
            p.isKeyPage = isKeyPage;
            long textOrder = IndexAPI.addPage(p);
            if (IndexAPI.addPageIndex(textOrder)) {
                IndexAPI.DisableOldPage(url);
            }
            long dbaddr = App.Indices.size() + IndexServer.IndexDBStart - 1;
            long indexend = System.currentTimeMillis();
            log("TIME IO:" + (ioend - begin) / 1000.0
                    + " INDEX:" + (indexend - ioend) / 1000.0 + "  TEXTORDER:" + textOrder + " (" + dbaddr + ") ");

            subUrls.remove(url);
            subUrls.remove(url + "/");
            subUrls.remove(url.substring(0, url.length() - 1));

            runBGTask(subUrls);

            return url;
        }
    }

    public synchronized static void runBGTask(final String url, String customContent) {
        backgroundThreadQueue.addFirst(() -> {
            synchronized (App.class) {
                final String furl = Html.getUrl(url);
                log("(KeyPage)For:" + furl);
                String rurl = IndexPage.addPage(furl, customContent, true);
                IndexPage.backgroundLog(furl, rurl);
            }
        });
    }

    private synchronized static void runBGTask(HashSet<String> subUrls) {

        if (subUrls == null || isShutdown) {
            return;
        }
        boolean atNight = true;

        int max_background = atNight ? 1000 : 0;

        if (backgroundThreadQueue.size() < max_background) {
            for (final String vurl : subUrls) {
                final String url = Html.getUrl(vurl);
                backgroundThreadQueue.addLast(() -> {
                    synchronized (App.class) {
                        log("For:" + url + " ," + backgroundThreadQueue.size());
                        String r = addPage(url, null, false);
                        backgroundLog(url, r);
                    }
                });
            }
        }
    }

    public static void backgroundLog(String url, String output) {

        if (output == null) {
            log("Has indexed:" + url);
        } else if (url.equals(output)) {
            log("Indexed:" + url);
        } else {
            log("Retry:" + url);
        }
        log("");
    }

    //background index thread
    private static Thread backgroundThread = null;
    private static boolean isShutdown = false;
    private final static ConcurrentLinkedDeque<Runnable> backgroundThreadQueue = new ConcurrentLinkedDeque<>();

    public static void start() {
        isShutdown = false;
        backgroundThread = new Thread(() -> {
            final long SLEEP_TIME = 0;//2000;

            while (!isShutdown) {
                Runnable act = backgroundThreadQueue.pollFirst();
                if (act != null) {
                    act.run();
                }

                if (!isShutdown) {
                    try {
                        Thread.sleep(SLEEP_TIME);
                    } catch (InterruptedException ex) {
                        log(ex.getMessage());
                    }
                }
            }
        });
        backgroundThread.setPriority(Thread.MIN_PRIORITY);
        backgroundThread.start();
    }

    public static void shutdown() {
        isShutdown = true;
        try {
            backgroundThread.setPriority(Thread.MAX_PRIORITY);
            backgroundThread.join();
        } catch (InterruptedException ex) {
            log(ex.getMessage());
        }

        log("Background Task Ended");
    }
}

package ftserver;

import java.util.*;
import java.util.concurrent.*;
import ftserver.fts.KeyWord;
import iboxdb.localserver.*;

import static ftserver.App.*;
import ftserver.fts.Engine;

public class IndexPage {

    public static final String SystemShutdown = "SystemShutdown";

    public static void addSearchTerm(String keywords) {
        if (keywords == null) {
            return;
        }
        if (keywords.length() > PageSearchTerm.MAX_TERM_LENGTH) {
            keywords = keywords.substring(0, PageSearchTerm.MAX_TERM_LENGTH - 1);
        }

        PageSearchTerm pst = new PageSearchTerm();
        pst.time = new Date();
        pst.keywords = keywords;
        pst.uid = UUID.randomUUID();

        long huggersMem = 1024L * 1024L * 3L;
        if (App.IsAndroid) {
            huggersMem = 0;
        }

        boolean isShutdown = SystemShutdown.equals(keywords);

        if (isShutdown) {
            huggersMem = 0;
        }
        try (Box box = App.Item.cube()) {
            box.d("/PageSearchTerm").insert(pst);
            box.commit(huggersMem);
        }

    }

    public static ArrayList<PageSearchTerm> getSearchTerm(int len) {
        return App.Item.select(PageSearchTerm.class, "from /PageSearchTerm limit 0 , ?", len);
    }

    public static String getDesc(String str, KeyWord kw, int length) {
        if (kw.I == -1) {
            return str;
        }
        return Engine.Instance.getDesc(str, kw, length);
    }

    public static ArrayList<String> discover() {
        try (Box box = App.Index.cube()) {
            ArrayList<String> result = new ArrayList<String>();
            result.addAll(Engine.Instance.discover(box, (char) 0x0061, (char) 0x007A, 2,
                    (char) 0x4E00, (char) 0x9FFF, 2));

            result.addAll(Engine.Instance.discover(box, (char) 0x0621, (char) 0x064A, 2,
                    (char) 0x3040, (char) 0x312F, 2));

            result.addAll(Engine.Instance.discover(box, (char) 0x0410, (char) 0x044F, 2,
                    (char) 0xAC00, (char) 0xD7AF, 2));
            return result;
        }
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
            return "Temporarily Unreachable";
        } else {
            if (userDescription != null) {
                userDescription = Html.replace(userDescription);
            }
            p.userDescription = userDescription;
            p.show = true;
            p.isKeyPage = isKeyPage;
            long textOrder = IndexAPI.addPage(p);
            if (textOrder >= 0 && IndexAPI.addPageIndex(textOrder)) {
                IndexAPI.DisableOldPage(url);
            }
            long dbaddr = App.Indices.length() + IndexServer.IndexDBStart - 1;
            long indexend = System.currentTimeMillis();
            log("TIME IO:" + (ioend - begin) / 1000.0
                    + " INDEX:" + (indexend - ioend) / 1000.0 + "  TEXTORDER:" + textOrder + " (" + dbaddr + ") ");

            subUrls.remove(url);
            subUrls.remove(url + "/");
            subUrls.remove(url.substring(0, url.length() - 1));

            runBGTask(subUrls, isKeyPage);

            return url;
        }
    }

    public synchronized static void runBGTask(final String url, final String customContent) {
        final String ansi_reset = "\u001B[0m";
        final String ansi_red = "\u001B[31m";
        backgroundThreadQueue.addFirst(new Runnable() {
            @Override
            public void run() {
                final String furl = Html.getUrl(url);
                log(ansi_red + "(KeyPage)For:" + furl + ansi_reset);
                String rurl = IndexPage.addPage(furl, customContent, true);
                IndexPage.backgroundLog(furl, rurl);

            }
        });
    }

    private synchronized static void runBGTask(HashSet<String> subUrls, boolean isKeyPage) {

        if (subUrls == null || isShutdown) {
            return;
        }
        boolean atNight = true;

        int max_background = atNight ? 500 : 0;
        if (App.IsAndroid && max_background > 50) {
            max_background = 50;
        }

        if (isKeyPage) {
            max_background *= 2;
        }

        for (final String vurl : subUrls) {
            if (backgroundThreadQueue.size() > max_background) {
                break;
            }
            final String url = Html.getUrl(vurl);
            backgroundThreadQueue.addLast(new Runnable() {
                @Override
                public void run() {
                    log("For:" + url + " ," + backgroundThreadQueue.size());
                    String r = addPage(url, null, false);
                    backgroundLog(url, r);
                }
            });
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
        backgroundThread = new Thread(new Runnable() {
            @Override
            public void run() {
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

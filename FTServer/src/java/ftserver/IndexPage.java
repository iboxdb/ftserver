package ftserver;

import java.util.Date;
import java.util.HashSet;
import java.util.*;
import java.util.concurrent.*;
import static ftserver.App.*;

public class IndexPage {

    public static void addSearchTerm(String keywords) {
        if (keywords.length() < PageSearchTerm.MAX_TERM_LENGTH) {
            PageSearchTerm pst = new PageSearchTerm();
            pst.time = new Date();
            pst.keywords = keywords;
            pst.uid = UUID.randomUUID();
            App.Item.insert("/PageSearchTerm", pst);
        }
    }

    public static ArrayList<PageSearchTerm> getSearchTerm(int len) {
        return App.Item.select(PageSearchTerm.class, "from /PageSearchTerm limit 0 , ?", len);
    }

    public static Page getPage(String url) {
        return App.Auto.get(Page.class, "Page", url);
    }

    public static void removePage(String url) {
        IndexAPI.removePage(url);
    }

    public static String addPage(String url, boolean isKeyPage) {
        if (!isKeyPage) {
            if (App.Auto.get(Object.class, "Page", url) != null) {
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
            IndexAPI.removePage(url);
            p.isKeyPage = isKeyPage;
            IndexAPI.addPage(p);
            IndexAPI.addPageIndex(url);

            long textOrder = App.Auto.newId(0, 0);
            long indexend = System.currentTimeMillis();
            log("TIME IO:" + (ioend - begin) / 1000.0
                    + " INDEX:" + (indexend - ioend) / 1000.0 + "  TEXTORDER:" + textOrder + " ");

            subUrls.remove(url);
            subUrls.remove(url + "/");
            subUrls.remove(url.substring(0, url.length() - 1));

            runBGTask(subUrls);

            return url;
        }
    }

    public static void addPageCustomText(String url, String title, String content) {
        if (url == null || title == null || content == null) {
            return;
        }
        Page page = App.Auto.get(Page.class, "Page", url);

        PageText text = new PageText();
        text.textOrder = page.textOrder;
        text.priority = PageText.userPriority;
        text.url = page.url;
        text.title = title;
        text.text = content;
        text.keywords = "";

        IndexAPI.addPageTextIndex(text);
    }

    private synchronized static void runBGTask(HashSet<String> subUrls) {

        if (subUrls == null || isShutdown) {
            return;
        }
        boolean atNight = true;

        int max_background = atNight ? 1000 : 0;

        final long SLEEP_TIME = 2000;

        if (backgroundThread == null) {
            backgroundThread = new Thread(() -> {

                while (!isShutdown) {
                    Runnable act = backgroundThreadQueue.poll();
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

        if (backgroundThreadQueue.size() < max_background) {
            for (final String vurl : subUrls) {
                final String url = Html.getUrl(vurl);
                backgroundThreadQueue.add(() -> {
                    synchronized (App.class) {
                        log("For:" + url + " ," + backgroundThreadQueue.size());
                        String r = addPage(url, false);
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
    public static Thread waitingThread = null;

    private final static ConcurrentLinkedQueue<Runnable> backgroundThreadQueue = new ConcurrentLinkedQueue<>();

    private static boolean isShutdown = false;

    public static void shutdown() {
        if (waitingThread != null) {
            try {
                waitingThread.join();

            } catch (InterruptedException ex) {
                log(ex.getMessage());
            }
            waitingThread = null;
        }
        if (backgroundThread != null) {
            isShutdown = true;
            try {
                backgroundThread.setPriority(Thread.MAX_PRIORITY);
                backgroundThread.join();
            } catch (InterruptedException ex) {
                log(ex.getMessage());
            }
            backgroundThread = null;
        }

        log("Background Task Ended");
    }
}

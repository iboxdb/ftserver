package ftserver;

import java.util.Date;
import java.util.HashSet;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IndexPage {

    public static ConcurrentLinkedDeque<String> urlList
            = new ConcurrentLinkedDeque<String>();

    public static void addSearchTerm(String keywords) {
        if (keywords.length() < PageSearchTerm.MAX_TERM_LENGTH) {
            PageSearchTerm pst = new PageSearchTerm();
            pst.time = new Date();
            pst.keywords = keywords;
            pst.uid = UUID.randomUUID();
            App.Auto.insert("/PageSearchTerm", pst);
        }
    }

    public static ArrayList<PageSearchTerm> getSearchTerm(int len) {
        return App.Auto.select(PageSearchTerm.class, "from /PageSearchTerm limit 0 , ?", len);
    }

    public static Page getPage(String url) {
        return App.Auto.get(Page.class, "Page", url);
    }

    private static void addUrlList(String url) {

        urlList.add(url.replaceAll("<", ""));
        while (urlList.size() > 3) {
            urlList.remove();
        }
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
            long indexend = System.currentTimeMillis();
            Logger.getLogger(App.class.getName()).log(Level.INFO, "TIME IO:" + (ioend - begin) / 1000.0
                    + " INDEX:" + (indexend - ioend) / 1000.0 + "  " + url);

            subUrls.remove(url);
            subUrls.remove(url + "/");
            subUrls.remove(url.substring(0, url.length() - 1));

            addUrlList(url);
            runBGTask(subUrls);

            return url;
        }
    }

    public static void addPageCustomText(String url, String title, String content) {
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

    private static void runBGTask(HashSet<String> subUrls) {

        boolean atNight = true;

        int max_background = atNight ? 10_000 : 0;

        final long SLEEP_TIME = 2000;

        if (backgroundThreadCount.get() < max_background) {
            for (final String url : subUrls) {
                backgroundThreadCount.incrementAndGet();
                backgroundThread.submit((Runnable) () -> {
                    backgroundThreadCount.decrementAndGet();
                    if (App.Auto == null) {
                        return;
                    }
                    try {
                        long begin = System.currentTimeMillis();
                        String r = addPage(url, false);
                        if (r == null) {
                            Logger.getLogger(App.class.getName()).log(Level.INFO, "Has indexed:" + url);
                        } else if (url.equals(r)) {
                            Logger.getLogger(App.class.getName()).log(Level.INFO, "Indexed:" + url);
                        } else {
                            Logger.getLogger(App.class.getName()).log(Level.INFO, "Retry:" + url);
                        }

                        long sleep = SLEEP_TIME - (System.currentTimeMillis() - begin);
                        if (sleep < 1) {
                            sleep = 1;
                        }
                        Thread.sleep(sleep);
                    } catch (Throwable ex) {
                        Logger.getLogger(App.class.getName()).log(Level.WARNING, ex.getMessage() + " " + url);
                    }
                });
            }
        }
    }

    //background index thread
    private final static ExecutorService backgroundThread = Executors.newSingleThreadExecutor();
    private final static AtomicInteger backgroundThreadCount = new AtomicInteger(0);

}

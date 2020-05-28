package ftserver;

import java.util.Date;
import java.util.HashSet;
import java.util.*;
import java.util.concurrent.*;
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

    public static Page getPage(String url) {
        return App.Auto.get(Page.class, "Page", url);
    }

    public static ArrayList<PageSearchTerm> getSearchTerm(int len) {
        return App.Auto.select(PageSearchTerm.class, "from /PageSearchTerm limit 0 , ?", len);
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
        Page p = Html.get(url, subUrls);
        if (p == null) {
            return "temporarily unreachable";
        } else {
            IndexAPI.removePage(url);
            p.isKeyPage = isKeyPage;
            IndexAPI.addPage(p);

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
        final int SLEEP_TIME = 2000;

        for (final String url : subUrls) {
            WRITE_ESBG.submit((Runnable) () -> {
                long begin = System.currentTimeMillis();
                if (url.equals(addPage(url, false))) {
                    Logger.getLogger(App.class.getName()).log(Level.INFO, "Indexed:" + url);
                } else {
                    Logger.getLogger(App.class.getName()).log(Level.INFO, "Retry:" + url);
                }
                try {
                    Thread.sleep(SLEEP_TIME - (System.currentTimeMillis() - begin));
                } catch (InterruptedException ex) {

                }
            });
        }

    }

    //background index thread
    private final static ExecutorService WRITE_ESBG = Executors.newSingleThreadExecutor();

}

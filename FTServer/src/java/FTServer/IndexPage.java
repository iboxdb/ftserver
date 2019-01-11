package FTServer;

import iBoxDB.LocalServer.Box;
import java.util.HashSet;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IndexPage {

    public static ConcurrentLinkedDeque<String> searchList
            = new ConcurrentLinkedDeque<String>();

    public static ConcurrentLinkedDeque<String> urlList
            = new ConcurrentLinkedDeque<String>();

    public static ConcurrentLinkedDeque<String> waitingUrlList
            = new ConcurrentLinkedDeque<String>();

    private final static int SLEEP_TIME = 2000;

    public static String processRequest(String name, boolean isdelete) {

        if (isdelete) {
            waitingUrlList.clear();
        }

        // when input "http://www.abc.com" or "delete http://www.abc.com"
        String url = Page.getUrl(name);
        final boolean del = isdelete;

        HashSet<String> subUrls = new HashSet<String>();

        String result = IndexAPI.indexText(Page.getUrl(url), del, subUrls);

        urlList.add(url.replaceAll("<", ""));
        while (urlList.size() > 3) {
            urlList.remove();
        }

        subUrls.remove(url);
        subUrls.remove(url + "/");
        subUrls.remove(url.substring(0, url.length() - 1));

        if (waitingUrlList.size() < 1000) {
            try (Box box = App.Auto.cube()) {
                for (String surl : subUrls) {
                    url = Page.getUrl(surl);
                    if (box.selectCount("from Page where url==? limit 0,1", url) == 0) {
                        waitingUrlList.add(url);
                        Logger.getLogger(App.class.getName()).log(Level.INFO, "Added:" + url);
                    } else {
                        Logger.getLogger(App.class.getName()).log(Level.INFO, "Existed:" + url);
                    }
                }
            }
            if (waitingUrlList.size() > 0) {
                subUrls.clear();
                subUrls.addAll(waitingUrlList);
                waitingUrlList.clear();
                waitingUrlList.addAll(subUrls);
                runBGTask();
            }
        }
        return result;

    }

    public static void closeBGTask() {
        writeES.shutdown();
        waitingUrlList.clear();
        writeESBG.shutdown();
        try {
            writeESBG.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {

        }
    }

    public static void runBGTask() {
        writeESBG.submit(new Runnable() {
            @Override
            public void run() {
                for (String url : waitingUrlList) {
                    try {
                        Thread.sleep(SLEEP_TIME);
                    } catch (InterruptedException ex) {

                    }
                    if (url != null) {
                        Logger.getLogger(App.class.getName()).log(Level.INFO, url);
                        IndexAPI.indexText(Page.getUrl(url), false, null);
                        Logger.getLogger(App.class.getName()).log(Level.INFO, "Indexed:" + url);
                    }
                }
                waitingUrlList.clear();
            }
        });
    }

    public final static ExecutorService writeES = Executors.newSingleThreadExecutor();

    //background index thread
    private final static ExecutorService writeESBG = Executors.newSingleThreadExecutor();

}

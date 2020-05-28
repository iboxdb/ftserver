package ftserver;

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
    
    public static String removePage(String url) {
        
        url = Html.getUrl(url);
        if (url.length() < 5) {
            return "not http";
        }
        IndexAPI.removePage(url);
        return url;
    }
    
    public static String addPage(String url) {
        
        url = Html.getUrl(url);
        if (url.length() < 5) {
            return "not http";
        }
        
        HashSet<String> subUrls = new HashSet<>();        
        Page p = Html.get(url, subUrls);
        if (p == null) {
            return "temporarily unreachable";
        }
        
        removePage(url);
        
        p.isKeyPage = true;        
        IndexAPI.addPage(p);
        
        
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
                    url = Html.getUrl(surl);
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
        WRITE_ES.shutdown();
        waitingUrlList.clear();
        WRITE_ESBG.shutdown();
        try {
            WRITE_ESBG.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            
        }
    }
    
    public static void runBGTask() {
        final int SLEEP_TIME = 2000;
        
        WRITE_ESBG.submit(new Runnable() {
            @Override
            public void run() {
                for (String url : waitingUrlList) {
                    try {
                        Thread.sleep(SLEEP_TIME);
                    } catch (InterruptedException ex) {
                        
                    }
                    if (url != null) {
                        Logger.getLogger(App.class.getName()).log(Level.INFO, url);
                        String result = IndexAPI.indexText(Html.getUrl(url), false, null);
                        if (result != null && result.startsWith("http")) {
                            urlList.add(url.replaceAll("<", ""));
                            while (urlList.size() > 3) {
                                urlList.remove();
                            }
                            Logger.getLogger(App.class.getName()).log(Level.INFO, "Indexed:" + url);
                        } else {
                            
                            Logger.getLogger(App.class.getName()).log(Level.INFO, "Retry:" + url);
                        }
                    }
                }
                waitingUrlList.clear();
            }
        });
    }
    
    public final static ExecutorService WRITE_ES = Executors.newSingleThreadExecutor();

    //background index thread
    private final static ExecutorService WRITE_ESBG = Executors.newSingleThreadExecutor();
    
}

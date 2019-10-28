/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ftserver;

import java.util.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

/**
 *
 * @author user
 */
public class Html {

    public static Page get(String url, HashSet<String> subUrls) {

        //When used in private server, set values by yourself
        //Page page = new Page();
        //page.url = url;
        //page.title = "...";
        //page.description = "...";
        //page.content == "...";
        //return page;
        try {
            if (url == null || url.length() > Page.MAX_URL_LENGTH || url.length() < 8) {
                return null;
            }
            Page page = new Page();
            page.url = url;

            Document doc = Jsoup.connect(url).timeout(15 * 1000).get();

            if (subUrls != null) {
                Elements links = doc.select("a[href]");
                for (Element link : links) {
                    String ss = getFullUrl(link.attr("abs:href"));
                    if (ss != null) {
                        subUrls.add(ss);
                    }
                }
            }

            try {
                page.title = doc.selectFirst("title").text();
            } catch (Throwable e) {

            }
            try {
                if (page.title == null) {
                    page.title = doc.selectFirst("Title").text();
                }
            } catch (Throwable e) {
            }

            try {
                if (page.title == null) {
                    page.title = doc.selectFirst("TITLE").text();
                }
            } catch (Throwable e) {
            }

            if (page.title == null) {
                page.title = url;
            }
            page.title = page.title.trim();
            if (page.title.length() < 2) {
                page.title = url;
            }
            if (page.title.length() > 80) {
                page.title = page.title.substring(0, 80);
            }
            page.title = page.title.replaceAll("\t|\r|\n|�|<|>|�|\\$", " ");

            try {
                page.description = doc.selectFirst("meta[name='description']").attr("content");
            } catch (Throwable e) {

            }

            try {
                if (page.description == null) {
                    page.description = doc.selectFirst("meta[name='Description']").attr("content");
                }
            } catch (Throwable e) {

            }

            try {
                if (page.description == null) {
                    page.description = doc.selectFirst("meta[property='og:description']").attr("content");
                }
            } catch (Throwable e) {

            }

            if (page.description == null) {
                page.description = "";
            }
            if (page.description.length() > 200) {
                page.description = page.description.substring(0, 200);
            }
            page.description = page.description
                    .replaceAll(Character.toString((char) 8203), "")
                    .replaceAll("\t|\r|\n|�|<|>|�|\\$", " ");

            fixSpan(doc);
            String content = doc.body().text();
            content = content.replaceAll(Character.toString((char) 8203), "")
                    .replaceAll("&nbsp;", " ")
                    .replaceAll("&gt;", " ")
                    .replaceAll("&lt;", " ")
                    .replaceAll("\t|\r|\n|�|<|>|�|\\$|\\|", " ")
                    .replaceAll("　", " ")
                    .replaceAll("\\s+", " ")
                    .trim();

            if (content.length() < 10 && (!url.contains("localhost"))) {
                return null;
            }
            if (content.length() > 15000) {
                content = content.substring(0, 15000);
            }
            page.content = content + " " + page.url;

            return page;
        } catch (Throwable e) {
            //e.printStackTrace();
            return null;
        } finally {

        }
    }

    public static String getUrl(String name) {

        int p = name.indexOf("http://");
        if (p < 0) {
            p = name.indexOf("https://");
        }
        if (p >= 0) {
            name = name.substring(p).trim();
            int t = name.indexOf("#");
            if (t > 0) {
                name = name.substring(0, t);
            }
            t = name.indexOf(" ");
            if (t > 0) {
                name = name.substring(0, t);
            }
            return name;
        }
        return "";
    }

    private static void fixSpan(Document doc) {
        for (Element e : doc.select("span")) {
            e.text(" " + e.text() + " ");
        }
    }

    private static String getFullUrl(String url) {
        try {
            if (url == null) {
                return null;
            }
            url = url.trim();
            String lcurl = url.toLowerCase();
            if (lcurl.contains("download") || lcurl.contains("signup") || lcurl.contains("login")
                    || lcurl.contains("share") || lcurl.contains("mailto")
                    || lcurl.contains("report") || lcurl.contains("send")
                    || lcurl.contains("register") || lcurl.contains("search")
                    || lcurl.contains("cgi-bin") || lcurl.contains("javascript")) {
                return null;
            }

            if (lcurl.startsWith("http:") || lcurl.startsWith("https:")) {
                if (isHTMLURL(url)) {
                    return url;
                }
                return null;
            }

            return null;
        } catch (Throwable ex) {
            //ex.printStackTrace();
            return null;
        }
    }

    private static boolean isHTMLURL(String url) {
        if (url.length() > Page.MAX_URL_LENGTH) {
            return false;
        }
        return true;
    }

}

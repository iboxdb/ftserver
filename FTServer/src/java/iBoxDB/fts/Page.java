package iBoxDB.fts;

import iBoxDB.LocalServer.NotColumn;
import iBoxDB.LocalServer.UString;
import iBoxDB.fulltext.KeyWord;
import java.util.HashSet;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import jodd.jerry.Jerry;
import static jodd.jerry.Jerry.jerry;

public class Page {

    public long id;
    public String url;

    public String title;
    public String description;

    public UString content;

    public static Page get(String url, HashSet<String> subUrls) {
        try {
            if (url == null || url.length() > 100 || url.length() < 8) {
                return null;
            }
            Page page = new Page();
            page.url = url;

            HttpRequest httpRequest = HttpRequest.get(url);
            httpRequest.timeout(10 * 1000);
            HttpResponse response = httpRequest.send();
            if (response.statusCode() != 200) {
                return null;
            }
            try {
                if (response.charset() == null) {
                    String t = new String(response.bodyBytes());
                    String cs = "charset=";
                    int p = t.indexOf(cs);
                    if (p > 0) {
                        int e = t.indexOf("\"", p + cs.length() + 2);
                        if (e > 0) {
                            t = t.substring(p + cs.length(), e);

                            t = t.replaceAll("\"", "").trim();

                            if (t.length() < 8) {
                                response.charset(t);
                            }
                        }
                    }
                }
            } catch (Throwable e) {
            }
            if (response.charset() == null) {
                response.charset("utf-8");
            }

            Jerry doc = jerry(response.bodyText());
            doc.$("script").text("");
            doc.$("style").text("");
            doc.$("Script").text("");
            doc.$("Style").text("");

            if (subUrls != null) {
                for (Jerry a : doc.$("a")) {
                    String ss = getFullUrl(url, a.attr("href"));
                    if (ss != null) {
                        subUrls.add(ss);
                    }
                }
            }
            page.title = doc.$("title").text();
            if (page.title == null) {
                page.title = doc.$("Title").text();
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
            page.title = page.title.replaceAll("<", " ")
                    .replaceAll(">", " ").replaceAll("\\$", " ");

            page.description = doc.$("meta[name='description']").attr("content");
            if (page.description == null) {
                page.description = doc.$("meta[name='Description']").attr("content");
            }
            if (page.description == null) {
                page.description = "";
            }
            if (page.description.length() > 200) {
                page.description = page.description.substring(0, 200);
            }
            page.description = page.description.replaceAll("<", " ")
                    .replaceAll(">", " ").replaceAll("\\$", " ");

            doc = jerry(doc.text().replaceAll("&lt;", "<")
                    .replaceAll("&gt;", ">"));
            doc.$("script").text("");
            doc.$("style").text("");
            doc.$("Script").text("");
            doc.$("Style").text("");

            String content = doc.text();
            if (content.length() < 10) {
                return null;
            }
            if (content.length() > 5000) {
                content = content.substring(0, 5000);
            }
            content = content.replaceAll("\r", " ")
                    .replaceAll("\n", " ")
                    .replaceAll("   ", " ")
                    .replaceAll("  ", " ")
                    .replaceAll("  ", " ").trim();

            page.content = UString.S((content
                    + " " + page.url
                    + " " + page.description).replaceAll("<", " ")
                    .replaceAll(">", " ").replaceAll("\\$", " "));

            return page;
        } catch (Throwable e) {
            return null;
        }
    }

    private static String getFullUrl(String base, String url) {
        try {
            if (base == null || url == null) {
                return null;
            }
            base = base.trim().toLowerCase();
            url = url.trim().toLowerCase();
            if (base.length() < 2 || url.length() < 2) {
                return null;
            }
            int si = url.indexOf("#");
            if (si > 0) {
                url = url.substring(0, si);
            }
            if (url.startsWith("./")) {
                url = url.substring(2);
            }

            if (url.startsWith("javascript")) {
                return null;
            }
            if (url.contains("download")) {
                return null;
            }

            if (url.startsWith("http:") || url.startsWith("https:")) {
                return url;
            }
            if ((!base.startsWith("http:")) && (!base.startsWith("https:"))) {
                return null;
            }

            int t = base.indexOf("//");
            t = base.indexOf("/", t + 2);
            String domain = base;
            if (t > 0) {
                domain = domain.substring(0, t);
            }
            if (!domain.endsWith("/")) {
                domain += "/";
            }
            if (url.startsWith("/")) {
                url = domain + url.substring(1);
            } else {
                url = domain + url;
            }

            t = url.lastIndexOf("/");
            if (t > 0) {
                String tu = url.substring(t);
                if (tu.contains(".")) {
                    if ((!tu.contains(".html")) && (!tu.contains(".htm"))
                            && (!tu.contains(".shtml"))
                            && (!tu.contains(".asp"))
                            && (!tu.contains(".aspx")) && (!tu.contains(".php"))
                            && (!tu.contains(".jsp"))) {
                        return null;
                    }
                }
            }
            return url;
        } catch (Throwable ex) {
            return null;
        }
    }

    @NotColumn
    public KeyWord keyWord;
}

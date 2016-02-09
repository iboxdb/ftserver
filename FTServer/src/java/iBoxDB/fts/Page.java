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

    public static Page get(String url) {
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
 
    @NotColumn
    public KeyWord keyWord;
}

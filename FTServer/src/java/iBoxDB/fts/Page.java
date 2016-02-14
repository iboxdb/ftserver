package iBoxDB.fts;

import iBoxDB.LocalServer.NotColumn;
import iBoxDB.LocalServer.UString;
import iBoxDB.fulltext.KeyWord;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
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

    public static byte[] decompress(byte[] is) {
        try {
            GZIPInputStream gis = new GZIPInputStream(new java.io.ByteArrayInputStream(is));
            java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
            int count;
            byte data[] = new byte[1024 * 8];
            while ((count = gis.read(data, 0, 1024 * 8)) != -1) {
                os.write(data, 0, count);
            }
            gis.close();
            return os.toByteArray();
        } catch (Throwable e) {
            return null;
        }
    }

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

            byte[] result = response.bodyBytes();
            if ("gzip".equalsIgnoreCase(response.contentEncoding())) {
                result = decompress(result);
            }
            String charset = response.charset();
            if (charset == null) {

                ArrayList<Charset> tests = new ArrayList<Charset>();
                tests.add(Charset.forName("UTF-8"));
                tests.add(Charset.forName("ISO-8859-1"));
                tests.add(Charset.forName("UTF-16"));
                try {
                    tests.add(Charset.forName("GBK"));
                } catch (Throwable e) {

                }

                for (Charset cset : tests) {
                    String t = new String(result, cset);
                    final String cs = "charset=";
                    int p = t.indexOf(cs);
                    if (p > 0) {
                        int e = t.indexOf("\"", p + cs.length() + 2);
                        if (e > 0) {
                            t = t.substring(p + cs.length(), e);

                            t = t.replaceAll("\"", "").trim();

                            if (t.length() < 10) {
                                charset = t;
                                break;
                            }
                        }
                    }
                }
            }
            if (charset == null) {
                charset = "UTF-8";
            }
            charset = charset.trim();
            String resultS = new String(result, charset);

            Jerry doc = jerry(resultS);
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

            String content = doc.text().trim();
            if (content.length() < 50) {
                return null;
            }
            if (content.length() > 5000) {
                content = content.substring(0, 5000);
            }
            content = content.replaceAll("\r", " ")
                    .replaceAll("\n", " ")
                    .replaceAll("　", " ")
                    .replaceAll("   ", " ")
                    .replaceAll("   ", " ")
                    .replaceAll("  ", " ")
                    .replaceAll("  ", " ").trim();

            page.content = UString.S((content
                    + " " + page.url
                    + " " + page.description).replaceAll("<", " ")
                    .replaceAll(">", " ").replaceAll("\\$", " ")
                    .replaceAll("　", " "));

            return page;
        } catch (Throwable e) {
            return null;
        }
    }

    @NotColumn
    public KeyWord keyWord;
}

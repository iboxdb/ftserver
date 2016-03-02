package iBoxDB.fts;

import iBoxDB.LocalServer.NotColumn;
import iBoxDB.LocalServer.UString;
import iBoxDB.fulltext.KeyWord;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import jodd.jerry.Jerry;
import static jodd.jerry.Jerry.jerry;
import static jodd.jerry.Jerry.jerry;

public class BPage {

    public final static int MAX_URL_LENGTH = 100;

    public long id;
    public String url;

    public String title;
    public String description;

    public UString content;

    @NotColumn
    public long rankUpId() {
        return id | (1L << 60);
    }

    @NotColumn
    public static long rankDownId(long id) {
        return id & (~(1L << 60));
    }

    @NotColumn
    public String rankUpDescription() {
        return description + " " + title;
    }

    private static final Random cran = new Random();

    @NotColumn
    public String getRandomContent() {
        int len = content.toString().length() - 100;
        if (len <= 20) {
            return content.toString();
        }
        int s = cran.nextInt(len);
        if (s < 0) {
            s = 0;
        }
        if (s > len) {
            s = len;
        }

        int end = s + 200;
        if (end > content.toString().length()) {
            end = content.toString().length();
        }

        return content.toString().substring(s, end);
    }

    @NotColumn
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

    @NotColumn
    public static BPage get(String url, HashSet<String> subUrls) {
        try {
            if (url == null || url.length() > MAX_URL_LENGTH || url.length() < 8) {
                return null;
            }
            BPage page = new BPage();
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
            // -17 -69 -65
            String charset = response.charset();
            if (charset == null) {
                if (result[0] == -17 && result[1] == -69 && result[2] == -65) {
                    charset = "UTF-8";
                }
            }
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
            if ("gb2312".equalsIgnoreCase(charset)) {
                charset = "GBK";
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
                    .replaceAll(">", " ").replaceAll("\\$", " ")
                    .replaceAll("�", " ");

            doc.$("title").text("");
            doc.$("Title").text("");

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
                    .replaceAll(">", " ").replaceAll("\\$", " ")
                    .replaceAll("�", " ");

            doc = jerry(doc.text().replaceAll("&lt;", "<")
                    .replaceAll("&gt;", ">"));
            doc.$("script").text("");
            doc.$("style").text("");
            doc.$("Script").text("");
            doc.$("Style").text("");

            String content = doc.text().trim();
            content = content.replaceAll("\t|\r|\n|�|<|>", " ")
                    .replaceAll("\\$", " ")
                    .replaceAll("　", " ")
                    .replaceAll("\\s+", " ")
                    .trim();
            if (content.length() < 50) {
                return null;
            }
            if (content.length() > 5000) {
                content = content.substring(0, 5000);
            }
            page.content = UString.S(content + " " + page.url);

            return page;
        } catch (Throwable e) {
            return null;
        }
    }

    @NotColumn
    private static String getFullUrl(String base, String url) {
        try {
            if (base == null || url == null) {
                return null;
            }
            base = base.trim();
            url = url.trim();
            if (base.length() < 2 || url.length() < 2) {
                return null;
            }
            int si = url.indexOf("#");
            if (si > 0) {
                url = url.substring(0, si);
            }
            if (si == 0) {
                return null;
            }
            if (url.startsWith("./")) {
                url = url.substring(2);
            }

            String lcurl = url.toLowerCase();
            if (lcurl.startsWith("javascript")) {
                return null;
            }
            if (lcurl.contains("download") || lcurl.contains("signup") || lcurl.contains("login")) {
                return null;
            }

            if (lcurl.startsWith("http:") || lcurl.startsWith("https:")) {
                if (isHTMLURL(url)) {
                    return url;
                }
                return null;
            }

            String lcbase = base.toLowerCase();
            if ((!lcbase.startsWith("http:")) && (!lcbase.startsWith("https:"))) {
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
            if (!base.endsWith("/") && base.length() > domain.length()) {
                t = base.lastIndexOf("/");
                base = base.substring(0, t + 1);
            }

            if (url.startsWith("/")) {
                url = domain + url.substring(1);
            } else {
                url = base + url;
            }

            if (isHTMLURL(url)) {
                return url;
            }
            return null;
        } catch (Throwable ex) {
            //ex.printStackTrace();
            return null;
        }
    }

    @NotColumn
    private static boolean isHTMLURL(String url) {
        url = url.toLowerCase();
        int t = url.lastIndexOf("/");
        if (t > "https://-".length()) {
            String tu = url.substring(t);
            if (tu.contains(".")) {
                if ((!tu.contains(".html")) && (!tu.contains(".htm"))
                        && (!tu.contains(".shtml"))
                        && (!tu.contains(".asp"))
                        && (!tu.contains(".aspx")) && (!tu.contains(".php"))
                        && (!tu.contains(".jsp"))) {
                    return false;
                }
            }
        }
        return url.contains(".");
    }

    @NotColumn
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

    @NotColumn
    public KeyWord keyWord;
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FTServer;

import static FTServer.Page.MAX_URL_LENGTH;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import jodd.jerry.Jerry;
import static jodd.jerry.Jerry.jerry;

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
        HttpResponse response = null;
        try {
            if (url == null || url.length() > MAX_URL_LENGTH || url.length() < 8) {
                return null;
            }
            Page page = new Page();
            page.url = url;

            HttpRequest httpRequest = HttpRequest.get(url);
            httpRequest.timeout(15 * 1000);
            response = httpRequest.send();
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
                    int p = t.indexOf("<meta ");
                    int p2 = t.indexOf("<META ");
                    if (p < 0) {
                        p = Integer.MAX_VALUE;
                    }
                    if (p2 < 0) {
                        p2 = Integer.MAX_VALUE;
                    }
                    p = Math.min(p, p2);
                    if (p == Integer.MAX_VALUE) {
                        p = -1;
                    }
                    if (p > 0) {
                        t = t.substring(p);
                    }
                    final String cs = "charset=";
                    p = t.indexOf(cs);
                    if (p > 0) {
                        int e = t.indexOf("\"", p + cs.length() + 2);
                        int e2 = t.indexOf("\'", p + cs.length() + 2);
                        if (e < 0) {
                            e = Integer.MAX_VALUE;
                        }
                        if (e2 < 0) {
                            e2 = Integer.MAX_VALUE;
                        }
                        e = Math.min(e, e2);
                        if (e == Integer.MAX_VALUE) {
                            e = -1;
                        }
                        if (e > 0) {
                            t = t.substring(p + cs.length(), e);

                            t = t.replaceAll("\"", "").trim();
                            t = t.replaceAll("\'", "").trim();
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
            doc.$("script").remove();
            doc.$("Script").remove();

            doc.$("style").remove();
            doc.$("Style").remove();

            doc.$("textarea").remove();
            doc.$("Textarea").remove();

            doc.$("noscript").remove();
            doc.$("Noscript").remove();

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
                page.title = doc.$("TITLE").text();
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

            doc.$("title").remove();
            doc.$("Title").remove();

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
            page.description = page.description
                    .replaceAll(Character.toString((char) 8203), "")
                    .replaceAll("\t|\r|\n|�|<|>|�|\\$", " ");


            /*
            doc = jerry(doc.text().replaceAll("&lt;", "<")
                    .replaceAll("&gt;", ">"));    
            doc.$("script").text("");
            doc.$("style").text("");
            doc.$("Script").text("");
            doc.$("Style").text("");
             */
            fixSpan(doc);
            String content = doc.text();
            content = content.replaceAll(Character.toString((char) 8203), "")
                    .replaceAll("&nbsp;", " ")
                    .replaceAll("&gt;", " ")
                    .replaceAll("&lt;", " ")
                    .replaceAll("\t|\r|\n|�|<|>|�|\\$|\\|", " ")
                    .replaceAll("　", " ")
                    .replaceAll("\\s+", " ")
                    .trim();

            if (content.length() < 50 && (!url.contains("localhost"))) {
                //return null;
            }
            if (content.length() > 5000) {
                content = content.substring(0, 5000);
            }
            page.content = content + " " + page.url;

            return page;
        } catch (Throwable e) {
            //e.printStackTrace();
            return null;
        } finally {
            if (response != null) {
                response.close();
            }
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

    private static void fixSpan(Jerry doc) {
        for (Jerry j : doc.$("span")) {
            if (j.size() == 1) {
                j.text(j.text() + " ");
            }
        }
    }

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
            if (lcurl.contains("download") || lcurl.contains("signup") || lcurl.contains("login")
                    || lcurl.contains("share") || lcurl.contains("mailto")
                    || lcurl.contains("report") || lcurl.contains("send")
                    || lcurl.contains("register") || lcurl.contains("search")
                    || lcurl.contains("cgi-bin")) {
                return null;
            }

            if (lcurl.startsWith("http:") || lcurl.startsWith("https:")) {
                if (isHTMLURL(url)) {
                    return url;
                }
                return null;
            }

            if (lcurl.startsWith("//")) {
                if (base.startsWith("https:")) {
                    url = "https:" + url;
                } else {
                    url = "http:" + url;
                }
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

    private static boolean isHTMLURL(String url) {
        if (url.length() > Page.MAX_URL_LENGTH) {
            return false;
        }
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

    private static byte[] decompress(byte[] is) {
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
}

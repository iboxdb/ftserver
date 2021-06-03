
<%@page import="java.util.*"%>
<%@page import="ftserver.fts.*"%>
<%@page import="ftserver.*"%>
<%@page contentType="text/html" pageEncoding="UTF-8" session="false"%>
<%@include  file="_taghelper.jsp" %>

<%    long begin = System.currentTimeMillis();
%>

<%!    String IdToString(long[] ids, char p) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.length; i++) {
            if (i > 0) {
                sb.append(p);
            }
            sb.append(ids[i]);
        }
        return sb.toString();
    }

    boolean IsEnd(long[] ids) {
        return ids[0] < 0 && ids[1] < 0;
    }

    String ToKeyWordString(ArrayList<PageText> pages) {
        HashSet<String> hc = new HashSet<String>();
        for (PageText pg : pages) {
            if (pg.keyWord != null && pg.keyWord.previous != null) {
                continue;
            }
            if (pg.keyWord instanceof KeyWordE) {
                hc.add(((KeyWordE) pg.keyWord).K);
            }
            if (pg.keyWord instanceof KeyWordN) {

                hc.add(((KeyWordN) pg.keyWord).toKString());
            }
        }

        String[] ids = hc.toArray(new String[0]);
        char p = ' ';
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.length; i++) {
            if (i > 0) {
                sb.append(p);
            }
            sb.append(ids[i]);
        }
        return sb.toString();
    }
%>


<%    long pageCount = 8;
    //pageCount = 2;
    long[] startId = new long[]{Long.MAX_VALUE};

    String name = request.getParameter("q");
    String sid = request.getParameter("s");
    if (sid != null) {
        String[] ss = sid.trim().split("_");
        startId = new long[ss.length];
        for (int i = 0; i < ss.length; i++) {
            startId[i] = Long.parseLong(ss[i]);
        }
    }

    boolean isFirstLoad = startId[0] == Long.MAX_VALUE;
    if (isFirstLoad) {
        pageCount = 1;
    }
    if (App.IsAndroid) {
        if (pageCount > 4) {
            pageCount = 4;
        }
    }

    ArrayList<PageText> pages = new ArrayList<>();

    startId = IndexAPI.Search(pages, name, startId, pageCount);

%>

<div id="ldiv<%=IdToString(startId, '_')%>" >
    <% for (PageText p : pages) {
            if (p.priority == 0) {

                continue;
            }
            boolean isdesc = p.priority >= PageText.descriptionPriority;
            String content = isdesc ? p.text
                    : IndexPage.getDesc(p.text, p.keyWord, 150);

            if (content.length() < 200 && p.page != null) {
                content += "..." + p.page.getRandomContent(200);
            }
            String[] keywords = p.keywords.split(" ");

            //try (Tag h3 = HTML.tag("h3")) 
            {
                try (Tag div = HTML.tag("div", "class:", "spartcss", "dir:", "auto")) {
                    try (Tag a = HTML.tag("a",
                            "class:", "stext",
                            "target:", "_blank",
                            "href:", p.url,
                            "onclick:", "sendlog(this.href, 'content')")) {
                        HTML.text(p.title);
                    }
                }
            }

            try (Tag span = HTML.tag("div", "class:", "stext", "dir:", "auto")) {
                HTML.text(content);
            }
            HTML.tag("br");

            try (Tag div = HTML.tag("div", "class:", (isdesc ? "gt" : "gtt") + " ")) {

                for (String kw : keywords) {
                    String str = kw.trim();
                    if (str == null || str.length() < 1) {
                        continue;
                    }
                    try (Tag t = HTML.tag("a",
                            "href:", "s.jsp?q=" + encode(str),
                            "class:", "kw " + (isdesc ? "gt" : "gtt") + " stext_s")) {
                        HTML.text(" &nbsp; ");
                        HTML.text(str);
                        HTML.text(" &nbsp; ");
                    };
                    HTML.text(" &nbsp;");
                }
                if (keywords.length > 1) {
                    HTML.tag("br");
                }

                HTML.text("[ " + p.dbOrder + " ] ");
                if (!p.isAndSearch) {
                    HTML.text("*");
                }
                HTML.text(decodeTry(p.url));
                HTML.text(" ");
                HTML.text(p.createTime.toString());

            }
            HTML.tag("br");
        }%>
</div>
<div class="ui teal message" id="s<%= IdToString(startId, '_')%>" dir="auto">
    <%
        String content = ((System.currentTimeMillis() - begin) / 1000.0) + "s, "
                + "MEM:" + (java.lang.Runtime.getRuntime().totalMemory() / 1024 / 1024) + "MB ";
    %>
    <%=name%>  TIME: <%= content%>
    <a href="#btnsearch" ><b><%=  !IsEnd(startId) ? "CONTINUING" : "END"%></b></a>

</div>
<br>
<script>
    setTimeout(function () {
        highlight("ldiv<%= IdToString(startId, '_')%>");
    <% if (!IsEnd(startId)) {%>
        //startId is a big number, in javascript, have to write big number as a 'String'
        onscroll_loaddiv("s<%= IdToString(startId, '_')%>", "<%= IdToString(startId, '_')%>");
    <%}%>
    }, 100);
    extitle = "<%= ToKeyWordString(pages)%>";
</script>
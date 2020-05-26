<%@page import="java.util.HashSet"%>
<%@page import="ftserver.fts.*"%>
<%@page import="ftserver.*"%>
<%@page import="java.util.ArrayList"%>
<%@page import="iBoxDB.LocalServer.*"%>
<%@page contentType="text/html" pageEncoding="UTF-8" session="false"%>
<%@include  file="_taghelper.jsp" %>


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
        for (long l : ids) {
            if (l > 0) {
                return false;
            }
        }
        return true;
    }

    String ToKeyWordString(ArrayList<Page> pages) {
        HashSet<String> hc = new HashSet<String>();
        for (Page pg : pages) {
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


<%    long pageCount = 12;
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

    long begin = System.currentTimeMillis();
    ArrayList<Page> pages = new ArrayList<>();

    startId = IndexAPI.Search(pages, name, startId, pageCount);

    //Page isEmpty 
    if (isFirstLoad && pages.size() == 0) {
        Page p = new Page();
        p.id = -1;
        p.keyWord = new KeyWordE();
        p.keyWord.I = -1;
        p.title = "GOTO ADMIN";
        p.description = "";
        p.content = "";
        p.url = "admin.jsp";
        pages.add(p);
    }
%>

<div id="ldiv<%=IdToString(startId, '_')%>">
    <% for (Page p : pages) {
            //Format Paage    
            boolean sendlog = false;
            boolean isdesc = false;
            String content = null;
            if (p.id != p.keyWord.I) {
                //only show page description
                content = p.description;
                if (content.length() < 20) {
                    content += p.getRandomContent() + "...";;
                }
                sendlog = !isFirstLoad;
                isdesc = true;
            } else {
                //page content is too long, just get some chars
                content = IndexAPI.getDesc(p.content.toString(), p.keyWord, 80);
                if (content.length() < 100) {
                    content += " " + p.getRandomContent();
                }
                if (content.length() < 100) {
                    content += p.description;
                }
                if (content.length() > 200) {
                    content = content.substring(0, 200) + "..";
                }
            }

            try (Tag h3 = tag("h3")) {
                try (Tag div = tag("div", "class:", "spartcss")) {
                    try (Tag a = tag("a",
                            "class:", "stext",
                            "target:", "_blank",
                            "href:", p.url,
                            "onclick:", sendlog ? "sendlog(this.href, 'content')" : "sendlog(false)")) {
                        text(p.title);
                    }
                }
            }

            try (Tag span = tag("span", "class:", "stext")) {
                text(content);
            }
            tag("br");

            try (Tag div = tag("div", "class:", (isdesc ? "gt" : "gtt") + " spartcss")) {
                if (!p.isAnd) {
                    text("*");
                }
                text(p.url);
                text(" ");
                text(p.createTime.toString());
            }

        }%>
</div>
<div class="ui teal message" id="s<%= IdToString(startId, '_')%>">
    <%
        String content = ((System.currentTimeMillis() - begin) / 1000.0) + "s, "
                + "MEM:" + (java.lang.Runtime.getRuntime().totalMemory() / 1024 / 1024) + "MB ";
    %>
    <%=name%>  TIME: <%= content%>
    <a href="#btnsearch" ><b><%=  !IsEnd(startId) ? "HEAD" : "END"%></b></a>

</div>
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
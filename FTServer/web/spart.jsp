<%@page import="iBoxDB.fts.SServlet"%> 
<%@page import="iBoxDB.LocalServer.UString"%>
<%@page import="iBoxDB.fulltext.KeyWord"%>
<%@page import="java.util.ArrayList"%>
<%@page import="iBoxDB.fts.BPage"%>
<%@page import="iBoxDB.LocalServer.Box"%>
<%@page import="iBoxDB.fts.SDB"%>
<%@page import="iBoxDB.fts.SearchResource"%>
<%@page contentType="text/html" pageEncoding="UTF-8" session="false"%>
<%
    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache");
    response.setDateHeader("Expires", 0);
%>
<%
    if (SDB.search_db == null) {
        return;
    }
    long pageCount = 12;
    final String queryString = request.getQueryString();
    String name = java.net.URLDecoder.decode(queryString, "UTF-8");
    name = name.substring(2);
    long startId = Long.MAX_VALUE;
    int temp = name.indexOf("&");
    if (temp > 0) {
        String sid = name.substring(temp + 3);
        startId = Long.parseLong(sid);
        name = name.substring(0, temp);
    }

    ArrayList<BPage> pages = new ArrayList<BPage>();
    long begin = System.currentTimeMillis();
    Box box = SDB.search_db.cube();
    try {

        for (KeyWord kw : SearchResource.engine.searchDistinct(box, name, startId, pageCount)) {
            long id = kw.getID();
            id = BPage.rankDownId(id);
            BPage p = box.d("Page", id).select(BPage.class);
            p.keyWord = kw;
            pages.add(p);
            startId = id - 1;
        }
    } finally {
        box.close();
    }

%>
<%    if (pages.isEmpty()) {
        BPage p = new BPage();
        p.title = "End. " + name;
        p.description = "";
        p.content = UString.S("input URL to index more page");
        p.url = "./";
        pages.add(p);
    }
%>

<div id="ldiv<%= startId%>">
    <% for (BPage p : pages) {
            String content = null;
            if (pages.size() == 1 || p.keyWord == null) {
                content = p.description + "...";
                content += p.content.toString();
            } else if (p.id != p.keyWord.getID()) {
                content = p.description;
                if (content.length() < 20) {
                    content += p.getRandomContent() + "...";;
                }
            } else {
                content = SearchResource.engine.getDesc(p.content.toString(), p.keyWord, 80);
                if (content.length() < 100) {
                    content += p.getRandomContent();
                }
                if (content.length() < 100) {
                    content += p.description;
                }
                if (content.length() > 200) {
                    content = content.substring(0, 200) + "..";
                }
            }
    %>
    <h3>
        <a class="stext" target="_blank"   href="<%=p.url%>" ><%= p.title%></a></h3> 
    <span class="stext"> <%=content%> </span><br>
    <div class="gt">
        <%=p.url%>
    </div>
    <% }%>
</div>
<div class="ui teal message" id="s<%= startId%>">
    <%
        String content = ((System.currentTimeMillis() - begin) / 1000.0) + "s, "
                + "MEM:" + (java.lang.Runtime.getRuntime().totalMemory() / 1024 / 1024) + "MB ";
    %>
    <h4> <%= SServlet.lastEx != null ? "Readonly" : ""%></h4> 
    <%=name%>  TIME: <%= content%>
    <a href="#btnsearch" ><b><%= pages.size() >= pageCount ? "TOP" : "END"%></b></a>
</div>
<script>
    setTimeout(function () {
        highlight("ldiv<%= startId%>");
    <% if (pages.size() >= pageCount) {%>
        onscroll_loaddiv("s<%= startId%>", <%= startId%>);
    <%}%>
    }, 100);
</script>
<%@page import="FTServer.FTS.*"%>
<%@page import="FTServer.*"%>
<%@page import="java.util.ArrayList"%>
<%@page import="iBoxDB.LocalServer.*"%>
<%@page contentType="text/html" pageEncoding="UTF-8" session="false"%>
<%
    response.setHeader("Cache-Control", "non-cache, no-store, must-revalidate");
%>
<%
    if (App.Auto == null) {
        return;
    }
    long pageCount = 12;

    final String queryString = request.getQueryString();
    String name = java.net.URLDecoder.decode(queryString, "UTF-8");
    name = name.trim();
    name = name.substring(2);
    name = name.trim();

    long startId = Long.MAX_VALUE;
    int temp = name.lastIndexOf("&s=");
    if (temp > 0) {
        String sid = name.substring(temp + 3);
        startId = Long.parseLong(sid);
        name = name.substring(0, temp);
    }
    boolean isFirstLoad = startId == Long.MAX_VALUE;

    ArrayList<Page> pages = new ArrayList<Page>();
    long begin = System.currentTimeMillis();
    Box box = App.Auto.cube();
    try {

        for (KeyWord kw : SearchResource.engine.searchDistinct(box, name, startId, pageCount)) {

            startId = kw.getID() - 1;

            long id = kw.getID();
            id = Page.rankDownId(id);
            Page p = box.d("Page", id).select(Page.class);
            p.keyWord = kw;
            pages.add(p);

        }
    } finally {
        box.close();
    }

%>
<%    if (startId == Long.MAX_VALUE) {
        Page p = new Page();
        p.title = "not found " + name;
        p.description = "";
        p.content = UString.S("input URL to index more page");
        p.url = "./";
        pages.add(p);
    }
%>

<div id="ldiv<%= startId%>">
    <% for (Page p : pages) {
            String content = null;
            if ((pages.size() == 1 && isFirstLoad) || p.keyWord == null) {
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
    <%=name%>  TIME: <%= content%>
    <a href="#btnsearch" ><b><%= pages.size() >= pageCount ? "HEAD" : "END"%></b></a>
            <%= AppServlet.lastEx != null ? "-Readonly" : ""%>
</div>
<script>
    setTimeout(function () {
        highlight("ldiv<%= startId%>");
    <% if (pages.size() >= pageCount) {%>
        //startId is a big number, in javascript, have to write big number as a 'String'
        onscroll_loaddiv("s<%= startId%>", "<%= startId%>");
    <%}%>
    }, 100);
</script>
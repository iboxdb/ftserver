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
    long startId = Long.MAX_VALUE;
    /*
    final String queryString = request.getQueryString();
    String name = java.net.URLDecoder.decode(queryString, "UTF-8");
    name = name.trim();
    name = name.substring(2);

    int temp = name.lastIndexOf("&s=");
    if (temp > 0) {
        String sid = name.substring(temp + 3);
        startId = Long.parseLong(sid);
        name = name.substring(0, temp);
    }
     */
    String name = request.getParameter("q");
    String sid = request.getParameter("s");
    if (sid != null) {
        startId = Long.parseLong(sid);
    }

    boolean isFirstLoad = startId == Long.MAX_VALUE;

    long begin = System.currentTimeMillis();
    ArrayList<Page> pages = new ArrayList<Page>();

    startId = IndexAPI.Search(pages, name, startId, pageCount);


%>
<%    if (startId == Long.MAX_VALUE) {
        Page p = new Page();
        p.title = "not found " + name;
        p.description = "";
        p.content = "GoTO admin.jsp, input URL to index more page";
        p.url = "admin.jsp";
        pages.add(p);
    }
%>

<div id="ldiv<%= startId%>">
    <% for (Page p : pages) {
            //Format Paage    
            boolean sendlog = false;
            boolean isdesc = false;
            String content = null;
            if ((pages.size() == 1 && isFirstLoad) || p.keyWord == null) {
                //only have one page, show all
                content = p.description + "...";
                content += p.content.toString();
            } else if (p.id != p.keyWord.getID()) {
                //only show page description
                content = p.description;
                if (content.length() < 20) {
                    content += p.getRandomContent() + "...";;
                }
                //flags, second page and "description"
                sendlog = !isFirstLoad;
                isdesc = true;
            } else {
                //page content is too long, just get some chars
                content = IndexAPI.getDesc(p.content.toString(), p.keyWord, 80);
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
        <a class="stext" target="_blank" href="<%=p.url%>" 
           <%=sendlog ? "onclick=\"sendlog(this.href, 'content')\"" : ""%> >          
            <%= p.title%></a></h3> 
    <span class="stext"> <%=content%> </span><br>
    <div class="<%=isdesc ? "gt" : "gtt"%>" >
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

</div>
<script>
    setTimeout(function () {
        highlight("ldiv<%= startId%>");
    <% if (pages.size()
                >= pageCount) {%>
        //startId is a big number, in javascript, have to write big number as a 'String'
        onscroll_loaddiv("s<%= startId%>", "<%= startId%>");
    <%}%>
    }, 100);
</script>
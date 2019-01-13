<%@page import="FTServer.FTS.*"%>
<%@page import="FTServer.*"%>
<%@page import="java.util.ArrayList"%>
<%@page import="iBoxDB.LocalServer.*"%>
<%@page contentType="text/html" pageEncoding="UTF-8" session="false"%>
<%
    response.setHeader("Cache-Control", "non-cache, no-store, must-revalidate");
%>
<%
    long pageCount = 12;
    long startId = Long.MAX_VALUE;

    String name = request.getParameter("q");
    String sid = request.getParameter("s");
    if (sid != null) {
        startId = Long.parseLong(sid);
    }

    boolean isFirstLoad = startId == Long.MAX_VALUE;

    long begin = System.currentTimeMillis();
    ArrayList<Page> pages = new ArrayList<>();

    startId = IndexAPI.Search(pages, name, startId, pageCount);

%>
<%    if (startId == Long.MAX_VALUE) {
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
<%
    if (startId == -1) {
%>
<h3>Recommend:</h3>
<%
    }
%>
<div id="ldiv<%= startId%>">
    <% for (Page p : pages) {
            //Format Paage    
            boolean sendlog = false;
            boolean isdesc = false;
            String content = null;
            if (p.id != p.keyWord.getID()) {
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
    %>
    <h3><div class="spartcss">
            <a class="stext"  target="_blank" href="<%=p.url%>" 
               <%=sendlog ? "onclick=\"sendlog(this.href, 'content')\"" : ""%> >          
                <%= p.title%></a></div>
    </h3> 
    <span class="stext"> <%=content%> </span><br>
    <div class="<%=isdesc ? "gt" : "gtt"%> spartcss" >
        <%=p.url%>  <%=  p.createTime%>
    </div>
    <% }%>
</div>
<div class="ui teal message" id="s<%= startId%>">
    <%
        String content = ((System.currentTimeMillis() - begin) / 1000.0) + "s, "
                + "MEM:" + (java.lang.Runtime.getRuntime().totalMemory() / 1024 / 1024) + "MB ";
    %>
    <%=name%>  TIME: <%= content%>
    <a href="#btnsearch" ><b><%= pages.size() >= pageCount && startId > -1 ? "HEAD" : "END"%></b></a>

</div>
<script>
    setTimeout(function () {
        highlight("ldiv<%= startId%>");
    <% if (pages.size() >= pageCount && startId > -1) {%>
        //startId is a big number, in javascript, have to write big number as a 'String'
        onscroll_loaddiv("s<%= startId%>", "<%= startId%>");
    <%}%>
    }, 100);
</script>
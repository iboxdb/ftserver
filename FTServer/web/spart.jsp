<%@page import="java.util.HashSet"%>
<%@page import="ftserver.fts.*"%>
<%@page import="ftserver.*"%>
<%@page import="java.util.ArrayList"%>
<%@page import="iBoxDB.LocalServer.*"%>
<%@page contentType="text/html" pageEncoding="UTF-8" session="false"%>


<%!
    String IdToString(long[] ids, char p) {
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

<%
    response.setHeader("Cache-Control", "non-cache, no-store, must-revalidate");
%>

<%
    long pageCount = 12;
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

%>
<%    if (isFirstLoad && pages.size() == 0) {
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
    %>
    <h3><div class="spartcss">
            <a class="stext"  target="_blank" href="<%=p.url%>" 
               <%=sendlog ? "onclick=\"sendlog(this.href, 'content')\"" : ""%> >          
                <%= p.title%></a></div>
    </h3> 
    <span class="stext"> <%=content%> </span><br>
    <div class="<%=isdesc ? "gt" : "gtt"%> spartcss" >
        <%= p.isAnd ? "" : "*"%>
        <%=p.url%>  <%=  p.createTime%>

    </div>
    <% }%>
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
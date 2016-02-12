<%@page import="iBoxDB.LocalServer.UString"%>
<%@page import="iBoxDB.fulltext.KeyWord"%>
<%@page import="java.util.ArrayList"%>
<%@page import="iBoxDB.fts.Page"%>
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
    String name = (String) request.getAttribute("q");
    if (name == null) {
        return;
    }
    ArrayList<Page> pages = new ArrayList<Page>();
    long begin = System.currentTimeMillis();
    Box box = SDB.search_db.cube();
    try {
        for (KeyWord kw : SearchResource.engine.searchDistinct(box, name)) {
            Page p = box.d("Page", kw.getID()).select().select(Page.class);
            p.keyWord = kw;
            pages.add(p);
            if (pages.size() > 100) {
                break;
            }
        }
    } finally {
        box.close();
    }

    if (pages.isEmpty()) {
        Page p = new Page();
        p.title = "NotFound";
        p.content = UString.S("input URL to index");
        p.url = "https://github.com/iboxdb/ftserver";
        pages.add(p);
    }
%>
<!DOCTYPE html>
<html>
    <head>        
        <meta http-equiv="content-type" content="text/html; charset=UTF-8">
        <meta name="description" content="<%=name%> what is? iBoxDB NoSQL Database Full Text Search">
        <title><%=name%>, what is? iBoxDB Full Text Search</title>

        <link rel="stylesheet" type="text/css" href="css/semantic.min.css"> 

        <style>
            body {
                margin-top: 10px;
                margin-left: 10px;
                font-weight:lighter;
                overflow-x: hidden;
            }
            .stext{

            }
            .redtext{
                color: red;
            }
        </style> 
        <script>
            function hightlight() {
                var txt = document.title.substr(0, document.title.indexOf(','));

                var ts = document.getElementsByClassName("stext");

                var kws = txt.split(' ');
                for (var i = 0; i < kws.length; i++) {
                    var kw = String(kws[i]).trim();
                    if (kw.length < 1) {
                        continue;
                    }
                    var fontText = "<font class='redtext'>";
                    if (fontText.indexOf(kw) > -1) {
                        continue;
                    }
                    if ("</font>".indexOf(kw) > -1) {
                        continue;
                    }
                    for (var j = 0; j < ts.length; j++) {
                        var html = ts[j].innerHTML;
                        ts[j].innerHTML =
                                html.replace(new RegExp(kw, 'gi'),
                                        fontText + kw + "</font>");
                    }
                }
            }
        </script>
    </head>
    <body onload="hightlight()"> 
        <div class="ui left aligned grid">
            <div class="column"  style="max-width: 600px;"> 
                <form class="ui large form"  action="s" onsubmit="formsubmit()">
                    <div class="ui label input">

                        <div class="ui action input">
                            <a href="./"><i class="teal disk outline icon" style="font-size:42px"></i> </a>
                            <input name="q"  value="<%=name%>" required onfocus="formfocus()" />
                            <input id="btnsearch" type="submit"  class="ui teal right button" value="Search" /> 
                        </div>
                    </div>
                </form> 
                <script>
                    function formsubmit() {
                        btnsearch.disabled = "disabled";
                    }
                    function formfocus() {
                        btnsearch.disabled = undefined;
                    }
                </script>
            </div>
        </div>

        <div class="ui grid">
            <div class="ten wide column" style="max-width: 600px;">
                <% for (Page p : pages) {
                        String content = null;
                        if (pages.size() == 1 || p.keyWord == null) {
                            content = p.content.toString();
                        } else {
                            content = SearchResource.engine.getDesc(p.content.toString(), p.keyWord, 80);
                            if (content.length() < 200) {
                                content += p.description;
                            }
                            if (content.length() < 200) {
                                content += p.title;
                            }
                        }
                %>
                <h3>
                    <a class="stext" target="_blank"   href="<%=p.url%>" ><%= p.title%></a></h3> 
                <span class="stext"> <%=content%> </span>
                <% }%>


            </div>
            <div class="six wide column" style="max-width: 200px;">
                <%
                    String content = ((System.currentTimeMillis() - begin) / 1000.0) + "s, "
                            + "MEM:" + (java.lang.Runtime.getRuntime().totalMemory() / 1024 / 1024) + "MB ";
                %>
                <div class="ui segment">
                    <h4>Time</h4> 
                    <%= content%>
                </div>


            </div>
        </div>

    </body>
</html>


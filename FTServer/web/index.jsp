<%@page import="iBoxDB.fts.SDB"%>
<%@page import="iBoxDB.LocalServer.Box"%>
<%@page import="java.util.ArrayList"%>
<%@page import="iBoxDB.fts.SearchResource"%>
<%@page contentType="text/html" pageEncoding="UTF-8" session="false"%>
<%
    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache");
    response.setDateHeader("Expires", 0);
%>
<%
    ArrayList<String> discoveries = new ArrayList<String>();

    if (SDB.search_db != null) {
        Box box = SDB.search_db.cube();
        try {
            for (String skw : SearchResource.engine.discover(box, 'a', 'z', 4,
                    '\u2E80', '\u9fa5', 1)) {
                discoveries.add(skw);
            }
        } finally {
            box.close();
        }
    }
%>
<!DOCTYPE html>
<html>
    <head>        
        <meta http-equiv="content-type" content="text/html; charset=UTF-8">
        <meta name="description" content="iBoxDB NoSQL Database Full Text Search Server FTS">
        <title>Full Text Search Server</title>

        <link rel="stylesheet" type="text/css" href="css/semantic.min.css"> 

        <style>
            td{ 
                white-space:nowrap; 
                overflow: hidden
            }

            body {
                margin-top: 100px;
                overflow:hidden;
            }
            body > .grid {

            }

            .column {
                max-width: 60%;
            }

        </style> 

    </head>
    <body> 
        <div class="ui middle aligned center aligned grid">
            <div class="column"  >

                <h2 class="ui teal header" > 
                    <i class="disk outline icon" style="font-size:82px"></i> Full Text Search Server
                </h2>
                <form class="ui large form"  action="s"  onsubmit="formsubmit()"  >
                    <div class="ui label input">
                        <div class="ui action input">
                            <input name="q"  value=""  required onfocus="formfocus()" />
                            <input id="btnsearch" type="submit"  class="ui teal right button big" 
                                   value="Search"    /> 
                        </div> 
                    </div>
                </form>
                <script>
                    function formsubmit() {
                        document.getElementById('btnsearch').disabled = "disabled";
                    }
                    function formfocus() {
                        document.getElementById('btnsearch').disabled = undefined;
                    }
                </script>

                <div class="ui message" style="text-align: left">
                    Input [KeyWord] to search, input [URL] to index <br> 
                    Input [delete URL] to delete.
                    <br>
                    Recent Searches:<br>
                    <%
                        for (String str : SearchResource.searchList) {

                    %> <a href="s?q=<%=java.net.URLEncoder.encode(str)%>"><%=str%></a>. &nbsp;  
                    <%
                        }
                    %>

                    <br>Recent Records:<br>
                    <%
                        for (String str : SearchResource.urlList) {
                    %>
                    <a href="<%=str%>" target="_blank" ><%=str%></a>. <br> 
                    <%
                        }
                    %>

                    <br /><a  href="./">Refresh Discoveries</a>:&nbsp; 
                    <%
                        for (String str : discoveries) {
                    %> <a href="s?q=<%=java.net.URLEncoder.encode(str)%>"><%=str%></a>. &nbsp;  
                    <%
                        }
                    %>
                </div>

            </div>
        </div>

    </body>
    <!-- v0.9 -->
</html>
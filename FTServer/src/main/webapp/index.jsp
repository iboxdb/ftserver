<%@page import="ftserver.*"%>
<%@page import="java.util.*"%>
<%@page contentType="text/html" pageEncoding="UTF-8" session="false"%>
<%@include  file="_taghelper.jsp" %>
<%    long begin = System.currentTimeMillis();
%>
<!DOCTYPE html>
<html dir="auto">
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
                margin-top: 10px;
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
                <form class="ui large form"  action="s.jsp"  onsubmit="formsubmit()"  >
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

                <div class="ui message" style="text-align: left; font-size: 20px">
                    <a  href="admin.jsp" target="ADMIN_FTSERVER">Admin Pages (add http/https to server)</a><br>

                    <br>Recent Searches:<br>
                    <% for (PageSearchTerm pst : IndexPage.getSearchTerm(10)) {
                            String str = pst.keywords;
                            try ( Tag t = HTML.tag("a", "href:", "s.jsp?q=" + encode(str))) {
                                HTML.text(str);
                            }
                            HTML.text(", &nbsp; ");
                        }
                    %> 

                    <br>

                    <br><a  href="./">Refresh Discoveries:</a> &nbsp;  
                    <%
                        for (String str : IndexPage.discover()) {
                            try ( Tag t = HTML.tag("a", "href:", "s.jsp?q=" + encode(str))) {
                                HTML.text(str);
                            }
                            HTML.text(" &nbsp; ");
                        }

                        HTML.tag("br");
                    %>
                    <br>

                </div>
                <% HTML.text("Load Time: " + (System.currentTimeMillis() - begin) / 1000.0 + "s");
                %>
            </div>
        </div>

    </body>
    <!-- <%=version()%> -->
</html>
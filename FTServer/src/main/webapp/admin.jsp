<%@page import="ftserver.*"%> 
<%@page contentType="text/html" pageEncoding="UTF-8" session="false" %>
<%@include  file="_taghelper.jsp" %>

<%    String url = request.getParameter("url");
    String msg = request.getParameter("msg");
    if (url != null) {
        // when input "http://www.abc.com" 
        url = url.trim();
        boolean ishttp = false;
        if (url.startsWith("http://") || url.startsWith("https://")) {
            ishttp = true;
        }

        if (ishttp) {
            if (msg == null) {
                msg = "";
            }
            msg = msg.trim();

            final String furl = Html.getUrl(url);

            IndexPage.runBGTask(furl, msg);

            url = "BackgroundIndexRunning-SeeConsoleOutput";

        }
    }
    if (url == null) {
        url = "https://www.iboxdb.com/";
    }
%>

<!DOCTYPE html>
<html>
    <head>        
        <meta http-equiv="content-type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Index Page</title>

        <link rel="stylesheet" type="text/css" href="css/semantic.min.css"> 

        <style>
            body {
                margin-top: 10px;
                padding: 10px;
            }

            .grid{
                max-width: 100%;
                width:100%;
                text-align: center;
            }
            .column {
            }

        </style> 
    </head>
    <body> 
        <div class="grid">
            <div class="column" > 
                <form class="ui large form"  action="admin.jsp" onsubmit="formsubmit()" method="post">
                    <div class="ui label input">

                        <div class="ui action input">
                            <input name="url"  value="<%=url%>" required onfocus="formfocus()" />
                            <input id="btnsearch" type="submit"  class="ui teal right button" 
                                   value=" ADD "  /> 
                        </div>

                        <div class="ui">Description:</div>
                        <div class="ui action input">

                            <textarea name="msg" maxlength="800" height="200px"></textarea>
                        </div>

                    </div>
                </form> 

                <h3>Input HTTP or HTTPS  ://URL</h3>

                The indexing process will continue until all be done, check the Log for details,
                it needs bandwidth and time.
                Close the App can stop it. 
                <br>
                Some pages may not finish the Indexing process because the App has stopped, try re-add again. 
                <br>
                Some pages were designed to not be indexed, it would never show up.
                <br>
                Search Format:<br>
                [Word1 Word2 Word3] = text has <b>Word1</b> and <b>Word2</b> and <b>Word3</b><br>
                ["Word1 Word2 Word3"] = text has <b>"Word1 Word2 Word3"</b> as a whole<br>
                <br>
                <a href="./"><i class="teal disk outline icon" style="font-size:70px"></i></a>
                <script>
                    function formsubmit() {
                        document.getElementById('btnsearch').disabled = "disabled";
                        document.getElementById('btnsearch').value = "Loading";
                    }
                    function formfocus() {
                        document.getElementById('btnsearch').disabled = undefined;
                        document.getElementsByName('url')[0].value = "";
                    }
                </script>



            </div>
        </div>

        <%
            request.setAttribute("admin", true);
        %>

    </body>
</html>


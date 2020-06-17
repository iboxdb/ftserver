<%@page import="ftserver.*"%> 
<%@page contentType="text/html" pageEncoding="UTF-8" session="false" %>
<%@include  file="_taghelper.jsp" %>

<%    String url = request.getParameter("url");
    String msg = request.getParameter("msg");
    if (url != null) {
        // when input "http://www.abc.com" or "delete http://www.abc.com"
        url = url.trim();
        Boolean isdelete = null;
        if (url.startsWith("http://") || url.startsWith("https://")) {
            isdelete = false;
        } else if (url.startsWith("delete")) {
            url = url.substring(6).trim();
            if ((url.startsWith("http://") || url.startsWith("https://"))) {
                isdelete = true;
            }
        }

        if (isdelete != null) {
            if (isdelete) {
                synchronized (App.class) {
                    IndexPage.removePage(url);
                }
                url = "deleted";
            } else {
                if (msg == null) {
                    msg = "";
                }

                final String furl = Html.getUrl(url);
                final String fmsg = msg.trim();
                final String[] fresult = new String[]{"background running"};
                Thread t = new Thread(() -> {
                    synchronized (App.class) {
                        log("For:" + furl);
                        String rurl = IndexPage.addPage(furl, true);
                        IndexPage.backgroundLog(furl, rurl);
                        if (furl.equals(rurl) && fmsg.length() > 0) {

                            String ttitle = furl;
                            String tmsg = fmsg;
                            int pos = fmsg.indexOf('\n');
                            if (pos > 0) {
                                ttitle = fmsg.substring(0, pos);
                                tmsg = fmsg.substring(pos).trim();
                            }
                            IndexPage.addPageCustomText(furl, ttitle, tmsg);

                        }
                        fresult[0] = rurl;
                    }
                });
                IndexPage.waitingThread = t;
                t.start();
                t.join(3000);

                url = fresult[0];
            }
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
        <title>Administrator</title>

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
            .rt{
                color: red;
            }
            .gt{
                color: green;
            }
            .gtt{
                color: #4092cc;
            }
        </style> 
        <script>
            function highlight(loadedDivId) {

            }
            function sendlog(url, txt) {

            }
        </script>
    </head>
    <body > 
        <div class="ui left aligned grid">
            <div class="column"  style="max-width: 600px;"> 
                <h3>input HTTP or HTTPS  ://URL</h3>
                <form class="ui large form"  action="admin.jsp" onsubmit="formsubmit()" method="post">
                    <div class="ui label input">

                        <div class="ui action input">
                            <a href="./"><i class="teal add outline icon" style="font-size:42px"></i> </a>
                            <input name="url"  value="<%=url%>" required onfocus="formfocus()" />
                            <input id="btnsearch" type="submit"  class="ui teal right button" 
                                   value=" ADD PAGE "  /> 
                        </div>

                        <div class="ui">Informtion:</div>
                        <div class="ui action input">

                            <textarea name="msg" maxlength="200" height="200px"></textarea>
                        </div>

                    </div>
                </form> 
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

        <div class="ui grid">
            <div class="ten wide column" style="max-width: 600px;" id="maindiv">
                <% request.setCharacterEncoding("utf-8");
                    request.setAttribute("admin", true);%>
                <jsp:include page="spart.jsp" >
                    <jsp:param name="q" value="<%=url%>"></jsp:param>
                </jsp:include>

            </div>
            <div class="six wide column" style="max-width: 200px;">



            </div>
        </div>

    </body>
</html>


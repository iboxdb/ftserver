<%@page import="java.util.logging.Logger"%>
<%@page import="java.util.logging.Level"%>
<%@page import="ftserver.*"%>
<%@page contentType="text/html" pageEncoding="UTF-8" session="false" %>
<%@include  file="_taghelper.jsp" %>



<%    DelayService.delayIndex();
    final String queryString = request.getQueryString();

    String name = request.getParameter("q").replaceAll("<", "").trim();
    try {
        IndexPage.addSearchTerm(name);
    } catch (Throwable ex) {
        log(" Search " + ex.getMessage() + " " + name);
    }
%>

<!DOCTYPE html>
<html>
    <head>        
        <meta http-equiv="content-type" content="text/html; charset=UTF-8">
        <meta name="description" content="<%=name.replaceAll("\"", "")%> what is? iBoxDB NoSQL Document Database Full Text Search FTS">
        <title><%=name.replaceAll("\"", "")%>, what is? iBoxDB NoSQL Document Database Full Text Search</title>

        <link rel="stylesheet" type="text/css" href="css/semantic.min.css"> 

        <style>
            body {
                margin-top: 10px;
                margin-left: 10px;
                font-weight:lighter;
                overflow-x: hidden;
                
            }
            .stext{
               font-size: 22px; 
            }
            .stext_s{
               font-size: 18px; 
            }
            .spartcss{
                white-space: normal;
                overflow: visible;
                text-overflow:ellipsis;
                font-weight: bold;
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
            .kw{
                background-color: #eee;
            }
            .rtl{
                text-align: initial !important;
            }
        </style> 
        <script>
            var extitle = "";
            function highlight(loadedDivId) {

                var txt = document.title.substr(0, document.title.indexOf(','));
                txt += (" " + extitle);
                var div = document.getElementById(loadedDivId);
                var ts = div.getElementsByClassName("stext");

                var kws = txt.split(/[ 　]/);
                for (var i = 0; i < kws.length; i++) {
                    var kw = String(kws[i]).trim();
                    if (kw.length < 1) {
                        continue;
                    }
                    var fontText = "<font class='rt'>";
                    if (fontText.indexOf(kw.toLowerCase()) > -1) {
                        continue;
                    }
                    if ("</font>".indexOf(kw.toLowerCase()) > -1) {
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
        <script>
            var div_load = null;
            document.addEventListener("scroll", function () {
                scroll_event();
            });
            function onscroll_loaddiv(divid, startId) {
                div_load = document.getElementById(divid);
                div_load.startId = startId;
                scroll_event();
            }
            function scroll_event() {
                if (div_load !== null) {
                    var top = div_load.getBoundingClientRect().top;
                    var se = document.documentElement.clientHeight;

                    top = top - 1000;
                    if (top <= se) {
                        var startId = div_load.startId;
                        div_load = null;
                        var xhr = new XMLHttpRequest();
                        xhr.onreadystatechange = function () {
                            if (xhr.readyState == XMLHttpRequest.DONE) {
                                var html = xhr.responseText;

                                var frag = document.createElement("div");
                                frag.innerHTML = html;
                                var maindiv = document.getElementById('maindiv');
                                maindiv.appendChild(frag);

                                var ss = frag.getElementsByTagName("script");
                                for (var i = 0; i < ss.length; i++) {
                                    eval(ss[i].innerHTML);
                                }
                            }
                        }
                        var url = "spart.jsp?<%= queryString%>" + "&s=" + startId;
                        xhr.open('GET', url, true);
                        xhr.send(null);
                    }
                }
            }
            function sendlog(url, txt) {
                if (url) {
                    try {
                        var xhr = new XMLHttpRequest();
                        url = "log.jsp?url=" + window.escape(url) + "&txt=" + window.escape(txt);
                        xhr.open('GET', url, true);
                        xhr.send(null);
                    } catch (e) {
                    }
                }
                return true;
            }
        </script>
    </head>
    <body > 
        <div class="ui left aligned grid">
            <div class="column"  style="max-width: 800px;"> 
                <form class="ui large form"  action="s.jsp" onsubmit="formsubmit()">
                    <div class="ui label input">

                        <div class="ui action input">
                            <a href="./"><i class="teal disk outline icon" style="font-size:60px"></i> </a>
                            <input name="q" class="large" value="<%=name.replaceAll("\"", "&quot;")%>" required onfocus="formfocus()" />
                            <input id="btnsearch" type="submit"  class="ui teal right button large" value="Search" /> 
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
            </div>
        </div>

        <div class="rtl ui grid">
            <div class="fontsize ten wide column" style="max-width: 800px;" id="maindiv">
                <jsp:include page="spart.jsp" ></jsp:include>

            </div>
            <div class="six wide column" style="max-width: 80px;">

                <div class="ui segment">
                    <h4>FTS</h4> 
                </div> 


            </div>
        </div>

    </body>
</html>


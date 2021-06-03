<%@page import="java.util.logging.Logger"%>
<%@page import="java.util.logging.Level"%>
<%@page import="ftserver.*"%>
<%@page contentType="text/html" pageEncoding="UTF-8" session="false" %>
<%@include  file="_taghelper.jsp" %>



<%    final String queryString = request.getQueryString();

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
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <meta name="description" content="<%=name.replaceAll("\"", "")%> what is? iBoxDB NoSQL Document Database Full Text Search FTS">
        <title><%=name.replaceAll("\"", " ")%>, what is? iBoxDB NoSQL Document Database Full Text Search</title>

        <link rel="stylesheet" type="text/css" href="css/semantic.min.css"> 

        <style>
            body {
                margin-top: 10px;
                padding: 20px;
            }
            .grid{
                max-width: 100%;
                width:100%;
                text-align: center;
            }
            .column {
            }
            .stext{
                font-size: 22px; 
            }
            .stext_s{
                font-size: 18px; 
            }
            .spartcss{
                font-weight: bold;
            }
            .rt{
                color: red;
                font-weight: bolder;
            }
            .rt2{
                font-weight:bolder;
                font-style: italic;
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
            #maindiv{
                overflow-x: hidden !important;
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
                var kwsDo = {};
                for (var i = 0; i < kws.length; i++) {
                    var kw = String(kws[i]).trim();
                    if (kw.length < 1) {
                        continue;
                    }

                    if (kwsDo[kw]) {
                        continue;
                    }
                    kwsDo[kw] = kw;
                    var isword = kw.charCodeAt(0) < 0x3040;
                    if (kw.length < 3 && isword) {
                        kw = " " + kw + " ";
                    }
                    var fontText = isword ? "<font class='rt2'>" : "<font class='rt'>";


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

                    top = top - 500;
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
    <body> 

        <div class="grid">
            <div class="column" > 
                <form class="ui large form"  action="s.jsp" onsubmit="formsubmit()">
                    <div class="ui label input">

                        <div class="ui action input">

                            <input name="q" class="large" value="<%=name.replaceAll("\"", "&quot;")%>" required onfocus="formfocus()"  dir="auto" />
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

        <div class="grid">
            <div class="rtl column" id="maindiv">
                <br>
                <jsp:include page="spart.jsp" ></jsp:include>
            </div>
        </div>

        <div class="grid" >
            <div class="column" >
                <a href="./"><i class="teal disk outline icon" style="font-size:70px"></i></a>
            </div>
        </div>            
    </body>
</html>


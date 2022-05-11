
<%@page import="ftserver.*"%>
<%@page contentType="text/html" pageEncoding="UTF-8" session="false" %>
<%@include  file="_taghelper.jsp" %>



<%    final String queryString = request.getQueryString();
    String s = request.getParameter("q");
    //System.out.println(s);
    if (s == null) {
        return;
    }
    String name = s.replaceAll("<", "").trim();
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
                font-size: 20px; 
            }
            .stext_s{
                font-size: 18px; 
            }
            .spartcss{

            }
            B{
                color: red;
                font-weight: bolder;
            }
            I{
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
            var splitHelperPage = "s.jsp?q=";
            function splitHelper() {
                try {
                    var html = "";
                    var splitWord = [" of ", " a ", " to ", "的"];
                    var sh = document.getElementById("searchHelp");
                    var q = document.getElementsByName("q")[0];

                    var value = new String(q.value).trim();
                    if (value.length > 1 && value.indexOf(" ") > 0 && value.indexOf('"') < 0
                            && value.charCodeAt(0) < 0x3040)
                    {
                        value = "\"" + value + "\"";
                        var url = splitHelperPage + encodeURI(value);
                        html += "<a href='" + url + "' >" + value + ";</a> "
                    }

                    value = new String(q.value).trim();
                    for (var i = 0; i < splitWord.length; i++) {
                        value = value.replace(splitWord[i], " ");
                    }
                    value = value.trim();

                    if (value != String(q.value)) {
                        var url = splitHelperPage + encodeURI(value);
                        html += "<a href='" + url + "' >" + value + ";</a> "
                    }

                    sh.innerHTML = html;
                } catch (e) {
                }
            }
        </script>
        <script>
            var multiTexts = {};
            function hideMultiText(loadedDivId) {
                var div = document.getElementById(loadedDivId);
                var es = div.getElementsByTagName("div");
                for (var i = 0; i < es.length; i++) {
                    var hurl = es[i].getAttribute("hurl");
                    if (hurl) {
                        if (multiTexts[hurl]) {
                            //console.log("hurl hide " + hurl);
                            es[i].style.display = "none";
                        } else {
                            multiTexts[hurl] = hurl;
                            es[i].style.display = "";
                        }
                    }
                }
            }
        </script>
        <script>
            var extitle = "";
            function highlight(loadedDivId) {

                var txt = document.title.substr(0, document.title.indexOf(','));
                txt += (" " + extitle);
                var div = document.getElementById(loadedDivId);
                var ts = div.getElementsByClassName("stext");
                var tshtml = [];
                for (var j = 0; j < ts.length; j++) {
                    var html = ts[j].innerHTML;
                    tshtml[j] = " " + html + " ";
                }

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
                    var kwreg = kw;
                    var kwreg2 = kw;
                    var isword = kw.charCodeAt(0) < 0x3040;
                    if (isword) {
                        kwreg = "(\\s+)(" + kw + ")(\\W+)";
                        kwreg2 = "$1<i>$2</i>$3";
                    } else {
                        kwreg = "(.+)(" + kw + ")(.+)";
                        kwreg2 = "$1<b>$2</b>$3";
                    }

                    for (var j = 0; j < ts.length; j++) {
                        var html = tshtml[j];
                        tshtml[j] =
                                html.replace(new RegExp(kwreg, 'gi'), kwreg2);
                    }
                }
                for (var j = 0; j < ts.length; j++) {
                    var html = tshtml[j];
                    ts[j].innerHTML = html;
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
                /*
                 if (url) {
                 try {
                 var xhr = new XMLHttpRequest();
                 url = "log.jsp?url=" + window.escape(url) + "&txt=" + window.escape(txt);
                 xhr.open('GET', url, true);
                 xhr.send(null);
                 } catch (e) {
                 }
                 }
                 */
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

                            <input name="q" class="large" value="<%=name.replaceAll("\"", "&quot;")%>" required 
                                   onfocus="formfocus()"  dir="auto" 
                                   onchange="splitHelper()" />
                            <input id="btnsearch" type="submit"  class="ui teal right button large" value="Search" /> 
                        </div>
                    </div>
                </form> 
                <div style="text-align:left" id="searchHelp"></div>
                <script>
                    function formsubmit() {
                        document.getElementById('btnsearch').disabled = "disabled";
                    }
                    function formfocus() {
                        document.getElementById('btnsearch').disabled = undefined;
                    }
                    splitHelper();
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


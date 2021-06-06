<%@page import="ftserver.App"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@include  file="../_taghelper.jsp" %>

<%    //SetHead Cookie
    Cookie[] reqCookies = request.getCookies();
    if (reqCookies == null) {
        reqCookies = new Cookie[0];
    }
    Cookie testCookie = new Cookie("test", "0");
    for (Cookie c : reqCookies) {
        if (testCookie.getName().equalsIgnoreCase(c.getName())) {
            testCookie = c;
        }
    }

    testCookie = new Cookie(testCookie.getName(), Integer.toString(Integer.valueOf(testCookie.getValue()) + 1));
    testCookie.setMaxAge(Integer.MAX_VALUE);
    response.addCookie(testCookie);
%>
<%
    Object o = application.getAttribute("android_submit");
    if (o == null) {
        o = 0;
    }
    o = ((int) o + 1);
    request.setAttribute("android_submit", o);
    o = request.getAttribute("android_submit");
    application.setAttribute("android_submit", o);
%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Android JSP Test</title>
    </head>
    <body>
        <h1>GET</h1>
        <form target="submit.jsp" method="get">
            <input type="text"  name="ip1" /><br>
            <textarea name="tt1" rows="10" cols="30"></textarea><br>
            <input type="submit"  name="s1" />
        </form>

        <h1>POST</h1>
        <form target="submit.jsp" method="post">
            <input type="text"  name="ip1" /><br>
            <textarea name="tt1" rows="10" cols="30"></textarea><br>
            <input type="submit"  name="s2" />
        </form>

        <%= request.getParameter("ip1")%>
        <br>
        <%= request.getParameter("tt1")%>

        <hr> Cookies : <br>

        <%
            for (Cookie c : reqCookies) {
                out.println(c.getName() + " - " + c.getValue() + "<br>");
                if (testCookie.getName().equalsIgnoreCase(c.getName())) {
                    testCookie = c;
                }
            }
        %>
        <hr> Attribute : <br>
        <%= o%>
        <hr>
        <jsp:include page="inc.jsp" >
            <jsp:param name="pass1"  value= "PASS-PASS-01" />
            <jsp:param name="pass2"  value= "PASS02-数据库引擎" />
        </jsp:include> 
        <hr>
        <%
            if (App.IsAndroid) {
                Object o2 = Class.forName("ftserver.android.App");
                try (Tag h = HTML.tag("h3")) {
                    h.text(o2.toString());
                }
            }
        %>
    </body>    
</html>

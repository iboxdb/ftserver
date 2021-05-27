<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    //SetHead Cookie
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
            <textarea name="tt1" rows="5" cols="100"></textarea><br>
            <input type="submit"  name="s1" />
        </form>

        <h1>POST</h1>
        <form target="submit.jsp" method="post">
            <input type="text"  name="ip1" /><br>
            <textarea name="tt1" rows="5" cols="100"></textarea><br>
            <input type="submit"  name="s2" />
        </form>

        <%= request.getParameter("ip1")%>
        <br>
        <%= request.getParameter("tt1")%>
        <br> Cookies : <br>

        <%
            for (Cookie c : reqCookies) {
                out.println(c.getName() + " - " + c.getValue() + "<br>");
                if (testCookie.getName().equalsIgnoreCase(c.getName())) {
                    testCookie = c;
                }
            }
        %>
        <br> Attribute : <br>
        <%= o%>
    </body>    
</html>

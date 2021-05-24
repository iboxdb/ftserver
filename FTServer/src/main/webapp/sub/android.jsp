<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Android Test</title>
    </head>
    <body>
        <%
            Object o = application.getAttribute("RuntimeEnv");
        %>
        <h1><%= o%></h1>
        <%
            Object o2 = Class.forName("ftserver.android.WebApp");
        %>
        <h1><%= o2%></h1>
    </body>
</html>

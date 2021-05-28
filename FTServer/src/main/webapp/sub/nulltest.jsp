<%-- 
    Document   : nulltest
    Created on : May 28, 2021, 9:05:30 AM
    Author     : user
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h1>Hello World!</h1>
        <%
            String s = null;
            s = s.substring(10);
        %>
        <%=s%>
    </body>
</html>

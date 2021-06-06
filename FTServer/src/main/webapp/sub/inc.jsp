<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h1>Hello World!</h1>
        <h1><%= request.getParameter("pass1")%></h1>
        <h1><%= request.getParameter("pass2")%></h1>
        
        <%= request.getParameter("ip1")%>
        <br>
        <%= request.getParameter("tt1")%>

    </body>
</html>

<%@page import="FTServer.Html"%>
<%@page contentType="text/html" pageEncoding="UTF-8" session="false"%>
<%
    response.setHeader("Cache-Control", "non-cache, no-store, must-revalidate");
%> 

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="description" content="iBoxDB NoSQL Database Full Text Search Server FTS">
        <title>About</title>
    </head>
    <body>
        <h1>FTServer</h1>
        <h2>Full Text Search Server</h2>
        <h3>Version : 1.0</h3>
        <h3>Build : Netbeans 8.2</h3>
        <h3>Free to Modify</h3>
        <h5>Time: <%= new java.util.Date()%></h5>

    </body>
</html>

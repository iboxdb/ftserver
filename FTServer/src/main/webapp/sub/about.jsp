<%@page contentType="text/html" pageEncoding="UTF-8" session="false"%>
<%@include  file="../_taghelper.jsp" %>


<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="description" content="iBoxDB NoSQL Database Full Text Search Server FTS">
        <meta name="keywords" content="nosql iboxdb,fts.server">        
        <title>About iBoxDB FTServer</title>
    </head>
    <body>
        <h1>FTServer</h1>
        <h2>Full Text Search Server</h2>
        <h3>Version : <%=version()%></h3>
        <h3>Build : Netbeans 12</h3>
        <h3>Free to Modify</h3>
        <h5>Time: <%= new java.util.Date()%></h5>

        <%
            //System Encoding UTF-8
            //String str = sun.security.action.GetPropertyAction.privilegedGetProperty("file.encoding");
            //out.println( "System Encoding " + str );
        %>
    </body>
</html>

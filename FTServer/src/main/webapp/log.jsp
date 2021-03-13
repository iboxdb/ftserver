<%@page import="java.util.logging.*"%>
<%@page import="ftserver.*"%> 
<%@page contentType="text/html" pageEncoding="UTF-8" session="false"%>
<%@include  file="_taghelper.jsp" %>

<%    String url = request.getParameter("url");
    String txt = request.getParameter("txt");
    if (url == null) {
        url = "";
    }
    if (txt == null) {
        txt = "";
    }
%>

<%=url%>
<%=txt%>

<%@page import="FTServer.AppServlet"%>
<%@page contentType="text/html" pageEncoding="UTF-8" session="false"%>

<%!
    static java.util.concurrent.ConcurrentHashMap<String, Integer> clicks
            = new java.util.concurrent.ConcurrentHashMap< String, Integer>();
%>
<%
    String url = request.getParameter("url");
    String txt = request.getParameter("txt");
%>

<%
    if (url.contains("localhost")) {
        return;
    }

    // change the rule by yourself.
    if (clicks.size() > 10000) {
        clicks.clear();
    }
    Integer c = clicks.get(url);
    if (c == null) {
        c = 0;
    }
    c++;
    if (c > 3) {
        clicks.remove(url);
        AppServlet.waitingUrlList.add(url);
        AppServlet.runBGTask();
        out.println(url);
    } else {
        clicks.put(url, c);
    }
%>

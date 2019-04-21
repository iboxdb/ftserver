<%@page import="java.util.logging.*"%>
<%@page import="ftserver.*"%> 
<%@page contentType="text/html" pageEncoding="UTF-8" session="false"%>

<%!
    static java.util.concurrent.ConcurrentHashMap<String, Integer> clicks
            = new java.util.concurrent.ConcurrentHashMap< String, Integer>();
%>
<%
    String url = request.getParameter("url");
    String txt = request.getParameter("txt");
    if (url == null) {
        url = "";
    }
    if (txt == null) {
        txt = "";
    }
%>

<%
    // change the re-index rule by yourself.
    if (clicks.size() > 1000) {
        clicks.clear();
    }
    Integer c = clicks.get(url);
    if (c == null) {
        c = 0;
    }
    c++;
    if (c >= 3) {
        clicks.remove(url);

        final String furl = url;
        IndexPage.WRITE_ES.submit(new Runnable() {
            @Override
            public void run() {
                Logger.getLogger(App.class.getName()).log(Level.INFO, furl);
                try {
                    //RE-INDEX, move page forward
                    //IndexAPI.indexText(furl, false, null);
                    //Logger.getLogger(App.class.getName()).log(Level.INFO, "RE-Indexed:" + furl);
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
        });

    } else {
        clicks.put(url, c);
    }
%>
<%=url%>
<%= c%>

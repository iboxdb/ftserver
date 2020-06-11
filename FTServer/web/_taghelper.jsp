<%@page contentType="text/html" pageEncoding="UTF-8" session="false"%>
<%@page import="javax.servlet.http.*" %>
<%@page import="javax.servlet.jsp.*" %>
<%@page import="java.io.*" %>
<%@page import="java.util.*" %>
<%
    response.setHeader("Cache-Control", "non-cache, no-store, must-revalidate");
    
    jspWriter.set(out);
%>
<%!    
    private static ThreadLocal<JspWriter> jspWriter = new ThreadLocal<JspWriter>();
    
    private class Tag implements Closeable {
        
        private String name;
        
        public Tag(String _name, Map<String, Object> attributes) {
            this.name = _name;
            
            text("<");
            text(name);
            if (attributes != null) {
                for (Map.Entry<String, Object> e : attributes.entrySet()) {
                    text(" ");
                    text(e.getKey());
                    text("=\"");
                    text(e.getValue().toString());
                    text("\"");
                }
            }
            text(">");
            
        }
        
        @Override
        public void close() {
            text("</");
            text(name);
            text(">");
        }
        
    }
    
    public Tag tag(String name) {
        return tag(name, (Object[]) null);
    }
    
    public Tag tag(String name, Object... ason) {
        Map<String, Object> map = null;
        if (ason != null) {
            map = (Map<String, Object>) iBoxDB.LocalServer.Ason.ason(ason);
        }
        return new Tag(name, map);
    }
    
    public void text(String text) {
        try {
            jspWriter.get().print(text);
        } catch (Throwable ex) {
            
        }
    }
    
    public void freeWriter() {
        jspWriter.set(null);
    }
    
    public String encode(String text) {
        return java.net.URLEncoder.encode(text);
    }
    
    public String version() {
        return "1.3.1";
    }

%>
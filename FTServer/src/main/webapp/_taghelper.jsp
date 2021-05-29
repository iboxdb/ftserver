<%@page import="ftserver.App"%>
<%@page contentType="text/html" pageEncoding="UTF-8" session="false"%>

<%@page import="java.io.*" %>
<%@page import="java.util.*" %>
<%@page import="iboxdb.localserver.*" %>

<%
    response.setHeader("Cache-Control", "non-cache, no-store, must-revalidate");
    request.setCharacterEncoding("UTF-8");
    HTMLClass HTML = new HTMLClass(out);
%>
<%!    
    public static abstract class Tag implements Closeable {
        
    }
    
    public static class HTMLClass {
        
        Writer jspWriter;
        
        private HTMLClass(Writer _jspWriter) {
            jspWriter = _jspWriter;
        }
        
        public void text(String text) {
            try {
                jspWriter.write(text);
            } catch (Throwable ex) {
                
            }
        }
        
        public class TagImpl extends Tag {
            
            private String name;
            
            public TagImpl(String _name, Map<String, Object> attributes) {
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
                map = (Map<String, Object>) Ason.ason(ason);
            }
            return new TagImpl(name, map);
        }
    }
    
    public String encode(String text) throws UnsupportedEncodingException {
        return java.net.URLEncoder.encode(text, "UTF-8");
    }
    
    public void log(String msg) {
        App.log(msg);
    }
    
    public String version() {
        return "1.8";
    }

%>
<%@page import="java.io.RandomAccessFile"%>
<%@page contentType="text/html" pageEncoding="UTF-8" session="false"%>
<%@include  file="_taghelper.jsp" %>

<%!    private static String[] books = null;

    private static final Random ran = new Random();
    //UTF-8 Text
    //private static String book1_path = "175315.txt";
    //private static String book2_path = "phoenix.txt";
    private static String book1_path = "/home/user/github/hero.txt";
    private static String book2_path = "/home/user/github/phoenix.txt";

%>

<%    if (books == null) {
        try {
            String[] tmp = new String[2];

            RandomAccessFile rf = new RandomAccessFile(book1_path, "r");
            byte[] bs = new byte[(int) rf.length()];
            rf.readFully(bs);
            rf.close();
            tmp[0] = new String(bs);

            rf = new RandomAccessFile(book2_path, "r");
            bs = new byte[(int) rf.length()];
            rf.readFully(bs);
            rf.close();
            tmp[1] = new String(bs);

            books = tmp;
        } catch (Throwable e) {
            out.println("No Book");
            return;
        }
    }
%>


<%
    double base = 100.0;

    String b = request.getParameter("book");
    String s = request.getParameter("start");
    String l = request.getParameter("length");

    if (b == null) {
        b = "0";
    }
    if (s == null) {
        s = "0";
        l = "4040";
    }

    int book = Integer.parseInt(b);
    double start = Double.parseDouble(s) / base;
    int length = Integer.parseInt(l);

    int startIndex = (int) (start * books[book].length());
    int endIndex = startIndex + length;
    if (endIndex > books[book].length()) {
        endIndex = books[book].length();
    }

    String content = books[book].substring(startIndex, endIndex);

    String title = content.length() > 100 ? content.substring(0, 100) : content;
    String description = content.length() > 300 ? content.substring(100, 300) : content;
    String text = content.length() > 500 ? content.substring(300) : content;
    String keywords = "keyword1 keywords2,keyword3 hello";

    title = title.replaceAll("\t|\r|\n|\"", "");
    description = description.replaceAll("\t|\r|\n|\"", "");

    if (book == 0) {
        text += "  " + new Date();
    }

    boolean nodesc = ran.nextInt(5) == 0;
    if (nodesc) {
        description = "";
    }
%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title><%= title%></title>
        <meta name="description" content="<%=description%>">
        <meta name="keywords" content="<%=keywords%>">        

    </head>
    <body>
        <h1>Hello World!</h1>
        <p><%= text%></p>


        <%
            HTML.tag("br");
            for (int i = 0; i < 1100; i++) {
                book = ran.nextInt(books.length);
                start = ran.nextInt((int) base);
                length = ran.nextInt(600) * 100 + 100;
                
                length = ran.nextInt(6000);
                String url = "book.jsp?book=" + book + "&start=" + start + "&length=" + length;

                try (Tag t = HTML.tag("a", "href:", url)) {
                    HTML.text(" &nbsp; ");
                }
                HTML.tag("br");
            }
        %>
    </body>
</html>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h1>GET</h1>
        <form target="submit.jsp" method="get">
            <input type="text"  name="ip1" /><br>
            <textarea name="tt1" rows="5" cols="100"></textarea>
            <input type="submit"  name="s1" />
        </form>

        <h1>POST</h1>
        <form target="submit.jsp" method="post">
            <input type="text"  name="ip1" /><br>
            <textarea name="tt1" rows="5" cols="100"></textarea>
            <input type="submit"  name="s2" />
        </form>

        <%= request.getParameter("ip1")%>
        <br>
        <%= request.getParameter("tt1") %>

    </body>    
</html>

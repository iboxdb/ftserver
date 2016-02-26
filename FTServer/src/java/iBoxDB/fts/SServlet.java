package iBoxDB.fts;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "SServlet", urlPatterns = {"/s"}, asyncSupported = true)
public class SServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String name = request.getParameter("q");

        if (name.length() > 500) {
            return;
        }
        name = name.replaceAll("<", " ").replaceAll(">", " ")
                .replaceAll("\"", " ").replaceAll(",", " ")
                .replaceAll("\\$", " ").trim();

        final AsyncContext ctx = request.startAsync(request, response);
        ctx.setTimeout(30 * 1000);
        request.setAttribute("q", name);

        Boolean isdelete = null;

        if (name.startsWith("http://") || name.startsWith("https://")) {
            isdelete = false;
        } else if (name.startsWith("delete")
                && (name.contains("http://") || name.contains("https://"))) {
            isdelete = true;
        }
        if (isdelete == null) {
            SearchResource.searchList.add(name);
            while (SearchResource.searchList.size() > 15) {
                SearchResource.searchList.remove();
            }
            getReadES(name).submit(new Runnable() {
                @Override
                public void run() {
                    ctx.dispatch("/s.jsp");
                }
            });
        } else {
            final boolean del = isdelete;
            getWriteES(name).submit(new Runnable() {
                @Override
                public void run() {
                    ctx.getRequest().setAttribute("q", SearchResource.indexText(
                            ctx.getRequest().getAttribute("q").toString(), del));
                    ctx.dispatch("/s.jsp");
                }
            });
        }

    }

    private final ExecutorService[] readES = new ExecutorService[]{
        Executors.newSingleThreadExecutor(), Executors.newSingleThreadExecutor()
    };

    private ExecutorService getReadES(Object key) {
        return readES[Math.abs(key.hashCode()) % readES.length];
    }

    private final ExecutorService[] writeES = new ExecutorService[]{
        Executors.newSingleThreadExecutor(), Executors.newSingleThreadExecutor()
    };

    private ExecutorService getWriteES(Object key) {
        return writeES[Math.abs(key.hashCode()) % writeES.length];
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}

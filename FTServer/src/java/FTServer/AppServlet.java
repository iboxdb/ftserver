package FTServer;

import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "AppServlet", urlPatterns = {"/s"}, asyncSupported = true)
public class AppServlet extends HttpServlet {

    public static ConcurrentLinkedDeque<String> searchList
            = new ConcurrentLinkedDeque<String>();

    public static ConcurrentLinkedDeque<String> urlList
            = new ConcurrentLinkedDeque<String>();

    public static ConcurrentLinkedDeque<String> waitingUrlList
            = new ConcurrentLinkedDeque<String>();

    private final static int SLEEP_TIME = 2000;
    public static Throwable lastEx;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        final String queryString = request.getQueryString();
        String name = java.net.URLDecoder.decode(queryString, "UTF-8");
        name = name.trim();
        name = name.substring(2);
        name = name.trim();

        //String name = request.getParameter("q");
        if (name.length() > 500) {
            return;
        }

        Boolean isdelete = null;
        Boolean isfullrecord = null;

        if (name.startsWith("http://") || name.startsWith("https://")) {
            isdelete = false;
            isfullrecord = name.contains(" full");
        } else if (name.startsWith("delete")
                && (name.contains("http://") || name.contains("https://"))) {
            isdelete = true;
        }
        if (isdelete == null) {
            // just Query
            searchList.add(name.replaceAll("<", ""));
            while (searchList.size() > 15) {
                searchList.remove();
            }
            response.sendRedirect("s.jsp?" + queryString);

        } else {
            // when input "http://www.abc.com" or "delete http://www.abc.com"
            final String url = Page.getUrl(name);
            final boolean del = isdelete;
            final boolean full = isfullrecord != null && isfullrecord.booleanValue();

            final AsyncContext ctx = request.startAsync(request, response);
            ctx.setTimeout(30 * 1000);
            writeES.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (lastEx != null) {
                            return;
                        }

                        HashSet<String> subUrls
                                = del || (!full) ? null : new HashSet<String>();

                        SearchResource.indexText(url, del, subUrls);
                        urlList.add(url.replaceAll("<", ""));
                        while (urlList.size() > 3) {
                            urlList.remove();
                        }
                        if (subUrls != null) {
                            subUrls.remove(url);
                            subUrls.remove(url + "/");
                            subUrls.remove(url.substring(0, url.length() - 1));
                            if (subUrls.size() > 0) {
                                for (String url : subUrls) {
                                    waitingUrlList.add(url);
                                }
                                runBGTask();
                            }
                        }

                        ((HttpServletResponse) ctx.getResponse()).sendRedirect("s.jsp?q=" + java.net.URLEncoder.encode(url));
                    } catch (Throwable ex) {
                        lastEx = ex;
                    } finally {
                        ctx.complete();
                    }
                }
            });
        }

    }

    public static void runBGTask() {
        writeESBG.submit(new Runnable() {
            @Override
            public void run() {
                for (String url : waitingUrlList) {
                    if (lastEx != null) {
                        return;
                    }
                    try {
                        Thread.sleep(SLEEP_TIME);
                        if (url != null) {
                            System.out.println(url);
                            SearchResource.indexText(url, false, null);
                        }
                    } catch (Throwable ex) {
                        lastEx = ex;
                        Logger.getLogger(AppServlet.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                waitingUrlList.clear();
            }
        });
    }

    //index thread
    private final static ExecutorService writeES = Executors.newSingleThreadExecutor();
    //background index thread
    private final static ExecutorService writeESBG = Executors.newSingleThreadExecutor();

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

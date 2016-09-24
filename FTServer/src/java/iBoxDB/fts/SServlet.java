package iBoxDB.fts;

import static iBoxDB.fts.SearchResource.urlList;
import java.io.IOException;
import java.util.HashSet;
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

@WebServlet(name = "SServlet", urlPatterns = {"/s"}, asyncSupported = true)
public class SServlet extends HttpServlet {

    private final static int SleepTime = 2000;
    public static Throwable lastEx;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        final String queryString = request.getQueryString();
        String name = java.net.URLDecoder.decode(queryString, "UTF-8");
        name = name.substring(2);
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
            SearchResource.searchList.add(name.replaceAll("<", ""));
            while (SearchResource.searchList.size() > 15) {
                SearchResource.searchList.remove();
            }
            response.sendRedirect("s.jsp?" + queryString);

        } else {
            final String url = BPage.getUrl(name);
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

                        synchronized (writeESBG) {
                            HashSet<String> subUrls
                                    = del || (!full) ? null : new HashSet<String>();
                            SearchResource.indexText(url, del, subUrls);
                            SearchResource.urlList.add(url.replaceAll("<", ""));
                            while (SearchResource.urlList.size() > 3) {
                                SearchResource.urlList.remove();
                            }
                            if (subUrls != null) {
                                subUrls.remove(url);
                                subUrls.remove(url + "/");
                                subUrls.remove(url.substring(0, url.length() - 1));
                                if (subUrls.size() > 0) {
                                    for (String url : subUrls) {
                                        SearchResource.waitingUrlList.add(url);
                                    }
                                    addBGTask();
                                }
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

    public static void addBGTask() {
        writeESBG.submit(new Runnable() {
            @Override
            public void run() {
                for (String url : SearchResource.waitingUrlList) {
                    if (lastEx != null) {
                        return;
                    }
                    try {
                        Thread.sleep(SleepTime);
                        System.gc();
                        if (url != null) {
                            System.out.println(url);
                            SearchResource.indexText(url, false, null);
                        }
                        System.gc();
                    } catch (Throwable ex) {
                        lastEx = ex;
                        Logger.getLogger(SServlet.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                SearchResource.waitingUrlList.clear();
            }
        });
    }

    private final ExecutorService writeES = Executors.newSingleThreadExecutor();
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

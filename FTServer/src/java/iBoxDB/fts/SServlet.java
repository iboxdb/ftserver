package iBoxDB.fts;

import iBoxDB.LocalServer.Box;
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

    public static int SleepTime = 2000;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String name = java.net.URLDecoder.decode(request.getQueryString(), "UTF-8");
        name = name.substring(2);
        //String name = request.getParameter("q");

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
            final String url = BPage.getUrl(name);
            final boolean del = isdelete;
            writeES.submit(new Runnable() {
                @Override
                public void run() {
                    HashSet<String> subUrls = new HashSet<String>();
                    ctx.getRequest().setAttribute("q", SearchResource.indexText(
                            url, del, subUrls));
                    ctx.getRequest().setAttribute("index", true);
                    subUrls.remove(url);
                    if (subUrls.size() > 0) {
                        try (Box box = SDB.search_db.cube()) {
                            for (String url : subUrls) {
                                BURL burl = new BURL();
                                burl.id = box.newId(1, 1);
                                burl.url = url;
                                box.d("URL").insert(burl);
                            }
                            box.commit().Assert();
                        }
                        addBGTask();
                    }
                    ctx.dispatch("/s.jsp");
                }
            });
        }

    }

    private void addBGTask() {
        writeESBG.submit(new Runnable() {
            @Override
            public void run() {
                Iterable<BURL> urls
                        = SDB.search_db.select(BURL.class, "FROM URL ORDER BY id LIMIT 0,1");
                for (BURL burl : urls) {
                    SearchResource.indexText(burl.url, false, null);
                    SDB.search_db.delete("URL", burl.id);
                    addBGTask();
                    //System.out.println(burl.url);
                }

                try {
                    Thread.sleep(SleepTime);
                } catch (InterruptedException ex) {
                    Logger.getLogger(SServlet.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });
    }

    private final ExecutorService[] readES = new ExecutorService[]{
        Executors.newSingleThreadExecutor(), Executors.newSingleThreadExecutor()
    };

    private ExecutorService getReadES(Object key) {
        return readES[Math.abs(key.hashCode()) % readES.length];
    }

    private final ExecutorService writeES = Executors.newSingleThreadExecutor();
    private final ExecutorService writeESBG = Executors.newSingleThreadExecutor();

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

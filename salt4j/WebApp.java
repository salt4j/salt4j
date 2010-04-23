package salt4j;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.thread.QueuedThreadPool;

abstract public class WebApp extends HttpServlet {
    final String host; final int port;
    public WebApp(String host, int port) { this.host = host; this.port = port; }
    public WebApp() { this("localhost", 12345); }
    
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    abstract protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException;

    public void serve() {
        Server server = new Server();

        SocketConnector connector = new SocketConnector();
        connector.setHost(host); connector.setPort(port);
        connector.setForwarded(true); //for nginx.
        connector.setMaxIdleTime(1000);
        connector.setThreadPool(new QueuedThreadPool(Runtime.getRuntime().availableProcessors() * 2));
        server.addConnector(connector);

        Context root = new Context(server,"/", Context.SESSIONS);
        root.addServlet(new ServletHolder(this), "/*");

        try { server.start(); }
        catch (Exception e) { throw new RuntimeException(e.getMessage(), e); }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
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
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}

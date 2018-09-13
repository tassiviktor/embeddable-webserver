package hu.viktortassi.httpserver;

import hu.viktortassi.httpserver.exceptions.E400BadRequestException;
import hu.viktortassi.httpserver.exceptions.E500InternalServerError;
import hu.viktortassi.httpserver.interfaces.HttpHandler;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpRequestProcessorThread extends Thread {

    protected final String CATEGORY_NAME = "HttpRequestProcessor";
    protected Socket conn;
    protected HttpExchange he;
    protected Map<String, Class<? extends HttpHandler>> contextHandlers;

    public Logger getLog() {
        return log;
    }

    public void setLog(Logger log) {
        this.log = log;
    }

    Logger log;
    
    public void setConn(Socket conn) {
        this.conn = conn;
    }

    public Map<String, Class<? extends HttpHandler>> getContextHandlers() {
        return contextHandlers;
    }

    public void setContextHandlers(Map<String, Class<? extends HttpHandler>> contextHandlers) {
        this.contextHandlers = contextHandlers;
    }

    @Override
    public void run() {
        //InputStream in = null;
        try {
            //in = conn.getInputStream();
            he = new HttpExchange(conn);
            he.getEnvironmentVars().put("Client-Ip", conn.getRemoteSocketAddress().toString().split(":", 2)[0].split("/", 2)[1]);
            he.buildExchange();
            runContext();
        } catch (IOException ex) {
            log.log(Level.INFO,"I/O error on socket. (" + conn.getInetAddress() + ")",ex);
            // Nothing to do.
        } catch (E400BadRequestException ex) {
            log.log(Level.INFO,"Error processing request. (" + conn.getInetAddress() + ")",ex);
        } catch (E500InternalServerError ex) {
            log.log(Level.INFO,"Internal Server error (500) (" + conn.getInetAddress() + ")",ex);
        } catch (Exception ex) {
            log.log(Level.INFO,"Unhandled exception - Internal server error (500) (" + conn.getInetAddress() + ")",ex);
        } finally {
            try {
                conn.close();
            } catch (IOException ex) {
                // Error closing connection. Dontcare.
            } finally {
                log.log(Level.INFO,"Connection closed. (" + conn.getInetAddress() + ")");
            }
        }

    }

    protected void runContext() throws E500InternalServerError, E400BadRequestException {
        if (contextHandlers.size() < 1) {
            throw new E500InternalServerError("No context handlers defined.");
        }
        Iterator it = contextHandlers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            //System.out.println(pair.getKey() + " = " + pair.getValue());
            String path = this.he.getEnvironmentVariable(he.ENV_REQUEST_URI);
            String key = pair.getKey().toString();

            if (path == null){
                throw new E400BadRequestException("No header data");
            }
            if(key != null && (key.equals("*") || path.matches(key)) ){
                Class<HttpHandler> c = (Class<HttpHandler>) pair.getValue();
                HttpHandler h;
                try {
                    h = c.newInstance();
                } catch (InstantiationException | IllegalAccessException | NullPointerException ex) {
                    throw new E500InternalServerError("Unable to instantiate Http context handler", ex);
                }
                h.handle(he);
                break;
            }
        }
    }

}

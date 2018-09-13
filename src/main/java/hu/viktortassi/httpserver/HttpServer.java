package hu.viktortassi.httpserver;

import hu.viktortassi.httpserver.exceptions.HttpServerException;
import hu.viktortassi.httpserver.exceptions.TooMuchThreadException;
import hu.viktortassi.httpserver.interfaces.HttpHandler;
import hu.viktortassi.httpserver.interfaces.HttpStatuses.Status;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;

public class HttpServer extends Thread {

    @Inject private Logger logger;
    
    private ServerSocket serverSocket;

    private boolean acceptRequests = true;

    protected Map<String, Class<? extends HttpHandler>> contexts = new HashMap<>();
    protected ThreadManager threadManager;

    public void create() throws HttpServerException {
        try {
            serverSocket = new ServerSocket(8888);
        } catch (IOException ex) {
            throw new HttpServerException("Server cannot be created.", ex);
        }
        threadManager = new ThreadManager(100);
    }

    @Override
    public void run() {
        if (serverSocket == null) {
            return;
        }
        while (acceptRequests) {
            Socket incomingConnection = null;
            
            try {
                HttpRequestProcessorThread r = (HttpRequestProcessorThread) threadManager.createThread(HttpRequestProcessorThread.class);
                incomingConnection = serverSocket.accept();
                logger.log(Level.INFO, "Incoming connection from: " + incomingConnection.getInetAddress());
                r.setLog(logger);
                r.setConn(incomingConnection);
                r.setContextHandlers(contexts);
                r.start();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            } catch (TooMuchThreadException ex) {
                //Responds 429 Too Many Requests
                sendStatusResponse(Status.TooManyRequest, incomingConnection);
            } catch (InstantiationException ex) {
                logger.log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }

    private void sendStatusResponse(Status s, Socket incomingConnection) {
        if (incomingConnection == null) {
            return;
        }
        try {
            PrintWriter out = new PrintWriter(new OutputStreamWriter(incomingConnection.getOutputStream()));
            out.println("HTTP/1.1 " + s.getCode() + " " + s.getMessage());
            out.println("Connection: Closed");
            out.println();
            out.print(s.getMessage());
            out.flush();
            out.close();
            incomingConnection.close();
            logger.log(Level.INFO,"Response sent. " + s.getCode());
        } catch (IOException ex) {

        }
    }

    public boolean isAcceptRequests() {
        return acceptRequests;
    }

    public synchronized void setAcceptRequests(boolean acceptRequests) {
        this.acceptRequests = acceptRequests;
    }

    public void addContext(String path, Class<? extends HttpHandler> handlerClass) throws HttpServerException {
        if (contexts.containsKey(path)) {
            throw new HttpServerException("Multiple handler detected on same context.");
        }
        contexts.put(path, handlerClass);
    }
}

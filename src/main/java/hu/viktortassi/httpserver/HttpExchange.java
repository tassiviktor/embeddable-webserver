
package hu.viktortassi.httpserver;

import hu.viktortassi.httpserver.exceptions.E400BadRequestException;
import hu.viktortassi.httpserver.interfaces.HttpResponseHeaders;
import hu.viktortassi.httpserver.interfaces.HttpStatuses.Status;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpExchange {

    protected final String CATEGORY_NAME = "HTTPExchange";

    public final String ENV_REQUEST_METHOD = "Request-Method";
    public final String ENV_REQUEST_URI = "Request-Uri";
    public final String ENV_QUERY_STRING = "Query-String";
    public final String ENV_REQUEST_PROTOCOL = "Request-Protocol";

    private final Map<String, String> requestHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<String, String> environmentVars = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    protected Socket connection;

    String rawInput;
    
    HttpResponseHeader responseHeaders;
    
    String requestBody;
    
    OutputStream responseBody;

    public HttpExchange(Socket conn) throws IOException {
        this.requestBody = "";
        connection = conn;
        this.responseBody = connection.getOutputStream();
     }

    public void buildExchange() throws IOException, E400BadRequestException {
        StringBuilder sb = new StringBuilder();
        InputStream is = connection.getInputStream();
        //first time wait max 1s
        for(int i=0; (i < 1000) && (is.available() == 0) ; i+=100){
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {

            }
        }
        //TODO: run time parsing to let wait for content length
        while (is.available() > 0){
            byte[] b = new byte[is.available()];
            is.read(b, 0, is.available());
            sb.append(new String(b));
        }
        rawInput = sb.toString();
        processInput();
    }

    protected void processInput() throws IOException, E400BadRequestException {
        String lines[] = rawInput.split("\\r?\\n");        
        String line;
        int headerLines = 0;
        if (lines.length < 1){
            return;
        }
        while (lines.length > headerLines &&  ((line = lines[headerLines]) != null) && !line.equals("") && headerLines <= 101) {
            if (headerLines == 101) {
                throw new E400BadRequestException("Too much header lines.");
            }
            if (headerLines == 0) {
                //Protocol and version
                String[] parts = line.split(" ", 3);
                this.environmentVars.put(ENV_REQUEST_METHOD, parts[0]);
                String URL = parts[1];
                this.environmentVars.put(ENV_REQUEST_PROTOCOL, parts[2]);
                //Split uri
                String[] urlParts = URL.split("\\?", 2);
                if (urlParts.length < 2) {
                    this.environmentVars.put(ENV_REQUEST_URI, parts[1]);
                    this.environmentVars.put(ENV_QUERY_STRING, "");
                } else {
                    this.environmentVars.put(ENV_REQUEST_URI, urlParts[0]);
                    this.environmentVars.put(ENV_QUERY_STRING, urlParts[1]);
                }
            } else {
                String[] parts = line.split(":", 2);
                requestHeaders.put(parts[0], parts[1]);
            }

            headerLines++;
        }
        // Skip when double empty lines in header
        if (lines.length > headerLines && lines[headerLines].equals("")){
            headerLines++;
        }
        
        StringBuilder sb = new StringBuilder();
        while (lines.length > headerLines){
            sb.append(lines[headerLines]);
            headerLines++;
        }
        requestBody = sb.toString();
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public Map<String, String> getEnvironmentVars() {
        return environmentVars;
    }

    public String getEnvironmentVariable(String key) {
        return environmentVars.get(key);
    }

    public void sendSimpleStatusResponse(Status s) {

        responseHeaders = new HttpResponseHeader(s);
        responseHeaders.add(HttpResponseHeaders.CONNECTION, "Close");

        try {
            responseBody.write(responseHeaders.buildHeaders().getBytes());
            responseBody.write(s.getMessage().getBytes());
            responseBody.close();
        } catch (IOException ex) {
            return;
        }
    }

    public Socket getConnection() {
        return connection;
    }

    public OutputStream getResponseBody() {
        return responseBody;
    }

    public HttpResponseHeader getHeaders() {
        return responseHeaders;
    }

    public void setHeaders(HttpResponseHeader headers) {
        this.responseHeaders = headers;
    }
    
    public String getMethod(){
        return this.environmentVars.get(ENV_REQUEST_METHOD);
    }

    public String getProtocol(){
        return this.environmentVars.get(ENV_REQUEST_PROTOCOL);
    }
    
    public String getRequestUri(){
        return this.environmentVars.get(ENV_REQUEST_URI);
    }
    
    public String getQueryString(){
        return this.environmentVars.get(ENV_QUERY_STRING);
    }
    
    public String getRequestBody(){
        return this.requestBody;
    }
}

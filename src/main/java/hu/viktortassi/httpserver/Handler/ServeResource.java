package hu.viktortassi.httpserver.Handler;

import hu.viktortassi.httpserver.HttpExchange;
import hu.viktortassi.httpserver.HttpResponseHeader;
import hu.viktortassi.httpserver.interfaces.HttpHandler;
import static hu.viktortassi.httpserver.interfaces.HttpResponseHeaders.*;
import hu.viktortassi.httpserver.interfaces.HttpStatuses.Status;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;

public class ServeResource implements HttpHandler {

    protected final String CATEGORY_NAME = "ServeResource";

    @Override
    public void handle(HttpExchange exchange) {

        String path = exchange.getEnvironmentVariable(exchange.ENV_REQUEST_URI);

        File file = getFile(path);

        if (file == null) {
            exchange.sendSimpleStatusResponse(Status.NotFound);
            return;
        }

        if (file.isDirectory()) {
            path += "/index.html";
            file = getFile(path);
            if (file == null) {
                exchange.sendSimpleStatusResponse(Status.NotFound);
                return;
            }
        }
        serveFile(file, exchange);
    }

    protected File getFile(String path) {
        path = processPath(path);
        if (path == null) {
            return null;
        }
        path = path.replace("//", "/");
        ClassLoader classLoader = getClass().getClassLoader();

        if (path == null || !path.startsWith("public/")) {
            return null;
        }

        URL r = classLoader.getResource(path);

        if (r == null) {
            return null;
        }

        return new File(r.getFile());
    }

    protected String processPath(String path) {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            URL r = classLoader.getResource("public" + path);
            if (r == null) {
                return null;
            }
            return classLoader.getResource("").toURI().relativize(r.toURI()).toString();
        } catch (URISyntaxException ex) {
            return null;
        }
    }

    protected void serveFile(File f, HttpExchange serveResourceHandler) {

        FileInputStream fis;
        OutputStream responseBody = null;
        HttpResponseHeader header = new HttpResponseHeader(Status.Ok);
        serveResourceHandler.setHeaders(header);

        try {
            fis = new FileInputStream(f);
            addMimeHeader(serveResourceHandler, f);
        } catch (IOException ex) {
            serveResourceHandler.sendSimpleStatusResponse(Status.NotFound);
            return;
        }

        header.add(CONNECTION, "Close");
        header.add(CONTENT_LENGTH, String.valueOf(f.length()));

        try {
            responseBody = serveResourceHandler.getResponseBody();
            responseBody.write(header.buildHeaders().getBytes());
            copyFileStreamToOutputStream(fis, responseBody);
            //serveResourceHandler.getLog().info(CATEGORY_NAME, "RESPONSE[" + serveResourceHandler.getEnvironmentVariable("Client-Ip") + "] " + header.getResponseStatus().getMessage());
        } catch (IOException ex) {
            //serveResourceHandler.getLog().debug(CATEGORY_NAME, "I/O error while sending file.", ex);
        } finally {
            try {
                if (responseBody != null) {
                    responseBody.close();
                }
            } catch (IOException ex) {
                //serveResourceHandler.getLog().debug(CATEGORY_NAME, "I/O error while closing stream/socket.", ex);
            }
        }

    }

    protected void addMimeHeader(HttpExchange exChange, File f) throws IOException {
        String canonicalPath;
        canonicalPath = f.getCanonicalPath();
        String mimeType;
        mimeType = getFileMime(f);

        switch (mimeType) {
            case "text/plain":
            case "text/html":
            case "text/css":
            case "text/csv":
            case "application/javascript":
            case "application/json":
            case "application/xhtml+xml":
            case "application/xml":
                FileInputStream fis = new FileInputStream(f);
                InputStreamReader isr = new InputStreamReader(fis);
                String encoding = isr.getEncoding();
                exChange.getHeaders().add(CONTENT_TYPE, mimeType + "; charset=" + Charset.forName(encoding).toString().toLowerCase());
                break;
            default:
                if (mimeType != null) {
                    exChange.getHeaders().add(CONTENT_TYPE, mimeType);
                } else {
                    //exChange.getLog().warn(CATEGORY_NAME, "Unable to detect mime type of " + canonicalPath);
                }
        }

    }

    public static void copyFileStreamToOutputStream(InputStream i, OutputStream o) throws IOException {
        int count;
        byte[] buffer = new byte[65535]; // or 4096, or more
        while ((count = i.read(buffer)) > 0) {
            o.write(buffer, 0, count);
        }
    }

    public static String getFileMime(File f) {
        String result;
        try {
            result = Files.probeContentType(f.toPath());
        } catch (IOException ex) {
            result = null;
        }
        return result;
    }
}

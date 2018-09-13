package hu.viktortassi.httpserver;

import hu.viktortassi.httpserver.interfaces.HttpResponseHeaders;
import hu.viktortassi.httpserver.interfaces.HttpStatuses.Status;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class HttpResponseHeader implements HttpResponseHeaders {

    protected Status responseStatus;

    protected final Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public HttpResponseHeader(Status s) {
        responseStatus = s;
        headers.put(SERVER_NAME, "OneMinOrder POS server");
    }

    @Override
    public void setCookie(String key, String value) {
        add(SET_COOKIE, value);
    }

    @Override
    public void setCookie(String key, String value, Integer maxAge) {
        add(SET_COOKIE, value + "; Max-Age=" + maxAge);
    }

    public Status getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(Status responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String buildHeaders() {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ");
        sb.append(responseStatus.getCode());
        sb.append(" ");
        sb.append(responseStatus.getMessage());
        sb.append("\r\n");
        Iterator<Map.Entry<String, String>> iterator = headers.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, String> keyValue = iterator.next();
            sb.append(keyValue.getKey());
            sb.append(": ");
            sb.append(keyValue.getValue());
            sb.append("\r\n");
        }
        sb.append("\r\n");
        return sb.toString();
    }

    public void add(String key, String value) {
        headers.put(key, value);
    }
}

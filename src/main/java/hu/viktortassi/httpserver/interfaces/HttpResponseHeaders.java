package hu.viktortassi.httpserver.interfaces;

public interface HttpResponseHeaders {

    public static final String ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    public static final String CONNECTION = "Connection";
    public static final String CONTENT_DISPOSITION = "Content-Disposition";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String REDIRECT = "Location";
    public static final String SERVER_NAME = "Server";
    public static final String SET_COOKIE = "Set-Cookie";
    public static final String WWW_AUTHENTICATE = "WWW-Authenticate";

    public void setCookie(String key, String value);

    public void setCookie(String key, String value, Integer maxAge);
}
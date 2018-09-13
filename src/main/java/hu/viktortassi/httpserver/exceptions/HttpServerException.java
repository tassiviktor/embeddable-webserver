package hu.viktortassi.httpserver.exceptions;

import java.io.IOException;

public class HttpServerException extends Exception {

    /**
     * Creates a new instance of <code>HttpServerException</code> without detail
     * message.
     */
    public HttpServerException() {
    }

    /**
     * Constructs an instance of <code>HttpServerException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public HttpServerException(String msg) {
        super(msg);
    }

    public HttpServerException(String message, Throwable cause) {
        super(message, cause);
    }
}

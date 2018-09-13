/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.viktortassi.httpserver.exceptions;

/**
 *
 * @author tassiviktor
 */
public class E500InternalServerError extends Exception {

    /**
     * Creates a new instance of <code>E500InternalServerError</code> without
     * detail message.
     */
    public E500InternalServerError() {
    }

    /**
     * Constructs an instance of <code>E500InternalServerError</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public E500InternalServerError(String msg) {
        super(msg);
    }
    
    public E500InternalServerError(String message, Throwable cause) {
        super(message, cause);
}
}

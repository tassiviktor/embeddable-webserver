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
public class TooMuchThreadException extends Exception {

    /**
     * Creates a new instance of <code>TooMuchThreadException</code> without
     * detail message.
     */
    public TooMuchThreadException() {
    }

    /**
     * Constructs an instance of <code>TooMuchThreadException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public TooMuchThreadException(String msg) {
        super(msg);
    }
}

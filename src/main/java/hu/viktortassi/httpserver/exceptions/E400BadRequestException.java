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
public class E400BadRequestException extends Exception {

    /**
     * Creates a new instance of <code>E400BadRequestException</code> without
     * detail message.
     */
    public E400BadRequestException() {
    }

    /**
     * Constructs an instance of <code>E400BadRequestException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public E400BadRequestException(String msg) {
        super(msg);
    }
}

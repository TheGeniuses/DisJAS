/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rmi;

/**
 * @author sai
 */
public
class InvalidRequestException
        extends Exception {

    /**
     * Creates a new instance of
     * <code>InvalidMethodException</code> without detail message.
     */
    public
    InvalidRequestException() {
    }

    /**
     * Constructs an instance of
     * <code>InvalidMethodException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public
    InvalidRequestException(String msg) {
        super(msg);
    }
}

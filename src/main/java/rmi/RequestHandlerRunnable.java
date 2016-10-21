/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rmi;

import coms.ComSocket;

/**
 * @author sai
 */
public abstract
class RequestHandlerRunnable
        extends RequestHandler
        implements Runnable {

    protected
    RequestHandlerRunnable(ComSocket s) {
        super(s);
    }


    @Override
    public
    void run() {
        super.run();
    }

}

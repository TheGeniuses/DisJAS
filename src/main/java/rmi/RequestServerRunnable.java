/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rmi;

import java.net.ServerSocket;

/**
 * @author sai
 */
public abstract
class RequestServerRunnable
        extends RequestServer
        implements Runnable {

    protected
    RequestServerRunnable(
            ServerSocket s,
            int maxCon
    ) {
        super(s, maxCon);
    }

    @Override
    public
    void run() {
        super.run();
    }

}

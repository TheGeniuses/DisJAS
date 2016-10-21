/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coms.tcp;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author sai
 */
public
class TCPASender
        implements Runnable {
    private TCPComSocket cs;
    private String       msg;

    public
    TCPASender(
            TCPComSocket cs,
            String msg
    ) {
        this.cs = cs;
        this.msg = msg;
    }

    public
    void error(IOException ex) {
        Logger.getLogger(TCPASender.class.getName()).log(Level.SEVERE, "ASender: error sending msg\n", ex);
    }

    @Override
    public final
    void run() {
        try {
            this.cs.bsend(msg);
        } catch (IOException ex) {
            error(ex);
        }
    }


}

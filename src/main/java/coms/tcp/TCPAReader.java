/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coms.tcp;

import coms.AsyncReadTrigger;

import java.io.IOException;

/**
 * @author sai
 */
public
class TCPAReader
        implements Runnable {
    private AsyncReadTrigger art;
    private TCPComSocket     cs;

    public
    TCPAReader(
            TCPComSocket cs,
            AsyncReadTrigger art
    ) {
        this.art = art;
        this.cs = cs;
    }

    @Override
    public
    void run() {
        try {
            String msg = this.cs.sread();
            if (msg != null) {
                this.art.execute(msg);
            } else {
                this.art.connectionLost();
            }
        } catch (IOException ex) {
            this.art.abort(ex);
        }
    }

}

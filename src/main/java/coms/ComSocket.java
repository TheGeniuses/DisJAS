/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coms;

import java.io.IOException;

/**
 * @author sai
 */
public abstract
class ComSocket {
    protected boolean     connected = false;
    protected IOException lastError = null;
    protected boolean     showComs  = true;

    public abstract
    boolean connect()
            throws IOException;

    public abstract
    boolean disconnect()
            throws IOException;

    public abstract
    String getConnectionInfo();

    public
    IOException getLastError() {
        return this.lastError;
    }

    public
    boolean isConnected() {
        return this.connected;
    }

    public abstract
    String read()
            throws IOException;

    public abstract
    void send(String msg)
            throws IOException;

    protected
    void setError(IOException e) {
        this.lastError = e;
    }

    public
    void setShowComs(boolean e) {
        this.showComs = e;
    }

    public abstract
    boolean waitForConnection()
            throws IOException;

    public abstract
    boolean waitForDisconnect()
            throws IOException;

}

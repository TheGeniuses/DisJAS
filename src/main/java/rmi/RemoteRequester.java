/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rmi;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import coms.ComSocket;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author sai
 */
public
class RemoteRequester {
    //errors
    public static final int CANNOT_SEND_REQUEST = -32001;
    public static final int DISCONNECTED        = -32005;
    public static final int ERROR_ON_RECEIVE    = -32004;
    public static final int RECEIVE_TIMEOUT     = -32003;
    public static final int SEND_TIMEOUT        = -32002;
    protected ComSocket remoteClient;
    private boolean connected = false;
    private int id;
    private JSONRPC2Response response = null;
    private RequestHandlerRunnable rh;


    public
    RemoteRequester(
            ComSocket s,
            int id,
            RequestHandlerRunnable rh
    ) {
        remoteClient = s;
        this.id = id;
        connected = true;
        this.rh = rh;
    }

    public synchronized
    void disconnect() {
        connected = false;
        try {
            //this.rh.stop();
            this.remoteClient.disconnect();
        } catch (IOException ex) {
            Logger.getLogger(RemoteRequester.class.getName()).log(Level.SEVERE, "RemoteRequester#disconnect: error on ComSocket#disconnect\n", ex);
        }
    }

    public
    String getConnectionInfo() {
        return this.remoteClient.getConnectionInfo();
    }

    public
    int getID() {
        return id;
    }

    public synchronized
    JSONRPC2Response sendRequest(JSONRPC2Request req) {
        boolean reqSent = false;
        JSONRPC2Response reqResponse;
        int error       = 0;
        if (!connected) {
            error = RemoteRequester.DISCONNECTED;
            JSONRPC2Error reqError = new JSONRPC2Error(error, "RemoteRequester#sendRequest: disconnected from server " + remoteClient + '\n');
            return new JSONRPC2Response(reqError, req.getID());
        }
        try {
            this.remoteClient.send(req.toJSONString() + '\n');
            reqSent = true;
        } catch (IOException ex) {
            if (ex instanceof SocketTimeoutException) {
                error = RemoteRequester.SEND_TIMEOUT;
            } else {
                error = RemoteRequester.CANNOT_SEND_REQUEST;
            }
            Logger.getLogger(RemoteRequester.class.getName()).log(Level.SEVERE, "RemoteRequester#sendRequest: error on write\n", ex);
        }
        if (!reqSent) {
            JSONRPC2Error reqError = new JSONRPC2Error(error,
                    "RemoteRequester#sendRequest: an error ocurred while sending the request to " + remoteClient + '\n'
            );
            return new JSONRPC2Response(reqError, req.getID());
        }
        while (this.response == null) {
            try {
                wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(RemoteRequester.class.getName())
                      .log(Level.SEVERE, "RemoteRequester#sendRequest: an error ocurred while waiting for a response\n", ex);
            }
        }
        reqResponse = this.response;
        this.response = null;
        notifyAll();
        return reqResponse;
    }

    public synchronized
    JSONRPC2Response sendRequestAndStop(JSONRPC2Request req) {
        this.rh.waitForResponseAndStop();
        return this.sendRequest(req);
    }

    public synchronized
    void setResponse(JSONRPC2Response r) {
        while (this.response != null) {
            try {
                wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(RemoteRequester.class.getName()).log(Level.SEVERE, "RemoteRequester#setResponse: error on wait\n", ex);
            }
        }
        this.response = r;
        notifyAll();
    }

    public synchronized
    void stop() {
        connected = false;
    }

}

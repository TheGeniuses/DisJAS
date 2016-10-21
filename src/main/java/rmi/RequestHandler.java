/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rmi;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
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
public abstract
class RequestHandler {
    //errors
    public static final int     CANNOT_SEND_REQUEST    = -32001;
    public static final int     DISCONNECTED           = -32005;
    public static final int     ERROR_ON_RECEIVE       = -32004;
    public static final int     RECEIVE_TIMEOUT        = -32003;
    public static final int     SEND_TIMEOUT           = -32002;
    public static final int     UNIMPLEMENTED_REQUEST  = -32006;
    protected JSONRPC2Request  req;
    protected JSONRPC2Response resp;
    protected RemoteRequester  rr;
    protected           boolean running                = true;
    protected ComSocket        socket;
    private             boolean gotResponse            = false;
    private             boolean waitForResponseAndStop = false;

    protected
    RequestHandler(ComSocket s) {
        socket = s;
    }

    protected abstract
    boolean attendRequest(JSONRPC2Request r);

    protected abstract
    void error();

    protected abstract
    void executeAfterResponseSent(JSONRPC2Request r);

    protected
    boolean listen() {
        JSONRPC2Error   reqError = null;
        String          msg      = null;
        JSONRPC2Request request  = null;
        try {
            msg = socket.read();
            //if (msg == null && !this.running) return true;
            request = JSONRPC2Request.parse(msg);
        } catch (IOException | JSONRPC2ParseException ex) {
            if (ex instanceof JSONRPC2ParseException) {
                if (this.rr != null) {
                    try {
                        JSONRPC2Response response = JSONRPC2Response.parse(msg);
                        this.gotResponse = true;
                        if (this.waitForResponseAndStop) {
                            this.running = false;
                        }
                        this.rr.setResponse(response);
                    } catch (JSONRPC2ParseException ex1) {
                        Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, "RequestHandler#listen: error on parse\n", ex1);
                    }
                }
                if (!this.gotResponse) {
                    reqError = JSONRPC2Error.PARSE_ERROR;
                    reqError.setData("Raw response : " + msg);
                }
            } else {
                if (ex instanceof SocketTimeoutException) {
                    reqError = new JSONRPC2Error(RequestHandler.RECEIVE_TIMEOUT, "RequestHandler#listen: read timeout\n");
                } else {
                    reqError = new JSONRPC2Error(RequestHandler.ERROR_ON_RECEIVE, "RequestHandler#listen: error while reading\n");
                }
            }
            if (!this.gotResponse) {
                Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, "RequestHandler#listen: error while receiving\n", ex);
            }
        }
        if (reqError != null) {
            this.resp = new JSONRPC2Response(reqError, null);
            return false;
        }
        req = request;
        return true;
    }

    public
    void run() {
        while (running) {
            if (!listen()) {
                error();
            }
            if (!running) {
                break;
            }
            if (!this.gotResponse && attendRequest(req)) {
                try {
                    sendResponse();
                    executeAfterResponseSent(req);
                } catch (IOException ex) {
                    Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, "RequestHandler: error on sendResponse\n", ex);
                }
            } else if (!this.gotResponse) {
                error();
            }
            this.gotResponse = false;
        }
    }

    public
    void sendResponse()
            throws IOException {
        socket.send(resp.toJSONString() + '\n');
    }

    public
    void setRemoteRequester(RemoteRequester rr) {
        this.rr = rr;
    }

    public
    void stop() {
        this.running = false;
    }

    protected synchronized
    void waitForResponseAndStop() {
        this.waitForResponseAndStop = true;
    }

}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dataUDPRing;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import coms.udp.UDPComSocket;
import rmi.RequestHandlerRunnable;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author sai
 */
public abstract
class Handler
        extends RequestHandlerRunnable {

    protected Node node;

    public
    Handler(
            UDPComSocket previous,
            Node node
    ) {
        super(previous);
        this.node = node;
    }

    protected abstract
    void afterConnect(JSONRPC2Request r);

    protected abstract
    void afterDisconnect(JSONRPC2Request r);

    protected abstract
    void afterMessage(JSONRPC2Request r);

    protected abstract
    void afterReconnect(JSONRPC2Request r);

    protected abstract
    void afterStart(JSONRPC2Request r);

    protected abstract
    void afterStop(JSONRPC2Request r);

    protected abstract
    boolean attendConnect(JSONRPC2Request r);

    protected abstract
    boolean attendDisconnect(JSONRPC2Request r);

    protected
    boolean attendMessage(JSONRPC2Request r) {
        this.resp = new JSONRPC2Response("Message recieved\n", r.getID());
        return true;
    }

    protected abstract
    boolean attendReconnect(JSONRPC2Request r);

    @Override
    protected
    boolean attendRequest(JSONRPC2Request r) {
        switch (r.getMethod()) {
            case MessageFactory.MESSAGE:
                return attendMessage(r);
            case MessageFactory.CONNECT:
                return attendConnect(r);
            case MessageFactory.DISCONNECT:
                return attendDisconnect(r);
            case MessageFactory.RECONNECT:
                return attendReconnect(r);
            case MessageFactory.START:
                return attendStart(r);
            case MessageFactory.STOP:
                return attendStop(r);
            default:
                JSONRPC2Error error = JSONRPC2Error.METHOD_NOT_FOUND;
                error.setData(r.getMethod() + " is not a valid method\n");
                this.resp = new JSONRPC2Response(error, r.getID());
                return false;
        }
    }

    protected abstract
    boolean attendStart(JSONRPC2Request r);

    protected abstract
    boolean attendStop(JSONRPC2Request r);

    @Override
    protected
    void error() {
        try {
            this.sendResponse();
        } catch (IOException ex) {
            Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, "Handler#error: error while sending response\n", ex);
        }
    }

    @Override
    protected
    void executeAfterResponseSent(JSONRPC2Request r) {
        switch (r.getMethod()) {
            case MessageFactory.MESSAGE: {
                afterMessage(r);
                break;
            }
            case MessageFactory.CONNECT: {
                afterConnect(r);
                break;
            }
            case MessageFactory.DISCONNECT: {
                afterDisconnect(r);
                break;
            }
            case MessageFactory.RECONNECT: {
                afterReconnect(r);
                break;
            }
            case MessageFactory.START: {
                afterStart(r);
                break;
            }
            case MessageFactory.STOP: {
                afterStop(r);
                break;
            }
        }
    }

    public
    Node getNode() {
        return this.node;
    }
}

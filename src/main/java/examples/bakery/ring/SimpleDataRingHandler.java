/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package examples.bakery.ring;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import coms.udp.UDPComSocket;
import dataUDPRing.Handler;
import dataUDPRing.Node;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author sai
 */
public
class SimpleDataRingHandler
        extends Handler {

    protected UDPComSocket          next;
    protected SimpleDataRingHandler sendHandler;
    protected boolean stopped = false;

    public
    SimpleDataRingHandler(
            UDPComSocket previous,
            Node node,
            UDPComSocket next
    ) {
        super(previous, node);
        this.next = next;
    }

    @Override
    protected
    void afterConnect(JSONRPC2Request r) {
    }

    @Override
    protected
    void afterDisconnect(JSONRPC2Request r) {
    }

    @Override
    protected
    void afterMessage(JSONRPC2Request r) {
        this.node.modifyLocalData(r.getNamedParams());
    }

    @Override
    protected
    void afterReconnect(JSONRPC2Request r) {
    }

    @Override
    protected
    void afterStart(JSONRPC2Request r) {
        if (!this.next.isConnected()) {
            try {
                this.next.connect();
                Thread sht = new Thread(sendHandler);
                sht.start();
                System.out.println("Node(" + this.node.getID() + ") is ready to start\n");
                new Thread(this.node).start();
            } catch (IOException ex) {
                Logger.getLogger(SimpleDataRingHandler.class.getName())
                      .log(Level.SEVERE, "SimpleDataRingHandler#afterStart: error on UPDComSocket#connect\n", ex);
            }
        } else {
            System.out.println("Node(" + this.node.getID() + ") is ready to start\n");
            new Thread(this.node).start();
            this.node.forward(r.getMethod(), null);
        }
    }

    @Override
    protected
    void afterStop(JSONRPC2Request r) {
        if (!this.stopped) {
            this.node.stop();
            this.stopped = true;
            this.node.forward(r.getMethod(), null);
        }
    }

    @Override
    protected
    boolean attendConnect(JSONRPC2Request r) {
        return this.notSupported(r.getMethod(), r.getID());
    }

    @Override
    protected
    boolean attendDisconnect(JSONRPC2Request r) {
        return this.notSupported(r.getMethod(), r.getID());
    }

    @Override
    protected
    boolean attendReconnect(JSONRPC2Request r) {
        return this.notSupported(r.getMethod(), r.getID());
    }

    @Override
    protected
    boolean attendStart(JSONRPC2Request r) {
        if (!this.next.isConnected()) {
            this.resp = new JSONRPC2Response("Received start request, I'm not connected so it should be for me\n", r.getID());
        } else {
            this.resp = new JSONRPC2Response("Received start request, I'm connected so it should be for next node\n", r.getID());
        }
        return true;
    }

    @Override
    protected
    boolean attendStop(JSONRPC2Request r) {
        this.resp = new JSONRPC2Response("Received stop request\n", r.getID());
        return true;
    }

    private
    boolean notSupported(
            String request,
            Object id
    ) {
        JSONRPC2Error error = JSONRPC2Error.INVALID_REQUEST;
        error.setData(request + " is not supported\n");
        this.resp = new JSONRPC2Response(error, id);
        return false;
    }

    public
    void setSendHandler(SimpleDataRingHandler sendHandler) {
        this.sendHandler = sendHandler;
    }
}

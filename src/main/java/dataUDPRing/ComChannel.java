/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dataUDPRing;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import rmi.InvalidRequestException;
import rmi.RemoteRequester;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author sai
 */
public abstract
class ComChannel {
    protected MessageFactory  mf;
    protected RemoteRequester rr;

    public
    ComChannel(
            RemoteRequester rr,
            MessageFactory mf
    ) {
        this.rr = rr;
        this.mf = mf;
    }

    public abstract
    JSONRPC2Response passDisconnectRequest(Map<String, Object> params);

    public synchronized
    JSONRPC2Response passMessage(Map<String, Object> params) {
        try {
            JSONRPC2Response response = this.rr.sendRequest(this.mf.newRequest(MessageFactory.MESSAGE, params));
            return response;
        } catch (InvalidRequestException ex) {
            Logger.getLogger(ComChannel.class.getName()).log(Level.SEVERE, "ComChannel#passMessage: error invalid request\n", ex);
            return null;
        }
    }

    public abstract
    JSONRPC2Response passReconnectRequest(Map<String, Object> params);

    public abstract
    JSONRPC2Response passStartRequest(Map<String, Object> params);

    public abstract
    JSONRPC2Response passStopRequest(Map<String, Object> params);


}

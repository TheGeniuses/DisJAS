/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package examples.bakery.ring;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import dataUDPRing.ComChannel;
import dataUDPRing.MessageFactory;
import rmi.InvalidRequestException;
import rmi.RemoteRequester;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author sai
 */
public
class SimpleComChannel
        extends ComChannel {

    public
    SimpleComChannel(
            RemoteRequester rr,
            MessageFactory mf
    ) {
        super(rr, mf);
    }

    @Override
    public
    JSONRPC2Response passDisconnectRequest(Map<String, Object> params) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public
    JSONRPC2Response passReconnectRequest(Map<String, Object> params) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public
    JSONRPC2Response passStartRequest(Map<String, Object> params) {
        try {
            return this.rr.sendRequest(this.mf.newRequest(MessageFactory.START));
        } catch (InvalidRequestException ex) {
            Logger.getLogger(SimpleComChannel.class.getName()).log(Level.SEVERE, "SimpleComChannel#passStartRequest: invalid request\n", ex);
            return null;
        }
    }

    @Override
    public
    JSONRPC2Response passStopRequest(Map<String, Object> params) {
        try {
            return this.rr.sendRequest(this.mf.newRequest(MessageFactory.STOP));
        } catch (InvalidRequestException ex) {
            Logger.getLogger(SimpleComChannel.class.getName()).log(Level.SEVERE, "SimpleComChannel#passStopRequest: invalid request\n", ex);
            return null;
        }
    }

}

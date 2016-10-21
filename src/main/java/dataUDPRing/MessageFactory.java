/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dataUDPRing;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import rmi.ARequestFactory;
import rmi.InvalidRequestException;

import java.util.Map;

/**
 * @author sai
 */
public
class MessageFactory
        extends ARequestFactory {
    public static final String CONNECT    = "ring.connect";
    public static final String DISCONNECT = "ring.disconnect";
    public static final String MESSAGE    = "ring.msg";
    public static final String RECONNECT  = "ring.reconnect";
    public static final String START      = "ring.start";
    public static final String STOP       = "ring.stop";


    public
    MessageFactory(int id) {
        super(id);
    }

    @Override
    public
    JSONRPC2Request newRequest(
            String request,
            Map<String, Object> namedParams
    )
            throws InvalidRequestException {
        switch (request) {
            case MESSAGE:
                return new JSONRPC2Request(MESSAGE, namedParams, this.id);
            case CONNECT:
                return new JSONRPC2Request(CONNECT, namedParams, this.id);
            case RECONNECT:
                return new JSONRPC2Request(DISCONNECT, namedParams, this.id);
            default:
                throw new InvalidRequestException(request + " is not a valid request or request don't have named parameters\n");
        }
    }

    @Override
    public
    JSONRPC2Request newRequest(String request)
            throws InvalidRequestException {
        switch (request) {
            case DISCONNECT:
                return new JSONRPC2Request(DISCONNECT, this.id);
            case START:
                return new JSONRPC2Request(START, this.id);
            case STOP:
                return new JSONRPC2Request(STOP, this.id);
            default:
                throw new InvalidRequestException(request + " is not a valid request or request use listed or named parameters\n");
        }
    }

}

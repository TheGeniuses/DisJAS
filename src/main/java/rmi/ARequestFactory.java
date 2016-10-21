/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rmi;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;

import java.util.List;
import java.util.Map;

/**
 * @author sai
 */
public abstract
class ARequestFactory {
    protected int id;

    public
    ARequestFactory(int id) {
        this.id = id;
    }

    public
    int getID() {
        return this.id;
    }

    public
    JSONRPC2Request newRequest(String request)
            throws InvalidRequestException {
        throw new InvalidRequestException(request + " is not a valid request or request use listed or named parameters\n");
    }

    public
    JSONRPC2Request newRequest(
            String request,
            List<Object> listedParams
    )
            throws InvalidRequestException {
        throw new InvalidRequestException(request + " is not a valid request or request don't have listed parameters\n");
    }

    public
    JSONRPC2Request newRequest(
            String request,
            Map<String, Object> namedParams
    )
            throws InvalidRequestException {
        throw new InvalidRequestException(request + " is not a valid request or request don't have named parameters\n");
    }


}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coms.tcp;

import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import coms.AsyncReadTrigger;
import coms.ComSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author sai
 */
public
class TCPComSocket
        extends ComSocket {
    private AsyncReadTrigger art;
    private CONNECTION_TYPE  connectionType;
    private BufferedReader   in;
    private Socket           socket;
    private int              timeout;

    public
    TCPComSocket(
            Socket s,
            CONNECTION_TYPE ctype,
            int timeout
    ) {
        this.socket = s;
        this.connectionType = ctype;
        this.timeout = timeout;
    }

    public
    TCPComSocket(Socket s) {
        this.socket = s;
        this.connectionType = CONNECTION_TYPE.RSWB;
        this.timeout = 0;
    }

    public
    TCPComSocket(
            Socket s,
            int timeout
    ) {
        this.socket = s;
        this.connectionType = CONNECTION_TYPE.RSWB;
        this.timeout = timeout;
    }

    public
    TCPComSocket(
            Socket s,
            CONNECTION_TYPE ctype
    ) {
        this.socket = s;
        this.connectionType = ctype;
        this.timeout = 0;
    }

    public
    void aread(AsyncReadTrigger art) {
        new Thread(new TCPAReader(this, art)).start();
    }

    protected
    void bsend(String msg)
            throws IOException {
        this.socket.getOutputStream().write(msg.getBytes());
        if (this.showComs) {
            System.out.println("Sent: " + msg + '\n');
        }
    }

    @Override
    public
    boolean connect()
            throws IOException {
        //using json rmi
        //send : connect(timeout, connectionType)
        //wait for response
        //if response is succesfull then return true
        Map<String, Object> params = new HashMap<>();
        params.put("timeout", this.timeout);
        params.put("ctype", this.connectionType.ordinal());
        JSONRPC2Request conReq = new JSONRPC2Request("TCPComSocket.connect", params, 0);
        bsend(conReq.toJSONString() + '\n');
        String response = sread();
        try {
            JSONRPC2Response jsonresp = JSONRPC2Response.parse(response);
            if (jsonresp.indicatesSuccess()) {
                this.connected = true;
                return true;
            }
            return false;
        } catch (JSONRPC2ParseException ex) {
            Logger.getLogger(TCPComSocket.class.getName()).log(Level.SEVERE, "TCPComSocket#connect: error parsing response\n", ex);
            return false;
        }
    }

    @Override
    public
    boolean disconnect()
            throws IOException {
        //using json rmi
        //send : disconnect()
        //wait for response
        //if response is succesfull then close the socket and return true
        JSONRPC2Request conReq = new JSONRPC2Request("TCPComSocket.disconnect", 0);
        bsend(conReq.toJSONString() + '\n');
        String response = sread();
        try {
            JSONRPC2Response jsonresp = JSONRPC2Response.parse(response);
            if (jsonresp.indicatesSuccess()) {
                this.connected = false;
                this.socket.close();
                return true;
            }
            return false;
        } catch (JSONRPC2ParseException ex) {
            Logger.getLogger(TCPComSocket.class.getName()).log(Level.SEVERE, "TCPComSocket#disconnect: error parsing response\n", ex);
            return false;
        }
    }

    @Override
    public
    String getConnectionInfo() {
        return this.socket.getInetAddress().getHostName() + ':' + this.socket.getPort();
    }

    @Override
    public
    IOException getLastError() {
        return this.lastError;
    }

    @Override
    public
    boolean isConnected() {
        return this.connected;
    }

    @Override
    public
    String read()
            throws IOException {
        if (this.connectionType == CONNECTION_TYPE.ASYNC) {
            if (this.art != null) {
                aread(art);
            }
            return null;
        }
        String msg = sread();
        if (this.connectionType == CONNECTION_TYPE.SYNC) {
            bsend("ReadOK\n");
        }
        return msg;
    }

    @Override
    public
    void send(String msg)
            throws IOException {
        if (this.connectionType == CONNECTION_TYPE.RSWB) {
            bsend(msg);
        }
        if (this.connectionType == CONNECTION_TYPE.ASYNC) {
            new Thread(new TCPASender(this, msg)).start();
        }
        if (this.connectionType == CONNECTION_TYPE.SYNC) {
            sread();
        }
    }

    public
    void setART(AsyncReadTrigger art) {
        this.art = art;
    }

    @Override
    protected
    void setError(IOException e) {
        this.lastError = e;
    }

    @Override
    public
    void setShowComs(boolean e) {
        this.showComs = e;
    }

    protected
    String sread()
            throws IOException {
        if (this.in == null) {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        String msg = in.readLine();
        if (this.showComs) {
            System.out.println("Received: " + msg + '\n');
        }
        return msg;
    }

    @Override
    public
    String toString() {
        return this.socket.toString();
    }

    @Override
    public
    boolean waitForConnection()
            throws IOException {
        //using json rmi
        //wait for request
        //if request is "connect(timeout, connectionType)" return true if the parameters are correct and supported
        //else return false
        String req = sread();
        try {
            JSONRPC2Request conReq = JSONRPC2Request.parse(req);
            if (conReq.getMethod().compareTo("TCPComSocket.connect") != 0) {
                return false;
            }
            Map<String, Object> params = conReq.getNamedParams();
            this.timeout = ((Long) params.get("timeout")).intValue();
            this.connectionType = CONNECTION_TYPE.values()[((Long) params.get("ctype")).intValue()];
            JSONRPC2Response response = new JSONRPC2Response("Connected", conReq.getID());
            bsend(response.toJSONString() + '\n');
            this.connected = true;
            return true;
        } catch (JSONRPC2ParseException ex) {
            Logger.getLogger(TCPComSocket.class.getName()).log(Level.SEVERE, "TCPComSocket#waitForConnection: error parsing request\n", ex);
            return false;
        }
    }

    @Override
    public
    boolean waitForDisconnect()
            throws IOException {
        //using json rmi
        //wait for request
        //if request is "disconnect()" and this.connected then send an ok response and return true
        //set this.connected to false and stop any thread associated with this ComSocket
        String req = sread();
        try {
            JSONRPC2Request conReq = JSONRPC2Request.parse(req);
            if (conReq.getMethod().compareTo("TCPComSocket.disconnect") != 0) {
                return false;
            }
            JSONRPC2Response response = new JSONRPC2Response("Disconnected", conReq.getID());
            bsend(response.toJSONString() + '\n');
            this.connected = false;
            return true;
        } catch (JSONRPC2ParseException ex) {
            Logger.getLogger(TCPComSocket.class.getName()).log(Level.SEVERE, "TCPComSocket#waitForDisconnect: error parsing request\n", ex);
            return false;
        }
    }

    /**
     * The type of the connection <li>ASYNC : asynchronous read and write operations</li> <li>SYNC : synchronous read and write operations</li>
     * <li>RSWB : synchronous read and buffered write (if buffer is not full then the write is executed and the program continues, if the buffer is
     * full then the write waits until a read is executed)</li>
     */
    public
    enum CONNECTION_TYPE {
        ASYNC,
        SYNC,
        RSWB
    }

}

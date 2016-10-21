/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coms.udp;

import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import coms.ComSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author sai
 */
public
class UDPComSocket
        extends ComSocket {
    protected InetAddress laddr;
    protected boolean     listening;
    protected Integer     lport;
    protected int         maxLenght;
    protected InetAddress raddr = null;
    protected Integer     rport = null;
    protected DatagramSocket socket;


    public
    UDPComSocket(
            Integer lport,
            Integer rport,
            InetAddress laddr,
            InetAddress raddr,
            int maxLenght
    )
            throws SocketException {
        this.listening = raddr == null;
        if (listening) {
            this.laddr = laddr;
            this.lport = lport;
        } else {
            this.laddr = laddr;
            this.lport = lport;
            this.raddr = raddr;
            this.rport = rport;
        }
        this.connected = false;
        this.socket = new DatagramSocket(lport);
        this.maxLenght = maxLenght;
    }

    protected
    String bread()
            throws IOException {
        byte[]         data   = new byte[this.maxLenght];
        DatagramPacket packet = new DatagramPacket(data, this.maxLenght);
        this.socket.receive(packet);
        String msg = new String(packet.getData());
        if (this.showComs) {
            System.out.println("Received: " + msg + "through " + packet.getSocketAddress() + "\n");
        }
        return msg;
    }

    protected
    void bsend(String msg)
            throws IOException {
        byte[]         data   = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, this.raddr, this.rport);
        this.socket.send(packet);
        if (this.showComs) {
            System.out.println("Sent: " + msg + "through " + packet.getSocketAddress() + "\n");
        }
    }

    @Override
    public
    boolean connect()
            throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("ip", this.laddr.getHostAddress());
        params.put("port", Integer.toString(this.lport));
        JSONRPC2Request conReq = new JSONRPC2Request("UDPComSocket.connect", params, 0);
        bsend(conReq.toJSONString() + '\n');
        String response = bread();
        try {
            JSONRPC2Response jsonresp = JSONRPC2Response.parse(response);
            if (jsonresp.indicatesSuccess()) {
                this.connected = true;
                return true;
            }
            return false;
        } catch (JSONRPC2ParseException ex) {
            Logger.getLogger(UDPComSocket.class.getName()).log(Level.SEVERE, "UDPComSocket#connect: error parsing response\n", ex);
            return false;
        }
    }

    @Override
    public
    boolean disconnect()
            throws IOException {
        JSONRPC2Request conReq = new JSONRPC2Request("UDPComSocket.disconnect", 0);
        bsend(conReq.toJSONString() + '\n');
        String response = bread();
        try {
            JSONRPC2Response jsonresp = JSONRPC2Response.parse(response);
            if (jsonresp.indicatesSuccess()) {
                this.connected = false;
                //this.socket.close();
                return true;
            }
            return false;
        } catch (JSONRPC2ParseException ex) {
            Logger.getLogger(UDPComSocket.class.getName()).log(Level.SEVERE, "UDPComSocket#disconnect: error parsing response\n", ex);
            return false;
        }
    }

    @Override
    public
    String getConnectionInfo() {
        if (connected) {
            return '(' + this.laddr.getHostAddress() + ':' + this.lport + ") ==> (" + this.raddr.getHostAddress() + ':' + this.rport + ")\n";
        } else {
            if (listening) {
                return '(' + this.laddr.getHostAddress() + ':' + this.lport + ") ==> (listening...)\n";
            } else {
                return '(' +
                       this.laddr.getHostAddress() +
                       ':' +
                       this.lport +
                       ") not connected yet with (" +
                       this.raddr.getHostAddress() +
                       ':' +
                       this.rport +
                       ")\n";
            }
        }

    }

    public
    InetAddress getRemoteAddress() {
        return this.raddr;
    }

    public
    Integer getRemotePort() {
        return this.rport;
    }

    @Override
    public
    String read()
            throws IOException {
        return this.bread();
    }

    @Override
    public
    void send(String msg)
            throws IOException {
        this.bsend(msg);
    }

    public
    void setRemoteAddress(
            Integer rport,
            InetAddress raddr
    ) {
        this.rport = rport;
        this.raddr = raddr;
        this.connected = true;
        this.listening = false;
    }

    @Override
    public
    boolean waitForConnection()
            throws IOException {
        String req = bread();
        try {
            JSONRPC2Request conReq = JSONRPC2Request.parse(req);
            if (conReq.getMethod().compareTo("UDPComSocket.connect") != 0) {
                return false;
            }
            Map<String, Object> params = conReq.getNamedParams();
            this.raddr = InetAddress.getByName((String) params.get("ip"));
            this.rport = Integer.valueOf((String) params.get("port"));
            this.listening = false;
            JSONRPC2Response response = new JSONRPC2Response("Connected", conReq.getID());
            bsend(response.toJSONString() + '\n');
            this.connected = true;
            return true;
        } catch (JSONRPC2ParseException ex) {
            Logger.getLogger(UDPComSocket.class.getName()).log(Level.SEVERE, "UDPComSocket#waitForConnection: error parsing request\n", ex);
            return false;
        }
    }

    @Override
    public
    boolean waitForDisconnect()
            throws IOException {
        String req = bread();
        try {
            JSONRPC2Request conReq = JSONRPC2Request.parse(req);
            if (conReq.getMethod().compareTo("UDPComSocket.disconnect") != 0) {
                return false;
            }
            JSONRPC2Response response = new JSONRPC2Response("Disconnected", conReq.getID());
            bsend(response.toJSONString() + '\n');
            this.connected = false;
            return true;
        } catch (JSONRPC2ParseException ex) {
            Logger.getLogger(UDPComSocket.class.getName()).log(Level.SEVERE, "UDPComSocket#waitForDisconnect: error parsing request\n", ex);
            return false;
        }
    }

}

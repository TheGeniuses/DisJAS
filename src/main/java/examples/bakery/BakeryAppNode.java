/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package examples.bakery;

import coms.udp.UDPComSocket;
import dataUDPRing.MessageFactory;
import examples.bakery.ring.BakeryNode;
import examples.bakery.ring.SimpleComChannel;
import examples.bakery.ring.SimpleDataRingHandler;
import rmi.RemoteRequester;

import java.io.IOException;
import java.net.InetAddress;

/**
 * @author sai
 */
public
class BakeryAppNode {
    public static final int     BASE_LISTEN_PORT = 4000;
    public static final int     BASE_SEND_PORT   = 5000;
    public static final boolean showComs         = false;

    /**
     * @param args : id, address, id
     */
    public static
    void main(String[] args)
            throws IOException {
        Integer      localID       = Integer.valueOf(args[0]);
        InetAddress  nextNodeIP    = InetAddress.getByName(args[1]);
        Integer      nextNodeID    = Integer.valueOf(args[2]);
        InetAddress  localIP       = InetAddress.getLocalHost();
        UDPComSocket sendSocket    = new UDPComSocket(BASE_SEND_PORT + localID, BASE_LISTEN_PORT + nextNodeID, localIP, nextNodeIP, 1000);
        UDPComSocket receiveSocket = new UDPComSocket(BASE_LISTEN_PORT + localID, null, localIP, null, 1000);
        sendSocket.setShowComs(showComs);
        receiveSocket.setShowComs(showComs);

        //Create ring node
        SimpleDataRingHandler sendHandler     = new SimpleDataRingHandler(sendSocket, null, null);
        RemoteRequester       remoteRequester = new RemoteRequester(sendSocket, localID, sendHandler);
        sendHandler.setRemoteRequester(remoteRequester);
        MessageFactory        mf             = new MessageFactory(localID);
        SimpleComChannel      sendChannel    = new SimpleComChannel(remoteRequester, mf);
        BakeryNode            node           = new BakeryNode(sendChannel, localID);
        SimpleDataRingHandler receiveHandler = new SimpleDataRingHandler(receiveSocket, node, sendSocket);

        //Connect
        System.out.println("Node(" + localID + "): " + sendSocket.getConnectionInfo());
        sendSocket.connect();
        System.out.println("Node(" + localID + "): " + sendSocket.getConnectionInfo());
        new Thread(sendHandler).start();

        //Waiting for connection
        System.out.println("Node(" + localID + "): " + receiveSocket.getConnectionInfo());
        receiveSocket.waitForConnection();
        receiveHandler.setSendHandler(sendHandler);
        new Thread(receiveHandler).start();
        System.out.println("Node(" + localID + "): " + receiveSocket.getConnectionInfo());

    }
}

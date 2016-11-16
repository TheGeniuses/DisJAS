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
class BakeryAppStarter {
    public static final int     BASE_LISTEN_PORT = 4000;
    public static final int     BASE_SEND_PORT   = 5000;
    public static final boolean showComs         = true;
    public static Integer localID;

    /**
     * @param args : id, address, id, waitAndConnect(Boolean)
     */
    public static synchronized
    void main(String[] args)
            throws IOException {
        localID = Integer.valueOf(args[0]);
        InetAddress  nextNodeIP    = InetAddress.getByName(args[1]);
        Integer      nextNodeID    = Integer.valueOf(args[2]);
        InetAddress  localIP       = InetAddress.getLocalHost();
        UDPComSocket sendSocket    = new UDPComSocket(BASE_SEND_PORT + localID, BASE_LISTEN_PORT + nextNodeID, localIP, nextNodeIP, 1000);
        UDPComSocket receiveSocket = new UDPComSocket(BASE_LISTEN_PORT + localID, null, localIP, null, 1000);
        sendSocket.setShowComs(showComs);
        receiveSocket.setShowComs(showComs);

        //Connect
        System.out.println("Node(" + localID + "): " + sendSocket.getConnectionInfo());
        sendSocket.connect();
        System.out.println("Node(" + localID + "): " + sendSocket.getConnectionInfo());

        //create ring node
        SimpleDataRingHandler sendHandler     = new SimpleDataRingHandler(sendSocket, null, null);
        RemoteRequester       remoteRequester = new RemoteRequester(sendSocket, localID, sendHandler);
        sendHandler.setRemoteRequester(remoteRequester);
        MessageFactory        mf             = new MessageFactory(localID);
        SimpleComChannel      sendChannel    = new SimpleComChannel(remoteRequester, mf);
        BakeryNode            node           = new BakeryNode(sendChannel, localID);
        SimpleDataRingHandler receiveHandler = new SimpleDataRingHandler(receiveSocket, node, sendSocket);
        new Thread(sendHandler).start();

        //send start request
        Control control = new Control(remoteRequester, mf, receiveSocket, receiveHandler, node);
        control.setVisible(true);

        //Waiting for connection
        //        System.out.println("Node(" + localID + "): " + receiveSocket.getConnectionInfo());
        //        receiveSocket.waitForConnection();
        //        System.out.println("Node(" + localID + "): " + receiveSocket.getConnectionInfo());
        //        new Thread(receiveHandler).start();
        //        System.out.println("Node("+localID+") is ready to start\n");

    }
}

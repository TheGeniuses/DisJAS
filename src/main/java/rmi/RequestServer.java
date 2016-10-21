/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rmi;

import coms.ComSocket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author sai
 */
public abstract
class RequestServer {
    protected static boolean active                = false;
    protected static int     maxConnections        = -1;
    protected static int     maxConnectionsCurrent = -1;
    protected static ServerSocket socket;

    protected
    RequestServer(
            ServerSocket s,
            int maxCon
    ) {
        socket = s;
        active = true;
        maxConnections = maxCon > 0 ? maxCon : -1;
        maxConnectionsCurrent = maxConnections;
    }

    protected abstract
    ComSocket createNewComSocket(Socket s);

    protected abstract
    void createNewRequestHandler(ComSocket s);

    public
    void reset() {
        active = true;
        maxConnectionsCurrent = maxConnections;
    }

    public
    void run() {
        while (active && maxConnectionsCurrent != 0) {
            try {
                Socket    client = socket.accept();
                ComSocket cs     = createNewComSocket(client);
                cs.setShowComs(true);
                if (cs.waitForConnection()) {
                    System.out.println("Connection accepted from: " + cs.toString() + '\n');
                    maxConnections--;
                } else {
                    return;
                }
                createNewRequestHandler(cs);
            } catch (IOException ex) {
                Logger.getLogger(RequestServer.class.getName()).log(Level.SEVERE, "RequestServer: error on accept\n", ex);
            }
        }
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(RequestServer.class.getName()).log(Level.SEVERE, "RequestServer: error closing server socket\n", ex);
        }
    }

    public
    void stop() {
        active = false;
    }

}

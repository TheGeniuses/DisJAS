/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dataUDPRing;

import java.util.Map;

/**
 * @author sai
 */
public abstract
class Node
        implements Runnable {
    protected int        id;
    protected boolean    lastClient;
    protected ComChannel out;
    protected boolean    paused;
    protected boolean    waitingForConnection;
    protected boolean    waitingForMsgResponse;

    public
    Node(
            ComChannel cc,
            int id
    ) {
        this.id = id;
        this.out = cc;
        this.paused = false;
        this.lastClient = false;
    }

    public abstract
    void forward(
            String request,
            Map<String, Object> data
    );

    public
    int getID() {
        return this.id;
    }

    public abstract
    void modifyLocalData(Map<String, Object> data);

    public
    void pause() {
        this.paused = true;
    }

    public synchronized
    void resume() {
        this.paused = false;
        notifyAll();
    }

    public
    void setComChannel(ComChannel cc) {
        this.out = cc;
    }

    public
    void stop() {
        this.lastClient = true;
    }

}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package examples.bakery.ring;

import dataUDPRing.ComChannel;
import dataUDPRing.MessageFactory;
import dataUDPRing.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author sai
 */
public
class BakeryNode
        extends Node
        implements Runnable {
    private int     repeatedSameValueRequests = 0;
    private boolean running                   = true;
    private int     turn                      = 0;

    public
    BakeryNode(
            ComChannel cc,
            int id
    ) {
        super(cc, id);
    }

    @Override
    public
    void forward(
            String request,
            Map<String, Object> data
    ) {
        switch (request) {
            case MessageFactory.START: {
                this.out.passStartRequest(data);
                break;
            }
            case MessageFactory.STOP: {
                this.out.passStopRequest(data);
                break;
            }
        }
    }

    @Override
    public synchronized
    void modifyLocalData(Map<String, Object> data) {
        Integer reqID   = Integer.valueOf((String) data.get("id"));
        Integer reqTurn = Integer.valueOf((String) data.get("turn"));
        if (reqID == this.id) {
            this.turn = Math.max(reqTurn, this.turn) + 1 + this.repeatedSameValueRequests;
            this.repeatedSameValueRequests = 0;
            this.waitingForMsgResponse = false;
            notifyAll();
        } else {
            if (this.waitingForMsgResponse && reqTurn == this.turn) {
                this.repeatedSameValueRequests++;
            } else {
                this.repeatedSameValueRequests = 0;
            }
            reqTurn = Math.max(this.turn, reqTurn);
            data.put("turn", Integer.toString(reqTurn));
            this.out.passMessage(data);
        }
    }

    @Override
    public synchronized
    void run() {
        this.running = true;
        while (running) {
            while (paused) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(BakeryNode.class.getName())
                          .log(Level.SEVERE, "BakeryNode#run: error on wait(paused or waiting for response)\n", ex);
                }
            }
            try {
                wait((long) (Math.random() * 5000));
            } catch (InterruptedException ex) {
                Logger.getLogger(BakeryNode.class.getName()).log(Level.SEVERE, "BakeryNode#run: error on wait(random wait)\n", ex);
            }
            updateTurn();
            serveClient();
            if (this.lastClient) {
                this.running = false;
                System.out.println("Node(" + id + ") is closed\n");
            }
        }
    }

    private
    void serveClient() {
        System.out.println("Node(" + id + ") served client with turn " + turn + '\n');
    }

    private synchronized
    void updateTurn() {
        this.waitingForMsgResponse = true;
        Map<String, Object> params = new HashMap<>();
        params.put("id", Integer.toString(id));
        params.put("turn", Integer.toString(turn));
        this.out.passMessage(params);
        while (this.waitingForMsgResponse) {
            try {
                wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(BakeryNode.class.getName()).log(Level.SEVERE, "BakeryNode#updateTurn: error on wait\n", ex);
            }
        }
    }

}

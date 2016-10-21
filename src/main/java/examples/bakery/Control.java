/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package examples.bakery;

import coms.udp.UDPComSocket;
import dataUDPRing.MessageFactory;
import examples.bakery.ring.BakeryNode;
import examples.bakery.ring.SimpleDataRingHandler;
import rmi.InvalidRequestException;
import rmi.RemoteRequester;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static examples.bakery.BakeryAppStarter.localID;

/**
 * @author sai
 */
public
class Control
        extends javax.swing.JFrame {

    private boolean firstStart = true;
    private MessageFactory mf;
    private BakeryNode node;
    private SimpleDataRingHandler receiveHandler;
    private UDPComSocket receiveSocket;
    private RemoteRequester rr;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton startButton;
    private javax.swing.JButton stopButton;

    /**
     * Creates new form Control
     */
    public
    Control(
            RemoteRequester rr,
            MessageFactory mf,
            UDPComSocket receiveSocket,
            SimpleDataRingHandler receiveHandler,
            BakeryNode node
    ) {
        initComponents();
        this.rr = rr;
        this.mf = mf;
        this.receiveSocket = receiveSocket;
        this.receiveHandler = receiveHandler;
        this.node = node;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private
    void initComponents() {

        startButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        startButton.setText("START");
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public
            void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        stopButton.setText("STOP");
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public
            void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                        .addContainerGap()
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(startButton,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                109,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE
                                                                        )
                                                                        .addComponent(stopButton,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                109,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE
                                                                        ))
                                                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                      .addGroup(layout.createSequentialGroup()
                                                      .addContainerGap()
                                                      .addComponent(startButton,
                                                              javax.swing.GroupLayout.PREFERRED_SIZE,
                                                              59,
                                                              javax.swing.GroupLayout.PREFERRED_SIZE
                                                      )
                                                      .addGap(18, 18, 18)
                                                      .addComponent(stopButton,
                                                              javax.swing.GroupLayout.PREFERRED_SIZE,
                                                              59,
                                                              javax.swing.GroupLayout.PREFERRED_SIZE
                                                      )
                                                      .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private
    void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
        try {
            this.rr.sendRequest(this.mf.newRequest(MessageFactory.START));
            if (firstStart) {
                System.out.println("Node(" + BakeryAppStarter.localID + "): " + receiveSocket.getConnectionInfo());
                receiveSocket.waitForConnection();
                System.out.println("Node(" + BakeryAppStarter.localID + "): " + receiveSocket.getConnectionInfo());
                new Thread(receiveHandler).start();
                new Thread(this.node).start();
                this.firstStart = false;
            } else {
                new Thread(this.node).start();
            }
            System.out.println("Node(" + localID + ") is ready to start\n");
        } catch (InvalidRequestException ex) {
            Logger.getLogger(Control.class.getName()).log(Level.SEVERE, "Control#startButtonActionPerformed: invalid request\n", ex);
        } catch (IOException ex) {
            Logger.getLogger(Control.class.getName()).log(Level.SEVERE, "Control#startButtonActionPerformed: error on wait for connection\n", ex);
        }
    }//GEN-LAST:event_startButtonActionPerformed

    private
    void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
        try {
            this.rr.sendRequest(this.mf.newRequest(MessageFactory.STOP));
        } catch (InvalidRequestException ex) {
            Logger.getLogger(Control.class.getName()).log(Level.SEVERE, "Control#stopButtonActionPerformed: invalid request\n", ex);
        }
    }//GEN-LAST:event_stopButtonActionPerformed
    // End of variables declaration//GEN-END:variables
}

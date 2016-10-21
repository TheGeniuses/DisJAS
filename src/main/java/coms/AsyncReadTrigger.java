/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coms;

import java.io.IOException;

/**
 * @author sai
 */
public abstract
class AsyncReadTrigger {

    public abstract
    void abort(IOException e);

    public abstract
    void connectionLost();

    public abstract
    void execute(String m);

}

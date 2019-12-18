/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ProtocolHAFISCA;

import java.net.Socket;

/**
 *
 * @author damien
 */
public class Client_connecte {
    private Socket sock = null;
    private long id = 0;

    public Socket getSock() {
        return sock;
    }

    public void setSock(Socket sock) {
        this.sock = sock;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Client_connecte(Socket s, long l) 
    {
        this.sock = s;
        this.id = l;
    }


    
    
    
    
}

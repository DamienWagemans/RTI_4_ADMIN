/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ProtocolHAFISCA;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import requeteCard.*;
import java.io.Serializable;
import java.net.Socket;

/**
 *
 * @author damien
 */
public class ReponseADMIN implements Serializable{
    
    private int type;
    private Object data;
    
    public static int LOGIN_OK = 1;
    public static int LCLIENTS = 2;
    public static int PAUSE_RESUME = 3;
    public static int STOP = 4;
    
    public static int LOGIN_FAIL = 501;

    
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public ReponseADMIN(int type) {
        this.type = type;
        this.data = null;
    }
    
    public ReponseADMIN(int type, Object ob) {
        this.type = type;
        this.data = ob;
    }
    
    public ReponseADMIN()
    {
        this.type = 0;
        this.data = null;
    }

    public Object getObject() {
        return data;
    }

    public void setObject(Object data) {
        this.data = data;
    }
    
    
    public void EnvoieReponse_JAVA(Socket cliSocket) throws IOException 
    { 
        ObjectOutputStream oos; 
        oos = new ObjectOutputStream(cliSocket.getOutputStream());
        oos.writeObject(this); 
        oos.flush();

    }
    
    public void RecevoirReponse_JAVA(Socket CSocket) throws IOException, ClassNotFoundException
    { 
        ObjectInputStream ois=null; 
        ReponseADMIN temp = new ReponseADMIN();
        System.out.println("En attente d'une requete" + CSocket.toString());
        ois = new ObjectInputStream(CSocket.getInputStream());
        temp= (ReponseADMIN)ois.readObject();
        this.setType(temp.getType());
        this.setObject(temp.getObject());
        System.out.println("Requete lue par le serveur, instance de " + this.getClass().getName());
    }
    
    

}

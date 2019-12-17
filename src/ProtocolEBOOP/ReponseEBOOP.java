/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ProtocolEBOOP;

import interface_req_rep.Reponse;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

/**
 *
 * @author Dos Santos
 */
public class ReponseEBOOP implements Reponse, Serializable{
    
    public static int CLIENT_INCONNU = 501;
    public static int CREATION_FAIL = 502;
    public static int ANNULER_FAIL = 503;
    
    public static int CREATION_ACK = 202;
    public static int CLIENT_CONNU = 201;
    public static int ANNULER_ACK = 203;
    public static int TRAVERSEE = 204;
    public static int PORT_ID_NOM = 205;
    
    
    
    
    private int type; 
    private Object classe;
    
    public ReponseEBOOP(int c, Object classe) {
        setTypeRequete(c); 
        setObjectClasse(classe); 
    }
    
    public ReponseEBOOP() {
        setTypeRequete(0); 
        setObjectClasse(null); 
    }
    
    public int getTypeRequete() { return type; }
    public void setTypeRequete(int type) {
        this.type = type; 
    }
    
    public Object getObjectClasse() { return classe; }
    public void setObjectClasse(Object classe) {
        this.classe = classe; 
    }

    @Override
    public int getCode() {
        return type;
    }
    
    public void RecevoirReponse(Socket cliSock) throws IOException, ClassNotFoundException{
        
        ObjectInputStream ois = null;
        ReponseEBOOP temp = new ReponseEBOOP();
        ois = new ObjectInputStream(cliSock.getInputStream());
        temp = (ReponseEBOOP)ois.readObject();
        this.setObjectClasse(temp.getObjectClasse());
        this.setTypeRequete(temp.getTypeRequete());
        System.out.println(" *** Reponse recue : " + this.getTypeRequete());

    }
    
    public void EnvoieReponse(Socket CSocket) throws IOException{
        ObjectOutputStream oos; 
        oos = new ObjectOutputStream(CSocket.getOutputStream());
        oos.writeObject(this); 
        oos.flush(); 
    }
}

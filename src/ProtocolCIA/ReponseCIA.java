/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ProtocolCIA;

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
public class ReponseCIA implements Reponse, Serializable{
    public static int LOGIN_OK = 201;
    public static int BOOKING_OK = 202;
    public static int VOYAGEUR_CREE_ACK = 203;
    public static int TICKET_ACK = 204;

    
    public static int LOGIN_FAIL = 501;
    public static int BOOKING_FAIL = 502;
    public static int VOYAGEUR_INTROUVABLE = 503;
    public static int VOYAGEUR_CREE_FAIL = 504;
    public static int TICKET_SOLDE_INSUFFISANT = 505;
    public static int TICKET_NAVIRE_COMPLET = 506;
    public static int TICKET_AUCUNE_TRAVERSEE = 507;
    public static int TICKET_CARTE_ERRONE = 508;

    
    
    
    
    private int type; 
    private Object classe;
    
    public ReponseCIA(int c, Object classe) {
        setTypeRequete(c); 
        setObjectClasse(classe); 
    }
    
    public ReponseCIA() {
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
        ReponseCIA temp = new ReponseCIA();
        ois = new ObjectInputStream(cliSock.getInputStream());
        temp = (ReponseCIA)ois.readObject();
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

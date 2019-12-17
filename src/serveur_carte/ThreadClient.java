/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serveur_carte;

import database.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import requeteCard.Reponse_card;
import requeteCard.Requete_card;

/**
 *
 * @author damien
 */
public class ThreadClient extends Thread{ 
    private Socket Sock;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Statement instruc;
    private Requete_card req;
    private Reponse_card rep;
    
    
    public ThreadClient(Socket s, Statement st) 
    {
        Sock = s;
        instruc = st;
        
        
    }
    public void run() 
    {
        int ret;
        try 
        {
            while(!isInterrupted())
            {
                System.out.println("Serveur carte: thread crée : en attente d'une requete");
                ois = new ObjectInputStream(Sock.getInputStream());
     
                req = (Requete_card)ois.readObject();
                
                System.out.println("Num carte : " + req.getNumero_carte() + " Montant : " + req.getMontant());
                ret = facily_CHECKIN.MiseAJourSolde(req.getNumero_carte(),req.getMontant(), instruc);
                switch (ret)
                {
                    case 1:
                        rep = new Reponse_card(Reponse_card.PAIEMENT_OK);
                        break;
                    
                    case -1:
                        rep = new Reponse_card(Reponse_card.CARTE_ERRONEE);
                        break;
                        
                    case -2:
                        rep = new Reponse_card(Reponse_card.SOLDE_INSUFFISANT);
                        break;
                }
                oos = new ObjectOutputStream(Sock.getOutputStream());
                oos.writeObject(rep); 
                oos.flush(); 
            }
            System.out.println("je sors");
        } catch (IOException | ClassNotFoundException  | SQLException  ex) {
            Logger.getLogger(ThreadClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}

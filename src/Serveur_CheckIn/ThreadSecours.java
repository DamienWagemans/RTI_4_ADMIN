/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Serveur_CheckIn;

import ProtocolHAFISCA.Client_connecte;
import ProtocolHAFISCA.RequeteADMIN;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author damien
 */
public class ThreadSecours extends Thread
{
    private ThreadServeur threadServeur = null;
    private ServerSocket SSocket_Secours = null;
    
    private Vector<Client_connecte> sock_connected_clients = null;
     
    public ThreadSecours (ThreadServeur ts)
    {
        this.threadServeur = ts;
        this.SSocket_Secours = ts.getSSocket_Secours();
        sock_connected_clients = new Vector<Client_connecte>();
    }

    public Vector<Client_connecte> getSock_connected_clients() {
        return sock_connected_clients;
    }

    public void setSock_connected_clients(Vector<Client_connecte> sock_connected_clients) {
        this.sock_connected_clients = sock_connected_clients;
    }
    
    
    
    @Override
    public void run()
    {
        while(!isInterrupted())
        {
            try 
            {
                //ce thread se contente uniquement d'acception les connexions venant des clients, de lire l'identifiant qui leur
                //a été attribué par le serveur compagnie et d'ajouter le tout sa liste des clien connecté a ne pas confondre avec
                //le vector de string dans ThreadServeur.
                //Ce sera le thread administrateur, qui sera chargé
                // de venir récuperer la liste des sockets et ainsi pour envoyer des instructions a tout
                // les client connectés. !!
                System.out.println("Thread secour en attente de connexion");
                Socket connected_sock = SSocket_Secours.accept();
                System.out.println("Thread secour client connecté");
                RequeteADMIN req = new RequeteADMIN();
                req.RecevoirRequete_JAVA(connected_sock);
                //donc la un client vient de se connecter, j'ajoute a la liste des clients la socket liée a l'identifiant
                sock_connected_clients.add(new Client_connecte(connected_sock, (long)req.getObject()));
            } 
            catch (IOException ex) 
            {
                Logger.getLogger(ThreadSecours.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ThreadSecours.class.getName()).log(Level.SEVERE, null, ex);
            }

            
        }
    }
}

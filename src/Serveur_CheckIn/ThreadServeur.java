/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Serveur_CheckIn;

import ProtocolCIA.RequeteCIA;
import interface_req_rep.Requete;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author damien
 */
public class ThreadServeur extends Thread{
    private int port;
    private int nbr_client;
    private SourceTaches tachesAExecuter;
    
    private ServerSocket SSocket_CheckINApp = null;
    private ServerSocket SSocket_Admin = null;
    private ServerSocket SSocket_Secours = null;
    
    //Contiendra les clients connectés, lorsqu'un createrenable (requeteCIA) sera crée,
    // le runnable a la reference de threadserveur, il peut donc acceder a cette liste et ajouter des clients
    //dès qu'on sort du runnable, on supprime le client de la liste
    private Vector<String> Connected_Clients = null;
    private boolean serveur_en_pause = false;
    
    private Socket socket_carte = null;
    private RequeteCIA req = new RequeteCIA();
    private Statement instruc;

    //chauqe client qui se connecte aura un identifiant. Le thread secour aura une liste de socket liée a un identifiant
    // quand un cleint se deconnecte, il prévient le serveur compagnie via la port 50000, en donnant son id,
    // le serveur compagnie peut donc enlever la socket de la liste du thread secour
    public long id = 0;
    
    //quand un client va se déconnecter, il va falloir que je supprime sa socket dans la liste du thread secours
    // donc quand un client se déconnecte, il envoit une requete STOP au thread client, comme d'ab mais en plus deca,
    //il envoit sa socket, le thread client pourra donc supprimer la socket de la liste
    
    //ca marche pas, socket pas serializable..
    public ThreadSecours TS = null;
    
    
    
    public boolean isServeur_en_pause() {
        return serveur_en_pause;
    }

    public void setServeur_en_pause(boolean serveur_en_pause) {
        this.serveur_en_pause = serveur_en_pause;
    }

    public ServerSocket getSSocket_CheckINApp() {
        return SSocket_CheckINApp;
    }

    public void setSSocket_CheckINApp(ServerSocket SSocket_CheckINApp) {
        this.SSocket_CheckINApp = SSocket_CheckINApp;
    }

    public Vector<String> getConnected_Clients() {
        return Connected_Clients;
    }

    public void setConnected_Clients(Vector<String> Connected_Clients) {
        this.Connected_Clients = Connected_Clients;
    }

    public ServerSocket getSSocket_Admin() {
        return SSocket_Admin;
    }

    public void setSSocket_Admin(ServerSocket SSocket_Admin) {
        this.SSocket_Admin = SSocket_Admin;
    }

    public ServerSocket getSSocket_Secours() {
        return SSocket_Secours;
    }

    public void setSSocket_Secours(ServerSocket SSocket_Secours) {
        this.SSocket_Secours = SSocket_Secours;
    }
    
    public ServerSocket getSSocket() {
        return SSocket_CheckINApp;
    }

    public void setSSocket(ServerSocket SSocket) {
        this.SSocket_CheckINApp = SSocket;
    }

    public Socket getSocket_carte() {
        return socket_carte;
    }

    public void setSocket_carte(Socket socket_carte) {
        this.socket_carte = socket_carte;
    }

    public Statement getInstruc() {
        return instruc;
    }

    public void setInstruc(Statement instruc) {
        this.instruc = instruc;
    }
    
    
    public ThreadServeur(int p, int nbr_c, SourceTaches st, Socket socket_c) 
    {
        nbr_client = nbr_c;
        port = p; 
        tachesAExecuter = st; 
        socket_carte = socket_c;
        Connected_Clients = new Vector<String>();
    }
    
    @Override
    public void run() 
    {
        try 
        {
            //pour la reception des clients CheckIn
            SSocket_CheckINApp = new ServerSocket(port);
            //pour la connexion de l'administrateur
            SSocket_Admin = new ServerSocket(port+1);
            //lorsqu'un client CheckInApp se connecte, il se connecte en meme temps sur un autre port de secours
            // ainsi, un client a donc deux flux vers le serveur : 1 pour les commandes traditionnels (buy ticket , etc..)
            // et un deuxieme flux pour les ordres d'administrztion donnée par l'administrateur
            SSocket_Secours = new ServerSocket(port+2);
            
            
            //le thread secours sera en attente pour les doubles connexions de clients
            // le thread second aura donc les sockets de tout les clients connecté 
            ThreadSecours TS = new ThreadSecours(this);
            //le thread admin peut envoyé des ordres au thread secours pour dire par exemple a tout ses client de se 
            //mettre en pause, ..
            ThreadAdministrateur TA = new ThreadAdministrateur(this, TS);
            this.TS = TS;
            TS.start();
            TA.start();
            
            
            
        }
        catch (IOException e) 
        {
            System.err.println("Erreur de port d'écoute ! ? [" + e + "]"); 
            System.exit(1); 
        }
        // Démarrage du pool de threads
        for (int i=0; i<nbr_client; i++)
        {
            ThreadClient thr = new ThreadClient (tachesAExecuter, "Thread du pool n°" + String.valueOf(i));
            thr.start(); 
        }
        try{
            //Connexion à la base de donnée
            System.out.println("Essai de connexion JDBC");
            Class leDriver= Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection con= DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/ORCL","BD_FERRIES","Damien");
            //Connection con= DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE","RTI","unizuniz1999");
            con.setAutoCommit(false);
            System.out.println("Connexion à la BDD RTI réalisée");
            instruc= con.createStatement();
            System.out.println("Création d'une instance d'instruction pour cette connexion");
            
            Socket CSocket;
            while (!Thread.interrupted()) 
            {
               
                System.out.println("************ Serveur en attente");
               
                CSocket = SSocket_CheckINApp.accept(); 
                System.out.println(CSocket.getRemoteSocketAddress().toString()+ " / accept / thread serveur");
                
                System.err.println("Voici l'etat du serveur  "+ this.isServeur_en_pause());
                ObjectInputStream ois=null; 
                ois = new ObjectInputStream(CSocket.getInputStream());
                Requete temp= (Requete) ois.readObject();
                
                System.out.println(temp.getClass().getName());
                Runnable travail = temp.createRunnable(CSocket, socket_carte, instruc, this); 
                if (travail != null)
                {
                    
                    tachesAExecuter.recordTache(travail);


                     System.out.println("Travail mis dans la file"); 
                }
                else System.out.println("Pas de mise en file"); 
            }
        }catch (SQLException  e) {
            Logger.getLogger(ThreadServeur.class.getName()).log(Level.SEVERE, null, e);
        }catch (ClassNotFoundException ex) {
            Logger.getLogger(ThreadServeur.class.getName()).log(Level.SEVERE, null, ex);
        }catch (IOException e) 
        {
            System.err.println("Erreur d'accept ! ? [" + e.getMessage() + "]"); 
            System.exit(1); 
        }

        

    } 
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ProtocolCIA;

import ClassesCIA.Booking;
import ClassesCIA.Login;
import ClassesCIA.Reservation;
import ClassesCIA.Ticket;
import ClassesCIA.Voyageur;
import interface_req_rep.Requete;
import database.*;
import static divers.Config_Applic.pathLogin;
import divers.Persistance_Properties;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import requeteCard.Reponse_card;
import requeteCard.Requete_card;
/**
 *
 * @author Dos Santos
 */
//SERVEUR RECOIT UNE REQUETE ET ENVOIE UNE REPONSE
//CLIENT ENVOIE UNE REQUETE ET RECOIT UNE REPONSE
public class RequeteCIA implements Requete , Serializable{
    
    public static int LOGIN = 1;
    public static int VERIF_BOOKING = 2;
    public static int BUY_TICKET = 3;
    public static int CREATION_VOYAGEUR = 4;
    
    public static int STOP = -1;
    
    private int type;
    private Object classe = null;
    private Properties myProperties;
    
     public RequeteCIA() 
    {
        myProperties = Persistance_Properties.LoadProp(pathLogin);
        setTypeRequete(0); 
        setObjectClasse(null); 
    }
    public RequeteCIA(int t, Object classe) 
    {
        myProperties = Persistance_Properties.LoadProp(pathLogin);
        setTypeRequete(t); 
        setObjectClasse(classe); 
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
    public Runnable createRunnable(Socket s, Socket Sock_card, Statement instruc) {
        return new Runnable() 
        {
            public void run() {
                TraitementRequete(s,Sock_card, instruc);
            }
        };
    }
    
    public void TraitementRequete(Socket s, Socket Sock_card, Statement instruct) 
    {
        int Login = 0;
        try 
        {
            
            while(this.getTypeRequete() != -1)
            {
                while (Login == 0)
                {
                    Login = TraiterRequeteLogin(s, instruct);
                    this.RecevoirRequete(s);
                }
                
                switch(this.getTypeRequete())
                {
                    case 2:
                        TraiterRequeteBooking(s, instruct);
                        break;
                    case 3: 
                        TraiterTicket(s,Sock_card, instruct);
                        break;
                    case 4:
                        Creation_Voyageur(s, instruct);
                      
                }
                this.RecevoirRequete(s);

            } 
        } catch (IOException ex ) {
            Logger.getLogger(RequeteCIA.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(RequeteCIA.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(RequeteCIA.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    
    public int TraiterRequeteLogin(Socket sock, Statement instruc) throws IOException
    {
        
        RequeteCIA req = new RequeteCIA();
        Login log = new Login((Login)getObjectClasse());
        String adresseDistante = sock.getRemoteSocketAddress().toString(); 
        System.out.println("Début de traiteRequete : adresse distante = " + adresseDistante);
        //String password = (String)tablePwdNoms.get(log.getUsername());
        String pass = myProperties.getProperty(log.getUsername());
        
        ReponseCIA rep;
        if(pass != null){
            rep = new ReponseCIA(ReponseCIA.LOGIN_OK, null);
            System.out.println(adresseDistante+" / User "+ log.getUsername() + " : Login OK / "  +Thread.currentThread().getName());
            rep.EnvoieReponse(sock);
            return 1;
        }    
        else
        {
            rep = new ReponseCIA(ReponseCIA.LOGIN_FAIL, null);
            System.out.println(adresseDistante+" / User "+ log.getUsername() + " : Bad Login  / " +Thread.currentThread().getName());
            rep.EnvoieReponse(sock);
            return -1;
        }

    }
    
    public void TraiterRequeteBooking(Socket sock, Statement instruc) throws IOException, SQLException {
        String adresseDistante = sock.getRemoteSocketAddress().toString(); 
        Booking book = new Booking((Booking)this.getObjectClasse());
        ReponseCIA rep = new ReponseCIA(ReponseCIA.BOOKING_FAIL, null);
        System.out.println(adresseDistante+" / Code Reservation : "+ book.getCode_Reservation() + " / Titulaire : "+ book.getCode_Titulaire() + "#" +Thread.currentThread().getName());
        facility SqlInstruct  = new facility();
        ResultSet rs2 = SqlInstruct.SelectAllRowFromTable("RESERVATIONS", instruc);
        int cpt =0;
        while (rs2.next())
        {
            if(rs2.getString("ID_RESERVATION").equals(book.getCode_Reservation()) && rs2.getString("TITULAIRE").equals(book.getCode_Titulaire())){
                rep.setTypeRequete(ReponseCIA.BOOKING_OK);                    
                break;
            }
        }
        rep.EnvoieReponse(sock);
    }
    
    public void TraiterTicket(Socket sock, Socket Sock_card, Statement instruc) throws SQLException, IOException, ClassNotFoundException
    {
        String adresseDistante = sock.getRemoteSocketAddress().toString();
        String id_traverse;
        int montant;
        int ret;
                
        ReponseCIA rep = new ReponseCIA(ReponseCIA.VOYAGEUR_INTROUVABLE, null);
        
        Ticket ticket = new Ticket ((Ticket)this.getObjectClasse());
        System.out.println(adresseDistante+" / Buy Ticket : "+ ticket.getNom_conducteur() + " / " +Thread.currentThread().getName());
        facility SqlInstruct = new facility();
        ResultSet rs2 = SqlInstruct.SelectAllRowFromTable("VOYAGEURS", instruc);
        while(rs2.next())
        {
            //si je trouve le nom
            if(rs2.getString("NOM").equals(ticket.getNom_conducteur()))
            {
                //je regard esi traversée disponible prochainement
                System.out.println("Nom trouvé");
                id_traverse = facily_CHECKIN.rech_traversee (instruc);
                if(id_traverse.equals("Aucune traversée"))
                {
                    System.out.println("Aucune traversée");
                    rep.setTypeRequete(ReponseCIA.TICKET_AUCUNE_TRAVERSEE);
                    System.out.println(adresseDistante+" / Aucune traversee / " +Thread.currentThread().getName());
                    break;
                }
                else
                {
                    //si traversée disponible, je regarde si place disponible
                    System.out.println("Traversée trouvée ");
                    ret = facily_CHECKIN.Verif_place_navire(id_traverse, 1, instruc);
                    if(ret == -1)
                    {
                        System.out.println("Navire complet");
                        rep.setTypeRequete(ReponseCIA.TICKET_NAVIRE_COMPLET);
                        System.out.println(adresseDistante+" / Navire complet / " +Thread.currentThread().getName());
                        break;
                    }
                    //si place disponible, j'applique le paiment vers serveur card
                    else
                    {
                        System.out.println("Tout ok, j'accede a server card");
                        //calcul montant
                        montant = (random (1, 3) * ticket.getNombre_passager() * 1000);
                        //envoi de la requete au server card
                        Requete_card req_card = new Requete_card(montant, ticket.getNum_carte());
                        ObjectOutputStream oos = new ObjectOutputStream(Sock_card.getOutputStream());
                        oos.writeObject(req_card); 
                        oos.flush();
                        System.out.println("Requete envoyée au serveur card!");
                        
                        ObjectInputStream ois = new ObjectInputStream(Sock_card.getInputStream());
                        Reponse_card rep_card = (Reponse_card)ois.readObject();
                        
                        
                        if(rep_card.getType() == Reponse_card.CARTE_ERRONEE)
                        {
                            System.out.println("Requete recue du serveur card : carte eronnee");
                            System.out.println(adresseDistante+" / Carte invalide / " +Thread.currentThread().getName());
                            rep.setTypeRequete(ReponseCIA.TICKET_CARTE_ERRONE);
                        }
                        else if(rep_card.getType() == Reponse_card.SOLDE_INSUFFISANT)
                        {
                            System.out.println("Requete recue du serveur card : solde insuffisant");
                            System.out.println(adresseDistante+" / Solde insuffisant / " +Thread.currentThread().getName());
                            rep.setTypeRequete(ReponseCIA.TICKET_SOLDE_INSUFFISANT);
                        }
                        else if(rep_card.getType() == Reponse_card.PAIEMENT_OK)
                        {
                            System.out.println("Requete recue du serveur card : paiment ok");
                            rep.setTypeRequete(ReponseCIA.TICKET_ACK);
                            
                            //creation du machine reservation ici !
                            Reservation res = new Reservation();
                            res.creation_reservation_DB(id_traverse, ticket.getNom_conducteur(), instruc);
                            rep.setObjectClasse(res);
                            System.out.println(adresseDistante+" / Reservation crée : "+ res.getID() + " / "+ Thread.currentThread().getName());
                            System.out.println("Reservation crée !");
                        }

                    }
                    
                }           
                break;
            }
        }
        rep.EnvoieReponse(sock);
        
    }
    
    public void Creation_Voyageur(Socket sock, Statement instruc) throws IOException
    {
        String adresseDistante = sock.getRemoteSocketAddress().toString(); 
        
        
        Voyageur v = new Voyageur ((Voyageur)this.getObjectClasse());
        int num_client = v.hashCode();
        System.out.println("voyageur : " + v.getImmatriculation() + " " + v.getMail());
        String Champs = ("'"+ Integer.toString(num_client)  +"', '" + v.getNom() + "','" + v.getPrenom() + "','" +  v.getAdresse() + "','" + v.getImmatriculation()+ "','" + v.getMail() + "'");
        facility SqlInstruct = new facility();
        try 
        {
            SqlInstruct.InsertIntoTable("VOYAGEURS",  Champs,  instruc);
            System.out.println(adresseDistante+" / Ajout du voyageur : "+ v.getNom()+ " / " +Thread.currentThread().getName());
            ReponseCIA rep = new ReponseCIA(ReponseCIA.VOYAGEUR_CREE_ACK, null);
            rep.setObjectClasse(v);
            rep.EnvoieReponse(sock);
        } 
        catch (SQLException e) 
        {
            ReponseCIA rep = new ReponseCIA(ReponseCIA.VOYAGEUR_CREE_FAIL, null);
            rep.EnvoieReponse(sock);
            System.out.println("Error Occured : \n" + e.getLocalizedMessage());
        }
        
    }
   
    public void EnvoieRequete(Socket cliSocket) throws IOException 
    { 
        ObjectOutputStream oos; 
        oos = new ObjectOutputStream(cliSocket.getOutputStream());
        oos.writeObject(this); 
        oos.flush();

    }
    
    public void RecevoirRequete(Socket CSocket) throws IOException, ClassNotFoundException
    { 
        ObjectInputStream ois=null; 
        RequeteCIA temp = new RequeteCIA();
        System.out.println("En attente d'une requete" + CSocket.toString());
        ois = new ObjectInputStream(CSocket.getInputStream());
        temp= (RequeteCIA)ois.readObject();
        this.setTypeRequete(temp.getTypeRequete());
        this.setObjectClasse(temp.getObjectClasse());
        System.out.println("Requete lue par le serveur, instance de " + this.getClass().getName());
    }
    
    public int random(int high, int low) {
            return((int)(Math.random() * (high+1-low)) + low);
    }
        
}

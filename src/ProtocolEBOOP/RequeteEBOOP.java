/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ProtocolEBOOP;



import ClassesEBOOP.Client;
import ClassesEBOOP.Reservation;
import ClassesEBOOP.Traversee;
import Serveur_CheckIn.ThreadServeur;
import database.facility;
import interface_req_rep.Requete;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.sql.*;
import java.util.Hashtable;
import java.util.Vector;
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
public class RequeteEBOOP implements Requete , Serializable{
    

    public static int VERIF_NUM_CLIENT = 1;
    public static int CREATION_CLIENT = 2;
    public static int GET_TRAVERSEE = 3;
    public static int GET_TRAV_WITHOUT_RES = 4;
    public static int PAIEMENT_OK = 5;
    public static int ANNULER_RES = 6;
    public static int CREATION_RES = 7;
    public static int PAIEMENT = 8;
    public static int NOM_PORT = 9;

    
    public static int STOP = -1;
    
    private int type;
    private Object classe = null;

    
     public RequeteEBOOP() 
    {
        setTypeRequete(0); 
        setObjectClasse(null); 
    }
    public RequeteEBOOP(int t, Object classe) 
    {
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
    public Runnable createRunnable(Socket s, Socket Sock_card, Statement instruc, ThreadServeur ts) {
        return new Runnable() 
        {
            public void run() {
                try {
                    TraitementRequete(s,Sock_card, instruc);
                } catch (SQLException ex) {
                    Logger.getLogger(RequeteEBOOP.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
    }
    
    public void TraitementRequete(Socket s, Socket Sock_card, Statement instruct) throws SQLException 
    {
        try 
        {
            while(this.getTypeRequete() != -1)
            {   
                switch(this.getTypeRequete())
                {
                    case 1:
                        check_client(s, instruct);
                        break;
                    case 2:
                        Creation_client(s, instruct);
                        break;
                    case 3:
                        recuperation_traversee(s,instruct);
                        break;
                    case 4:
                        recup_trav_sans_res(s, instruct);
                        break;
                    case 5:
                        payement_res(s, instruct);
                        break;
                    case 6:
                        annuler_reservation(s, instruct);
                        break;
                    case 7:
                        creation_reservation(s, instruct);
                        break;
                    case 8:
                        paiement_serveur_carte(s, Sock_card, instruct);
                        break;
                    case 9:
                        get_nom_port(s, instruct);
                        break;
                }
                this.RecevoirRequete(s);

            } 
        } catch (IOException ex ) {
            Logger.getLogger(RequeteEBOOP.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(RequeteEBOOP.class.getName()).log(Level.SEVERE, null, ex);
        }     
    }
	
    public void paiement_serveur_carte(Socket s, Socket s_card,Statement instruct) throws IOException, ClassNotFoundException{
            Vector<Object> v = (Vector<Object>)getObjectClasse();
            ReponseEBOOP rep = new ReponseEBOOP();
            //on recupere le montant + le code la carte
            Requete_card req_card = new Requete_card((int)v.get(0), (String)v.get(1));
            ObjectOutputStream oos = new ObjectOutputStream(s_card.getOutputStream());
            oos.writeObject(req_card); 
            oos.flush();
            System.out.println("Requete envoyée au serveur card!");
            ObjectInputStream ois = new ObjectInputStream(s_card.getInputStream());
            Reponse_card rep_card = (Reponse_card)ois.readObject();
            rep.setObjectClasse(rep_card.getType());
            rep.EnvoieReponse(s);
            System.out.println("Requete envoyée au servlet " + (int)rep.getObjectClasse());
    }

    public void payement_res(Socket s, Statement instruct)throws SQLException, IOException{
            Vector<Reservation> vect_res = (Vector<Reservation>)getObjectClasse();
            for(int i = 0; i < vect_res.size(); i++){
                    vect_res.get(i).payement_reservation(instruct);
            }

    }

    public void annuler_reservation(Socket s, Statement instruct) throws SQLException, IOException{
            ReponseEBOOP rep = new ReponseEBOOP();
            Vector<Reservation> vect_res = (Vector<Reservation>)getObjectClasse();
            for(int i=0;i<vect_res.size(); i++){
                    vect_res.get(i).supprimer_reservation_db(instruct);
            }
            vect_res.removeAllElements();
            rep.setTypeRequete(ReponseEBOOP.ANNULER_ACK);
            rep.setObjectClasse(null);
            rep.EnvoieReponse(s);
    }

    public void creation_reservation(Socket s, Statement instruct) throws SQLException, IOException{
            ReponseEBOOP rep = new ReponseEBOOP();		
            String info = (String)getObjectClasse();
            String split[] = info.split(";");
            System.out.println("id trav :" + split[0] + "nom client: " + split[1]);
            //on recupere les traversées correspondante en verifier si il y a toujours de la place
            Vector<Traversee> vect_trav = recup_trav(instruct);
            
            Vector<Reservation> vect_res = new Vector();
            for(int i = 0; i< vect_trav.size(); i++ ){						
                Traversee t = vect_trav.get(i);

                //on recupere l'id de la traversée
                String temp = split[0].substring(0, t.getId().length());
                if(t.getId().equals(temp))
                {
                    //je crée une reservation a partir de l'id de traversée
                    Reservation res = new Reservation(temp);
                    //split 1 vaut le nom du client
                    //je crée une reservation avec le champs payé à N
                    res.creation_reservation_DB(temp, split[1], instruct);
                    vect_res.add(res);
                }
            }
            if(!vect_res.isEmpty()){
                rep.setTypeRequete(ReponseEBOOP.CREATION_ACK);
                rep.setObjectClasse(vect_res);
            }
            else{
                //dans le cas ou la place aurait été prise par une autre session 
                rep.setTypeRequete(ReponseEBOOP.CREATION_FAIL);
                rep.setObjectClasse(null);
            }
            rep.EnvoieReponse(s);
    }
    
    public void check_client(Socket sock, Statement instruc) throws SQLException, IOException
    {
        Client c = (Client)this.getObjectClasse();
        c = database.facily_EBOOP.getClient(c.getId_client(), instruc);
        ReponseEBOOP rep = new ReponseEBOOP();
        if (c == null)
        {
            rep.setTypeRequete(ReponseEBOOP.CLIENT_INCONNU);
        }
        else
        {
            rep.setTypeRequete(ReponseEBOOP.CLIENT_CONNU);
            rep.setObjectClasse(c);
        }
        rep.EnvoieReponse(sock);
    }
    
    
    public void Creation_client(Socket sock, Statement instruc) throws IOException
    {           
        Client c = new Client ();
        c = ((Client)this.getObjectClasse());
        
        int num_client = c.hashCode();
        c.setId_client(Integer.toString(num_client));
        String Champs = ("'"+ Integer.toString(num_client)  +"', '" + c.getNom() + "','" + c.getPrenom() + "','" +  c.getAdresse() + "','" + c.getImmatriculation()+ "','" + c.getEmail()+ "'");
        facility SqlInstruct = new facility();
        try 
        {
            SqlInstruct.InsertIntoTable("VOYAGEURS",  Champs,  instruc);
            ReponseEBOOP rep = new ReponseEBOOP(ReponseEBOOP.CREATION_ACK, c);
            rep.EnvoieReponse(sock);
        } 
        catch (SQLException e) 
        {
            ReponseEBOOP rep = new ReponseEBOOP(ReponseEBOOP.CREATION_FAIL, null);
            rep.EnvoieReponse(sock);
        }
        
    }
    
    public void recuperation_traversee(Socket s, Statement instruct) throws SQLException, IOException
    {
        Vector<Traversee> vec_send = recup_trav(instruct);
        ReponseEBOOP rep = new ReponseEBOOP(ReponseEBOOP.TRAVERSEE, vec_send);
        rep.EnvoieReponse(s);
    }
	
    public Vector<Traversee> recup_trav(Statement instruct) throws SQLException
    {
        Vector<Traversee> vec_tmp = database.facily_EBOOP.rech_traversee(instruct);
        Vector<Traversee> vec_send = new Vector<Traversee>();

        //je verifie si il y a de la place disponible dans les taversées
        for(int i =0; i < vec_tmp.size(); i++)
        {
            Traversee t = vec_tmp.get(i);
            if(database.facily_EBOOP.Verif_place_navire(t.getId(), 1, instruct) == 1)
            {
                vec_send.add(t);
            }
        }
        return vec_send;
    }
    
    public void get_nom_port (Socket s, Statement instruc) throws SQLException, IOException
    {
        Hashtable hash = new Hashtable();
        hash = database.facily_EBOOP.get_port_id_nom(instruc);
        ReponseEBOOP rep = new ReponseEBOOP(ReponseEBOOP.PORT_ID_NOM, hash);
        rep.EnvoieReponse(s);
    }
	
	public void recup_trav_sans_res(Socket s, Statement instruct) throws SQLException, IOException{
		
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
        RequeteEBOOP temp = new RequeteEBOOP();
        System.out.println("En attente d'une requete" + CSocket.toString());
        ois = new ObjectInputStream(CSocket.getInputStream());
        temp= (RequeteEBOOP)ois.readObject();
        this.setTypeRequete(temp.getTypeRequete());
        this.setObjectClasse(temp.getObjectClasse());
        System.out.println("Requete lue par le serveur, instance de " + this.getClass().getName());
    }
    

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import ClassesEBOOP.Client;
import ClassesEBOOP.Traversee;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author damien
 */
public class facily_EBOOP extends facility{
    public static int MiseAJourSolde(String NumCarte, int Montant, Statement instruct) throws SQLException
    {
        ResultSet rs;
        String compte_id;
        int montant_compte = 0;
        rs = SelectRowFromTable("COMPTE_ID", "CARTE_CREDIT", "NUMERO_CARTE = '" + NumCarte + "'", instruct);
        if(!rs.next())
        {
            System.out.println("Carte de credit introuvable");
            return -1;
        }
        else
        {
            compte_id = rs.getString("COMPTE_ID");
            System.out.println("ID du compte correspondant a la carte : " + compte_id);
            rs = SelectRowFromTable("MONTANT", "COMPTE", "COMPTE_ID = '" + compte_id + "'", instruct);
            rs.next();

            System.out.println(montant_compte);
            if(Montant > montant_compte)
            {
                System.out.println("Solde insuffisant !");
                return -2;
            }
            else
            {
                montant_compte = montant_compte - Montant;
                instruct.executeQuery("UPDATE COMPTE set MONTANT = '" + montant_compte + "' WHERE COMPTE_ID = '" + compte_id + "'");
                return 1;
            }
            
        }
    }
    
    public static int Verif_place_navire (String id_traversee, int nb_place, Statement instruct) throws SQLException
    {
        ResultSet rs;
        int capacite;
        int place_prise;
        
        rs = instruct.executeQuery("SELECT CAPACITE_LEGER FROM NAVIRES N INNER JOIN TRAVERSEES T ON N.MATRICULE = T.NAVIRE_ID WHERE T.ID_TRAVERSEE = '" + id_traversee + "'");
        rs.next();
        capacite = rs.getInt("CAPACITE_LEGER");
        System.out.println("place_res : " + capacite);
        
        rs = instruct.executeQuery("SELECT count (*) as ok from (SELECT * FROM RESERVATIONS R INNER JOIN TRAVERSEES T on  R.TRAVERSEE = T.ID_TRAVERSEE INNER JOIN NAVIRES N on T.NAVIRE_ID = N.MATRICULE WHERE T.ID_TRAVERSEE = '" + id_traversee + "')");
        rs.next();
        place_prise = rs.getInt("ok");
        System.out.println("place_prise : " + place_prise);
        
        place_prise = place_prise + nb_place;
        
        if(place_prise > capacite)
        {
            return -1;
        }
        else
        {
            return 1;
        }
        
    }
    
    public static synchronized Vector<Traversee> rech_traversee (Statement instruct) throws SQLException
    {
        Vector<Traversee> vec_trav = new Vector<>();
        //vec_trav = null;
        ResultSet rs;
        int year, month, day;
        Date maintenant = new Date(); 


        year = maintenant.getYear() + 1900;
        month = maintenant.getMonth() + 1;
        day = maintenant.getDate();
      
        
        //Format formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        //String s = formatter.format(date);
        String date = (day + "/" + month + "/" + year);
        rs = instruct.executeQuery("select * from traversees where depart >= '" + date + "'");
        System.out.println(maintenant);
        
        while(rs.next())
        {
            Traversee trav = new Traversee();
            trav.setDepart(rs.getDate("DEPART").toString());
            trav.setId(rs.getString("ID_TRAVERSEE"));
            trav.setPort_depart(rs.getString("PORT_DEPART"));
            trav.setPort_destination(rs.getString("PORT_DESTINATION"));
            vec_trav.add(trav);
            
        }
        return vec_trav;

    } 

    public static Client getClient(String num_client, Statement instruc) throws SQLException
    {
        ResultSet rs;
        rs = SelectRowFromTable("*", "VOYAGEURS", "NUM_CLIENT = '" + num_client + "'", instruc);
        if(!rs.next())
        {
            return null;
        }
        else
        {
            Client client = new Client (num_client, rs.getString("NOM"), rs.getString("PRENOM"));
            client.setId_client(num_client);
            client.setEmail(rs.getString("MAIL"));
            client.setImmatriculation(rs.getString("IMMATRICULATION"));
            client.setAdresse(rs.getString("ADRESSE"));
            return client;
        }
    }
    public static void Payement_Reservation(String id_res, Statement instruct) throws SQLException{
        instruct.executeQuery("UPDATE RESERVATIONS SET PAYE = 'O' WHERE ID_RESERVATION = '" + id_res + "'");
        instruct.executeQuery("COMMIT");
    } 
        
    public static Hashtable get_port_id_nom(Statement instruc) throws SQLException
    {
        ResultSet rs;
        rs = instruc.executeQuery("Select * from PORTS");
        Hashtable hash = new Hashtable();
        while(rs.next())
        {
            hash.put(rs.getString("NUMERO"), rs.getString("NOM"));
        }
        return hash;
    }
    
}

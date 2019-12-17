
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 *
 * @author damien
 */
public class facily_CHECKIN extends facility{
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
            rs = SelectRowFromTable("MONTANT", "COMPTE", "COMPTE_ID = '" + compte_id + "'", instruct);
            rs.next();
			montant_compte = rs.getInt("MONTANT");
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
    
    public static synchronized String rech_traversee (Statement instruct) throws SQLException
    {
        ResultSet rs;
        String id_traversee;
        Date Date_trav = new Date();
        Date maintenant = new Date(); 
        String maDate = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.FRANCE).
        format(maintenant);
        System.out.println("Date et heure = " + maDate);
        rs = instruct.executeQuery("SELECT * FROM TRAVERSEES ORDER BY DEPART");
        while(rs.next())
        {
            Date_trav = rs.getDate("DEPART");
            System.out.println(Date_trav);
            System.out.println(maintenant);
             
            if(Date_trav.after(maintenant))
            {
                id_traversee = rs.getString("id_traversee");
                System.out.println(id_traversee);
                return id_traversee;
            }
        }
        return "Aucune traversée";
    }   
    
    public static String getIDClient_with_name(String name, Statement instruc) throws SQLException
    {
        ResultSet rs;
        String IDClient;

        rs = SelectRowFromTable("NUM_CLIENT", "VOYAGEURS", "NOM = '" + name + "'", instruc);
        if(!rs.next())
        {
            return "Pas trouvé";
        }
        else
        {
            
            IDClient = rs.getString("NUM_CLIENT");
            System.out.println("NUM CLIENT : " + IDClient);
            return IDClient;
        }
    }
    
    public static Date getHeure_with_id(String ID_Traversee, Statement instruc) throws SQLException
    {
        ResultSet rs;
        Date heure;

        rs = SelectRowFromTable("DEPART", "TRAVERSEES", "ID_TRAVERSEE = '" + ID_Traversee + "'", instruc);
        if(!rs.next())
        {
            return null;
        }
        else
        {
            heure = rs.getDate("DEPART");
            System.out.println("HEURE DEPART : " + heure);
            return heure;
        }
    }
    public static String getNomFerry_with_idTrav(String ID_Trav, Statement instruc) throws SQLException
    {
        ResultSet rs;
        String NomFerry;
        
        rs = instruc.executeQuery("SELECT NOM FROM NAVIRES N INNER JOIN TRAVERSEES T ON N.MATRICULE = T.NAVIRE_ID WHERE T.ID_TRAVERSEE = '" + ID_Trav + "'");
        if(!rs.next())
        {
            return null;
        }
        else
        {
            NomFerry = rs.getString("NOM");
            System.out.println("NOM DU FERRY : " + NomFerry);
            return NomFerry;
        }
    }
     
    public static String getIDRes_with_IdClient_IdTrav(String ID_Trav, String ID_Client, Statement instruc) throws SQLException
    {
        ResultSet rs;
        String ID_Res;

        rs = SelectRowFromTable("ID_RESERVATION", "RESERVATIONS", "TITULAIRE = '" + ID_Client + "' AND TRAVERSEE = '" + ID_Trav + "'", instruc);
        if(!rs.next())
        {
            return "Pas trouvé";
        }
        else
        {
            ID_Res = rs.getString("ID_RESERVATION");
            System.out.println("ID RES : " + ID_Res);
            return ID_Res;
        }
    }
        
    public static void Payement_Reservation(String id_res, Statement instruct) throws SQLException{
		instruct.executeQuery("UPDATE RESERVATIONS SET PAYE = 'O' WHERE ID_RESERVATION = '" + id_res + "'");
		instruct.executeQuery("COMMIT");
	}    
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Dos Santos
 */

public class facility {
    
    public static synchronized ResultSet InsertIntoTable(String Table, String Champs, Statement instruc) throws SQLException
    {
        ResultSet rs;
        rs = instruc.executeQuery("INSERT INTO " + Table +  " VALUES (" + Champs +")");
        rs = instruc.executeQuery("COMMIT");
        System.out.println("Isertion effectuer");
        return rs;
    }
	
	public static synchronized void DeleteFromTableWhere(String Table, String Champs, Statement instruc) throws SQLException
    {
        ResultSet rs;
        rs = instruc.executeQuery("DELETE FROM " + Table +  " WHERE " + Champs);
        rs = instruc.executeQuery("COMMIT");
        System.out.println("Supression effectuer");
    }
    
    public static synchronized ResultSet SelectRowFromTable(String Champs, String Table, String where,Statement instruc)
    {
        try 
        {
            ResultSet rs;
            return rs = instruc.executeQuery("SELECT " + Champs + " FROM " + Table +  " WHERE " + where);
            
        } catch (SQLException ex) {
            Logger.getLogger(facility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static synchronized ResultSet SelectAllRowFromTable(String Table,Statement instruc)
    {
        try 
        {
            ResultSet rs;
            return rs = instruc.executeQuery("SELECT * FROM "  + Table);
            
        } catch (SQLException ex) {
            Logger.getLogger(facility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static ResultSet SelectVoy_ForTraversee_Date(String Tr, String date, Statement instruc)
    {
        try
        {
            ResultSet rs;
            return rs = instruc.executeQuery("SELECT NOM, NATIONALITE \n" +
                                             "FROM VOYAGEURS vo INNER JOIN RESERVATIONS re ON (re.TITULAIRE = vo.NUM_CLIENT)\n" +
                                             "                  INNER JOIN TRAVERSEES tr ON (re.TRAVERSEE = tr.ID_TRAVERSEE)\n" +
                                             "WHERE tr.DEPART = '" + date + "'  AND re.TRAVERSEE = '" + Tr + "'\n" +
                                             "ORDER BY vo.NATIONALITE");
        } catch (SQLException ex) {
            Logger.getLogger(facility.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    public static ResultSet Select_Moy_Age_From_Traversee(String Tr, Statement instruc)
    {
        try
        {
            ResultSet rs;
            return rs = instruc.executeQuery("SELECT AVG(trunc((SYSDATE - vo.DATE_NAISSANCE) / (365))) as age_moyen\n" +
                                             "FROM  VOYAGEURS vo INNER JOIN RESERVATIONS re ON (re.TITULAIRE = vo.NUM_CLIENT)\n" +
                                             "                   INNER JOIN TRAVERSEES tr ON (re.TRAVERSEE = tr.ID_TRAVERSEE)\n" +
                                             "WHERE re.TRAVERSEE = '" + Tr + "'");
        } catch (SQLException ex) {
            Logger.getLogger(facility.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ClassesEBOOP;

import database.facily_CHECKIN;
import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

/**
 *
 * @author damien
 */
public class Reservation implements Serializable{
    String ID;
    Date Depart;
    String ID_client;
    String Nom_ferry;
	String ID_traversee;

    public Reservation(String ID, Date Depart, String ID_client, String N) {
        this.ID = ID;
        this.Depart = Depart;
        this.ID_client = ID_client;
        this.Nom_ferry = N;
    }
	
	public Reservation(String ID_Trav) {
        this.ID = "";
        this.Depart = null;
        this.ID_client = "";     
        this.Nom_ferry = "";
        this.ID_traversee = ID_Trav;
    }

    public Reservation ()
    {
        this.ID = "";
        this.Depart = null;
        this.ID_client = "";     
        this.Nom_ferry = "";
    }
    
    public Reservation(Reservation r)
    {
        this.ID = r.ID;
        this.Depart = r.Depart;
        this.ID_client = r.ID_client;     
        this.Nom_ferry = r.Nom_ferry;  
    }

    public String getNom_ferry() {
        return Nom_ferry;
    }

    public void setNom_ferry(String Nom_ferry) {
        this.Nom_ferry = Nom_ferry;
    }
    
    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Date getDepart() {
        return Depart;
    }

    public void setDepart(Date Depart) {
        this.Depart = Depart;
    }

    public String getID_client() {
        return ID_client;
    }

    public void setID_client(String ID_client) {
        this.ID_client = ID_client;
    }
	public String getID_traversee() {
        return ID_traversee;
    }

    public void setID_traversee(String ID_traversee) {
        this.ID_traversee = ID_traversee;
    }
    
    public void creation_reservation_DB(String id_traverse, String Nom_conducteur, Statement instruc) throws SQLException
    {
        int year, month, day;
        this.Depart=facily_CHECKIN.getHeure_with_id(id_traverse, instruc);
        this.ID_client=facily_CHECKIN.getIDClient_with_name(Nom_conducteur, instruc);
        this.Nom_ferry= facily_CHECKIN.getNomFerry_with_idTrav(id_traverse, instruc);
        
        System.out.println("Date : " + this.Depart);
        
        year = Depart.getYear() + 1900;
        month = Depart.getMonth() + 1;
        day = Depart.getDate();

        //Format formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        //String s = formatter.format(date);
        String date = (day + "/" + month + "/" + year);
        System.out.println("Date : "+ date);
        String champs = ("'','" + date + "','" + id_traverse + "','" +  this.ID_client + "','N','N'");
        facily_CHECKIN.InsertIntoTable("RESERVATIONS", champs, instruc);
        //facility_CHECKIN.InsertIntoTable("VOYAGEURS",  Champs,  instruc);
        this.ID = facily_CHECKIN.getIDRes_with_IdClient_IdTrav(id_traverse, this.ID_client, instruc);
    }
	public void supprimer_reservation_db(Statement instruc) throws SQLException{
		String Champs = "ID_RESERVATION = " + "'"+this.ID+"'";
		facily_CHECKIN.DeleteFromTableWhere("RESERVATIONS", Champs, instruc);
	}
	public void payement_reservation(Statement instruct) throws SQLException{
		facily_CHECKIN.Payement_Reservation(this.ID, instruct);
	}
}

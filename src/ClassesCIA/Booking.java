/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ClassesCIA;

import java.io.Serializable;

/**
 *
 * @author Dos Santos
 */
public class Booking implements Serializable{
    private String Code_Reservation;
    private String Code_Titulaire;
    
    public String getCode_Titulaire(){
        return Code_Titulaire;
    }
    public void setCode_Titulaire(String nbr ){
        Code_Titulaire = nbr;
    }
    
    public String getCode_Reservation(){
        return Code_Reservation;
    }
    public void setCode_Reservation(String reserv){
        Code_Reservation = reserv;
    }
    
    public Booking(){
        setCode_Titulaire("");
        setCode_Reservation("");
    }
    public Booking(String nbr, String reserv){
        setCode_Titulaire(nbr);
        setCode_Reservation(reserv);
    }
    public Booking(Booking temp){
        setCode_Titulaire(temp.getCode_Titulaire());
        setCode_Reservation(temp.getCode_Reservation());
    }
}

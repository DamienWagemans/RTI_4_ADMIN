/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ClassesCIA;

import java.io.Serializable;

/**
 *
 * @author damien
 */
public class Ticket implements Serializable{
    private String Nom_conducteur;
    private String immatriculation;
    private int nombre_passager;
    private String num_carte;

    public Ticket (){
        this.Nom_conducteur = "";
        this.immatriculation = "";
        this.nombre_passager = 0;
        this.num_carte = "";
    }
    
    public Ticket(String Nom_conducteur, String immatriculation, int nombre_passager, String num_carte) {
        this.Nom_conducteur = Nom_conducteur;
        this.immatriculation = immatriculation;
        this.nombre_passager = nombre_passager;
        this.num_carte = num_carte;
    }
    public Ticket (Ticket temp){
        this.Nom_conducteur = temp.Nom_conducteur;
        this.immatriculation = temp.immatriculation;
        this.nombre_passager = temp.nombre_passager;
        this.num_carte = temp.num_carte;
    }
    
    public String getNom_conducteur() {
        return Nom_conducteur;
    }

    public void setNom_conducteur(String Nom_conducteur) {
        this.Nom_conducteur = Nom_conducteur;
    }

    public String getImmatriculation() {
        return immatriculation;
    }

    public void setImmatriculation(String immatriculation) {
        this.immatriculation = immatriculation;
    }

    public int getNombre_passager() {
        return nombre_passager;
    }

    public void setNombre_passager(int nombre_passager) {
        this.nombre_passager = nombre_passager;
    }

    public String getNum_carte() {
        return num_carte;
    }

    public void setNum_carte(String num_carte) {
        this.num_carte = num_carte;
    }

    
}

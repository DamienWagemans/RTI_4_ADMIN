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
public class Voyageur implements Serializable{
    String nom;
    String prenom;
    String adresse;
    String immatriculation;
    String mail;

    public Voyageur()
    {
        this.nom = "";
        this.prenom = "";
        this.adresse = "";
        this.mail = "";
        this.immatriculation = "";
    }
    public Voyageur(String nom, String prenom, String Adresse, String Imma, String mail) {
        this.nom = nom;
        this.prenom = prenom;
        this.adresse = Adresse;
        this.mail = mail;
        this.immatriculation = Imma;
    }
    
    public Voyageur (Voyageur tmp)
    {
        this.nom = tmp.nom;
        this.prenom = tmp.prenom;
        this.adresse = tmp.adresse;
        this.mail = tmp.mail;
        this.immatriculation = tmp.immatriculation;
    }

    public String getImmatriculation() {
        return immatriculation;
    }

    public void setImmatriculation(String immatriculation) {
        this.immatriculation = immatriculation;
    }

    
    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }
    
}

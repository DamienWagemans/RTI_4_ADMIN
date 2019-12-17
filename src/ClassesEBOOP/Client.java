/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ClassesEBOOP;

import java.io.Serializable;

/**
 *
 * @author damien
 */
public class Client  implements Serializable{
    private String id_client;
    private String nom;
    private String prenom;
    private String immatriculation;
    private String email;
    private String adresse;

    public Client(String id_client, String nom, String prenom, String immatriculation, String email, String adresse) {
        this.id_client = id_client;
        this.nom = nom;
        this.prenom = prenom;
        this.immatriculation = immatriculation;
        this.email = email;
        this.adresse = adresse;
    }
    public Client() {
        this.id_client = null;
        this.nom = null;
        this.prenom = null;
        this.immatriculation = null;
        this.email = null;
        this.adresse = null;
    }

    public String getImmatriculation() {
        return immatriculation;
    }

    public void setImmatriculation(String immatriculation) {
        this.immatriculation = immatriculation;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
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

    public String getId_client() {
        return id_client;
    }

    public void setId_client(String id_client) {
        this.id_client = id_client;
    }

    public Client(String id_client) {
        this.id_client = id_client;
        this.prenom = null;
        this.nom = null;
        this.email = null;
        this.adresse = null;
        this.immatriculation = null;
    }

    public Client(String id_client, String nom, String prenom) {
        this.id_client = id_client;
        this.nom = nom;
        this.prenom = prenom;
        this.email = null;
        this.adresse = null;
        this.immatriculation = null;
    }
    
    
    
}

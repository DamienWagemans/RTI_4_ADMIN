/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package requeteCard;

import java.io.Serializable;

/**
 *
 * @author damien
 */
public class Requete_card implements Serializable{
    
    private int montant;
    private String numero_carte;

    public Requete_card(int montant, String numero_carte) {
        this.montant = montant;
        this.numero_carte = numero_carte;
    }

    public Requete_card(Requete_card tmp)
    {
        this.montant = tmp.montant;
        this.numero_carte = tmp.numero_carte;
    }
    
    public int getMontant() {
        return montant;
    }

    public void setMontant(int montant) {
        this.montant = montant;
    }

    public String getNumero_carte() {
        return numero_carte;
    }

    public void setNumero_carte(String numero_carte) {
        this.numero_carte = numero_carte;
    }
    
    
    
}

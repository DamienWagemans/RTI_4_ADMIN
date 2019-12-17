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
public class Reponse_card implements Serializable{
    
    private int type;
    
    public static int PAIEMENT_OK = 1;
    public static int CARTE_ERRONEE = -1;
    public static int SOLDE_INSUFFISANT = -2;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Reponse_card(int type) {
        this.type = type;
    }
    
    

}

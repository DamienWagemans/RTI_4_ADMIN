/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interface_req_rep;

import Serveur_CheckIn.ThreadServeur;
import java.net.Socket;
import java.sql.Statement;

/**
 *
 * @author damien
 */
public interface Requete {
    public Runnable createRunnable (Socket s, Socket Sock_card, Statement instruc, ThreadServeur ts);

}

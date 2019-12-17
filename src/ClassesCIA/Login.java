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
public class Login implements Serializable{
    
    private String Username;
    private String Password;
    
    public Login(){
        setUsername("");
        setPassword("");
    }
    public Login(String user, String mdp){
        setUsername(user);
        setPassword(mdp);
    }
    
    public Login(Login log){
        setUsername(log.getUsername());
        setPassword(log.getPassword());
    }
    
    public String getUsername(){
        return Username;
    }
    public void setUsername(String user){
        Username = user;
    }
    public String getPassword(){
        return Password;
    }
    public void setPassword(String mdp){
        Password = mdp;
    }
}

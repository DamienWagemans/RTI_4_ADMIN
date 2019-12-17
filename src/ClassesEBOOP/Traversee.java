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
public class Traversee implements Serializable{
    private String id;
    private String depart;
    private String port_depart;
    private String port_destination;

    public Traversee(String id, String depart, String port_depart, String port_destination) {
        this.id = id;
        this.depart = depart;
        this.port_depart = port_depart;
        this.port_destination = port_destination;
    }

    public Traversee() 
    {
        this.id = null;
        this.depart = null;
        this.port_depart = null;
        this.port_destination = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDepart() {
        return depart;
    }

    public void setDepart(String depart) {
        this.depart = depart;
    }

    public String getPort_depart() {
        return port_depart;
    }

    public void setPort_depart(String port_depart) {
        this.port_depart = port_depart;
    }

    public String getPort_destination() {
        return port_destination;
    }

    public void setPort_destination(String port_destination) {
        this.port_destination = port_destination;
    }
    
    
}

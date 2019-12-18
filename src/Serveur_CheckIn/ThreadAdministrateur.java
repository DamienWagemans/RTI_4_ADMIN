/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Serveur_CheckIn;

import ClassesCIA.Login;
import ProtocolHAFISCA.ReponseADMIN;
import ProtocolHAFISCA.RequeteADMIN;
import divers.Config_Applic;
import divers.Persistance_Properties;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author damien
 */
public class ThreadAdministrateur extends Thread{
    private ThreadServeur threadServeur = null;
    private ThreadSecours threadSecours = null;
    
    private ServerSocket SSocket_Admin = null;
    private Socket Cli_sock = null;
    
    private boolean Admin_connected = false;
        
    
    public ThreadAdministrateur(ThreadServeur ts, ThreadSecours tS)
    {
        threadServeur = ts;
        threadSecours = tS;
        
        SSocket_Admin = ts.getSSocket_Admin();
        
    }
    
    
    @Override
    public void run()
    {
        try 
        {
            while(!isInterrupted())
            {
                Cli_sock = SSocket_Admin.accept();
                if(Admin_connected == false)
                {
                    Admin_connected = true;
                    communication_admin(Cli_sock);
                }
                else
                {
                    //on refuse la connection
                    System.out.println("Un admin est deja connecté ");
                    RequeteADMIN req = new RequeteADMIN(RequeteADMIN.ADMIN_DEJA_CONNECTE, null);
                    req.EnvoieRequete_JAVA(Cli_sock);
                    //Cli_sock.close();
                }
            }
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(ThreadAdministrateur.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void communication_admin(Socket sock) throws IOException
    {
        Thread thread_Connected_to_Admin = new Thread()
        {
            @Override
            public void run() 
            {

                try 
                {
                    boolean fin = false;
                    Socket socket_connected = sock;
                    System.out.println("L'administrateur est connecté");
                    RequeteADMIN req = new RequeteADMIN(RequeteADMIN.CONNECT_OK, null);
                    req.EnvoieRequete_JAVA(socket_connected);
                    
                    req.RecevoirRequete_JAVA(Cli_sock);
                    while (fin == false)
                    {
                        switch(req.getType())
                        {
                            //dans le cas ou l'admin veut se deconnecter
                            case 6:
                                System.out.println("L'admin s'en va");
                                Admin_connected = false;
                                sock.close();
                                fin = true;
                                break;
                            case 2:
                                System.out.println("Login de l'admin");
                                Login log = new Login((Login)req.getObject());
                                boolean log_ok = check_login(log.getUsername(), log.getPassword());
                                if(log_ok == true)
                                {
                                    ReponseADMIN rep = new ReponseADMIN(ReponseADMIN.LOGIN_OK, null);
                                    rep.EnvoieReponse_JAVA(Cli_sock);
                                }
                                else
                                {
                                    ReponseADMIN rep = new ReponseADMIN(ReponseADMIN.LOGIN_FAIL, null);
                                    rep.EnvoieReponse_JAVA(Cli_sock);
                                }
                                break;
                            case 3:
                                System.out.println("Dans liste de clients");
                                                
                                for(int i =0 ; i< threadServeur.getConnected_Clients().size(); i++)
                                {
                                    System.out.println("Voici les clients connectés : " +  threadServeur.getConnected_Clients().get(i));
                                }
                                ReponseADMIN rep = new ReponseADMIN(ReponseADMIN.LCLIENTS, threadServeur.getConnected_Clients());
                                rep.EnvoieReponse_JAVA(Cli_sock);
                                break;
                            case 4:
                                System.out.println("Dans paus/resume");
                                if(threadServeur.isServeur_en_pause()== true)
                                {
                                    System.out.println("J'enleve la pause");
                                    threadServeur.setServeur_en_pause(false);
                                }
                                else
                                {
                                    System.out.println("Je met la pause");
                                    threadServeur.setServeur_en_pause(true);
                                }
                                    
                                RequeteADMIN req_admin = new RequeteADMIN(RequeteADMIN.PAUSE_RESUME, null);
                                if(threadSecours.getSock_connected_clients().size() != 0)
                                {
                                    
                                    for(int i = 0; i < threadSecours.getSock_connected_clients().size(); i++)
                                    {
                                        //lorsqu'un client se deconnecte, je met son id a -1
                                        if (threadSecours.getSock_connected_clients().get(i).getId()!=-1)
                                        {
                                            req_admin.EnvoieRequete_JAVA(threadSecours.getSock_connected_clients().get(i).getSock());
                                            System.out.println("J'envoi au client connecté");
                                            //req.EnvoieRequete_JAVA(Cli_sock);
                                        }
                                    }
                                }
                                break;
                            case 5:
                                req_admin = new RequeteADMIN(RequeteADMIN.STOP, (int)req.getObject());
                                if(threadSecours.getSock_connected_clients().size() != 0)
                                {
                                    
                                    for(int i = 0; i < threadSecours.getSock_connected_clients().size(); i++)
                                    {
                                        if (threadSecours.getSock_connected_clients().get(i).getId()!=-1)
                                        {
                                            req_admin.EnvoieRequete_JAVA(threadSecours.getSock_connected_clients().get(i).getSock());
                                            System.out.println("J'envoi au client connecté");
                                            //req.EnvoieRequete_JAVA(Cli_sock);
                                        }
                                    }
                                }
                                break;
                        }
                           
                        req.RecevoirRequete_JAVA(Cli_sock);
                    }
                } 
                
                catch (EOFException ex)
                {
                    System.out.println("dddddddddd");
                }
                catch (IOException ex) 
                {
                    Logger.getLogger(ThreadAdministrateur.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(ThreadAdministrateur.class.getName()).log(Level.SEVERE, null, ex);
                }

                
            }
        };
        thread_Connected_to_Admin.start();

    }
    
    public boolean check_login (String username, String password)
    {
        Properties myProperties = Persistance_Properties.LoadProp(Config_Applic.pathLogin);
        String pass = myProperties.getProperty(username);
        
        if(pass != null)
        {
            if(pass.equals(password))
            {
                return true;
            }
        }    
        return false;
    }
}

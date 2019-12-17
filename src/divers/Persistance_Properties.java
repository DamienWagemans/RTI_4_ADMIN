/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package divers;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

/**
 *
 * @author damien
 */
public class Persistance_Properties {
    public static void SaveProp(Properties props,String cheminfichier)
        {
            try
            {
                OutputStream out = new FileOutputStream(cheminfichier);
                props.store(out,"Fichier properties"); 
                out.flush(); 
            }
            catch(NullPointerException | IOException e){
                System.err.println("SaveProp: "+e.getMessage());
            }
        }
        
        public static Properties LoadProp(String cheminfichier)
        {
            Properties props = new Properties(); 
            try
            {
                props.load(new FileInputStream(cheminfichier));
                return props;
            }
            catch(NullPointerException | IOException e){
                System.err.println("LoadProp: "+e.getMessage());
                return null;
            }
        }
    
}

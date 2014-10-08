/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package beans.util;

import java.io.File;

/**
 *
 * @author Omar Ernesto Cabrera Rosero
 */
public class Delete {
    
   public void deleteFolder(File dir){
        File[] files = dir.listFiles();
        for (int x=0; x< files.length;x++){
            if(files[x].isDirectory()){
                deleteFolder(files[x]);
            }
            files[x].delete();
        }
    }   
}

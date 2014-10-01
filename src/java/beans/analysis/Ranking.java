/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package beans.analysis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

/**
 *
 * @author od
 */
public class Ranking {

    public Ranking() {
    }
    
    public ArrayList<String> getRanking(String classifier, int top, String fileName){
        Process p;
        ProcessBuilder pb = new ProcessBuilder();
        
        ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance()
                .getExternalContext().getContext();
        String directory = ctx.getRealPath("/") + "Resources/R/ranking/";
        String fileInput = ctx.getRealPath("/") + "Resources/csv/" + fileName;
        
        pb = new ProcessBuilder("Rscript", directory + "attributeSelection.R", fileInput, directory, String.valueOf(top), classifier);
        ArrayList<String> rankingValues = new ArrayList<String>();
        try {
            p = pb.start();
            InputStream is = p.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                //System.out.println("ranking"+line.replaceAll("\\[1\\]",""));
                rankingValues.add(line.replaceAll("\\[1\\] ","").replaceAll("\"", ""));
            }
        } catch (IOException ex) {
            Logger.getLogger(DataAnalysis.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return rankingValues;
        
    }
}

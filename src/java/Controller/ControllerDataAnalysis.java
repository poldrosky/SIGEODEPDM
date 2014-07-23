package Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author Omar Ernesto Cabrera Rosero
 */

@ManagedBean
@SessionScoped
public class ControllerDataAnalysis implements Serializable {

    private String table;
    private String fileOutput;

    
    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getFileOutput() {
        return fileOutput;
    }

    public void setFileOutput(String fileOutput) {
        this.fileOutput = fileOutput;
    }
    private StreamedContent fileHelp;

    public StreamedContent getFileHelp() {
        runCommand();
        InputStream stream = ((ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext()).getResourceAsStream("/Resources/R/"+fileOutput+".pdf");
        fileHelp = new DefaultStreamedContent(stream, "application/pdf", fileOutput+".pdf");
        return fileHelp;
    }

    public void setFileHelp(StreamedContent fileHelp) {
        this.fileHelp = fileHelp;
    }

    public void runCommand() {
        Process p;
        ProcessBuilder pb = new ProcessBuilder();

        ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance()
                .getExternalContext().getContext();
        String realPath = ctx.getRealPath("/") + "Resources/R/";

        pb = new ProcessBuilder("Rscript", realPath + "icfes.R", this.table, this.fileOutput, realPath);
        try {
            p = pb.start();
            InputStream is = p.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException ex) {
            Logger.getLogger(ControllerDataAnalysis.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    /**
     * Creates a new instance of ControllerDataAnalysis
     */
    public ControllerDataAnalysis() {
    }
}

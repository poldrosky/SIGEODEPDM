package managedBeans.analysis;

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
import managedBeans.data.DataViewAssociationMB;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author Omar Ernesto Cabrera Rosero
 */

@ManagedBean
@SessionScoped
public class DataAnalysis implements Serializable {

    private StreamedContent qualityDataFile;
    private DataViewAssociationMB dataView;

    public DataAnalysis() {
        dataView = (DataViewAssociationMB) FacesContext.getCurrentInstance().getApplication().evaluateExpressionGet(FacesContext.getCurrentInstance(), "#{dataViewAssociationMB}", DataViewAssociationMB.class);
    }
    
   public StreamedContent getQualityDataFile() {
        qualityData();
        InputStream stream = ((ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext()).getResourceAsStream("/Resources/R/qualityData/reporte.pdf");
        qualityDataFile = new DefaultStreamedContent(stream, "application/pdf", "reporte.pdf");
        return qualityDataFile;
    }

    public void setQualityDataFile(StreamedContent qualityDataFile) {
        this.qualityDataFile = qualityDataFile;
    }


    public void qualityData() {
        Process p;
        ProcessBuilder pb = new ProcessBuilder();
        
        
        ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance()
                .getExternalContext().getContext();
        String directory = ctx.getRealPath("/") + "Resources/R/qualityData/";
        String fileInput = ctx.getRealPath("/")+ "Resources/csv/" + dataView.getFileName();
        
        System.out.println(directory + fileInput);
        
        
        pb = new ProcessBuilder("Rscript", directory + "qualityData.R", fileInput, "reporte", directory);
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
            Logger.getLogger(DataAnalysis.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    
    
}

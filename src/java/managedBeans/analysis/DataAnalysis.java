package managedBeans.analysis;

import beans.util.ItemList;
import com.csvreader.CsvWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import managedBeans.data.DataViewAssociationMB;
import managedBeans.data.DataViewClassificationMB;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DualListModel;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author Omar Ernesto Cabrera Rosero
 */
public class DataAnalysis {

    private StreamedContent qualityDataFile;

    public DataAnalysis() {
    }

    public StreamedContent getQualityDataFile(String userLogin, DualListModel<ItemList> variables, List<String[]> resultado, ArrayList<String[]> data) throws IOException {
        buildCSV(userLogin, variables, resultado, data);
        qualityData();
        InputStream stream = ((ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext()).getResourceAsStream("/Resources/R/qualityData/" + fileName + ".pdf");
        qualityDataFile = new DefaultStreamedContent(stream, "application/pdf", fileName + ".pdf");
        return qualityDataFile;
    }
    
    String fileName="";

    public void buildCSV(String userLogin, DualListModel<ItemList> variables, List<String[]> resultado, ArrayList<String[]> data) throws IOException {

        ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance()
                .getExternalContext().getContext();
        String realPath = ctx.getRealPath("/") + "Resources/csv/";

        Date date = new java.util.Date();
        Timestamp time = new Timestamp(date.getTime());

        fileName = userLogin + time.toString().replace(" ", "H");

        File file = new File(realPath + fileName);
        CsvWriter csvOutput = new CsvWriter(new FileWriter(file, true), ',');

        for (int i = 0; i < variables.getTarget().size(); i++) {
            csvOutput.write(String.valueOf(variables.getTarget().get(i)));
        }
        csvOutput.endRecord();

        if (resultado != null) {
            for (String[] fila : resultado) {
                for (String columna : fila) {
                    System.out.println(columna);
                    csvOutput.write(columna);
                }
                csvOutput.endRecord();

            }
        } else {
            for (String[] fila : data) {
                for (String columna : fila) {
                    System.out.println(columna);
                    csvOutput.write(columna);
                }
                csvOutput.endRecord();
            }
        }
        csvOutput.flush();
        csvOutput.close();
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
        String fileInput = ctx.getRealPath("/") + "Resources/csv/" + fileName;

        System.out.println(directory + fileInput);


        pb = new ProcessBuilder("Rscript", directory + "qualityData.R", fileInput, fileName, directory);
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

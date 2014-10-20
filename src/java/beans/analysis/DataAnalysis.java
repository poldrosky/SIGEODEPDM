package beans.analysis;

import beans.util.Delete;
import com.csvreader.CsvWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author Omar Ernesto Cabrera Rosero
 */
public class DataAnalysis {

    private StreamedContent qualityDataFile;
    private StreamedContent associationFile;
    private StreamedContent classificationFile;
    private StreamedContent clusteringFile;
    private Delete delete;
    
    
    public DataAnalysis() {
        delete = new Delete();
    }

    public StreamedContent getQualityDataFile(String userLogin, List<String> colNameData, List<String[]> resultado, ArrayList<String[]> data) throws IOException {
        buildCSV(userLogin, colNameData, resultado, data);
        qualityData();
        InputStream stream = ((ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext()).getResourceAsStream("/Resources/R/qualityData/"+ fileName + "/"+fileName+".pdf");
        qualityDataFile = new DefaultStreamedContent(stream, "application/pdf", fileName + ".pdf");
        ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance()
                .getExternalContext().getContext();
        String Directory = ctx.getRealPath("/")+ "Resources/R/qualityData/"+ fileName;
        File dir = new File(Directory);
        delete.deleteFolder(dir);
        dir.delete();
        String FileCSV = ctx.getRealPath("/")+ "Resources/csv/" + fileName;
        File csv = new File(FileCSV);
        csv.delete();
        return qualityDataFile;
    }
    
    String fileName="";

    public void buildCSV(String userLogin, List<String> colNameData, List<String[]> resultado, ArrayList<String[]> data) throws IOException {

        ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance()
                .getExternalContext().getContext();
        String realPath = ctx.getRealPath("/") + "Resources/csv/";

        Date date = new java.util.Date();
        Timestamp time = new Timestamp(date.getTime());

        fileName = userLogin + time.toString().replace(" ", "H");

        File file = new File(realPath + fileName);
        CsvWriter csvOutput = new CsvWriter(new FileWriter(file, true), ',');

        for (int i = 0; i < colNameData.size(); i++) {
            csvOutput.write(String.valueOf(colNameData.get(i)));
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
    
    public StreamedContent getAssociationFile(String userLogin, List<String> colNameData, List<String[]> resultado, 
            ArrayList<String[]> data, int kValue, double lcmSuppPerc, int lcmMinLen, String tag, String nameAnalysis) throws IOException {
        buildCSV(userLogin, colNameData, resultado, data);
        association(kValue, lcmSuppPerc, lcmMinLen, tag);
        InputStream stream = ((ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext()).getResourceAsStream("/Resources/R/association/"  + fileName + "/"+fileName+".pdf");
        associationFile = new DefaultStreamedContent(stream, "application/pdf", fileName+nameAnalysis + ".pdf");
        ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance()
                .getExternalContext().getContext();
        String Directory = ctx.getRealPath("/")+ "Resources/R/association/"+ fileName;
        File dir = new File(Directory);
        delete.deleteFolder(dir);
        dir.delete();
        String FileCSV = ctx.getRealPath("/")+ "Resources/csv/" + fileName;
        File csv = new File(FileCSV);
        csv.delete();
        return associationFile;
    }
    
    public void association(int kValue, double lcmSuppPerc, int lcmMinLen, String tag){
        Process p;
        ProcessBuilder pb = new ProcessBuilder();

        ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance()
                .getExternalContext().getContext();
        String directory = ctx.getRealPath("/") + "Resources/R/association/";
        String fileInput = ctx.getRealPath("/") + "Resources/csv/" + fileName;

        System.out.println(directory + fileInput);


        pb = new ProcessBuilder("Rscript", directory + "association.R", fileInput, fileName, directory, String.valueOf(kValue),
                String.valueOf(lcmSuppPerc), String.valueOf(lcmMinLen), tag);
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
    
    public StreamedContent getClassificationFile(String userLogin, List<String> colNameData, List<String[]> resultado, ArrayList<String[]> data, 
            String classValue, int maxM, int minM, int deltaM, double maxC, double minC, double deltaC, double confidence,
            double support, int nfolds, String nameAnalysis) throws IOException {
        buildCSV(userLogin, colNameData, resultado, data);
        classification(classValue, maxM, minM, deltaM, maxC, minC, deltaC, confidence, support, nfolds);
        InputStream stream = ((ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext()).getResourceAsStream("/Resources/R/classification/" + fileName + "/"+fileName+".pdf");
        classificationFile = new DefaultStreamedContent(stream, "application/pdf", fileName+nameAnalysis + ".pdf");
        ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance()
                .getExternalContext().getContext();
        String Directory = ctx.getRealPath("/")+ "Resources/R/classification/"+ fileName;
        File dir = new File(Directory);
        delete.deleteFolder(dir);
        dir.delete();
        String FileCSV = ctx.getRealPath("/")+ "Resources/csv/" + fileName;
        File csv = new File(FileCSV);
        csv.delete();
        return classificationFile;
    }
    
    public void classification(String classValue, int maxM, int minM, int deltaM, double maxC, double minC, double deltaC, double confidence,
            double support, int nfolds){
        Process p;
        ProcessBuilder pb = new ProcessBuilder();

        
        ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance()
                .getExternalContext().getContext();
        String directory = ctx.getRealPath("/") + "Resources/R/classification/";
        String fileInput = ctx.getRealPath("/") + "Resources/csv/" + fileName;

        pb = new ProcessBuilder("Rscript", directory + "runJ48_Binary.R", fileInput, fileName, directory, classValue,
                String.valueOf(maxM), String.valueOf(minM), String.valueOf(deltaM), String.valueOf(maxC), 
                String.valueOf(minC), String.valueOf(deltaC), String.valueOf(confidence), String.valueOf(support), String.valueOf(nfolds));
        
                
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
    
    public StreamedContent getClusteringFile(String userLogin, List<String> colNameData, List<String[]> resultado, 
            ArrayList<String[]> data, int valueN, String tag, String nameAnalysis) throws IOException {
        buildCSV(userLogin, colNameData, resultado, data);
        clustering(valueN, tag);
        InputStream stream = ((ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext()).getResourceAsStream("/Resources/R/clustering/"  + fileName + "/"+fileName+".pdf");
        clusteringFile = new DefaultStreamedContent(stream, "application/pdf", fileName+nameAnalysis + ".pdf");
        ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance()
                .getExternalContext().getContext();
        String Directory = ctx.getRealPath("/")+ "Resources/R/clustering/"+ fileName;
        File dir = new File(Directory);
        delete.deleteFolder(dir);
        dir.delete();
        String FileCSV = ctx.getRealPath("/")+ "Resources/csv/" + fileName;
        File csv = new File(FileCSV);
        csv.delete();
        return clusteringFile;
    }
    
    public void clustering(int valueN, String tag){
        Process p;
        ProcessBuilder pb = new ProcessBuilder();

        ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance()
                .getExternalContext().getContext();
        String directory = ctx.getRealPath("/") + "Resources/R/clustering/";
        String fileInput = ctx.getRealPath("/") + "Resources/csv/" + fileName;

        System.out.println(directory + fileInput);


        pb = new ProcessBuilder("Rscript", directory + "clustering.R", fileInput, fileName, directory, 
                String.valueOf(valueN), tag);
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
    
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }   
}
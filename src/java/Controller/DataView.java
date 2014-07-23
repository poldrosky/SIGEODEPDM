package Controller;

import beans.connection.ConnectionJdbcMB;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import org.primefaces.model.DualListModel;
import beans.connection.ConnectionJdbcMB;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import managedBeans.login.ApplicationControlMB;


@ManagedBean
@SessionScoped
public class DataView {

    private DualListModel<SelectItem> variables;
    private String fact;  
    private List<SelectItem> facts;
    
    private List<SelectItem> variablesSource;    
    private List<SelectItem> variablesTarget;  
    
    ConnectionJdbcMB connectionJdbcMB;
    ApplicationControlMB applicationControlMB;
    FacesContext context;
    
    private SelectItem selectedList;
    
    private int countColNameData;
    private int countData;
    private ArrayList<Object[]> data;
    private ArrayList<String> colNameData;
    
    
     public DataView() {
    }
    
    public DataView(int countData, int countColNameData, ArrayList<Object[]> data, ArrayList<String> colNameData) {
        this.countData = countData;
        this.countColNameData = countColNameData;        
        this.data = data;
        this.colNameData = colNameData;
    }
    
    
    @PostConstruct
    public void init() {
        variablesSource = new ArrayList<SelectItem>();
        variablesTarget = new ArrayList<SelectItem>(); 
        
        SelectItemGroup g1 = new SelectItemGroup("Fatales");
        g1.setSelectItems(new SelectItem[] {new SelectItem("fact_murder", "Homicidios"), new SelectItem("fact_suicide", "Suicidios"),
            new SelectItem("fact_traffic", "Muertes Accidentes Tránsito"), new SelectItem("fact_accidents", "Muertes Accidentales")});
         
        SelectItemGroup g2 = new SelectItemGroup("No Fatales");
        g2.setSelectItems(new SelectItem[] {new SelectItem("fact_interpersonal", "Interpersonal"), new SelectItem("fact_intrafamiliar", "Intrafamiliar"),
            new SelectItem("fact_self_inflicted", "Auto Inflingido"), new SelectItem("fact_transport", "Lesiones de Tránsito"), 
            new SelectItem("fact_unintencional", "No intencional")});
         
        facts = new ArrayList<SelectItem>();
        facts.add(g1);
        facts.add(g2); 

        variables = new DualListModel<SelectItem>(variablesSource, variablesTarget);

    }
    
   
    public void load() {
        List<SelectItem> variablesSource = new ArrayList<SelectItem>();
        List<SelectItem> variablesTarget = new ArrayList<SelectItem>();
        
        context = FacesContext.getCurrentInstance();
        connectionJdbcMB = (ConnectionJdbcMB) context.getApplication().evaluateExpressionGet(context, "#{connectionJdbcMB}", ConnectionJdbcMB.class);
        connectionJdbcMB.connectToDb();
        ResultSet rs = connectionJdbcMB.consult("Select * from common_variables");
        ResultSet rsOwn = connectionJdbcMB.consult("Select * from own_variables where fact like '"+this.fact+"'");
        
         try {
            while (rs.next()) {
                String descriptionVariable = rs.getString("description");
                String nameVariable = rs.getString("name");
                SelectItem item = new SelectItem(nameVariable, descriptionVariable);
                variablesSource.add(item);

            }
            while (rsOwn.next()) {
                String descriptionVariable = rsOwn.getString("description");
                String nameVariable = rsOwn.getString("name");
                SelectItem item = new SelectItem(nameVariable, descriptionVariable);
                variablesSource.add(item);
            }

        } catch (SQLException ex) {
            Logger.getLogger(DataView.class.getName()).log(Level.SEVERE, null, ex);
        }
            
             
        variables = new DualListModel<SelectItem>(variablesSource, variablesTarget);
        
    }
    
    public void loadData() {
        colNameData = new ArrayList<String>();
        data = new ArrayList<Object[]>();
        
        System.out.println("event"+this.fact);
        System.out.println("vari"+this.variables.getTarget());
        
        
             
        colNameData.add("Columna1");
        colNameData.add("Columna2");
        
    }

    public DualListModel<SelectItem> getVariables() {
        return variables;
    }

    public void setVariables(DualListModel<SelectItem> variables) {
        this.variables = variables;
    }
    
    public String getFact() {
        return fact;        
    }
 
    public void setFact(String fact) {
        this.fact = fact;
    }
   
    public List<SelectItem> getFacts() {
        return facts;
    }

    public SelectItem getSelectedList() {
        return this.selectedList;
    }
    
    public int getCountColNameData() {
        return countColNameData;
    }

    public int getCountData() {
        return countData;
    }

    public void setCountColNameData(int countColNameData) {
        this.countColNameData = countColNameData;
    }

    public void setCountData(int countData) {
        this.countData = countData;
    }

    public ArrayList<String> getColNameData() {
        return colNameData;
    }

    public void setColNameData(ArrayList<String> colNameData) {
        this.colNameData = colNameData;
    }

    public ArrayList<Object[]> getData() {
        return data;
    }

    public void setData(ArrayList<Object[]> data) {
        this.data = data;
    }    
}

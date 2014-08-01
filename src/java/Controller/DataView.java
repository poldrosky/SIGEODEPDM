package Controller;

import beans.connection.ConnectionJdbcMB;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import managedBeans.login.ApplicationControlMB;
import org.primefaces.model.DualListModel;

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
    private List<String> colNameData;

    public DataView() {
    }

    public DataView(int countData, int countColNameData, ArrayList<Object[]> data, List<String> colNameData) {
        this.countData = countData;
        this.countColNameData = countColNameData;
        this.data = data;
        this.colNameData = colNameData;
    }

    @PostConstruct
    public void init() {
        colNameData = new ArrayList<>();
        variablesSource = new ArrayList<>();
        variablesTarget = new ArrayList<>();

        facts = new ArrayList<>();
        facts.add(new SelectItem("fact_murder", "Homicidios"));
        facts.add(new SelectItem("fact_suicide", "Suicidios"));
        facts.add(new SelectItem("fact_traffic", "Muertes Accidentes Tránsito"));
        facts.add(new SelectItem("fact_accidents", "Muertes Accidentales"));

        facts.add(new SelectItem("fact_interpersonal", "Interpersonal"));
        facts.add(new SelectItem("fact_intrafamiliar", "Intrafamiliar"));
        facts.add(new SelectItem("fact_self_inflicted", "Auto Inflingido"));
        facts.add(new SelectItem("fact_transport", "Lesiones de Tránsito"));
        facts.add(new SelectItem("fact_unintencional", "No intencional"));

        variables = new DualListModel<>(variablesSource, variablesTarget);


    }

    public void load() {
        context = FacesContext.getCurrentInstance();
        connectionJdbcMB = (ConnectionJdbcMB) context.getApplication().evaluateExpressionGet(context, "#{connectionJdbcMB}", ConnectionJdbcMB.class);
        connectionJdbcMB.connectToDb();
        ResultSet rs = connectionJdbcMB.consult("Select * from common_variables");
        ResultSet rsOwn = connectionJdbcMB.consult("Select * from own_variables where fact like '" + this.fact + "'");

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


        variables = new DualListModel<>(variablesSource, variablesTarget);

    }

    public void loadData() throws SQLException {
        colNameData = new ArrayList<>();
        data = new ArrayList<>();
        
        
        
        ArrayList<String> arrayList = new ArrayList<>();

        for (int i = 0; i < this.variables.getTarget().size(); i++) {
            for (SelectItem fuente : variablesSource) {
                String a = (String) fuente.getValue();
                String b = String.valueOf(variables.getTarget().get(i));

                if (a.equals(b)) {
                    colNameData.add(fuente.getLabel());
                    break;
                }
            }
        }

        if (this.fact.equals("fact_murder")){
            String aux;
            aux = "fact_murder natural join dim_victim natural join dim_date natural join dim_time natural"
                    + " join dim_neighborhood natural join dim_quadrant natural join dim_murder";
        
        
            ResultSet rs = connectionJdbcMB.consult("SELECT date_value, day_number, day_name, week_number FROM "+aux);
        }
 
        //data.add(new Object[]{"Omar","sdd"});



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

    public List<String> getColNameData() {
        return colNameData;
    }

    public void setColNameData(List<String> colNameData) {
        this.colNameData = colNameData;
    }

    public ArrayList<Object[]> getData() {
        return data;
    }

    public void setData(ArrayList<Object[]> data) {
        this.data = data;
    }
}

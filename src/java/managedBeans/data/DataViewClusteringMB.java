package managedBeans.data;

import beans.analysis.DataAnalysis;
import beans.connection.ConnectionJdbcMB;
import beans.util.ItemList;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import managedBeans.login.ApplicationControlMB;
import managedBeans.login.LoginMB;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.data.FilterEvent;
import org.primefaces.model.DualListModel;
import org.primefaces.model.StreamedContent;


@ManagedBean
@SessionScoped
public class DataViewClusteringMB {

    private DualListModel<ItemList> variables;
    private String fact;
    private List<SelectItem> facts;
    private List<ItemList> variablesSource;
    private List<ItemList> variablesTarget;
    ConnectionJdbcMB connectionJdbcMB;
    ApplicationControlMB applicationControlMB;
    FacesContext context;
    private SelectItem selectedList;
    private int countColNameData;
    private int countData;
    private ArrayList<String[]> data;
    private List<String> colNameData = new ArrayList<>();
    private Date startDate;
    private Date endDate;
    private LoginMB loginMB;
    private DataAnalysis analysis;
    private int valueN;    

    public DataViewClusteringMB() {
        this.startDate = new Date(1000);
        loginMB = (LoginMB) FacesContext.getCurrentInstance().getApplication().evaluateExpressionGet(FacesContext.getCurrentInstance(), "#{loginMB}", LoginMB.class);
        analysis = new DataAnalysis();
    }

    public DataViewClusteringMB(int countData, int countColNameData, ArrayList<String[]> data, List<String> colNameData) {
        this.countData = countData;
        this.countColNameData = countColNameData;
        this.data = data;
        this.colNameData = colNameData;
    }

    public void reset() {
        variables = new DualListModel<>();
    }

    @PostConstruct
    public void init() {
        colNameData = new ArrayList<>();
        variablesSource = new ArrayList<>();
        variablesTarget = new ArrayList<>();
        valueN = 3;

        startDate = new Date(100, 0, 1);
        endDate = new Date();

        facts = new ArrayList<>();
        facts.add(new SelectItem("fact_murder", "Homicidios"));
        facts.add(new SelectItem("fact_suicides", "Suicidios"));
        facts.add(new SelectItem("fact_traffic", "Muertes Accidentes Tránsito"));
        facts.add(new SelectItem("fact_accidents", "Muertes Accidentales"));

        facts.add(new SelectItem("fact_interpersonal", "Interpersonal"));
        facts.add(new SelectItem("fact_intrafamiliar", "Intrafamiliar"));
        facts.add(new SelectItem("fact_self_inflicted", "Auto Inflingido"));
        facts.add(new SelectItem("fact_transport", "Lesiones de Tránsito"));
        facts.add(new SelectItem("fact_unintencional", "No intencional"));

        variables = new DualListModel<>(variablesSource, variablesTarget);
    }

    public void loadVariablesPickList() {
        context = FacesContext.getCurrentInstance();
        connectionJdbcMB = (ConnectionJdbcMB) context.getApplication().evaluateExpressionGet(context, "#{connectionJdbcMB}", ConnectionJdbcMB.class);
        connectionJdbcMB.connectToDb();
        
        variablesSource = new ArrayList<>();
        variablesTarget = new ArrayList<>();
        variables = new DualListModel<>(variablesSource, variablesTarget);
        
        ResultSet rs = null;
        ResultSet rsOwn = connectionJdbcMB.consult("Select * from own_variables ORDER BY id");
        
        if (fact.equals("fact_murder") || fact.equals("fact_suicides") || fact.equals("fact_traffic") || fact.equals("fact_accidents")) {
            rs = connectionJdbcMB.consult("Select * from common_variables_fatal ORDER BY id");
        } else{
            rs = connectionJdbcMB.consult("Select * from common_variables_non_fatal ORDER by id");
        }
        
        try {
            while (rs.next()) {
                String descriptionVariable = rs.getString("description");
                String nameVariable = rs.getString("name");
                //SelectItem item = new SelectItem(nameVariable, descriptionVariable);

                variablesSource.add(new ItemList(descriptionVariable, nameVariable));

            }
            while (rsOwn.next()) {
                if(fact.equals(rsOwn.getString("fact"))){
                    String descriptionVariable = rsOwn.getString("description");
                    String nameVariable = rsOwn.getString("name");
                
                //SelectItem item = new SelectItem(nameVariable, descriptionVariable);
                variablesSource.add(new ItemList(descriptionVariable, nameVariable));
                //variablesSource.add(item);
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(DataViewClusteringMB.class.getName()).log(Level.SEVERE, null, ex);
        }


        variables = new DualListModel<>(variablesSource, variablesTarget);

    }

    public void loadDataTable() throws SQLException {
        colNameData = new ArrayList<>();
        data = new ArrayList<>();
        String aux1, aux2 = null, aux3;

        //ArrayList<String> arrayList = new ArrayList<>();

        for (int i = 0; i < this.variables.getTarget().size(); i++) {
            for (ItemList source : variablesSource) {
                String a = source.getValueEn();
                String b = String.valueOf(variables.getTarget().get(i));

                if (a.equals(b)) {
                    colNameData.add(source.getValueSp());
                    break;
                }
            }
        }


        aux1 = String.valueOf(variables.getTarget()).replace("[", "").replace("]", "");

        SimpleDateFormat d = new SimpleDateFormat("dd-M-yyyy");
        String sD = d.format(startDate);
        String eD = d.format(endDate);

        if (fact.equals("fact_murder") || fact.equals("fact_suicides") || fact.equals("fact_traffic")
                || fact.equals("fact_accidents")) {
            aux3 = " WHERE date_value BETWEEN '" + sD + "' AND '" + eD + "'";
        } else {
            aux3 = " WHERE event_date.date_value BETWEEN '" + sD + "' AND '" + eD + "'";
        }

        // Fatal
        if (this.fact.equals("fact_murder")) {
            aux2 = "fact_murder natural join dim_victim natural join dim_date natural join dim_time natural"
                    + " join dim_neighborhood natural join dim_quadrant natural join dim_murder natural join "
                    + " dim_fatal left join dim_jobs using (job_key) left join dim_vulnerable_groups using (vulnerable_group_key)";
        }

        if (this.fact.equals("fact_suicides")) {
            aux2 = "fact_suicides natural join dim_victim natural join dim_date natural join dim_time natural"
                    + " join dim_neighborhood natural join dim_quadrant natural join dim_suicide natural join "
                    + " dim_fatal left join dim_jobs using (job_key) left join dim_vulnerable_groups using (vulnerable_group_key)";
        }

        if (this.fact.equals("fact_traffic")) {
            aux2 = "fact_traffic natural join dim_victim natural join dim_date natural join dim_time natural"
                    + " join dim_neighborhood natural join dim_quadrant natural join dim_traffic natural join "
                    + " dim_fatal left join dim_jobs using (job_key) left join dim_vulnerable_groups using (vulnerable_group_key)";
        }

        if (this.fact.equals("fact_accidents")) {
            aux2 = "fact_accidents natural join dim_victim natural join dim_date natural join dim_time natural"
                    + " join dim_neighborhood natural join dim_quadrant natural join dim_accidents natural join "
                    + " dim_fatal left join dim_jobs using (job_key) left join dim_vulnerable_groups using (vulnerable_group_key)";
        }
        
        //non_fatal

        if (this.fact.equals("fact_interpersonal")) {
            aux2 = "fact_interpersonal fact"
                    + " NATURAL JOIN dim_interpersonal"
                    + " NATURAL JOIN dim_anatomical_location "
                    + " NATURAL JOIN dim_time "
                    + " LEFT JOIN dim_kind_of_injury USING (kind_of_injury_key) "
                    + " NATURAL JOIN dim_neighborhood"
                    + " NATURAL JOIN dim_non_fatal"
                    + " NATURAL JOIN dim_victim"
                    + " NATURAL JOIN dim_jobs"
                    + " LEFT JOIN dim_vulnerable_groups USING (vulnerable_group_key)"
                    + " JOIN dim_date checkup_date ON (checkup_date.date_key = fact.date_key)"
                    + " JOIN dim_date event_date ON (event_date.date_key = fact.checkup_date_key) "
                    + " JOIN dim_time checkup_time ON (checkup_time.time_key = fact.time_key) "
                    + " JOIN dim_time event_time ON (event_time.time_key = fact.checkup_time_key)"
                    + " JOIN dim_icd10 first_cie10 ON (first_cie10.icd10_key = dim_non_fatal.first_cie10)"
                    + " JOIN dim_icd10 second_cie10 ON (second_cie10.icd10_key = dim_non_fatal.second_cie10)";
        }

        if (this.fact.equals("fact_fact_intrafamiliar")) {
            aux2 = "fact_intrafamiliar fact"
                    + " NATURAL JOIN dim_abuse"
                    + " LEFT JOIN dim_anatomical_location USING (anatomical_location_key)"
                    + " LEFT JOIN dim_kind_of_injury USING (kind_of_injury_key)"
                    + " NATURAL JOIN dim_neighborhood"
                    + " NATURAL JOIN dim_non_fatal"
                    + " NATURAL JOIN dim_time"
                    + " NATURAL JOIN dim_victim"
                    + " LEFT JOIN dim_aggressor USING (aggressor_key)"
                    + " NATURAL JOIN dim_jobs"
                    + " LEFT JOIN dim_vulnerable_groups USING (vulnerable_group_key)"
                    + " JOIN dim_date checkup_date ON (checkup_date.date_key = fact.date_key)"
                    + " JOIN dim_date event_date ON (event_date.date_key = fact.checkup_date_key) "
                    + " JOIN dim_time checkup_time ON (checkup_time.time_key = fact.time_key) "
                    + " JOIN dim_time event_time ON (event_time.time_key = fact.checkup_time_key)"
                    + " JOIN dim_icd10 first_cie10 ON (first_cie10.icd10_key = dim_non_fatal.first_cie10)"
                    + " JOIN dim_icd10 second_cie10 ON (second_cie10.icd10_key = dim_non_fatal.second_cie10)";

        }

        if (this.fact.equals("fact_self_inflicted")) {
            aux2 = "fact_self_inflicted fact"
                    + " NATURAL JOIN dim_time"
                    + " NATURAL JOIN dim_anatomical_location"
                    + " LEFT JOIN dim_kind_of_injury USING (kind_of_injury_key)"
                    + " NATURAL JOIN dim_neighborhood"
                    + " NATURAL JOIN dim_non_fatal"
                    + " NATURAL JOIN dim_self_inflicted"
                    + " NATURAL JOIN dim_victim"
                    + " NATURAL JOIN dim_jobs"
                    + " LEFT JOIN dim_vulnerable_groups USING (vulnerable_group_key)"
                    + " JOIN dim_date checkup_date ON (checkup_date.date_key = fact.date_key)"
                    + " JOIN dim_date event_date ON (event_date.date_key = fact.checkup_date_key) "
                    + " JOIN dim_time checkup_time ON (checkup_time.time_key = fact.time_key) "
                    + " JOIN dim_time event_time ON (event_time.time_key = fact.checkup_time_key)"
                    + " JOIN dim_icd10 first_cie10 ON (first_cie10.icd10_key = dim_non_fatal.first_cie10)"
                    + " JOIN dim_icd10 second_cie10 ON (second_cie10.icd10_key = dim_non_fatal.second_cie10)";

        }

        if (this.fact.equals("fact_transport")) {
            aux2 = "fact_transport fact"
                    + " NATURAL JOIN dim_time"
                    + " NATURAL JOIN dim_anatomical_location"
                    + " LEFT JOIN dim_kind_of_injury USING (kind_of_injury_key)"
                    + " NATURAL JOIN dim_neighborhood"
                    + " NATURAL JOIN dim_non_fatal"
                    + " left join dim_security_elements USING (security_elements_key)"
                    + " NATURAL JOIN dim_transport"
                    + " NATURAL JOIN dim_victim"
                    + " NATURAL JOIN dim_jobs"
                    + " LEFT JOIN dim_vulnerable_groups USING (vulnerable_group_key)"
                    + " JOIN dim_date checkup_date ON (checkup_date.date_key = fact.date_key)"
                    + " JOIN dim_date event_date ON (event_date.date_key = fact.checkup_date_key) "
                    + " JOIN dim_time checkup_time ON (checkup_time.time_key = fact.time_key) "
                    + " JOIN dim_time event_time ON (event_time.time_key = fact.checkup_time_key)"
                    + " JOIN dim_icd10 first_cie10 ON (first_cie10.icd10_key = dim_non_fatal.first_cie10)"
                    + " JOIN dim_icd10 second_cie10 ON (second_cie10.icd10_key = dim_non_fatal.second_cie10)";

        }

        if (this.fact.equals("fact_unintencional")) {
            aux2 = "fact_unintentional fact"
                    + " NATURAL JOIN dim_time"
                    + " NATURAL JOIN dim_anatomical_location"
                    + " LEFT JOIN dim_kind_of_injury USING (kind_of_injury_key)"
                    + " NATURAL JOIN dim_neighborhood"
                    + " NATURAL JOIN dim_non_fatal"
                    + " NATURAL JOIN dim_victim"
                    + " NATURAL JOIN dim_jobs"
                    + " LEFT JOIN dim_vulnerable_groups USING (vulnerable_group_key)"
                    + " JOIN dim_date checkup_date ON (checkup_date.date_key = fact.date_key)"
                    + " JOIN dim_date event_date ON (event_date.date_key = fact.checkup_date_key) "
                    + " JOIN dim_time checkup_time ON (checkup_time.time_key = fact.time_key) "
                    + " JOIN dim_time event_time ON (event_time.time_key = fact.checkup_time_key)"
                    + " JOIN dim_icd10 first_cie10 ON (first_cie10.icd10_key = dim_non_fatal.first_cie10)"
                    + " JOIN dim_icd10 second_cie10 ON (second_cie10.icd10_key = dim_non_fatal.second_cie10)";
        }

        System.out.println("SELECT " + aux1 + " FROM " + aux2 + aux3);

        if (!aux1.isEmpty()) {
            ResultSet rs = connectionJdbcMB.consult("SELECT " + aux1 + " FROM " + aux2 + aux3);

            while (rs.next()) {
                String[] arrayList;
                int columns = rs.getMetaData().getColumnCount();
                arrayList = new String[columns];

                for (int i = 0; i < columns; i++) {
                    arrayList[i] = rs.getString(i + 1);
                }
                data.add(arrayList);
            }
        }
    }
    List<String[]> resultado;

    public void filter(FilterEvent event) {
        DataTable table = (DataTable) event.getSource();
        resultado = table.getFilteredData();
    }
    
    public StreamedContent qualityData() {
        try {
            if (data != null && !data.isEmpty()) {
                return analysis.getQualityDataFile(loginMB.getUserLogin(), colNameData, resultado, data);
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR!!", "No ha cargado datos"));
            }
        } catch (IOException ex) {
            Logger.getLogger(DataViewClusteringMB.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    } 
    
    public StreamedContent clusteringAnalysis() { 
        String tag=null;
        for(SelectItem f:facts){
            if(f.getValue().equals(fact)){
                tag = f.getLabel();
            }        
        }
        try {
            if (data != null && !data.isEmpty()) {
                return analysis.getClusteringFile(loginMB.getUserLogin(), colNameData, resultado, data, valueN, tag);
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR!!", "No ha cargado datos"));
            }
        } catch (IOException ex) {
            Logger.getLogger(DataViewClusteringMB.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }    

    public DualListModel<ItemList> getVariables() {
        return variables;
    }

    public void setVariables(DualListModel<ItemList> variables) {
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

    public ArrayList<String[]> getData() {
        return data;
    }

    public void setData(ArrayList<String[]> data) {
        this.data = data;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getValueN() {
        return valueN;
    }

    public void setValueN(int valueN) {
        this.valueN = valueN;
    }
      
}

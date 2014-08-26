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
public class DataViewClassificationMB {

    private DualListModel<ItemList> variables;
    private String fact;
    private List<SelectItem> facts;
    private String classValue;
    private List<SelectItem> classValues;
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
    private double confidence;
    private double support;
    private int maxM;
    private int minM;
    private int deltaM;
    private double maxC;
    private double minC;
    private double deltaC;

    public DataViewClassificationMB() {
        this.startDate = new Date(1000);
        loginMB = (LoginMB) FacesContext.getCurrentInstance().getApplication().evaluateExpressionGet(FacesContext.getCurrentInstance(), "#{loginMB}", LoginMB.class);
        analysis = new DataAnalysis();
    }

    public DataViewClassificationMB(int countData, int countColNameData, ArrayList<String[]> data, List<String> colNameData, String classValue,
            int maxM, int minM, int deltaM, double maxC, double minC, double deltaC, double confidence,
            double support) {
        this.countData = countData;
        this.countColNameData = countColNameData;
        this.data = data;
        this.colNameData = colNameData;
        this.classValue = classValue;
        this.maxM = maxM;
        this.minM = minM;
        this.deltaM = deltaM;
        this.maxC = maxC;
        this.minC = minC;
        this.deltaC = deltaC;
        this.confidence = confidence;
        this.support = support;
    }

    public void reset() {
        variables = new DualListModel<>();
    }

    @PostConstruct
    public void init() {
        colNameData = new ArrayList<>();
        variablesSource = new ArrayList<>();
        variablesTarget = new ArrayList<>();

        startDate = new Date(100, 0, 1);
        endDate = new Date();

        facts = new ArrayList<>();
        facts.add(new SelectItem("fatal", "Fatales"));
        facts.add(new SelectItem("non_fatal", "No fatales"));


        variables = new DualListModel<>(variablesSource, variablesTarget);
        
        confidence = 0.6;
        support = 0.005;
        maxM = 100;
        minM = 100;
        deltaM = 10;
        maxC = 0.5;
        minC = 0.5;
        deltaC = 0.1;        
    }

    public void loadVariablesPickList() {
        context = FacesContext.getCurrentInstance();
        connectionJdbcMB = (ConnectionJdbcMB) context.getApplication().evaluateExpressionGet(context, "#{connectionJdbcMB}", ConnectionJdbcMB.class);
        connectionJdbcMB.connectToDb();
        
        variablesSource = new ArrayList<>();
        variablesTarget = new ArrayList<>();
        variables = new DualListModel<>(variablesSource, variablesTarget);
        
        ResultSet rs = null;
        

        if (fact.equals("fatal")) {
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

        } catch (SQLException ex) {
            Logger.getLogger(DataViewClassificationMB.class.getName()).log(Level.SEVERE, null, ex);
        }


        variables = new DualListModel<>(variablesSource, variablesTarget);
        loadClass();
    }

    public void loadClass() {
        if (fact.equals("fatal")) {
            classValues = new ArrayList<>();
            classValues.add(new SelectItem("Homicidios", "Homicidios"));
            classValues.add(new SelectItem("Suicidios", "Suicidios"));
            classValues.add(new SelectItem("Transito", "Transito"));
            classValues.add(new SelectItem("Accidentes", "Accidentes"));
        } else {
            classValues = new ArrayList<>();
            classValues.add(new SelectItem("Interpersonal", "Interpersonal"));
            classValues.add(new SelectItem("Intrafamiliar", "Intrafamiliar"));
            classValues.add(new SelectItem("Auto_Inflingido", "Auto Inflingido"));
            classValues.add(new SelectItem("Lesiones_Transito", "Lesiones de Tránsito"));
            classValues.add(new SelectItem("No_Intencional", "No intencional"));
        }
    }

    public void loadDataTable() throws SQLException {
        colNameData = new ArrayList<>();
        colNameData.add("TIPO_EVENTO");
        data = new ArrayList<>();
        ResultSet rs=null;
        String aux1,  aux3;

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

        aux3 = " WHERE date_value BETWEEN '" + sD + "' AND '" + eD + "'";

        // Fatal variables
        String aux_murder = null;
        String aux_suicides = null;
        String aux_traffic = null;
        String aux_accidents = null;
        
        //nonFatal variables
        String aux_interpersonal = null;
        String aux_intrafamiliar = null;
        String aux_self_inflicted = null;
        String aux_transport = null;
        String aux_unintentional = null;
        
        if (this.fact.equals("fatal")) {
            aux_murder = "fact_murder natural join dim_victim natural join dim_date natural join dim_time natural"
                    + " join dim_neighborhood natural join dim_quadrant natural join dim_murder natural join "
                    + " dim_fatal left join dim_jobs using (job_key) left join dim_vulnerable_groups using (vulnerable_group_key)";

            aux_suicides = "fact_suicides natural join dim_victim natural join dim_date natural join dim_time natural"
                    + " join dim_neighborhood natural join dim_quadrant natural join dim_suicide natural join "
                    + " dim_fatal left join dim_jobs using (job_key) left join dim_vulnerable_groups using (vulnerable_group_key)";

            aux_traffic = "fact_traffic natural join dim_victim natural join dim_date natural join dim_time natural"
                    + " join dim_neighborhood natural join dim_quadrant natural join dim_traffic natural join "
                    + " dim_fatal left join dim_jobs using (job_key) left join dim_vulnerable_groups using (vulnerable_group_key)";

            aux_accidents = "fact_accidents natural join dim_victim natural join dim_date natural join dim_time natural"
                    + " join dim_neighborhood natural join dim_quadrant natural join dim_accidents natural join "
                    + " dim_fatal left join dim_jobs using (job_key) left join dim_vulnerable_groups using (vulnerable_group_key)";

            if (!aux1.isEmpty()) {
                rs = connectionJdbcMB.consult("SELECT " + "'Homicidios'," + aux1 + " FROM " + aux_murder + aux3 + " UNION "
                        + "SELECT " + "'Suicidios'," + aux1 + " FROM " + aux_suicides + aux3 + " UNION "
                        + "SELECT " + "'Auto_Inflingido'," + aux1 + " FROM " + aux_traffic + aux3 + " UNION "
                        + "SELECT " + "'Accidentes'," + aux1 + " FROM " + aux_accidents + aux3);
            }
        } else {
            aux_interpersonal = "fact_interpersonal natural join dim_interpersonal natural join dim_anatomical_location natural join"
                    +" dim_date natural join dim_time left join dim_kind_of_injury using (kind_of_injury_key) natural join dim_neighborhood"
                    +" natural join dim_non_fatal natural join dim_victim";
            
            aux_intrafamiliar = "fact_intrafamiliar natural join dim_abuse left join dim_anatomical_location using (anatomical_location_key)"
                    + " natural join dim_date left join dim_kind_of_injury using (kind_of_injury_key) natural join dim_neighborhood natural join"
                    + " dim_non_fatal natural join dim_time natural join dim_victim left join dim_aggressor using (aggressor_key)";
            
            aux_self_inflicted = "fact_self_inflicted natural join dim_date natural join dim_time natural join dim_anatomical_location"
                    + " left join dim_kind_of_injury using (kind_of_injury_key) natural join dim_neighborhood  natural join dim_non_fatal"
                    + " natural join dim_self_inflicted natural join dim_victim";
            
            aux_transport = "fact_transport natural join dim_date natural join dim_time natural join dim_anatomical_location left"
                    + " join dim_kind_of_injury using(kind_of_injury_key) natural join dim_neighborhood natural join dim_non_fatal"
                    + " left join dim_security_elements using (security_elements_key) natural join dim_transport natural join"
                    + " dim_victim ";
            
            aux_unintentional = "fact_unintentional natural join dim_date natural join dim_time natural join dim_anatomical_location left"
                    + " join dim_kind_of_injury using (kind_of_injury_key) natural join dim_neighborhood natural join dim_non_fatal natural"
                    + " join dim_victim";
            
            if (!aux1.isEmpty()) {
                rs = connectionJdbcMB.consult("SELECT " + "'Interpersonal'," + aux1 + " FROM " + aux_interpersonal + aux3 + " UNION "
                        + "SELECT " + "'Intrafamiliar'," + aux1 + " FROM " + aux_intrafamiliar + aux3 + " UNION "
                        + "SELECT " + "'Transito'," + aux1 + " FROM " +  aux_self_inflicted + aux3 + " UNION "
                        + "SELECT " + "'Lesiones_Transito'," + aux1 + " FROM " + aux_transport + aux3 + " UNION "
                        + "SELECT " + "'No_Intencional'," + aux1 + " FROM " + aux_unintentional + aux3);
            }
        }

       

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
    List<String[]> resultado;

    public void filter(FilterEvent event) {
        DataTable table = (DataTable) event.getSource();
        resultado = table.getFilteredData();
    }

    public StreamedContent qualityData() {
        try {
            return analysis.getQualityDataFile(loginMB.getUserLogin(), colNameData, resultado, data);
        } catch (IOException ex) {
            Logger.getLogger(DataViewClassificationMB.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public StreamedContent classificationAnalysis() {                
        try {
            return analysis.getClassificationFile(loginMB.getUserLogin(), colNameData, resultado, data,
                     classValue, maxM, minM, deltaM, maxC, minC, deltaC, confidence, support);
        } catch (IOException ex) {
            Logger.getLogger(DataViewClassificationMB.class.getName()).log(Level.SEVERE, null, ex);
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

    public String getClassValue() {
        return classValue;
    }

    public void setClassValue(String classValue) {
        this.classValue = classValue;
    }

    public List<SelectItem> getClassValues() {
        return classValues;
    }

    public void setClassValues(List<SelectItem> classValues) {
        this.classValues = classValues;
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
    
    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public double getSupport() {
        return support;
    }

    public void setSupport(double support) {
        this.support = support;
    }

    public int getMaxM() {
        return maxM;
    }

    public void setMaxM(int maxM) {
        this.maxM = maxM;
    }

    public int getMinM() {
        return minM;
    }

    public void setMinM(int minM) {
        this.minM = minM;
    }

    public int getDeltaM() {
        return deltaM;
    }

    public void setDeltaM(int deltaM) {
        this.deltaM = deltaM;
    }

    public double getMaxC() {
        return maxC;
    }

    public void setMaxC(double maxC) {
        this.maxC = maxC;
    }

    public double getMinC() {
        return minC;
    }

    public void setMinC(double minC) {
        this.minC = minC;
    }

    public double getDeltaC() {
        return deltaC;
    }

    public void setDeltaC(double deltaC) {
        this.deltaC = deltaC;
    }
    
    
}
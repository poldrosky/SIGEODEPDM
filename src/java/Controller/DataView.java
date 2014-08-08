package Controller;

import beans.connection.ConnectionJdbcMB;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import managedBeans.login.ApplicationControlMB;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.primefaces.component.datatable.DataTable;
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
    private List<String> colNameData = new ArrayList<>();
    private Date startDate;
    private Date endDate;

    public DataView() {
        this.startDate = new Date(1000);
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
        String aux1, aux2 = null, aux3;

        ArrayList<String> arrayList = new ArrayList<>();

        for (int i = 0; i < this.variables.getTarget().size(); i++) {
            for (SelectItem source : variablesSource) {
                String a = (String) source.getValue();
                String b = String.valueOf(variables.getTarget().get(i));

                if (a.equals(b)) {
                    colNameData.add(source.getLabel());
                    break;
                }
            }
        }

        aux1 = String.valueOf(variables.getTarget()).replace("[", "").replace("]", "");

        SimpleDateFormat d = new SimpleDateFormat("dd-M-yyyy");
        String sD = d.format(startDate);
        String eD = d.format(endDate);

        aux3 = " WHERE date_value BETWEEN '" + sD + "' AND '" + eD + "'";

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

        System.out.println("SELECT " + aux1 + " FROM " + aux2 + aux3);

        if (!aux1.isEmpty()) {
            ResultSet rs = connectionJdbcMB.consult("SELECT " + aux1 + " FROM " + aux2 + aux3);

            while (rs.next()) {
                arrayList = new ArrayList<>();
                int columns = rs.getMetaData().getColumnCount();
                for (int i = 0; i < columns; i++) {
                    arrayList.add(rs.getString(i + 1));
                }
                data.add(arrayList.toArray());
            }
        }
    }

    public void preProcess(Object document) {
        HSSFWorkbook book = (HSSFWorkbook) document;
        HSSFSheet sheet = book.getSheetAt(0);// Se toma hoja del libro
        
        HSSFCellStyle cellStyle = book.createCellStyle();
        HSSFFont font = book.createFont();
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        cellStyle.setFont(font);

        Iterator<Row> rowIterator = sheet.iterator();
        while(rowIterator.hasNext()){
            Row row = rowIterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            while(cellIterator.hasNext()){
                Cell cell = cellIterator.next();
                System.out.println(cell.getStringCellValue());
            }
        }
                
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
}

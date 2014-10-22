/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package beans.connection;

import java.io.Serializable;
import java.sql.*;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.sql.DataSource;



/**
 *
 * @author SANTOS
 */
@ManagedBean(name = "connectionJdbcMB")
@SessionScoped
public class ConnectionJdbcMB implements Serializable {


   @Resource(name = "jdbc/od_dwh3")
    private DataSource ds;
    public Connection conn;
    private Statement st;
    private ResultSet rs;
    private String msj;
    private String user;
    private String db;   
    private String password;
    private String server;
    private String url = "";
    private boolean showMessages = true;//determinar si mostrar o no los mensajes de error

    /**
     * Creates a new instance of ConnectionJdbcMB
     */
    public ConnectionJdbcMB() {
    }

    @PreDestroy
    public synchronized void disconnect() {
        try {
            if (!conn.isClosed()) {
                conn.close();
                System.out.println("Cerrada conexion a base de datos " + url + " ... OK  " + this.getClass().getName());
            }
        } catch (Exception e) {
            System.out.println("Error al cerrar conexion a base de datos " + url + " ... " + e.toString());
        }
    }

    public String checkConnection() {
        /*
         * verifica si se puede conectar a la bodega y db observatorio y almacena
         * estos datos en la tabla configuraciones
         * si se conecta retorna "index" que es la pagina de inicio de la aplicacion
         */
        boolean continueProcess = true;
        try {
            if (ds == null) {
                System.out.println("ERROR: No se obtubo data source");
                continueProcess = false;
            }
            if (continueProcess) {
                conn = ds.getConnection();
                if (conn == null || conn.isClosed()) {
                    System.out.println("Error: No se obtubo conexion");
                    continueProcess = false;
                }
            }
            if (continueProcess) {
                try {
                    Class.forName("org.postgresql.Driver").newInstance();// seleccionar SGBD
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    msj = e.toString() + " --- Clase: " + this.getClass().getName();
                    continueProcess = false;
                }
            }
            if (continueProcess) {

                boolean correctConnection = true;

                
                try {
                    url = "jdbc:postgresql://" + server + "/" + db;
                    conn = DriverManager.getConnection(url, user, password);//conectarse db del observatorio
                    if (conn != null && !conn.isClosed()) {
                        msj = msj + " Conexión a base de datos observatorio Correcta.";
                    } else {
                        msj = msj + " Conexión a base de datos observatorio Fallida.";
                        correctConnection = false;
                    }
                } catch (Exception e) {
                    msj = msj + " Conexión a base de datos observatorio Fallida.";
                    correctConnection = false;
                    conn = null;
                }
                if (correctConnection) {//se realizaron las dos conexiones(observatorio y bodega)                    
                    
                    return "index";//se redirirecciona a la pagina principal
                }
            }
        } catch (Exception e) {
            msj = e.toString();
        }
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR", "La conexion no pudo ser creada con los datos suministrados. " + msj));
        return "";
    }

    public final boolean connectToDb() {
        /*
         * conectarse a la base datos observatorio y bodega
         * y retornar true si se realizo la conexion
         */
        boolean continueProcess = true;
        try {
            if (conn == null || conn.isClosed()) {
                if (ds == null) {
                    System.out.println("ERROR: No se obtubo data source");
                    continueProcess = false;
                }
                if (continueProcess) {
                    try {
                        conn = ds.getConnection();
                    } catch (Exception e) {
                    }
                    if (conn == null || conn.isClosed()) {
                        System.out.println("Error: No se obtubo conexion");
                        continueProcess = false;
                    }
                }

                if (continueProcess) {
                    try {
                        Class.forName("org.postgresql.Driver").newInstance();// seleccionar SGBD
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                        System.out.println("Error1: " + e.toString() + " --- Clase: " + this.getClass().getName());
                        continueProcess = false;
                    }
                }

                if (continueProcess) {
                    ResultSet rs1 = consult("Select * from configurations");
                    try {
                        if (rs1.next()) {
                            user = rs1.getString("user_db");
                            db = rs1.getString("name_db");
                            password = rs1.getString("password_db");
                            server = rs1.getString("server_db");
                        } else {
                            continueProcess = false;
                        }
                    } catch (Exception e) {
                        continueProcess = false;
                    }
                }

                if (continueProcess) {
                    boolean correctConnection = true;

                    try {
                        url = "jdbc:postgresql://" + server + "/" + db;
                        conn = DriverManager.getConnection(url, user, password);//conectarse db del observatorio
                        if (conn != null && !conn.isClosed()) {
                            msj = msj + " Conexión a base de datos observatorio Correcta.";
                        } else {
                            msj = msj + " Conexión a base de datos observatorio Fallida.";
                            correctConnection = false;
                        }
                    } catch (Exception e) {
                        msj = msj + " Conexión a base de datos observatorio Fallida.";
                        correctConnection = false;
                        conn = null;
                    }
                    if (correctConnection) {//se realizaron las dos conexiones(observatorio y bodega)                    
                        System.out.println("Conexion a base de datos " + url + " ... OK  " + this.getClass().getName());
                        return true;
                    } else {
                        System.out.println("No se pudo conectar a base de datos " + url + " ... FAIL");
                        return false;
                    }
                }
            }
        } catch (Exception e) {
        }
        return true;
    }

    public Connection getConn() {
        return conn;
    }

    public void setConn(Connection con) {
        this.conn = con;
    }

    public ResultSet consult(String query) {
        msj = "";
        try {
            if (conn != null) {
                st = conn.createStatement();
                rs = st.executeQuery(query);
                //System.out.println("---- CONSULTA: " + query);
                return rs;
            } else {
                msj = "There don't exist connection";
                return null;
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.toString() + " - Clase: " + this.getClass().getName());
            msj = "ERROR: " + e.getMessage() + "---- CONSULTA:" + query;
            return null;
        }
    }

    public int non_query(String query) {
        msj = "";
        int reg;
        reg = 0;
        try {
            if (conn != null) {
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    reg = stmt.executeUpdate();
                }
            }

        } catch (SQLException e) {
            if (showMessages) {
                System.out.println("Error: " + e.toString() + " -- Clase: " + this.getClass().getName()+" -  "+ query);
            }
            msj = "ERROR: " + e.getMessage();
        }
        return reg;
    }

    public String insert(String Tabla, String elementos, String registro) {
        msj = "";
        int reg = 1;
        String success;
        try {
            if (conn != null) {
                st = conn.createStatement();
                st.execute("INSERT INTO " + Tabla + " (" + elementos + ") VALUES (" + registro + ")");
                if (reg > 0) {
                    success = "true";
                } else {
                    success = "false";
                }
                st.close();
            } else {
                success = "false";
                msj = "ERROR: There don't exist connection...";
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.toString() + " --- Clase: " + this.getClass().getName());
            System.out.println("numero: " + e.getErrorCode());
            success = e.getMessage();
            msj = "ERROR: " + e.getMessage();
        }
        return success;
    }

    public void remove(String Tabla, String condicion) {
        msj = "";

        int reg;
        try {
            if (conn != null) {
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM " + Tabla + " WHERE " + condicion)) {
                    reg = stmt.executeUpdate();
                    if (reg > 0) {
                    } else {
                    }
                }
            } else {
                msj = "ERROR: There don't exist connection";
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.toString() + " ---- Clase: " + this.getClass().getName());
            msj = "ERROR: " + e.getMessage();
        }
    }

    public void update(String Tabla, String campos, String donde) {
        msj = "";
        int reg;
        try {
            if (conn != null) {
                try (PreparedStatement stmt = conn.prepareStatement("UPDATE " + Tabla + " SET " + campos + " WHERE " + donde)) {
                    reg = stmt.executeUpdate();
                    if (reg > 0) {
                    } else {
                    }
                }
            } else {
                msj = "ERROR: There don't exist connection";
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.toString() + " ----- Clase: " + this.getClass().getName());
            msj = "ERROR: " + e.getMessage();
        }
    }

    public String getMsj() {
        return msj;
    }

    public void setMsj(String mens) {
        msj = mens;
    }

  
    // --------------------------    
    // -- METODOS GET Y SET -----
    // --------------------------    
    
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

        public boolean isShowMessages() {
        return showMessages;
    }

    public void setShowMessages(boolean showMessages) {
        this.showMessages = showMessages;
    }
      
}

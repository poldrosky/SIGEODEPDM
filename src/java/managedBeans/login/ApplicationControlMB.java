/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package managedBeans.login;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

/**
 *
 * @author santos
 */
@ManagedBean(name = "applicationControlMB")
@ApplicationScoped
public class ApplicationControlMB {

    @Resource(name = "jdbc/od")
    private DataSource ds;//fuente de datos(es configurada por glassfish)  
    private ResultSet rs;
    private Statement st;
    private String user;
    private String db;
    private String db_dwh;
    private String password;
    private String server;
    private String url = "";
    private ArrayList<String> currentIdSessions = new ArrayList<>();//lista de identificadores de sesiones
    private ArrayList<Integer> currentUserIdSessions = new ArrayList<>();//lista de id de usuarios logeados
    private String value = "-";//desde pagina de login se llama a este valor para que el objeto applicationControlMB sea visible en el contexto 
    private String realPath = "";
    public Connection conn;

    public ApplicationControlMB() {
        /*
         * Constuctor de la clase: obtiene la ruta real del servidor e 
         * inicia un timer que es llamado cada hora
         */
        ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        realPath = (String) servletContext.getRealPath("/");
        

    }

    @PostConstruct
    private void event() {
        connectToDb();
    }

    @PreDestroy
    private void destroySession() {
        try {
            if (conn != null && !conn.isClosed()) {
                disconnect();
            }
        } catch (Exception e) {
            //System.out.println("Termina session por inactividad 003 " + e.toString());
        }
    }

    public void disconnect() {
        try {
            if (!conn.isClosed()) {
                conn.close();
                System.out.println("Cerrada conexion a base de datos " + url + " ... OK  " + this.getClass().getName());
            }
        } catch (Exception e) {
            System.out.println("Error al cerrar conexion a base de datos " + url + " ... " + e.toString());
        }
    }
    

    private void printOutputFromProcces(Process p, String description) {
        /*
         * mostrar por consola el progreso de un proceso externo invocado
         */
        System.out.println("\nInicia proceso " + description + " /////////////////////////////////////////");
        try {
            //CODIGO PARA MOSTRAR EL PROGESO DE LA GENERACION DEL ARCHIVO
            InputStream is = p.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String ll;
            while ((ll = br.readLine()) != null) {
                System.out.println(ll);
            }
        } catch (IOException e) {
            System.out.println("Error 99 " + e.getMessage());
        }
        System.out.println("Termina proceso " + description + " /////////////////////////////////////////\n");
    }

    public final boolean connectToDb() {
        /*
         * Nos conectamos a la base de datos atraves 
         * de un DataSource = (conexion configurada por glassFish)
         * y generamos una conexion normal por JDBC
         */
        boolean returnValue = true;
        if (conn == null) {
            returnValue = false;
            try {
                if (ds == null) {
                    System.out.println("ERROR: No se obtubo data source... applicationControlMB");
                } else {
                    conn = ds.getConnection();
                    if (conn == null) {
                        System.out.println("Error: No se obtubo conexion");
                    } else {
                        ResultSet rs1 = consult("Select * from configurations");
                        if (rs1.next()) {
                            user = rs1.getString("user_db");
                            db = rs1.getString("name_db");
                            db_dwh = rs1.getString("name_db_dwh");
                            password = rs1.getString("password_db");
                            server = rs1.getString("server_db");
                            url = "jdbc:postgresql://" + server + "/" + db;
                            try {
                                Class.forName("org.postgresql.Driver").newInstance();// seleccionar SGBD
                            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                                System.out.println("Error 1 en " + this.getClass().getName() + ":" + e.getMessage());
                            }
                            //conn.close();
                            conn = DriverManager.getConnection(url, user, password);// Realizar la conexion
                            if (conn != null) {
                                System.out.println("Conexion a base de datos " + url + " " + this.getClass().getName());
                                returnValue = true;
                            } else {
                                System.out.println("No se pudo conectar a base de datos " + url + " ... FAIL");
                            }
                        } else {
                            System.out.println("No se pudo conectar a base de datos " + url + " ... FAIL");
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error 2 en " + this.getClass().getName() + ":" + e.getMessage());
            }
        }
        return returnValue;
    }

    public ResultSet consult(String query) {
        /*
         * se encarga de procesar una consulta que retorne una o varias tuplas
         * de la base de datos retornandolas en un ResultSet
         */
        //msj = "";
        try {
            if (conn != null) {
                st = conn.createStatement();
                rs = st.executeQuery(query);
                //System.out.println("---- CONSULTA: " + query);
                return rs;
            } else {
                System.out.println("There don't exist connection");
                return null;
            }
        } catch (SQLException e) {
            //System.out.println("Error 3 en " + this.getClass().getName() + ":" + e.getMessage() + "---- CONSULTA:" + query);
            return null;
        }
    }

    public int non_query(String query) {
        /*
         * se encarga de procesar una consulta que no retorne tuplas
         * ejemplo: INSERT, UPDATE, DELETE...
         * retorna 0 si se realizo correctamente
         * retorna 1 cuando la instuccion no pudo ejecutarse
         */
        int reg;
        reg = 0;
        try {
            if (conn != null) {
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    reg = stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.out.println("Error 4 en " + this.getClass().getName() + ":" + e.getMessage());
        }
        return reg;
    }

    public boolean hasLogged(int idUser) {
        /*
         * determina si un usario tiene una sesion iniciada 
         * idUser= identificador del usuario en la base de datos
         */
        boolean foundIdUser = false;
        //determinar si el usuario ya tiene iniciada una sesion
        for (int i = 0; i < currentUserIdSessions.size(); i++) {
            if (currentUserIdSessions.get(i) == idUser) {
                foundIdUser = true;
                break;
            }
        }
        return foundIdUser;
    }

    public void addSession(int idUser, String idSession) {
        /*
         * adicionar a la lista de sesiones activas
         */
        currentIdSessions.add(idSession);
        currentUserIdSessions.add(idUser);//System.out.println("Agregada Nueva sesion: " + idSession + "  usuario: " + idUser);
    }

    public void removeSession(int idUser) {
        /*
         * eliminar de la lista de sesiones activas dependiento del id del usuario
         */
        try {
            for (int i = 0; i < currentUserIdSessions.size(); i++) {
                if (currentUserIdSessions.get(i) == idUser) {
                    currentUserIdSessions.remove(i);
                    currentIdSessions.remove(i);//System.out.println("Session eliminada usuario: " + idUser);
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Error 9 en " + this.getClass().getName() + ":" + e.getMessage());
        }
    }

    public void removeSession(String idSession) {
        /*
         * eliminar de la lista de sesiones actuales dependiento del id de la sesion
         */
        try {
            for (int i = 0; i < currentUserIdSessions.size(); i++) {
                if (currentIdSessions.get(i).compareTo(idSession) == 0) {
                    currentUserIdSessions.remove(i);
                    currentIdSessions.remove(i);//System.out.println("Session eliminada sesion: " + idSession);
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Error 10 en " + this.getClass().getName() + ":" + e.getMessage());
        }
    }

    public boolean findIdSession(String idSessionSearch) {
        /*
         * buscar una session segun su id
         */
        boolean booleanReturn = false;
        for (int i = 0; i < currentIdSessions.size(); i++) {
            if (currentIdSessions.get(i).compareTo(idSessionSearch) == 0) {
                booleanReturn = true;
                break;
            }
        }
        return booleanReturn;
    }

    public void closeAllSessions() {
        /*
         * eliminar todas las sessiones activas (se usa cuando se realiza una 
         * restauracion de la copia de seguridad)
         */
        for (int i = 0; i < currentUserIdSessions.size(); i++) {
            currentUserIdSessions.remove(0);
            currentIdSessions.remove(0);
            i = -1;
        }


    }

    public int getMaxUserId() {
        //deteminar cual es el maximo identificador de los usuarios que esten en el sistema
        //los invitados inician en 1000
        if (currentUserIdSessions != null && !currentUserIdSessions.isEmpty()) {
            int max = 0;
            for (int i = 0; i < currentUserIdSessions.size(); i++) {
                if (currentUserIdSessions.get(i) > max) {
                    max = currentUserIdSessions.get(i);
                }
            }
            return max;
        } else {
            return 0;
        }
    }


    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package managedBeans.login;

import beans.connection.ConnectionJdbcMB;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

/**
 *
 * @author santos
 */
@ManagedBean(name = "loginMB")
@SessionScoped
public class LoginMB {

    private String idSession = "";//identificador de la session
    private String loginname = "";
    private String password = "";
    private int userId;
    private String userLogin = "";
    private String userName = "";
    private String userJob = "";
    private Boolean userSystem = true;//true = usuario del sistema //false = usuario invitado
    private String activeIndexAcoordion1 = "-1";
    private String activeIndexAcoordion2 = "-1";
    private boolean activeSantos = false;
    private boolean permissionFatal = false;
    private boolean permissionNonFatal = false;
    private boolean permissionVif = false;
    private boolean permissionIndicators = false;
    private boolean permissionAdministrator = false;
    StringEncryption stringEncryption = new StringEncryption();
    private boolean permissionRegistryDataSection = true;
    FacesContext context;
    ConnectionJdbcMB connectionJdbcMB;
    private String closeSessionDialog = "";
    private boolean autenticado = false;
    ApplicationControlMB applicationControlMB;

public LoginMB() {
    }

    public void logout1() {
        /*
         * fin de session por que se inicio una nueva session en otro equipo      
         */
        applicationControlMB.removeSession(idSession);//elimino de la lista de usuarios actuales en el sistema   
        try {
            ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
            String ctxPath = ((ServletContext) ctx.getContext()).getContextPath();
            ((HttpSession) ctx.getSession(false)).invalidate();  //System.out.println("se redirecciona");
            ctx.redirect(ctxPath + "/index.html?v=close");
        } catch (Exception ex) {//System.out.println("Excepcion cuando usuario cierra sesion sesion: " + ex.toString());
        }
    }

    public void logout2() {
        /*
         * fin de sesion dada por el usuario: botón "cerrar cesion"
         */
        applicationControlMB.removeSession(idSession);//System.out.println("Finaliza session "+loginname+" ID: "+idSession);

        try {
            ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
            String ctxPath = ((ServletContext) ctx.getContext()).getContextPath();
            ((HttpSession) ctx.getSession(false)).invalidate();//System.out.println("se redirecciona");
            ctx.redirect(ctxPath + "/index.html");
        } catch (Exception ex) {
            System.out.println("Excepcion cuando usuario cierra sesion sesion: " + ex.toString());
        }
    }

    public void reset() {
    }

    public String closeSessionAndLogin() {
        /*
         * terminar una session iniciada en otra terminal y continuar abriendo una nueva;
         * se usa cuando un mismo usuario intenta loguearse desde dos
         * terminales distintas, 
         */
        applicationControlMB.removeSession(userId);
        return continueLogin();
    }

    private String continueLogin() {
        /*
         * instanciar todas las variables necesarias para que un usuario inicie una session
         */
        context = FacesContext.getCurrentInstance();
        connectionJdbcMB = (ConnectionJdbcMB) context.getApplication().evaluateExpressionGet(context, "#{connectionJdbcMB}", ConnectionJdbcMB.class);
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("username", loginname);
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);

        idSession = session.getId();

        applicationControlMB.addSession(userId, idSession);

        return inicializeVariables();
    }



    private String inicializeVariables() {
        if (connectionJdbcMB.connectToDb()) {  
            autenticado = true;            
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Correcto!!", "Se ha ingresado al sistema");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return "homePage";
        } else {
            FacesMessage msg2 = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR", "Se debe crear una conexión");
            FacesContext.getCurrentInstance().addMessage(null, msg2);
            password = "";
            return "indexConfiguration";
        }
    }   

    public String CheckValidUser() {
        /*
         * determinar si el usuario puede acceder al sistema determinando si exite
         * el login, clave y la cuenta esta activa
         */
        closeSessionDialog = "-";
        password = stringEncryption.getStringMessageDigest(password, "SHA-1");
        context = FacesContext.getCurrentInstance();
        connectionJdbcMB = (ConnectionJdbcMB) context.getApplication().evaluateExpressionGet(context, "#{connectionJdbcMB}", ConnectionJdbcMB.class);
        connectionJdbcMB.connectToDb();
        ResultSet rs = connectionJdbcMB.consult("Select * from users where user_password like '" + password + "' and user_login like '" + loginname + "'");
        try {
            if (rs.next()) {
                ExternalContext contexto = FacesContext.getCurrentInstance().getExternalContext();
                applicationControlMB = (ApplicationControlMB) contexto.getApplicationMap().get("applicationControlMB");
                //determino si el usuario tiene una session alctiva
                if (applicationControlMB.hasLogged(rs.getInt("user_id"))) {
                    //System.out.println("Ingreso rechazado, ya tiene otra session activa");
                    closeSessionDialog = "closeSessionDialog.show()";//dialog que permite terminar sesion desde otra terminal
                    return "";//no dirigir a ninguna pagina
                } else {
                    userLogin = rs.getString("user_login");
                    userName = rs.getString("user_name");
                    userId = rs.getInt("user_id");
                    return continueLogin();
                }
            } else {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR", "Incorrecto Usuario o Clave");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                password = "";
                return "";
            }

        } catch (SQLException ex) {
            Logger.getLogger(LoginMB.class.getName()).log(Level.SEVERE, null, ex);
        }
return "";




    }

    public String getLoginname() {
        return loginname;
    }

    public void setLoginname(String loginname) {
        this.loginname = loginname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserJob() {
        return userJob;
    }

    public void setUserJob(String userJob) {
        this.userJob = userJob;
    }
    
    public String getActiveIndexAcoordion1() {
        return activeIndexAcoordion1;
    }

    public void setActiveIndexAcoordion1(String activeIndexAcoordion1) {
        this.activeIndexAcoordion1 = activeIndexAcoordion1;
    }

    public String getActiveIndexAcoordion2() {
        return activeIndexAcoordion2;
    }

    public void setActiveIndexAcoordion2(String activeIndexAcoordion2) {
        this.activeIndexAcoordion2 = activeIndexAcoordion2;
    }

    public String getCloseSessionDialog() {
        return closeSessionDialog;
    }

    public void setCloseSessionDialog(String closeSessionDialog) {
        this.closeSessionDialog = closeSessionDialog;
    }

    public String getIdSession() {
        return idSession;
    }

    public void setIdSession(String idSession) {
        this.idSession = idSession;
    }

    public boolean isPermissionFatal() {
        return permissionFatal;
    }

    public void setPermissionFatal(boolean permissionFatal) {
        this.permissionFatal = permissionFatal;
    }

    public boolean isPermissionNonFatal() {
        return permissionNonFatal;
    }

    public void setPermissionNonFatal(boolean permissionNonFatal) {
        this.permissionNonFatal = permissionNonFatal;
    }

    public boolean isPermissionVif() {
        return permissionVif;
    }

    public void setPermissionVif(boolean permissionVif) {
        this.permissionVif = permissionVif;
    }

    public boolean isPermissionIndicators() {
        return permissionIndicators;
    }

    public void setPermissionIndicators(boolean permissionIndicators) {
        this.permissionIndicators = permissionIndicators;
    }

    public boolean isPermissionAdministrator() {
        return permissionAdministrator;
    }

    public void setPermissionAdministrator(boolean permissionAdministrator) {
        this.permissionAdministrator = permissionAdministrator;
    }

//    public boolean isDisableNonFatalSection() {
//        return disableNonFatalSection;
//    }
//
//    public void setDisableNonFatalSection(boolean disableNonFatalSection) {
//        this.disableNonFatalSection = disableNonFatalSection;
//    }
//
//    public boolean isDisableFatalSection() {
//        return disableFatalSection;
//    }
//
//    public void setDisableFatalSection(boolean disableFatalSection) {
//        this.disableFatalSection = disableFatalSection;
//    }
//
//    public boolean isDisableVifSection() {
//        return disableVifSection;
//    }
//
//    public void setDisableVifSection(boolean disableVifSection) {
//        this.disableVifSection = disableVifSection;
//    }
//
//    public boolean isDisableIndicatorsSection() {
//        return disableIndicatorsSection;
//    }
//
//    public void setDisableIndicatorsSection(boolean disableIndicatorsSection) {
//        this.disableIndicatorsSection = disableIndicatorsSection;
//    }
//
//    public boolean isDisableAdministratorSection() {
//        return disableAdministratorSection;
//    }
//
//    public void setDisableAdministratorSection(boolean disableAdministratorSection) {
//        this.disableAdministratorSection = disableAdministratorSection;
//    }
    public boolean isPermissionRegistryDataSection() {
        return permissionRegistryDataSection;
    }

    public void setPermissionRegistryDataSection(boolean permissionRegistryDataSection) {
        this.permissionRegistryDataSection = permissionRegistryDataSection;
    }

    public boolean isAutenticado() {
        return autenticado;
    }

    public void setAutenticado(boolean autenticado) {
        this.autenticado = autenticado;
    }

    public Boolean getUserSystem() {
        return userSystem;
    }

    public void setUserSystem(Boolean userSystem) {
        this.userSystem = userSystem;
    }

    public boolean isActiveSantos() {
        return activeSantos;
    }

    public void setActiveSantos(boolean activeSantos) {
        this.activeSantos = activeSantos;
    }
}

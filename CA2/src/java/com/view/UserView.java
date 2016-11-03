/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.view;

import com.business.GroupBean;
import com.business.UserBean;
import com.model.Group;
import com.model.User;
import com.session.SessionUtils;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Vishnu
 */
@RequestScoped
@Named
public class UserView {
    
    @EJB private UserBean userBean;
    @EJB private GroupBean groupBean;
    private User user;

    @PostConstruct
    public void init(){
        user = new User();
    }
    
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    
    public String login() {
        
            HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance()
                    .getExternalContext().getRequest();
//        HttpSession session = req.getSession(false);
               
        try{
            req.login(user.getUsername(), user.getPassword());
            //session.setAttribute("userid", user.getUsername());
        }catch(Throwable t){
            t.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Authentication failed ! Please try again."));
            return (null);
        }
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Registered Successfully"));
        return ("secure/main?faces-redirect=true");
    }
    
    public void registerUser() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        
        String password = encryptPassword(user.getPassword());
        if(password != null ){
            user.setPassword(password);
            userBean.registerUser(user);

            Group group = new Group();
            group.setGroupid("USER");
            group.setUserid(user.getUsername());
            groupBean.addGroup(group);
        }
    }
    
    public void logout() throws IOException {
        
        //HttpServletRequest req = SessionUtils.getRequest();
        HttpSession session = SessionUtils.getSession();
        session.invalidate();
        FacesContext.getCurrentInstance().getExternalContext().redirect("login");
    }
    
    public String encryptPassword(String password){
        
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(UserView.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
        String pass = user.getPassword();

        try {
            md.update(pass.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(UserView.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        byte[] digest = md.digest();
        StringBuffer sb = new StringBuffer();
        
        for (int i = 0; i < digest.length; i++) {
            String hex = Integer.toHexString(0xff & digest[i]);
            if (hex.length() == 1) sb.append('0');
            sb.append(hex);
            
        }
        return sb.toString();
    }
    
    public void isLoggedIn() throws IOException{
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        
        if(ec.getUserPrincipal() != null){
            ec.redirect(ec.getRequestContextPath()+ "/faces/secure/main.xhtml");
        }
    }
}
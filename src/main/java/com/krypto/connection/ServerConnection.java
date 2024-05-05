/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.krypto.connection;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import java.lang.ProcessBuilder.Redirect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.json.JSONObject;

/**
 *
 * @author kumaran
 */
public class ServerConnection {

    private Connection getConnection() {
        Connection con = null;
        try {
            Class.forName("org.postgresql.Driver");
            con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres",
                            "postgres", "Kumaran5usha#");
            con.setSchema("authenticator");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return con;
    }
    
    public boolean newUserCheck(String data) throws SQLException{
        boolean rtnflag = true;
        Connection con = null;
        try{
            System.out.println("@@INSIDE NEW USER CHECK "+data);
            JSONObject js= new JSONObject(data);
            con = getConnection();
            PreparedStatement pst = null;
            String SQL = "select uid from userdetail where username=? and emailid=?";
            pst = con.prepareCall(SQL);
            pst.setString(1, js.getString("username"));
            pst.setString(2, js.getString("emailid"));
            ResultSet rs = pst.executeQuery();
            if(rs.next()){
                System.out.println("@@INSIDE userdetail check "+rs.getString("uid"));
                rtnflag = false;
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(con!=null){
                con.close();
            }
        }
        return rtnflag;
    }
    
    public boolean newUserSave(String newData) throws SQLException{
        boolean rtnflag = true;
        Connection con = null;
        try{
            System.out.println("@@INSIDE NEW USER CHECK "+newData);
            JSONObject js= new JSONObject(newData);
            String hashedPassword = passwordEncrypt(js.getString("password"));
            con = getConnection();
            String SQL = "insert into userdetail(username,password,emailid) values(?,?,?)";
            PreparedStatement pst = null;
            pst = con.prepareStatement(SQL);
            pst.setString(1, js.getString("username"));
            pst.setString(2, hashedPassword);
            pst.setString(3, js.getString("emailid"));
            pst.execute();
        }catch(Exception e){
            rtnflag=false;
            e.printStackTrace();
        }finally{
            if(con!=null){
                con.close();
            }
        }
        return rtnflag;
    }
    
    public HashMap loginCheck(String loginData) throws SQLException{
        Connection con = null;
        boolean rtnflag = true;
        HashMap rtnmap = new HashMap();
        try{
            System.out.println("LOGIN DATA >>>"+loginData);
            con = getConnection();
            PreparedStatement pst = null;
            JSONObject js = new JSONObject(loginData);
            String loginname = js.getString("username");
            System.out.println("THIS IS LOGINNAME "+loginname);
            String SQL = "select uid,password,emailid from userdetail where username=?";
            pst  = con.prepareStatement(SQL);
            pst.setString(1, loginname);
            ResultSet rs = pst.executeQuery();
            if(rs.next()){
                String hashedPassword = rs.getString("password");
                System.out.println("THIS IS HASHAED "+hashedPassword);
                rtnflag = loginDecrypt(hashedPassword,js.get("password").toString());
                rtnmap.put("status", rtnflag);
                if(rtnflag){
                    String mailid = rs.getString("emailid");
                    rtnmap.put("emailid", mailid);
                    int userid = rs.getInt("uid");
                    rtnmap.put("userid", userid);
                }
            }else{
                rtnmap.put("status", false);
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(con!=null){
                con.close();
            }
        }
        System.out.println("LOGIN RETURN FLAG >>"+rtnmap);
        return rtnmap;
    }
   
  
    public String passwordEncrypt(String password) {
        String hashedPassword = "";
        try {
            Argon2 argon2 = Argon2Factory.create();

            // Hash a password
            hashedPassword = argon2.hash(10, 65536, 1, password);

            // Print the hashed password
            System.out.println("Hashed password: " + hashedPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hashedPassword;
    }
    
    public boolean loginDecrypt(String hashedPassword,String password){
        boolean rtnflag = false;
        try{
            Argon2 argon2 = Argon2Factory.create();
            rtnflag = argon2.verify(hashedPassword, password);
        }catch(Exception e){
            e.printStackTrace();
        }
        return rtnflag;
    }
    
    public HashMap authcodesave(String data){
        System.out.println("INSIDE AUTH COde Creation");
        Connection con = null;
        HashMap rtnmap = new HashMap();
        try{
            con = getConnection();
            Random rand = new Random();
            int autCode = rand.nextInt((999998 - 100000));
            JSONObject js = new JSONObject(data);
            String SQL = "insert into authCode(uid,code) values(?,?)";
            PreparedStatement pst = con.prepareStatement(SQL);
            pst.setInt(1, js.getInt("userid"));
            pst.setInt(2, autCode);
            pst.execute();
            rtnmap.put("authcode", autCode);
            rtnmap.put("userid", js.getInt("userid"));
        }catch(Exception e){
            e.printStackTrace();
        }
        return rtnmap;
    }
    
    public HashMap mailsend(HashMap map){
        HashMap rtnmap = new HashMap();
        try{
            Properties prop = new Properties();
            prop.put("mail.smtp.auth", "true");
            prop.put("mail.smtp.starttls.enable","true");
            prop.put("mail.smtp.host", "smtp.gmail.com");
            prop.put("mail.smtp.port","587");
            String username = "kumarannathan871999@gmail.com";
            String password = "xwxlywcstlppuebx";
            
            Session session = Session.getDefaultInstance(prop, new Authenticator(){
                @Override
                protected PasswordAuthentication getPasswordAuthentication(){
                    return new PasswordAuthentication(username,password);
                }
            });
            
            Message message = prepareMailMessage(session,username,map);
            Transport.send(message);
            rtnmap.put("authcode", map.get("authcode").toString());
            rtnmap.put("userid", map.get("userid").toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rtnmap;
    }

    public Message prepareMailMessage(Session session, String fromid, HashMap map) {
        Message message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(fromid));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(map.get("emailid").toString()));
            message.setSubject("Authenticator Auth Code");
            String link = "http://localhost:3000/authcodeidentify?uid="+map.get("userid").toString();
            String body = "Please click the below link to get: <br/>"
                    + "<a href='" + link + "' target='_blank'>Link</a>";

            message.setContent("This is authCode " + body, "text/html");

            message.setContent("This is authCode " + body, "text/html");

            message.setContent("This is authCode " + body, "text/html");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }
    
    public HashMap getauth(String data) throws SQLException{
        Connection con=null;
        HashMap rtnmap = new HashMap();
        try{
            con = getConnection();
            JSONObject js = new JSONObject(data);
            String SQL = "select code from authCode where uid=?";
            PreparedStatement pst = con.prepareStatement(SQL);
            pst.setInt(1, Integer.parseInt(js.getString("userid")));
            ResultSet rs = pst.executeQuery();
            if(rs.next()){
                rtnmap.put("authcode", rs.getString("code"));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return rtnmap;
    }

}

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
import java.util.Map;
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
    
    public boolean loginCheck(String loginData) throws SQLException{
        Connection con = null;
        boolean rtnflag = true;
        try{
            System.out.println("LOGIN DATA >>>"+loginData);
            con = getConnection();
            PreparedStatement pst = null;
            JSONObject js = new JSONObject(loginData);
            String loginname = js.getString("username");
            String SQL = "select uid,password from userdetail where username=?";
            pst  = con.prepareStatement(SQL);
            pst.setString(1, loginname);
            ResultSet rs = pst.executeQuery();
            System.out.println("RETURN ROW : "+rs.getRow());
            if(rs.next()){
                System.out.println("THIS IS UI P "+rs.getInt("uid"));
                String hashedPassword = rs.getString("password");
                rtnflag = loginDecrypt(hashedPassword,js.get("password").toString());
            }else{
                rtnflag=false;
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(con!=null){
                con.close();
            }
        }
        System.out.println("LOGIN RETURN FLAG >>"+rtnflag);
        return rtnflag;
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

}

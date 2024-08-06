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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import org.json.JSONArray;
import org.json.JSONObject;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.Locale;

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

    public boolean newUserCheck(String data) throws SQLException {
        boolean rtnflag = true;
        Connection con = null;
        try {
            System.out.println("@@INSIDE NEW USER CHECK " + data);
            JSONObject js = new JSONObject(data);
            JSONObject savejs = js.getJSONObject("userdetails");
            System.out.println("thIS IS USEr DETAILS>>>" + savejs);
            System.out.println("thIS IS USEr DETAILS>>>" + savejs.getString("username"));
            con = getConnection();
            PreparedStatement pst = null;
            String SQL = "select uid from userdetail where username=?";
            pst = con.prepareCall(SQL);
            pst.setString(1, savejs.getString("username"));
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                System.out.println("@@INSIDE userdetail check " + rs.getString("uid"));
                rtnflag = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.close();
            }
        }
        return rtnflag;
    }

    public boolean newUserSave(String newData) throws SQLException {
        boolean rtnflag = true;
        Connection con = null;
        try {
            System.out.println("@@INSIDE NEW USER CHECK " + newData);
            Date currentdate = new Date();
            JSONObject js = new JSONObject(newData);
            System.out.println("THIS IS USERDETAISL >>" + js);
            JSONObject bodyjs = js.getJSONObject("userdetails");
            JSONObject reporttojs = bodyjs.getJSONObject("reportto");
            String hashedPassword = passwordEncrypt(bodyjs.getString("password"));
            JSONObject role = bodyjs.optJSONObject("role");
            con = getConnection();
            String SQL = "insert into userdetail(username,password,emailid,empid,firstname,lastname,role,reportto,isotpenabled,reporttoempid,joiningdate) values(?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement pst = null;
            pst = con.prepareStatement(SQL);
            pst.setString(1, bodyjs.getString("username"));
            pst.setString(2, hashedPassword);
            pst.setString(3, bodyjs.getString("emailid"));
            pst.setString(4, bodyjs.getString("empid"));
            pst.setString(5, bodyjs.getString("firstname"));
            pst.setString(6, bodyjs.getString("lastname"));
            pst.setString(7, role.getString("role"));
            pst.setString(8, reporttojs.getString("name"));
            pst.setBoolean(9, bodyjs.getBoolean("isotp"));
            pst.setString(10, bodyjs.getString("reporttoempid"));
            pst.setTimestamp(11, new Timestamp(currentdate.getTime()));
            pst.execute();
        } catch (Exception e) {
            rtnflag = false;
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.close();
            }
        }
        return rtnflag;
    }

    public HashMap loginCheck(String loginData) throws SQLException {
        Connection con = null;
        boolean rtnflag = true;
        HashMap rtnmap = new HashMap();
        try {
            System.out.println("LOGIN DATA >>>" + loginData);
            con = getConnection();
            PreparedStatement pst = null;
            JSONObject js = new JSONObject(loginData);
            String loginname = js.getString("username");
            System.out.println("THIS IS LOGINNAME " + loginname);
            String SQL = "select uid,password,emailid,role,empid,isotpenabled from userdetail where username=?";
            pst = con.prepareStatement(SQL);
            pst.setString(1, loginname);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                System.out.println("THIS IS HASHAED " + hashedPassword);
                rtnflag = loginDecrypt(hashedPassword, js.get("password").toString());
                rtnmap.put("status", rtnflag);
                if (rtnflag) {
                    String mailid = rs.getString("emailid");
                    rtnmap.put("emailid", mailid);
                    rtnmap.put("role", rs.getString("role"));
                    int userid = rs.getInt("uid");
                    rtnmap.put("userid", userid);
                    rtnmap.put("username", loginname);
                    rtnmap.put("empcode", rs.getString("empid"));
                    rtnmap.put("isotp", rs.getBoolean("isotpenabled"));
                }
            } else {
                rtnmap.put("status", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.close();
            }
        }
        System.out.println("LOGIN RETURN FLAG >>" + rtnmap);
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

    public boolean loginDecrypt(String hashedPassword, String password) {
        boolean rtnflag = false;
        try {
            Argon2 argon2 = Argon2Factory.create();
            rtnflag = argon2.verify(hashedPassword, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rtnflag;
    }

    public HashMap authcodesave(String data) throws SQLException {
        System.out.println("INSIDE AUTH COde Creation");
        Connection con = null;
        HashMap rtnmap = new HashMap();
        try {
            con = getConnection();
            Random rand = new Random();
            int autCode = rand.nextInt((999998 - 100000));
            JSONObject js = new JSONObject(data);
            deleteAuthCode(js.getInt("userid"), con);
            String SQL = "insert into authCode(uid,code) values(?,?)";
            PreparedStatement pst = con.prepareStatement(SQL);
            pst.setInt(1, js.getInt("userid"));
            pst.setInt(2, autCode);
            pst.execute();
            rtnmap.put("authcode", autCode);
            rtnmap.put("userid", js.getInt("userid"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.close();
            }
        }
        return rtnmap;
    }

    public HashMap mailsend(HashMap map) {
        HashMap rtnmap = new HashMap();
        try {
            Properties prop = new Properties();
            prop.put("mail.smtp.auth", "true");
            prop.put("mail.smtp.starttls.enable", "true");
            prop.put("mail.smtp.host", "smtp.gmail.com");
            prop.put("mail.smtp.port", "587");
            String username = "kumarannathan871999@gmail.com";
            String password = "xwxlywcstlppuebx";

            Session session = Session.getDefaultInstance(prop, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            Message message = prepareMailMessage(session, username, map);
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
            String body = "Please find the authenticator code <br/> " + map.get("authcode").toString();

            message.setContent("This is authCode " + body, "text/html");

            message.setContent("This is authCode " + body, "text/html");

            message.setContent("This is authCode " + body, "text/html");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    public HashMap getauth(String data) throws SQLException {
        Connection con = null;
        HashMap rtnmap = new HashMap();
        try {
            con = getConnection();
            JSONObject js = new JSONObject(data);
            String SQL = "select code from authCode where uid=?";
            PreparedStatement pst = con.prepareStatement(SQL);
            pst.setInt(1, Integer.parseInt(js.getString("userid")));
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                rtnmap.put("authcode", rs.getString("code"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.close();
            }
        }
        return rtnmap;
    }

    public HashMap authCodeCheck(String data) throws SQLException {
        Connection con = null;
        HashMap rtnmap = new HashMap();
        try {
            con = getConnection();
            JSONObject js = new JSONObject(data);
            System.out.println("THIS IS JS " + js);
            String SQL = "select code from authCode where uid=? and code=?";
            PreparedStatement pst = con.prepareStatement(SQL);
            pst.setInt(1, js.getInt("userid"));
            pst.setString(2, js.getString(("authcode")));
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                rtnmap.put("status", "success");
                deleteAuthCode(js.getInt(("userid")), con);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.close();
            }
        }
        return rtnmap;
    }

    public void deleteAuthCode(int userid, Connection con1) throws SQLException {
        Connection con = con1;
        try {
            String sql = "delete from authcode where uid=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, userid);
            pst.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean authCodeAvailable(String data) throws SQLException {
        boolean rtnflag = true;
        Connection con = null;
        try {
            con = getConnection();
            JSONObject js = new JSONObject(data);
            String sql = "select code from authcode where uid=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, js.getInt("userid"));
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                rtnflag = true;
            } else {
                rtnflag = false;
            }
        } catch (Exception e) {

        } finally {
            if (con != null) {
                con.close();
            }
        }
        return rtnflag;
    }

    public HashMap getempcode() throws SQLException {
        HashMap rtnmap = new HashMap();
        Connection con = null;
        try {
            con = getConnection();
            String SQL = "select count(1) from userdetail";
            PreparedStatement pst = con.prepareStatement(SQL);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                rtnmap.put("empcode", rs.getInt("count") + 1);
            } else {
                rtnmap.put("empcode", 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.close();
            }
        }
        return rtnmap;
    }

    public JSONArray getUsers(String data) throws SQLException {
        JSONArray rtnlst = new JSONArray();
        Connection con = null;
        try {
            con = getConnection();
            String SQL = "Select username,empid,role from userdetail";
            PreparedStatement pst = con.prepareStatement(SQL);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                JSONObject js = new JSONObject();
                js.put("name", rs.getString("username"));
                js.put("empid", rs.getString("empid"));
                js.put("role", rs.getString("role"));
                rtnlst.put(js);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.close();
            }
        }
        System.out.println("THIS IS USER LIST " + rtnlst);
        return rtnlst;
    }

    public JSONArray getusers(String data) throws SQLException {
        JSONArray rtnarr = new JSONArray();
        Connection con = null;
        try {
            System.out.println("@@INSIDE GET USERS" + data);
            con = getConnection();
            JSONObject js = new JSONObject(data);
            String usersql = "select username,empid from userdetail where reportto=?";
            PreparedStatement pst = con.prepareCall(usersql);
            pst.setString(1, js.getString("user"));
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                JSONObject jsobj = new JSONObject();
                jsobj.put("value", rs.getString("username"));
                jsobj.put("empid", rs.getString("empid"));
                rtnarr.put(jsobj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.close();
            }
        }
        return rtnarr;
    }

    public HashMap savetask(String data) throws SQLException {
        HashMap rtnmap = new HashMap();
        Connection con = null;
        try {
            System.out.println("@@INSIDE GET USERS" + data);
            con = getConnection();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            JSONObject taskdata1 = new JSONObject(data);
            JSONObject taskdata = taskdata1.optJSONObject("taskdata");
            System.out.println("THIS IS TASK DATA" + taskdata);
            JSONArray assigneearr = taskdata.getJSONArray("assignee");
            System.out.println("THIS IS JSON ARRAY >>" + assigneearr);
            JSONObject js = new JSONObject();
            js.put("username", taskdata.getString("reportto"));
            HashMap reporttomap = getReportToUserDetails(js.toString());
            int maxtaskcount = getmaxtaskcountdetails();
            System.out.println("MAX COUNT >>" + maxtaskcount);
            ArrayList<Integer> tasklist = new ArrayList();
            String sql = "insert into taskdetails(taskid,empcode,reporttocmpcode,subject,deadline,createddate,subtopiccount,iscompleted,summary)values(?,?,?,?,to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS'),CURRENT_TIMESTAMP,?,?,?)";
            PreparedStatement pst = con.prepareStatement(sql);
            for (int i = 0; i < assigneearr.length(); i++) {
                JSONObject assignee = assigneearr.getJSONObject(i);
                pst.setInt(1, maxtaskcount);
                pst.setString(2, assignee.getString("empid"));
                pst.setString(3, reporttomap.get("userid").toString());
                pst.setString(4, taskdata.getString("subject"));

                // Convert deadline string to Timestamp before setting the parameter
                String deadlineStr = taskdata.getString("deadline");
                Date d1 = sdf.parse(deadlineStr);
                Timestamp deadline = new Timestamp(d1.getTime()); // Parse deadline string using Timestamp
                pst.setTimestamp(5, deadline); // Set parameter 5 with the converted Timestamp

                pst.setInt(6, taskdata.getInt("subtopicount"));
                tasklist.add(maxtaskcount);
                pst.setBoolean(7, false);
                pst.setString(8, taskdata.getString("summary"));
                pst.addBatch();
                maxtaskcount++;
            }
            pst.executeBatch();

            if (taskdata.getInt("subtopicount") > 0) {
                System.out.println("INSIDE SAVE SUB TASK DATA");
                saveSubTaskDetails(taskdata.getJSONArray("subtask"), tasklist);
            }
            rtnmap.put("Status", "success");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.close();
            }
        }
        return rtnmap;
    }

    public int getmaxtaskcountdetails() throws SQLException {
        int maxcount = 0;
        Connection con = null;
        try {
            con = getConnection();
            String sql = "select count(1) from taskdetails";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                int temp = rs.getInt("count");
                temp = temp + 1;
                maxcount = temp;
            } else {
                maxcount = 1;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.close();
            }
        }
        return maxcount;
    }

    public HashMap getReportToUserDetails(String username) throws SQLException {
        Connection con = null;
        boolean rtnflag = true;
        HashMap rtnmap = new HashMap();
        try {
            System.out.println("LOGIN DATA >>>" + username);
            con = getConnection();
            PreparedStatement pst = null;
            JSONObject js = new JSONObject(username);
            String loginname = js.getString("username");
            System.out.println("THIS IS LOGINNAME " + loginname);
            String SQL = "select uid,password,emailid,role from userdetail where username=?";
            pst = con.prepareStatement(SQL);
            pst.setString(1, loginname);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                String mailid = rs.getString("emailid");
                rtnmap.put("emailid", mailid);
                rtnmap.put("role", rs.getString("role"));
                int userid = rs.getInt("uid");
                rtnmap.put("userid", userid);
                rtnmap.put("username", loginname);
            } else {
                rtnmap.put("status", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.close();
            }
        }
        System.out.println("LOGIN RETURN FLAG >>" + rtnmap);
        return rtnmap;
    }

    public JSONArray getroles() throws SQLException {
        JSONArray rolearr = new JSONArray();
        Connection con = null;
        try {
            con = getConnection();
            String SQL = "select id,rolename from roledetails";
            PreparedStatement pst = con.prepareStatement(SQL);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                JSONObject js = new JSONObject();
                js.put("id", rs.getInt("id"));
                js.put("role", rs.getString("rolename"));
                rolearr.put(js);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.close();
            }
        }
        return rolearr;
    }

    public void saveroles(String data) throws SQLException {
        Connection con = null;
        try {
            JSONObject js = new JSONObject(data);
            JSONArray roledetails = js.optJSONArray("roledetails");
            System.out.println("THIS IS ROLE DETAILS >>" + roledetails);
            con = getConnection();
            String SQL = "insert into roledetails(rolename)values(?)";
            PreparedStatement pst = con.prepareStatement(SQL);
            for (int i = 0; i < roledetails.length(); i++) {
                JSONObject role = new JSONObject(roledetails.get(i).toString());
                System.out.println("THIS IS ROLE >>>" + role);
                if (role.length() == 1) {
                    pst.setString(1, role.getString("role"));
                    pst.addBatch();
                }
            }
            pst.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.close();
            }
        }
    }

    public JSONArray gettasks(String empcode) throws SQLException {
        JSONArray taskarr = new JSONArray();
        Connection con = null;
        try {
            System.out.println("THIS IS EMP CODE>>>" + empcode);
            Date currentdate = new Date();
            con = getConnection();
            String SQL = "select taskid,Subject,subtopiccount,deadline from taskdetails where empcode=? and iscompleted=false and extendrequired=false and deadline>?";
            PreparedStatement pst = con.prepareStatement(SQL);
            pst.setString(1, empcode);
            pst.setTimestamp(2, new Timestamp(currentdate.getTime()));
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                JSONObject js = new JSONObject();
                js.put("taskid", rs.getInt("taskid"));
                js.put("Subject", rs.getString("subject"));
                js.put("Subtopiccount", rs.getInt("subtopiccount"));
                if (rs.getInt("subtopiccount") > 0) {
                    js.put("subtasks", getsubtaskdetails(rs.getInt("taskid")));
                } else {
                    js.put("subtasks", new JSONArray());
                }
                js.put("deadline", rs.getString("deadline"));
                taskarr.put(js);

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.close();
            }
        }
        return taskarr;
    }

    public JSONArray getsubtaskdetails(int taskid) throws SQLException {
        Connection con = null;
        JSONArray subtaskarr = new JSONArray();
        try {
            con = getConnection();
            String sql = "select subtaskid,subject,deadline,iscompleted from subtaskdetails where taskid=? and extendrequired=false";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, taskid);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                JSONObject js = new JSONObject();
                js.put("subtaskid", rs.getInt("subtaskid"));
                js.put("subject", rs.getString("subject"));
                js.put("deadline", rs.getString("deadline"));
                js.put("iscompleted", rs.getBoolean("iscompleted"));
                subtaskarr.put(js);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.close();
            }
        }
        return subtaskarr;
    }

    public void saveSubTaskDetails(JSONArray subtaskarr, ArrayList<Integer> taskid) throws SQLException {
        Connection con = null;
        try {
            System.out.println("INSIDE SUBTASK DETAILS" + subtaskarr);
            con = getConnection();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            String SQL = "insert into subtaskdetails(subject,deadline,createddate,taskid,iscompleted) values(?,to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS'),CURRENT_TIMESTAMP,?,?)";
            PreparedStatement pst = con.prepareStatement(SQL);
            for (int j = 0; j < taskid.size(); j++) {
                for (int i = 0; i < subtaskarr.length(); i++) {
                    JSONObject subtaskobj = subtaskarr.getJSONObject(i);
                    System.out.println("THIS IS INDIVIDUAL SUB TASK DETAILS >>" + subtaskobj);
                    pst.setString(1, subtaskobj.getString("subtaskheader"));
                    String deadlineStr = subtaskobj.getString("subtaskDeadline");
                    Date d1 = sdf.parse(deadlineStr);
                    Timestamp deadline = new Timestamp(d1.getTime()); // Parse deadline string using Timestamp
                    pst.setTimestamp(2, deadline);
                    pst.setInt(3, taskid.get(j));
                    pst.setBoolean(4, false);
                    pst.addBatch();
                }
            }
            pst.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.close();
            }
        }
    }

    public JSONArray getpendingtasks(String empcode) throws SQLException {
        JSONArray pendingtaskarr = new JSONArray();
        Connection con = null;
        try {
            con = getConnection();
            Date currentDate = new Date();
            ArrayList<Integer> taskidlist = new ArrayList();
            String SQL = "select s.taskid from subtaskdetails s inner join taskdetails t on s.taskid=t.taskid where s.deadline<? and s.extendrequired=false and t.empcode=?";
            PreparedStatement pst = con.prepareStatement(SQL);
            System.out.println("THIS IS DEADLINE " + new Timestamp(currentDate.getTime()));
            pst.setTimestamp(1, new Timestamp(currentDate.getTime()));
            pst.setString(2, empcode);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                if (!taskidlist.contains(rs.getInt("taskid"))) {
                    taskidlist.add(rs.getInt("taskid"));
                }
            }

            SQL = "select taskid from taskdetails where subtopiccount=0 and deadline<? and extendrequired=false and empcode=?";
            pst = null;
            pst = con.prepareStatement(SQL);
            pst.setTimestamp(1, new Timestamp(currentDate.getTime()));
            pst.setString(2, empcode);
            rs = pst.executeQuery();
            while (rs.next()) {
                if (!taskidlist.contains(rs.getInt("taskid"))) {
                    taskidlist.add(rs.getInt("taskid"));
                }
            }
            System.out.println("THS IS TASKID LISTT>>" + taskidlist);
            SQL = "select taskid,subject,subtopiccount,deadline from taskdetails where taskid=?";
            for (int i = 0; i < taskidlist.size(); i++) {
                int taskid = taskidlist.get(i);
                pst = null;
                pst = con.prepareStatement(SQL);
                pst.setInt(1, taskid);
                rs = null;
                rs = pst.executeQuery();
                if (rs.next()) {
                    JSONObject js = new JSONObject();
                    js.put("TaskName", rs.getInt("taskid"));
                    js.put("subject", rs.getString("subject"));
                    js.put("Taskid", rs.getInt("taskid"));
                    js.put("Deadline", rs.getString("deadline"));
                    js.put("SubTaskCount", rs.getInt("subtopiccount"));
                    if (rs.getInt("subtopiccount") > 0) {
                        js.put("Subtaskdetails", getsubtaskdetails(taskid));
                    }
                    pendingtaskarr.put(js);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.close();
            }
        }
        return pendingtaskarr;
    }

    public JSONArray gettasknumbers(String empcode) throws SQLException {
        JSONArray tasknumber = new JSONArray();
        Connection con = null;
        try {
            con = getConnection();
            Date currentdate = new Date();
            String SQL = "select count(1) from taskdetails where iscompleted=false and deadline>? and extendrequired=false and empcode=?";
            PreparedStatement pst = con.prepareStatement(SQL);
            System.out.println("THIS IS CURRENT DATE " + new Timestamp(currentdate.getTime()));
            pst.setTimestamp(1, new Timestamp(currentdate.getTime()));
            pst.setString(2, empcode);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                tasknumber.put(rs.getInt("count"));
            }

            JSONArray pendingtaskarr = getpendingtasks(empcode);
            tasknumber.put(pendingtaskarr.length());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.close();
            }
        }
        return tasknumber;
    }

    public boolean setCompleteTask(JSONObject js) throws SQLException {
        Connection con = null;
        boolean rtnflag = false;
        try {
            con = getConnection();
            String updatetask = "update taskdetails set iscompleted=true,completeddate=CURRENT_TIMESTAMP where taskid=? and empcode=?";
            String empcode = js.getString("empcode");
            int taskid = js.getInt("taskid");
            PreparedStatement pst = con.prepareStatement(updatetask);
            pst.setInt(1, taskid);
            pst.setString(2, empcode);
            pst.execute();
            rtnflag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.close();
            }
        }
        return rtnflag;
    }

    public JSONArray getCompletedTask(JSONObject js) throws SQLException {
        Connection con = null;
        JSONArray taskarr = new JSONArray();
        try {
            con = getConnection();
            String SQL = "select taskid,subject,deadline,completeddate,subtopiccount,rating from taskdetails where empcode=? and iscompleted=true";
            PreparedStatement pst = con.prepareStatement(SQL);
            pst.setString(1, js.getString("empcode"));
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                JSONObject taskjs = new JSONObject();
                taskjs.put("taskid", rs.getInt("taskid"));
                taskjs.put("subject", rs.getString("subject"));
                taskjs.put("deadline", rs.getString("deadline"));
                taskjs.put("completeddate", rs.getString("completeddate"));
                taskjs.put("subtaskcount", rs.getInt("subtopiccount"));
                taskjs.put("rating", rs.getInt("rating"));
                taskarr.put(taskjs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.close();
            }
        }
        return taskarr;
    }

    public boolean saveExtendTaskDetail(JSONObject js) throws SQLException {
        Connection con = null;
        boolean rtnflag = true;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            System.out.println("THIS IS EXTEND TASK DETAILS>>" + js);
            JSONObject extendtaskdetails = js.optJSONObject("taskdetails");
            con = getConnection();
            String sql = "insert into extendtaskdetails(taskid, subtaskid, subtopiccount, reason, empcode, createddate, extenddate) values (?, ?, ?, ?, ?, CURRENT_Date,?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, extendtaskdetails.getInt("taskid"));
            if (extendtaskdetails.getInt("Subtopiccount") == 0) {
                pst.setInt(2, 0);
                pst.setInt(3, 0);
            } else {
                pst.setInt(2, extendtaskdetails.getInt("subtaskid"));
                pst.setInt(3, extendtaskdetails.getInt("Subtopiccount"));
            }
            pst.setString(4, extendtaskdetails.getString("extendreason"));
            pst.setString(5, extendtaskdetails.getString("empcode"));
            Date d1 = sdf.parse(extendtaskdetails.getString("extenddate"));
            Timestamp extendate = new Timestamp(d1.getTime());
            pst.setTimestamp(6, extendate);
            pst.execute();

            if (extendtaskdetails.getInt("Subtopiccount") == 0) {
                extendTaskUpdate(extendtaskdetails.getInt("taskid"));
            } else {
                extendSubTaskUpdate(extendtaskdetails.getInt("taskid"), extendtaskdetails.getInt("subtaskid"));
            }

        } catch (Exception e) {
            System.out.println("INSIDE ERROR OF EXTEND TASK");
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.close();
            }
        }
        return rtnflag;
    }

    public boolean extendTaskUpdate(int taskid) throws SQLException {
        boolean rtnflag = true;
        Connection con = null;
        try {
            con = getConnection();
            String update = "update taskdetails set extendrequired=true where taskid=?";
            PreparedStatement pst = con.prepareStatement(update);
            pst.setInt(1, taskid);
            pst.execute();
        } catch (Exception e) {
            e.printStackTrace();
            rtnflag = false;
        } finally {
            if (con != null) {
                con.close();
            }
        }
        return rtnflag;
    }

    public boolean extendSubTaskUpdate(int taskid, int subtaskid) throws SQLException {
        boolean rtnflag = true;
        Connection con = null;
        try {
            System.out.println("KK" + taskid + "   " + subtaskid);
            con = getConnection();
            String update = "update subtaskdetails set extendrequired=true where taskid=? and subtaskid=?";
            PreparedStatement pst = con.prepareStatement(update);
            pst.setInt(1, taskid);
            pst.setInt(2, subtaskid);
            pst.execute();
        } catch (Exception e) {
            e.printStackTrace();
            rtnflag = false;
        } finally {
            if (con != null) {
                con.close();
            }
        }
        return rtnflag;
    }

    public HashMap getBarChartDetails(String empcode) throws SQLException {
        HashMap map = new HashMap();
        JSONArray rtnarr = new JSONArray();
        Connection con = null;
        try {
            con = getConnection();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            HashMap<String, Date> userlist = new HashMap();
            String users = "select empid,joiningdate from userdetail where reporttoempid=?";
            PreparedStatement pst = con.prepareStatement(users);
            pst.setString(1, empcode);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                userlist.put(rs.getString("empid"), sdf.parse(rs.getString("joiningdate")));
            }

            Date mindate = new Date();
            for (Map.Entry<String, Date> set : userlist.entrySet()) {
                JSONObject js = new JSONObject();
                js.put("value", set.getKey());
                js.put("label", set.getKey());
                js.put("children", getYearDetails(set.getValue()));
                rtnarr.put(js);
                if (set.getValue().before(mindate)) {
                    mindate = set.getValue();
                }
            }
            map.put("weekcount", getWeekCount(mindate));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.close();
            }
        }
        map.put("arr", rtnarr);
        return map;
    }

    public JSONArray getYearDetails(Date d1) {
        JSONArray rtnarr = new JSONArray();
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(d1);
            int startyear = cal.get(Calendar.YEAR);
            int startmonth = cal.get(Calendar.MONTH);
            Date d2 = new Date();
            cal.setTime(d2);
            int currentyear = cal.get(Calendar.YEAR);
            int currentmonth = cal.get(Calendar.MONTH);
            for (int i = startyear; i <= currentyear; i++) {
                int k = 0;
                if (i == startyear) {
                    k = startmonth;
                }
                JSONObject js = new JSONObject();
                js.put("value", i);
                js.put("label", i);
                js.put("children", getMonthDetails(k));
                rtnarr.put(js);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return rtnarr;
    }

    public JSONArray getMonthDetails(int mon) {
        JSONArray rtnarr = new JSONArray();
        try {
            String months[] = new String[]{"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
            for (int i = mon; mon <= months.length; i++) {
                JSONObject js = new JSONObject();
                js.put("value", months[i]);
                js.put("label", months[i]);
                rtnarr.put(js);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rtnarr;
    }

    public JSONObject getWeekCount(Date d1) {
        JSONObject js = new JSONObject();
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(d1);
            int startyear = cal.get(Calendar.YEAR);
            int startmonth = cal.get(Calendar.MONTH);
            Date d2 = new Date();
            cal.setTime(d2);
            int currentyear = cal.get(Calendar.YEAR);
            int currentmonth = cal.get(Calendar.MONTH);
            for (int i = startyear; i <= currentyear; i++) {
                int k = 0;
                if (i == startyear) {
                    k = startmonth;
                }
                for (int j = k; k < 12; k++) {
                    js.put(i + "-" + j, getWeekCountCalculation(i, j));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return js;
    }

    public int getWeekCountCalculation(int year, int month) {
        int rtn = 0;
        try {
            YearMonth yearMonth = YearMonth.of(year, month);

            // Get the first and last day of the month
            LocalDate firstDayOfMonth = yearMonth.atDay(1);
            LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();

            // Get the WeekFields instance for the default locale
            WeekFields weekFields = WeekFields.of(Locale.getDefault());

            // Get the week number of the first and last day of the month
            int firstWeek = firstDayOfMonth.get(weekFields.weekOfMonth());
            int lastWeek = lastDayOfMonth.get(weekFields.weekOfMonth());

            // Calculate the number of weeks in the month
            rtn = lastWeek - firstWeek + 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rtn;
    }
}

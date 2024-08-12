/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.krypto.connection;

import com.krypto.Entities.*;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.json.JSONArray;
import org.json.JSONObject;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;

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
        Session session = new HibernateConnection().getSessionfactory().openSession();
        Transaction tx = session.beginTransaction();
        try {
            JSONObject js = new JSONObject(data);
            JSONObject savejs = js.getJSONObject("userdetails");
            UserDetail user = session.get(UserDetail.class,savejs.getString("username"));
            if(user!=null && user.getUsername().length()>0){
                rtnflag = false;
            }
            tx.commit();
//            System.out.println("@@INSIDE NEW USER CHECK " + data);
//
//            System.out.println("thIS IS USEr DETAILS>>>" + savejs);
//            System.out.println("thIS IS USEr DETAILS>>>" + savejs.getString("username"));
//            con = getConnection();
//            PreparedStatement pst = null;
//            String SQL = "select uid from userdetail where username=?";
//            pst = con.prepareCall(SQL);
//            pst.setString(1, savejs.getString("username"));
//            ResultSet rs = pst.executeQuery();
//            if (rs.next()) {
//                System.out.println("@@INSIDE userdetail check " + rs.getString("uid"));
//                rtnflag = false;
//            }
        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
        } finally {
            session.close();
        }
        return rtnflag;
    }

    public boolean newUserSave(String newData) throws SQLException {
        boolean rtnflag = true;
        Session session = new HibernateConnection().getSessionfactory().openSession();
        Transaction tx = session.beginTransaction();
        try {
            JSONObject js = new JSONObject(newData);
            JSONObject bodyjs = js.getJSONObject("userdetails");
            JSONObject reporttojs = bodyjs.getJSONObject("reportto");
            String hashedPassword = passwordEncrypt(bodyjs.getString("password"));
            JSONObject role = bodyjs.optJSONObject("role");
            Date currentdate = new Date();

            UserDetail userdetail = new UserDetail();
            userdetail.setUsername(bodyjs.getString("username"));
            userdetail.setPassword(hashedPassword);
            userdetail.setEmailid(bodyjs.getString("emailid"));
            userdetail.setEmpid(bodyjs.getString("empid"));
            userdetail.setFirstname(bodyjs.getString("firstname"));
            userdetail.setLastname(bodyjs.getString("lastname"));
            userdetail.setRole(role.getString("role"));
            userdetail.setReportto(reporttojs.getString("name"));
            userdetail.setIsotpenabled(bodyjs.getBoolean("isotp"));
            userdetail.setReporttoempid(bodyjs.getString("reporttoempid"));
            userdetail.setJoiningdate(new Timestamp(currentdate.getTime()));
            session.save(userdetail);


            System.out.println("@@INSIDE NEW USER CHECK " + newData);
            tx.commit();
//
//            con = getConnection();
//            String SQL = "insert into userdetail(username,password,emailid,empid,firstname,lastname,role,reportto,isotpenabled,reporttoempid,joiningdate) values(?,?,?,?,?,?,?,?,?,?,?)";
//            PreparedStatement pst = null;
//            pst = con.prepareStatement(SQL);
//            pst.setString(1, bodyjs.getString("username"));
//            pst.setString(2, hashedPassword);
//            pst.setString(3, bodyjs.getString("emailid"));
//            pst.setString(4, bodyjs.getString("empid"));
//            pst.setString(5, bodyjs.getString("firstname"));
//            pst.setString(6, bodyjs.getString("lastname"));
//            pst.setString(7, role.getString("role"));
//            pst.setString(8, reporttojs.getString("name"));
//            pst.setBoolean(9, bodyjs.getBoolean("isotp"));
//            pst.setString(10, bodyjs.getString("reporttoempid"));
//            pst.setTimestamp(11, new Timestamp(currentdate.getTime()));
//            pst.execute();
        } catch (Exception e) {
            rtnflag = false;
            e.printStackTrace();
            tx.rollback();
        } finally {
            session.close();
        }
        return rtnflag;
    }


    public HashMap loginCheck(String loginData){
        Connection con = null;
        boolean rtnflag = true;
        HashMap rtnmap = new HashMap();
        Session session = null;
        Transaction tx= null;
        try {
            System.out.println("LOGIN DATA >>>" + loginData);
            con = getConnection();
            PreparedStatement pst = null;
            JSONObject js = new JSONObject(loginData);
            String loginname = js.getString("username");
            System.out.println("THIS IS LOGINNAME " + loginname);

            session = new HibernateConnection().getSessionfactory().openSession();
            tx = session.beginTransaction();
            Criteria criteria = session.createCriteria(UserDetail.class);
            criteria.add(Restrictions.eq("username",loginname));

            List loginuser = criteria.list();

            if(loginuser.size()>0){
                UserDetail user =(UserDetail) loginuser.get(0);
                rtnflag = loginDecrypt(user.getPassword(), js.get("password").toString());
                rtnmap.put("status", rtnflag);
                if (rtnflag) {
                    String mailid = user.getEmailid();
                    rtnmap.put("emailid", mailid);
                    rtnmap.put("role", user.getRole());
                    int userid = user.getUid();
                    rtnmap.put("userid", userid);
                    rtnmap.put("username", loginname);
                    rtnmap.put("empcode", user.getEmpid());
                    rtnmap.put("isotp", user.isIsotpenabled());
                }
            }else{
                rtnmap.put("status", false);
            }
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if(tx!=null){
                tx.rollback();
            }
        } finally {
            session.close();
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
        Session session = new HibernateConnection().getSessionfactory().openSession();
        Transaction tx = session.beginTransaction();
        try {
            JSONObject js = new JSONObject(data);
            Random rand = new Random();
            int autCode = rand.nextInt((999998 - 100000));
            AuthCode auth = new AuthCode(js.getInt("userid"),autCode);
            AuthCode deleteuser = new AuthCode();
            deleteuser.setUid(js.getInt("userid"));
            session.delete(deleteuser);
            session.save(auth);
            tx.commit();
            rtnmap.put("authcode", autCode);
            rtnmap.put("userid", js.getInt("userid"));
        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
        } finally {
            if (con != null) {
                con.close();
            }
            session.close();
        }
        return rtnmap;
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
        HashMap rtnmap = new HashMap();
        Session session = new HibernateConnection().getSessionfactory().openSession();
        Transaction tx = session.beginTransaction();
        try {
            JSONObject js = new JSONObject(data);
            System.out.println("THIS IS JS " + js);
            Criteria crt = session.createCriteria(AuthCode.class);
            crt.add(Restrictions.eq("uid",js.getInt("userid")));
            crt.add(Restrictions.eq("code",js.getString("authcode")));
            List authlist = crt.list();
            if(authlist!=null && authlist.size()>0){
                AuthCode auth =(AuthCode) authlist.get(0);
                rtnmap.put("status", "success");
                session.delete(auth);
            }
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
        } finally {
            session.close();
        }
        return rtnmap;
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
        Session session = new HibernateConnection().getSessionfactory().openSession();
        Transaction tx = session.beginTransaction();
        try {
            Criteria crt = session.createCriteria(UserDetail.class);
            crt.setProjection(Projections.rowCount());
            List user = crt.list();
            if(user!=null && user.size()>0){
                int count = Integer.parseInt(user.get(0).toString());
                rtnmap.put("empcode", count + 1);
            }else{
                rtnmap.put("empcode", 1);
            }
            tx.commit();
//            con = getConnection();
//            String SQL = "select count(1) from userdetail";
//            PreparedStatement pst = con.prepareStatement(SQL);
//            ResultSet rs = pst.executeQuery();
//            if (rs.next()) {
//
//            } else {
//
//            }
        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
        } finally {
            session.close();
        }
        return rtnmap;
    }

    public JSONArray getUsers(String data) throws SQLException {
        JSONArray rtnlst = new JSONArray();
        Connection con = null;
        Session session = new HibernateConnection().getSessionfactory().openSession();
        Transaction tx = session.beginTransaction();
        try {
            Criteria crt = session.createCriteria(UserDetail.class);
            crt.setProjection(Projections.projectionList().add(Projections.property("username"))
                    .add(Projections.property("empid"))
                    .add(Projections.property("role")));
            List<UserDetail> userlist = crt.list();
            for(int i=0;i<userlist.size();i++){
                UserDetail user = userlist.get(0);
                JSONObject js = new JSONObject();
                js.put("name", user.getUsername());
                js.put("empid", user.getEmpid());
                js.put("role", user.getRole());
                rtnlst.put(js);
            }
            tx.commit();
//            con = getConnection();
//            String SQL = "Select username,empid,role from userdetail";
//            PreparedStatement pst = con.prepareStatement(SQL);
//            ResultSet rs = pst.executeQuery();
//            while (rs.next()) {
//                JSONObject js = new JSONObject();
//                js.put("name", rs.getString("username"));
//                js.put("empid", rs.getString("empid"));
//                js.put("role", rs.getString("role"));
//                rtnlst.put(js);
//            }
        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
        } finally {
            session.close();
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
        Session session = new HibernateConnection().getSessionfactory().openSession();
        Transaction tx = session.beginTransaction();
        try {
            JSONObject taskdata1 = new JSONObject(data);
            JSONObject taskdata = taskdata1.optJSONObject("taskdata");
            System.out.println("THIS IS TASK DATA" + taskdata);
            JSONArray assigneearr = taskdata.getJSONArray("assignee");
            HashMap reporttomap = getReportToUserDetails(taskdata.getString("reportto"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

            for(int i=0;i<assigneearr.length();i++){
                JSONObject assignee = assigneearr.getJSONObject(i);
                int taskid = getmaxtaskcountdetails(assignee.getString("empid"));
                TaskDetails td = new TaskDetails();
                td.setTaskid(taskid);
                td.setEmpcode(assignee.getString("empid"));
                td.setReporttocmpcode(reporttomap.get("userid").toString());
                td.setSubject(taskdata.getString("subject"));
                String deadlineStr = taskdata.getString("deadline");
                Date d1 = sdf.parse(deadlineStr);
                Timestamp deadline = new Timestamp(d1.getTime());
                td.setDeadline(deadline);
                td.setCreateddate(new Timestamp(new Date().getTime()));
                td.setSubtopiccount(taskdata.getInt("subtopicount"));
                td.setIscompleted(false);
                td.setSummary(taskdata.getString("summary"));
                session.save(td);
                if(taskdata.getInt("subtopicount") > 0){
                    SubtaskDetails std = new SubtaskDetails();
                    std.setTaskid(taskid);
                    Criteria crtstd = session.createCriteria(SubtaskDetails.class);
                    crtstd.setProjection(Projections.rowCount());
                    List subtaskid = crtstd.list();
                    int maxsubtaskcount = 1;
                    if(subtaskid!=null && subtaskid.size()>0){
                        maxsubtaskcount= Integer.parseInt(subtaskid.get(0).toString())+1;
                    }
                    JSONArray subtasarr = taskdata.getJSONArray("subtask");
                    for(int j=0;j<subtasarr.length();j++) {
                        JSONObject subtaskobj = subtasarr.getJSONObject(j);
                        std.setSubtaskid(maxsubtaskcount);
                        std.setSubject(subtaskobj.getString("subtaskheader"));
                        String stdeadlineStr = subtaskobj.getString("subtaskDeadline");
                        Date subd = sdf.parse(stdeadlineStr);
                        Timestamp stdeadline = new Timestamp(subd.getTime()); // Parse deadline string using Timestamp
                        std.setDeadline(stdeadline);
                        std.setCreateddate(new Timestamp(new Date().getTime()));
                        std.setIscompleted(false);
                        std.setExtendrequired(false);
                        std.setTaskid(taskid);
                        session.save(std);
                        maxsubtaskcount++;
                    }
                }
            }
            rtnmap.put("Status", "success");

            tx.commit();
//            pst.setBoolean(4, false);
//
//
//            System.out.println("@@INSIDE GET USERS" + data);
//            con = getConnection();
//
//
//            System.out.println("THIS IS JSON ARRAY >>" + assigneearr);
//
//            System.out.println("MAX COUNT >>" + maxtaskcount);
//            ArrayList<Integer> tasklist = new ArrayList();
//            String sql = "insert into taskdetails(taskid,empcode,reporttocmpcode,subject,deadline,createddate,subtopiccount,iscompleted,summary)values(?,?,?,?,to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS'),CURRENT_TIMESTAMP,?,?,?)";
//            PreparedStatement pst = con.prepareStatement(sql);
//            for (int i = 0; i < assigneearr.length(); i++) {
//                JSONObject assignee = assigneearr.getJSONObject(i);
//
//                tasklist.add(maxtaskcount);
//                maxtaskcount++;
//            }
//            pst.executeBatch();
//
//            if (taskdata.getInt("subtopicount") > 0) {
//                System.out.println("INSIDE SAVE SUB TASK DATA");
//                saveSubTaskDetails(taskdata.getJSONArray("subtask"), tasklist);
//            }
//            rtnmap.put("Status", "success");
        } catch (Exception e) {
            e.printStackTrace();
            rtnmap.put("Status", "failed");
            tx.rollback();
        } finally {
            session.close();
        }
        return rtnmap;
    }

    public int getmaxtaskcountdetails(String empid){
        int maxcount = 0;
        Connection con = null;
        Session session = new HibernateConnection().getSessionfactory().openSession();
        Transaction tx = session.beginTransaction();
        try {
            Criteria crt = session.createCriteria(TaskDetails.class);
            crt.add(Restrictions.eq("empcode",empid));
            crt.setProjection(Projections.rowCount());
            List count = crt.list();
            if(count!=null && count.size()>0){
                maxcount = Integer.parseInt(count.get(0).toString()) +1;
            }else{
                maxcount = 1;
            }
            tx.commit();

//            con = getConnection();
//            String sql = "select count(1) from taskdetails";
//            PreparedStatement pst = con.prepareStatement(sql);
//            ResultSet rs = pst.executeQuery();
//            if (rs.next()) {
//                int temp = rs.getInt("count");
//                temp = temp + 1;
//                maxcount = temp;
//            } else {
//                maxcount = 1;
//            }

        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
        } finally {
            session.close();
        }
        return maxcount;
    }

    public HashMap getReportToUserDetails(String username) throws SQLException {
        Connection con = null;
        boolean rtnflag = true;
        HashMap rtnmap = new HashMap();
        Session session = null;
        Transaction tx = null;
        try {
            JSONObject js = new JSONObject(username);
            String loginname = js.getString("username");
            System.out.println("THIS IS LOGINNAME " + loginname);

            session = new HibernateConnection().getSessionfactory().openSession();
            tx = session.beginTransaction();
            UserDetail users = session.get(UserDetail.class,loginname);
            if(users!=null && users.getUsername().length()>0){
                String mailid = users.getEmailid();
                rtnmap.put("emailid", mailid);
                rtnmap.put("role", users.getRole());
                int userid = users.getUid();
                rtnmap.put("userid", userid);
                rtnmap.put("username", loginname);
            }else{
                rtnmap.put("status", false);
            }

            tx.commit();
//            System.out.println("LOGIN DATA >>>" + username);
//            con = getConnection();
//            PreparedStatement pst = null;
//
//            String SQL = "select uid,password,emailid,role from userdetail where username=?";
//            pst = con.prepareStatement(SQL);
//            pst.setString(1, loginname);
//            ResultSet rs = pst.executeQuery();
//            if (rs.next()) {
//                String mailid = rs.getString("emailid");
//                rtnmap.put("emailid", mailid);
//                rtnmap.put("role", rs.getString("role"));
//                int userid = rs.getInt("uid");
//                rtnmap.put("userid", userid);
//                rtnmap.put("username", loginname);
//            } else {
//                rtnmap.put("status", false);
//            }
        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
        } finally {
            session.close();
        }
        System.out.println("LOGIN RETURN FLAG >>" + rtnmap);
        return rtnmap;
    }

    public JSONArray getroles() throws SQLException {
        JSONArray rolearr = new JSONArray();
        Connection con = null;
        Session session = new HibernateConnection().getSessionfactory().openSession();
        Transaction tx = session.beginTransaction();
        try {
            Criteria crt = session.createCriteria(RoleDetails.class);
            List<RoleDetails> roles = crt.list();
            for(int i=0;i< roles.size();i++){
                RoleDetails role = roles.get(i);
                JSONObject js = new JSONObject();
                js.put("id", role.getId());
                js.put("role", role.getRolename());
                rolearr.put(js);
            }
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
        } finally {
            session.close();
        }
        return rolearr;
    }

    public void saveroles(String data) throws SQLException {
        Session session = null;
        Transaction tx = null;
        try {
            JSONObject js = new JSONObject(data);
            JSONArray roledetails = js.optJSONArray("roledetails");
            System.out.println("THIS IS ROLE DETAILS >>" + roledetails);
            session = new HibernateConnection().getSessionfactory().openSession();
            tx = session.beginTransaction();
            for(int i=0;i<roledetails.length(); i++){
                RoleDetails rd = new RoleDetails();
                JSONObject role = new JSONObject(roledetails.get(i).toString());
                rd.setRolename(role.getString("role"));
                session.save(rd);
            }

             tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
        } finally {
            session.close();
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
        Session session = new HibernateConnection().getSessionfactory().openSession();
        Transaction tx = session.beginTransaction();
        try {
            Date currentdate = new Date();
            Criteria crt = session.createCriteria(UserDetail.class);
            crt.add(Restrictions.eq("iscompleted",false));
            crt.add(Restrictions.eq("extendrequired",false));
            crt.add(Restrictions.eq("empcode",empcode));
            crt.add(Restrictions.gt("deadline",new Timestamp(currentdate.getTime())));
            crt.setProjection(Projections.rowCount());

            List countdetaila = crt.list();
            tasknumber.put(countdetaila.get(0));
//            con = getConnection();
//            String SQL = "select count(1) from taskdetails where iscompleted=false and deadline>? and extendrequired=false and empcode=?";
//            PreparedStatement pst = con.prepareStatement(SQL);
//            System.out.println("THIS IS CURRENT DATE " + new Timestamp(currentdate.getTime()));
//            pst.setTimestamp(1, new Timestamp(currentdate.getTime()));
//            pst.setString(2, empcode);
//            ResultSet rs = pst.executeQuery();
//            if (rs.next()) {
//                tasknumber.put(rs.getInt("count"));
//            }
            tx.commit();

            JSONArray pendingtaskarr = getpendingtasks(empcode);
            tasknumber.put(pendingtaskarr.length());
        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
        } finally {
            session.close();
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

package com.krypto.Entities;

import jakarta.annotation.*;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class UserDetail {

    @Id
    @Column(name="uid")
    private int uid;

    @Column(name="username")
    private String username;

    @Column(name="password")
    private String password;

    @Column(name="emaliid")
    private String emailid;

    @Column(name="empid")
    private String empid;

    @Column(name="firstname")
    private String firstname;

    @Column(name="lastname")
    private String lastname;

    @Column(name="role")
    private String role;

    @Column(name="reportto")
    private String reportto;

    @Column(name="reporttoempid")
    private  String reporttoempid;

    @Column(name="isotpenabled")
    private boolean isotpenabled;

    @Column(name="joiningdate")
    private Date joiningdate;

    public UserDetail() {}

    public UserDetail(int uid, String username, String password, String emailid, String empid, String firstname, String lastname, String role, String reportto, String reporttoempid, boolean isotpenabled, Date joiningdate) {
        this.uid = uid;
        this.username = username;
        this.password = password;
        this.emailid = emailid;
        this.empid = empid;
        this.firstname = firstname;
        this.lastname = lastname;
        this.role = role;
        this.reportto = reportto;
        this.reporttoempid = reporttoempid;
        this.isotpenabled = isotpenabled;
        this.joiningdate = joiningdate;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmailid() {
        return emailid;
    }

    public void setEmailid(String emailid) {
        this.emailid = emailid;
    }

    public String getEmpid() {
        return empid;
    }

    public void setEmpid(String empid) {
        this.empid = empid;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getReportto() {
        return reportto;
    }

    public void setReportto(String reportto) {
        this.reportto = reportto;
    }

    public String getReporttoempid() {
        return reporttoempid;
    }

    public void setReporttoempid(String reporttoempid) {
        this.reporttoempid = reporttoempid;
    }

    public boolean isIsotpenabled() {
        return isotpenabled;
    }

    public void setIsotpenabled(boolean isotpenabled) {
        this.isotpenabled = isotpenabled;
    }

    public Date getJoiningdate() {
        return joiningdate;
    }

    public void setJoiningdate(Date joiningdate) {
        this.joiningdate = joiningdate;
    }
}

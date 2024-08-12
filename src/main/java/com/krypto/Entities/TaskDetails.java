package com.krypto.Entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name="taskdetails")
public class TaskDetails {

    @Id
    @Column(name="taskid")
    private int taskid;

    @Column(name="empcode")
    private String empcode;

    @Column(name="reporttocmpcode")
    private String reporttocmpcode;

    @Column(name="subject")
    private String subject;

    @Column(name="deadline")
    private Date deadline;

    @Column(name="createddate")
    private Date createddate;

    @Column(name="subtopiccount")
    private int subtopiccount;

    @Column(name="iscompleted")
    private boolean iscompleted;

    @Column(name="rating")
    private int rating;

    @Column(name="summary")
    private String summary;

    @Column(name="extendrequired")
    private boolean extendrequired;

    public TaskDetails(){}

    public TaskDetails(int taskid, String empcode, String reporttocmpcode, String subject, Date deadline, Date createddate, int subtopiccount, boolean iscompleted, int rating, String summary, boolean extendrequired) {
        this.taskid = taskid;
        this.empcode = empcode;
        this.reporttocmpcode = reporttocmpcode;
        this.subject = subject;
        this.deadline = deadline;
        this.createddate = createddate;
        this.subtopiccount = subtopiccount;
        this.iscompleted = iscompleted;
        this.rating = rating;
        this.summary = summary;
        this.extendrequired = extendrequired;
    }

    public int getTaskid() {
        return taskid;
    }

    public void setTaskid(int taskid) {
        this.taskid = taskid;
    }

    public String getEmpcode() {
        return empcode;
    }

    public void setEmpcode(String empcode) {
        this.empcode = empcode;
    }

    public String getReporttocmpcode() {
        return reporttocmpcode;
    }

    public void setReporttocmpcode(String reporttocmpcode) {
        this.reporttocmpcode = reporttocmpcode;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public Date getCreateddate() {
        return createddate;
    }

    public void setCreateddate(Date createddate) {
        this.createddate = createddate;
    }

    public int getSubtopiccount() {
        return subtopiccount;
    }

    public void setSubtopiccount(int subtopiccount) {
        this.subtopiccount = subtopiccount;
    }

    public boolean isIscompleted() {
        return iscompleted;
    }

    public void setIscompleted(boolean iscompleted) {
        this.iscompleted = iscompleted;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public boolean isExtendrequired() {
        return extendrequired;
    }

    public void setExtendrequired(boolean extendrequired) {
        this.extendrequired = extendrequired;
    }
}

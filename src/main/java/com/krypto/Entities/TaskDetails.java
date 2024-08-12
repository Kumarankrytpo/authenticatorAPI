package com.krypto.Entities;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

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

    @Column(name="completeddate")
    private Date completeddate;

    @OneToMany(mappedBy = "taskid")
    private List<SubtaskDetails> subtaks;

    public TaskDetails(){}

    public TaskDetails(int taskid, String empcode, String reporttocmpcode, String subject, Date deadline, Date createddate, int subtopiccount, boolean iscompleted, int rating, String summary, boolean extendrequired, Date completeddate, List<SubtaskDetails> subtaks) {
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
        this.completeddate = completeddate;
        this.subtaks = subtaks;
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

    public Date getCompleteddate() {
        return completeddate;
    }

    public void setCompleteddate(Date completeddate) {
        this.completeddate = completeddate;
    }

    public List<SubtaskDetails> getSubtaks() {
        return subtaks;
    }

    public void setSubtaks(List<SubtaskDetails> subtaks) {
        this.subtaks = subtaks;
    }
}

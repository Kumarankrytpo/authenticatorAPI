package com.krypto.Entities;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="subtaskdetails")
public class SubtaskDetails {

    @Id
    @Column(name="subtaskid")
    private int subtaskid;

    @Column(name="subject")
    private String subject;

    @Column(name="deadline")
    private Date deadline;

    @Column(name="createddate")
    private Date createddate;

    @Column(name="taskid")
    @ManyToOne
    private  int taskid;

    @Column(name="iscompleted")
    private boolean iscompleted;

    @Column(name="completeddate")
    private Date completeddate;

    @Column(name="extendrequired")
    private boolean extendrequired;

    public SubtaskDetails(){}

    public SubtaskDetails(int subtaskid, String subject, Date deadline, Date createddate, int taskid, boolean iscompleted, Date completeddate, boolean extendrequired) {
        this.subtaskid = subtaskid;
        this.subject = subject;
        this.deadline = deadline;
        this.createddate = createddate;
        this.taskid = taskid;
        this.iscompleted = iscompleted;
        this.completeddate = completeddate;
        this.extendrequired = extendrequired;
    }

    public int getSubtaskid() {
        return subtaskid;
    }

    public void setSubtaskid(int subtaskid) {
        this.subtaskid = subtaskid;
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

    public int getTaskid() {
        return taskid;
    }

    public void setTaskid(int taskid) {
        this.taskid = taskid;
    }

    public boolean isIscompleted() {
        return iscompleted;
    }

    public void setIscompleted(boolean iscompleted) {
        this.iscompleted = iscompleted;
    }

    public Date getCompleteddate() {
        return completeddate;
    }

    public void setCompleteddate(Date completeddate) {
        this.completeddate = completeddate;
    }

    public boolean isExtendrequired() {
        return extendrequired;
    }

    public void setExtendrequired(boolean extendrequired) {
        this.extendrequired = extendrequired;
    }
}

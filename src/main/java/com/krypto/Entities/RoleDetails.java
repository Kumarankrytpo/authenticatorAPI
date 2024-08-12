package com.krypto.Entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="roledetails")
public class RoleDetails {

    @Id
    @Column(name="id")
    private int id;

    @Column(name="rolename")
    private String rolename;

    public RoleDetails(){}

    public RoleDetails(int id, String rolename) {
        this.id = id;
        this.rolename = rolename;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRolename() {
        return rolename;
    }

    public void setRolename(String rolename) {
        this.rolename = rolename;
    }
}

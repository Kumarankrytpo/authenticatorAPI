package com.krypto.Entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="authcode")
public class AuthCode {

    @Id
    @Column(name="uid")
    private int uid;

    @Column(name="code")
    private int code;

    public AuthCode() {}

    public AuthCode(int uid, int code) {
        this.uid = uid;
        this.code = code;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}

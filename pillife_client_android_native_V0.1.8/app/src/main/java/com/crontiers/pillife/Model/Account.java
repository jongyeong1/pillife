package com.crontiers.pillife.Model;

import java.io.Serializable;
import java.math.BigInteger;

// ~krime
public class Account implements Serializable, MvConfig {
    private BigInteger userId;
    private String username;
    private String name;

    public Account(){
        this.userId = new BigInteger("0000");
        this.name = "이종영";
        this.username = "관리자";
    }

    public BigInteger getUserId() {
        return userId;
    }

    public void setUserId(BigInteger userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

package com.ed;

/**
 * Created by edwardsbean on 2015/2/9 0009.
 */
public class Config {
    private String uid;
    private String pwd;
    private String pid;
    private String email;
    private String password;

    public Config(String uid, String pwd, String pid) {
        this.uid = uid;
        this.pwd = pwd;
        this.pid = pid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

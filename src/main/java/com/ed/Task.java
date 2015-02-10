package com.ed;

/**
 * Created by edwardsbean on 2015/2/10 0010.
 */
public class Task {
    private String email;
    private String password;

    public Task(String email, String password) {
        this.email = email;
        this.password = password;
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

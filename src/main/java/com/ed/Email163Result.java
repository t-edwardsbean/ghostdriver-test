package com.ed;

import java.util.Map;

/**
 * Created by shicongyu01_91 on 2015/3/11.
 */
public class Email163Result {
    private int code;
    private String desc;
    private Map<String, Integer> result;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Map<String, Integer> getResult() {
        return result;
    }

    public void setResult(Map<String, Integer> result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "Email163Result{" +
                "code=" + code +
                ", desc='" + desc + '\'' +
                ", result=" + result +
                '}';
    }
}

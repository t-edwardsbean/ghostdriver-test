package com.ed;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class AIMATest {

    @Test
    public void testGetPhone() throws Exception {
        String a = "2509003147|d04b7507a68e75d7d99c42680788fd72";
        System.out.println(a.split(""));

    }

    @Test
    public void testCode() throws Exception {
        String test = "【搜狐】您手机注册的验证码为3070(有效期为5分钟)。保护账号安全请用狐盾：http://t.cn/8kthjRI";
        Pattern p = Pattern.compile("([0-9]{4})");
        Matcher m = p.matcher(test);
        if (m.find( )) {
            System.out.println("Found value: " + m.group());
        } else {
            System.out.println("NO MATCH");
        }
    }
}
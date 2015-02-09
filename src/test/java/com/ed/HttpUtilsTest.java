package com.ed;


public class HttpUtilsTest {

    @org.junit.Test
    public void testGet() throws Exception {
        System.out.println(HttpUtils.Get("http://www.baidu.com"));
    }
}
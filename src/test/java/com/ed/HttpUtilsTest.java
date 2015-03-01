package com.ed;


import com.google.common.io.Files;
import com.jayway.jsonpath.JsonPath;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Lookup;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HttpUtilsTest {

    @org.junit.Test
    public void testGet() throws Exception {
        System.out.println(HttpUtils.Get("http://www.baidu.com"));
    }

    @Test
    public void testPost() throws Exception {
        //验证邮箱可用
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("email", "aasefasdf@sohu.com"));
        String response = HttpUtils.Post("https://passport.sohu.com/regist/checkuserid", new UrlEncodedFormEntity(nvps), "");
        String result = JsonPath.read(response, "$.status");
        System.out.println(result);
    }

    @Test
    public void testDownload() throws Exception {
        File file = new File("code.png");
        HttpGet httpget = new HttpGet("http://passport.sohu.com/captcha?time=0.13598114298656583");
        CookieStore cookieStore = new BasicCookieStore();
        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(cookieStore);
        CloseableHttpResponse response = HttpUtils.httpclient.execute(httpget, context);
        BufferedInputStream bis = new BufferedInputStream(response.getEntity().getContent());
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        int inByte;
        while((inByte = bis.read()) != -1) bos.write(inByte);
        bis.close();
        bos.close();
    }
}
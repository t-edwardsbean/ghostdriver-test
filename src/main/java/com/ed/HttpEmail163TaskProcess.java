package com.ed;

import com.google.gson.Gson;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by shicongyu01_91 on 2015/3/11.
 */
public class HttpEmail163TaskProcess  extends TaskProcess{
    private static Logger log = LoggerFactory.getLogger(HttpEmail163TaskProcess.class);

    public HttpEmail163TaskProcess(String phantomjsPath) {
        super(phantomjsPath);
    }

    @Override
    public void process(AIMA aima, Task task) throws Exception {
        CookieStore cookieStore = new BasicCookieStore();
        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(cookieStore);
        CloseableHttpResponse login = null;
        try {

            login = HttpUtils.httpclient.execute(new HttpGet("http://reg.email.163.com/unireg/call.do?cmd=register.entrance&from=163mail"), context);
        } catch (Exception e) {

        } finally {
            if (login != null) {
                EntityUtils.consume(login.getEntity());
            }
        }

        //check email
        HttpPost checkEmail = new HttpPost("http://reg.email.163.com/unireg/call.do?cmd=urs.checkName");
        checkEmail.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36");
        checkEmail.addHeader("Referer", "http://reg.email.163.com/unireg/call.do?cmd=register.entrance&from=163mail");
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("name", task.getEmail()));
        checkEmail.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
        CloseableHttpResponse checkEmailResponse = HttpUtils.httpclient.execute(checkEmail, context);
        String checkEmailResult = EntityUtils.toString(checkEmailResponse.getEntity());
        log.debug("检查邮箱名：" + checkEmailResult);
        Gson gson = new Gson();
        Email163Result email163Result = gson.fromJson(checkEmailResult, Email163Result.class);
        if (email163Result.getCode() == 200 && email163Result.getDesc().equals("OK") && email163Result.getResult().containsKey("163.com")) {
            log.info("163.com邮箱可以注册");
        }



        //get photo
        String path = "code.png";
        String uuCode;
        File file = new File(path);
        while (true) {
            HttpGet getCodePhoto = new HttpGet("http://reg.email.163.com/unireg/call.do?cmd=register.verifyCode&v=common/verifycode/vc_en&env=004510304285&t=" + (new Date()).getTime());
            getCodePhoto.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36");
            getCodePhoto.addHeader("Referer", "http://reg.email.163.com/unireg/call.do?cmd=register.entrance&from=163mail");
            CloseableHttpResponse getCodePhotoResponse = HttpUtils.httpclient.execute(getCodePhoto, context);
            BufferedInputStream bis = new BufferedInputStream(getCodePhotoResponse.getEntity().getContent());
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            int inByte;
            while ((inByte = bis.read()) != -1) bos.write(inByte);
            bis.close();
            bos.close();
            getCodePhotoResponse.close();
            log.debug("识别图片验证码");
            String result[] = UUAPI.easyDecaptcha("code.png", 2004);
            uuCode = result[1];
            log.debug(path + "图片验证码codeID:" + result[0]);
            log.debug(path + "图片验证码Result:" + uuCode);
            if ("-1008".equals(uuCode)) {
//                Thread.sleep(1000);
            } else {
                break;
            }
        }



    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String uid = "2509003147";
        String pwd = "1314520";
        String pid = "6555";

        Config config = new Config(uid, pwd, pid);
        RegistryMachine registryMachine = new RegistryMachine();
        registryMachine.setConfig(config);
        registryMachine.thread(1);
        registryMachine.setTaskProcess(new HttpEmail163TaskProcess(""));
        registryMachine.addTask(
                new Task("abc1234", "2692194")
        );
        boolean status = UUAPI.checkAPI();    //校验API，必须调用一次，校验失败，打码不成功

        if (!status) {
            System.out.print("API文件校验失败，无法使用打码服务");
            return;
        }
        registryMachine.run();
    }
}

package com.ed;

import com.jayway.jsonpath.JsonPath;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeader;
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
 * Created by edwardsbean on 2015/3/1 0001.
 */
public class HttpOldSohuTaskProcess extends TaskProcess {
    private static Logger log = LoggerFactory.getLogger(HttpOldSohuTaskProcess.class);

    public HttpOldSohuTaskProcess(String phantomjsPath) {
        super(phantomjsPath);
    }

    @Override
    public void process(AIMA aima, Task task) throws Exception {
        CookieStore cookieStore = new BasicCookieStore();
        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(cookieStore);
        CloseableHttpResponse login = HttpUtils.httpclient.execute(new HttpGet("http://i.sohu.com/login/reg.do"), context);
        login.close();

        //检查邮箱
        HttpGet checkEmail = new HttpGet("http://i.sohu.com/login/checksname?cn=" + task.getEmail());
        checkEmail.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36");
        checkEmail.addHeader("Referer", "http://i.sohu.com/login/reg.do");
        CloseableHttpResponse checkEmailResponse = HttpUtils.httpclient.execute(checkEmail, context);
        String checkEmailResult = EntityUtils.toString(checkEmailResponse.getEntity());
        String checkResult = JsonPath.read(checkEmailResult, "$.msg");
        log.debug("检查邮箱名：" + checkResult);


        while (true) {

            //获取图片验证码
            String code = getPictureCode(context);
            //提交
            HttpPost submitEmail = new HttpPost("http://i.sohu.com/login/sreg.do?_input_encode=utf-8");
            checkEmail.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36");
            checkEmail.addHeader("Referer", "http://i.sohu.com/login/reg.do");
            checkEmail.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            checkEmail.addHeader("Origin", "http://i.sohu.com");
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("bru", ""));
            nvps.add(new BasicNameValuePair("default_page", ""));
            nvps.add(new BasicNameValuePair("from", ""));
            nvps.add(new BasicNameValuePair("sappId", ""));
            nvps.add(new BasicNameValuePair("source", ""));
            nvps.add(new BasicNameValuePair("user", task.getEmail()));
            nvps.add(new BasicNameValuePair("nickname", task.getEmail()));
            nvps.add(new BasicNameValuePair("passwd", task.getPassword()));
            nvps.add(new BasicNameValuePair("vcode", code));
            nvps.add(new BasicNameValuePair("vcodeEn", ""));
            nvps.add(new BasicNameValuePair("agree", "on"));
            //中文url编码
            submitEmail.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
            CloseableHttpResponse submitEmailResponse = HttpUtils.httpclient.execute(submitEmail, context);
            String submitEmailResult = EntityUtils.toString(submitEmailResponse.getEntity());
            for (Cookie cookie : cookieStore.getCookies()) {
                if (cookie.getName().equals("errorcode")) {
                    if (cookie.getValue().contains("0001")) {
                        log.error("验证码错误");
                        continue;
                    }
                    log.error("注册失败：" + cookie.getValue());
                    return;
                }
            }
            if (submitEmailResult.contains("您的注册数量已超过正常限制,请使用已有账号进行登录")) {
                log.error("您的注册数量已超过正常限制,请使用已有账号进行登录");
                return;
            } else {
                log.info("注册成功");
                return;
            }
        }

    }

    public String getPictureCode(HttpClientContext context) throws Exception {
        String code;
        while (true) {
            String path = "code.png";
            File file = new File(path);
            CloseableHttpResponse pictureOneResponse = HttpUtils.httpclient.execute(new HttpGet("http://i.sohu.com/vcode/register/?nocache=" + (new Date()).getTime()), context);
            BufferedInputStream bis = new BufferedInputStream(pictureOneResponse.getEntity().getContent());
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            int inByte;
            while ((inByte = bis.read()) != -1) bos.write(inByte);
            bis.close();
            bos.close();
            pictureOneResponse.close();
            log.debug("识别图片验证码");
            String result[] = UUAPI.easyDecaptcha("code.png", 2004);
            code = result[1];
            log.debug(path + "图片验证码codeID:" + result[0]);
            log.debug(path + "图片验证码Result:" + code);
            if ("-1008".equals(code)) {
                Thread.sleep(1000);
            } else {
                return code;
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
        registryMachine.setTaskProcess(new HttpOldSohuTaskProcess(""));
        registryMachine.addTask(
                new Task("aazx123amm20", "2692194")
        );
        boolean status = UUAPI.checkAPI();    //校验API，必须调用一次，校验失败，打码不成功

        if (!status) {
            System.out.print("API文件校验失败，无法使用打码服务");
            return;
        }
        registryMachine.run();
    }
}

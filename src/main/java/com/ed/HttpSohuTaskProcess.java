package com.ed;

import com.jayway.jsonpath.JsonPath;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by edwardsbean on 2015/3/1 0001.
 */
public class HttpSohuTaskProcess extends TaskProcess {
    private static Logger log = LoggerFactory.getLogger(HttpSohuTaskProcess.class);

    public HttpSohuTaskProcess(String phantomjsPath) {
        super(phantomjsPath);
    }

    @Override
    public void process(AIMA aima, Task task) throws Exception {
        String email = task.getEmail() + "@sohu.com";
        String c = (new Date().getTime()) * 1000 + Math.round(Math.random() * 1000) + "";
        CookieStore cookieStore = new BasicCookieStore();
        BasicClientCookie stdCookie = new BasicClientCookie("PPUV", c);
        stdCookie.setDomain(".sohu.com");
        stdCookie.setPath("/");
        cookieStore.addCookie(stdCookie);
        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(cookieStore);
        //验证邮箱可用
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("email", email));
        String response = HttpUtils.Post("https://passport.sohu.com/regist/checkuserid", new UrlEncodedFormEntity(nvps), "");
        String result = JsonPath.read(response, "$.status");
        if (!"200".equals(result)) {
            log.error("邮箱不可用");
            return;
        }
        String phoneNum;
        String phoneCode;
        while (true) {
            //下载第一个验证码
            String codeOne = getPhoneCode("codeOne.png", context);
            //发送手机验证码
            HttpPost phonePost = new HttpPost("https://passport.sohu.com/regist/send_sms_captcha");
            nvps.clear();
            phoneNum = aima.getPhone();
            nvps.add(new BasicNameValuePair("mobile", phoneNum));
            nvps.add(new BasicNameValuePair("captcha", codeOne));
            phonePost.setEntity(new UrlEncodedFormEntity(nvps));
            CloseableHttpResponse phoneResponse = HttpUtils.httpclient.execute(phonePost, context);
            String phonePostResult = EntityUtils.toString(phoneResponse.getEntity());
            log.debug("请求发送短信：" + phonePostResult);
            result = JsonPath.read(phonePostResult, "$.status");
            if ("458".equals(result)) {
                log.error("验证码错误");
                //TODO 验证码错误反馈
                continue;
            } else if ("405".equals(result)) {
                log.error("该手机号当日短信发送次数超过上限");
                continue;
            }
            log.debug("发送验证码成功");
            phoneCode = aima.getSohuPhoneCode(phoneNum);
            if (phoneCode != null) {
                break;
            }
        }

        while (true) {
            //第二个图片验证码
            String codeSecond = getPhoneCode("codeSecond.png", context);
            //提交注册
            HttpPost submit = new HttpPost("https://passport.sohu.com/regist/email");
            nvps.clear();
            nvps.add(new BasicNameValuePair("email", task.getEmail()));
            nvps.add(new BasicNameValuePair("password", task.getPassword()));
            nvps.add(new BasicNameValuePair("new_password", task.getPassword()));
            nvps.add(new BasicNameValuePair("mobile", phoneNum));
            nvps.add(new BasicNameValuePair("mtoken", phoneCode));
            nvps.add(new BasicNameValuePair("captcha", codeSecond));
            nvps.add(new BasicNameValuePair("appid", "1000"));
            nvps.add(new BasicNameValuePair("domain", "sohu.com"));
            nvps.add(new BasicNameValuePair("ru", "http://login.mail.sohu.com/reg/signup_success.jsp"));
            submit.setEntity(new UrlEncodedFormEntity(nvps));
            CloseableHttpResponse submitResponse = HttpUtils.httpclient.execute(submit, context);
            String submitPostResult = EntityUtils.toString(submitResponse.getEntity());
            if (submitPostResult != null) {
                Element ele = Jsoup.parse(submitPostResult).select("#email_reg > p:nth-child(7) > span.info_tips").first();
                if (ele != null && "验证码错误".equals(ele.html())) {
                    log.debug("图片验证码错误,重试");
                    //TODO 错误验证码反馈
                    continue;
                } else {
                    log.debug("注册失败，未知错误：" + submitPostResult);
                    throw new MachineException("注册失败，未知错误");
                }
            }
            log.debug("注册成功");
            break;
        }


    }

    private String getPhoneCode(String path, HttpClientContext context) throws IOException, InterruptedException {
        while (true) {
            File file = new File(path);
            //TODO 时间戳计算
            String time = Math.random() + "";
            log.debug("生成时间戳：" + time);
            CloseableHttpResponse pictureOneResponse = HttpUtils.httpclient.execute(new HttpGet("http://passport.sohu.com/captcha?time=" + time), context);
            BufferedInputStream bis = new BufferedInputStream(pictureOneResponse.getEntity().getContent());
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            int inByte;
            while ((inByte = bis.read()) != -1) bos.write(inByte);
            bis.close();
            bos.close();
            pictureOneResponse.close();
            log.debug("识别图片验证码");
            String result[] = UUAPI.easyDecaptcha(path, 3005);
            String code = result[1];
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
        registryMachine.setTaskProcess(new HttpSohuTaskProcess(""));
        registryMachine.addTask(
                new Task("wsscy2014b", "2692194")
        );
        boolean status = UUAPI.checkAPI();    //校验API，必须调用一次，校验失败，打码不成功

        if (!status) {
            System.out.print("API文件校验失败，无法使用打码服务");
            return;
        }
        registryMachine.run();


    }
/**
 GOON:'100', //正常，可以继续
 OK:'200', //成功
 NOT_FOUND:'404', //没有查询到信息
 NOT_ALLOW:'405', //此操作被限制
 WRONG_PARAMS:'450', //输入参数错误
 ACCOUNT_EXIST:'451', //账号已存在
 ACCOUNT_NOT_ALLOW:'452', //账号不符合规范
 ACCOUNT_LOCKED:'453', //输入的账号被锁定
 ACCOUNT_INACTIVE:'454', //账号未激活，暂时不能使用
 NICK_EXIST:'455', //昵称已存在
 NICK_NOT_ALLOW:'456', //昵称不合规范
 NICK_NO_CHANGE:'457', //此用户不能修改昵称
 CAPTCHA_WRONG:'458', //验证码错误
 PSW_WRONG:'459', //密码错误
 PSW_NOT_ALLOW:'460', //密码不合规范
 MOBILE_WRONG:'461', //手机号错误
 MOBILE_HAVE_BIND:'462', //用户已经绑定了手机号
 MOBILE_OTHER_BIND:'463', //手机号已经被其他账号绑定
 MOBILE_CANT_UNBIND:'464', //手机注册的账号不能解绑
 SIG_WRONG:'465', //接口的签名错误
 ID_WRONG:'466', //输入的身份校验信息错误
 DPSW_WRONG:'467', //动态口令错误
 CAPTCHA_NEED:'468', //此操作需要提供验证码
 HUDUN_NEED:'469', //此操作需要狐盾动态口令
 OLD_EMAIL_WRONG : '470', //旧绑定邮箱错误
 OLD_ANSWER_WRONG : '471', //旧密保问题答案错误
 IP_LOCKED: '472', //IP被锁定
 EMAIL_WRONG:'420',
 SERIAL_NUM_WRONG : '474', //序列号错误
 HUDUN_NOT_BIND : '476',
 USER_LOGIN_FORBIDDEN : '477', //用户禁止登录
 BIND_EMAIL_WRONG : '478', //绑定邮箱错误
 BIND_EMAIL_SELF : '479',  //不能绑定自己
 MOBILE_EXIST: '480',	//该手机号已被注册
 INTERNAL_ERROR:'500' //内部服务器错误
 */
}

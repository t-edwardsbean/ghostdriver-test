package com.ed;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by edwardsbean on 2015/2/23 0023.
 */
public class ProxyProcess extends TaskProcess {
    public ProxyProcess(String phantomjsPath) {
        super(phantomjsPath);
    }
    private static Logger log = LoggerFactory.getLogger(ProxyProcess.class);
    @Override
    public void process(AIMA aima, Task task) {
        DesiredCapabilities caps = new DesiredCapabilities();
        List<String> args = new ArrayList<String>();
        args.add("--ignore-ssl-errors=yes");
        //启动phantomjs传递的命令行参数
        if (task.getArgs() != null) {
            args.addAll(task.getArgs());
        }
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, args.toArray(new String[args.size()]));
        //phantomjs启动后的参数
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent", "Mozilla/5.0 (Linux;U;Android 2.2.2;zh-cn;ZTE-C_N880S Build/FRF91) AppleWebkit/531.1(KHTML, like Gecko) Version/4.0 Mobile Safari/531.1");
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, phantomjsPath);
        caps.setJavascriptEnabled(true);
        caps.setCapability("takesScreenshot", true);
        PhantomJSDriver session = new PhantomJSDriver(caps);
        session.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        session.manage().timeouts().pageLoadTimeout(5, TimeUnit.SECONDS);


        try {

            try {
                session.get("https://mail.sina.com.cn/register/regmail.php");
            } catch (Exception e) {
                throw new MachineException("网络超时，或者代理不可用", e);
            }
            Object result = session.executePhantomJS("var page = this;" +
                    "return page");
//        Object resutl = session.executePhantomJS("var page = this;" +
//                "page.settings.resourceTimeout = 5000;");
            System.out.println(result);
            String beginTitle = session.getTitle();
            System.out.println(session.findElementByCssSelector("#openNow_2").getText());
            if (beginTitle.isEmpty()) {
                throw new MachineException("找不到登陆头,网路超时或代理不可用");
            }
            log.debug("检测登陆头：" + beginTitle);
        } finally {
            session.quit();
        }

    }

    public static void main(String[] args) {
        String uid = "2509003147";
        String pwd = "1314520";
        String pid = "1219";

        Config config = new Config(uid, pwd, pid);
        RegistryMachine registryMachine = new RegistryMachine();
        registryMachine.setConfig(config);
        registryMachine.thread(1);
        registryMachine.setTaskProcess(new ProxyProcess("E:\\GitHub\\registry-machine\\dependency\\phantomjs\\phantomjs.exe"));
        registryMachine.addTask(
                new Task("wasd1babaxq3", "2692194")
        );
        registryMachine.run();
    }
}

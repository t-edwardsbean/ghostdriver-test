package com.ed;


import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 测试获取网页alert的内容
 * Created by edwardsbean on 2015/2/24 0024.
 */
public class AlertProcess extends TaskProcess {
    public AlertProcess(String phantomjsPath) {
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
                log.debug("打开网页");
                session.get("http://localhost:9000/test.html");
            } catch (Exception e) {
                throw new MachineException("网络超时，或者代理不可用", e);
            }
            //定义全局变量lastAlert,并且必须在session.get打开网页之后执行
            session.executeScript("window.alert = function(message) {window.lastAlert = message;};");
            WebElement button = session.findElementByCssSelector("#submit");
            log.debug("按钮：" + button.getTagName());
            button.click();
            Object result = session.executeScript("return window.lastAlert;");
            log.debug("获取alert结果{}", result);
            try {
                FileUtils.copyFile(((TakesScreenshot) session).getScreenshotAs(OutputType.FILE), new File("alert.png"));
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        registryMachine.setTaskProcess(new AlertProcess("E:\\GitHub\\registry-machine\\dependency\\phantomjs\\phantomjs.exe"));
        registryMachine.addTask(
                new Task("wasd1babaxq3", "2692194")
        );
        registryMachine.run();
    }
}

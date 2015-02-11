package com.ed;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by edwardsbean on 2015/2/9 0009.
 */
public class SinaTaskProcess implements TaskProcess{
    private static Logger log = LoggerFactory.getLogger(SinaTaskProcess.class);

    public void process(AIMA aima, Task task) {
        DesiredCapabilities caps = new DesiredCapabilities();
        //启动phantomjs传递的命令行参数
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, new String[]{"--ignore-ssl-errors=yes"});
        //phantomjs启动后的参数
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent", "Mozilla/5.0 (Linux;U;Android 2.2.2;zh-cn;ZTE-C_N880S Build/FRF91) AppleWebkit/531.1(KHTML, like Gecko) Version/4.0 Mobile Safari/531.1");
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, "/home/edwardsbean/software/phantomjs-1.9.2-linux-x86_64/bin/phantomjs");
        caps.setJavascriptEnabled(true);
        caps.setCapability("takesScreenshot", true);
        RemoteWebDriver session = null;
        session = new PhantomJSDriver(caps);
        session.get("https://mail.sina.com.cn/register/regmail.php");
        log.debug(Thread.currentThread() + "填写邮箱名字");
        String email = task.getEmail();
        if(17 < email.length() || email.length() < 6 ) {
            throw new MachineException(Thread.currentThread() + "邮箱的长度应该在6-16个字符之间");
        } else if (!email.matches("\\w+")) {
            throw new MachineException(Thread.currentThread() + "邮箱名仅允许使用小写英文、数字或下划线");
        }

        WebElement emailElement = session.findElementByCssSelector("#emailName");
        WebElement passwordElement = session.findElementByCssSelector("#password_2");
        emailElement.sendKeys(email);
        String password = task.getPassword();
        if(17 < password.length() || password.length() < 6) {
            throw new MachineException(Thread.currentThread() + "密码的长度应该在6-16个字符之间");
        }
        passwordElement.sendKeys(password);
        WebElement emailElementAlert = session.findElementByXPath("//*[@id=\"form_2\"]/ul/li[1]/p");
        WebElement phoneElement = session.findElementByCssSelector("#phoneNum_2");
        String phone = aima.getPhone();
        phoneElement.sendKeys(phone);
        //如何监控emailcheck.php
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!emailElementAlert.getText().isEmpty()) {
            throw new MachineException(Thread.currentThread() + "邮箱不合法：" + emailElementAlert.getText());
        } else {
            log.debug(Thread.currentThread() + "邮箱ok,继续");
        }
        WebElement releaseCodeElement = session.findElementByCssSelector("#getCode_2");
        releaseCodeElement.click();
        try {
            FileUtils.copyFile(((TakesScreenshot) session).getScreenshotAs(OutputType.FILE), new File(Thread.currentThread().getId() + "sendcode.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String code = aima.getPhoneCode(phone);
        if (code != null) {
            WebElement codeElement = session.findElementByCssSelector("#checkCode_2");
            codeElement.sendKeys(code);
        }
        WebElement submit = session.findElementByCssSelector("#openNow_2");
        submit.click();
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.debug(Thread.currentThread() + "注册成功，账号：{},密码：{}", email, password);
        //必须删除cookie,否则其他session也是互通cookie
        session.manage().deleteAllCookies();
        try {
            FileUtils.copyFile(((TakesScreenshot) session).getScreenshotAs(OutputType.FILE), new File(Thread.currentThread().getId() + "result.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        session.quit();
    }
}

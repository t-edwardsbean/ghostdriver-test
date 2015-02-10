package com.ed;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Platform;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;

/**
 * Created by edwardsbean on 2015/2/9 0009.
 */
public class MachineTask {
    private static Logger log = LoggerFactory.getLogger(MachineTask.class);
    private String email;
    private String password;
    private AIMA aima;

    public MachineTask(String email, String password, AIMA aima) {
        this.email = email;
        this.password = password;
        this.aima = aima;
    }

    public void process() throws Exception {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("phantomjs.page.settings.userAgent", "Mozilla/5.0 (Linux;U;Android 2.2.2;zh-cn;ZTE-C_N880S Build/FRF91) AppleWebkit/531.1(KHTML, like Gecko) Version/4.0 Mobile Safari/531.1");
        caps.setJavascriptEnabled(true);
        caps.setPlatform(Platform.ANDROID);
        caps.setCapability("takesScreenshot", true);
        RemoteWebDriver session = new RemoteWebDriver(new URL("http://localhost:9999"), caps);
        session.get("https://mail.sina.com.cn/register/regmail.php");
        log.debug("填写邮箱名字");
        WebElement emailElement = session.findElementByCssSelector("#emailName");
        WebElement passwordElement = session.findElementByCssSelector("#password_2");
        emailElement.sendKeys(email);
        passwordElement.sendKeys(password);
        WebElement emailElementAlert = session.findElementByXPath("//*[@id=\"form_2\"]/ul/li[1]/p");
        WebElement phoneElement = session.findElementByCssSelector("#phoneNum_2");
        String phone = aima.getPhone();
        phoneElement.sendKeys(phone);
        //如何监控emailcheck.php
        Thread.sleep(1500);
        if (!emailElementAlert.getText().isEmpty()) {
            throw new MachineException("邮箱不合法：" + emailElementAlert.getText());
        } else {
            log.debug("邮箱ok,继续");
        }
        WebElement releaseCodeElement = session.findElementByCssSelector("#getCode_2");
        releaseCodeElement.click();
        FileUtils.copyFile(((TakesScreenshot) session).getScreenshotAs(OutputType.FILE), new File("sendcode.png"));
        String code = aima.getPhoneCode(phone);
        if (code != null) {
            WebElement codeElement = session.findElementByCssSelector("#checkCode_2");
            codeElement.sendKeys(code);
        }
        WebElement submit = session.findElementByCssSelector("#openNow_2");
        submit.click();
        Thread.sleep(4000);
        log.debug("注册成功，账号：{},密码：{}", email, password);
        FileUtils.copyFile(((TakesScreenshot) session).getScreenshotAs(OutputType.FILE), new File("result.png"));
        session.close();
    }
}

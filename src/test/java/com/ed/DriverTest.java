package com.ed;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;

/**
 * Created by edwardsbean on 15-2-11.
 */
public class DriverTest {
    @Test
    public void testWebDriver() throws Exception {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, new String[]{"--ignore-ssl-errors=yes"});
        caps.setCapability("phantomjs.page.settings.userAgent", "Mozilla/5.0 (Linux;U;Android 2.2.2;zh-cn;ZTE-C_N880S Build/FRF91) AppleWebkit/531.1(KHTML, like Gecko) Version/4.0 Mobile Safari/531.1");
        caps.setJavascriptEnabled(true);
        caps.setCapability("takesScreenshot", true);
        WebDriver session;
        session = new RemoteWebDriver(new URL("http://localhost:9999"), caps);
        session.get("https://mail.sina.com.cn/register/regmail.php");
        System.out.println(session.getTitle());
        WebElement passwordElement = session.findElement(By.cssSelector("#password_2"));
        passwordElement.click();
        
    }

    @Test
    public void testOwnnerDriver() throws Exception {
        DesiredCapabilities caps = new DesiredCapabilities();
        //启动phantomjs传递的命令行参数
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, new String[]{"--ignore-ssl-errors=yes"});
        //phantomjs启动后的参数
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent", "Mozilla/5.0 (Linux;U;Android 2.2.2;zh-cn;ZTE-C_N880S Build/FRF91) AppleWebkit/531.1(KHTML, like Gecko) Version/4.0 Mobile Safari/531.1");
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, "/home/edwardsbean/phantomjs-1.9.7/bin/phantomjs");
        caps.setJavascriptEnabled(true);
        caps.setCapability("takesScreenshot", true);
        RemoteWebDriver session = null;
        session = new PhantomJSDriver(caps);
        session.get("https://mail.sina.com.cn/register/regmail.php");
        System.out.println(session.getTitle());
    }
}

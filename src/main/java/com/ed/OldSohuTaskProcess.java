package com.ed;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by edwardsbean on 2015/3/1 0001.
 */
public class OldSohuTaskProcess extends TaskProcess {
    private static Logger log = LoggerFactory.getLogger(OldSohuTaskProcess.class);

    public OldSohuTaskProcess(String phantomjsPath) {
        super(phantomjsPath);
    }

    @Override
    public void process(AIMA aima, Task task) throws Exception {
        DesiredCapabilities caps = new DesiredCapabilities();
        List<String> args = new ArrayList<String>();
        args.add("--ignore-ssl-errors=yes");
        //启动phantomjs传递的命令行参数
        if (task.getArgs() != null) {
            args.addAll(task.getArgs());
        }
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, args.toArray(new String[args.size()]));
        //phantomjs启动后的参数
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36");
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, phantomjsPath);
        caps.setJavascriptEnabled(true);
        caps.setCapability("takesScreenshot", true);
        PhantomJSDriver session = null;
        try {
            session = new PhantomJSDriver(caps);
            session.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
            session.manage().timeouts().pageLoadTimeout(5, TimeUnit.SECONDS);
            try {
                session.get("http://i.sohu.com/login/reg.do");
            } catch (Exception e) {
                throw new MachineException("网络超时，或者代理不可用", e);
            }
            WebElement email = session.findElementByCssSelector("#user");
            email.sendKeys(task.getEmail());
            WebElement nick = session.findElementByCssSelector("#nickname");
            nick.sendKeys(task.getEmail());
            WebElement pass = session.findElementByCssSelector("#passwd");
            Thread.sleep(500);
            pass.sendKeys(task.getPassword());
            WebElement img = session.findElementByCssSelector("#chkPicDiv > img");
            //获取图片验证码
            String code;
            while (true) {
                String path = "code.png";
                File file = new File(path);
                TakeScreenShot(session, img);
                log.debug("识别图片验证码");
                String result[] = UUAPI.easyDecaptcha("tmp/code.png", 2004);
                code = result[1];
                log.debug(path + "图片验证码codeID:" + result[0]);
                log.debug(path + "图片验证码Result:" + code);
                if ("-1008".equals(code)) {
                    Thread.sleep(1000);
                } else {
                    break;
                }
            }
            WebElement codeElement = session.findElementByCssSelector("#vcode");
            codeElement.sendKeys(code);
            WebElement sug = session.findElementByCssSelector("#reg > li:nth-child(5) > div.checkAreaWar > label");
            WebElement submit = session.findElementByCssSelector("#reg > li:nth-child(6) > input");
            sug.click();
            submit.click();
            Thread.sleep(3000);
        }finally {
            FileUtils.copyFile(((TakesScreenshot) session).getScreenshotAs(OutputType.FILE), new File("tmp/sohu-end-exception.png"));
            Thread.sleep(1000);
            session.quit();
        }
    }
    public static void TakeScreenShot(PhantomJSDriver driver, WebElement element) {
        File screen = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        BufferedImage img = null;
        try {
            img = ImageIO.read(screen);
        } catch (IOException e) {
            e.printStackTrace();
        }

        File f = new File("tmp/code.png");

        Point point = element.getLocation();
        int width = element.getSize().getWidth();
        int height = element.getSize().getHeight();
        log.debug("图片位置：" + point);
        log.debug("图片长：" + width);
        log.debug("图片宽：" + height);
        BufferedImage dest = img.getSubimage(point.getX(), point.getY(), width, height);
        try {
            ImageIO.write(dest, "png", screen);
            FileUtils.copyFile(screen, f);
        } catch (IOException e) {
            e.printStackTrace();
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
        registryMachine.setTaskProcess(new OldSohuTaskProcess("dependency\\phantomjs\\phantomjs.exe"));
        registryMachine.addTask(
                new Task("aazx123am111", "2692194")
        );
        boolean status = UUAPI.checkAPI();    //校验API，必须调用一次，校验失败，打码不成功

        if (!status) {
            System.out.print("API文件校验失败，无法使用打码服务");
            return;
        }
        registryMachine.run();
    }
}

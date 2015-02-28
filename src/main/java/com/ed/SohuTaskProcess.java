package com.ed;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by edwardsbean on 15-2-13.
 */
public class SohuTaskProcess extends TaskProcess {
    private static Logger log = LoggerFactory.getLogger(SohuTaskProcess.class);
    public static String SOHU = "https://passport.sohu.com/web/dispatchAction.action?appid=1000&ru=http://login.mail.sohu.com/reg/signup_success.jsp";

    public SohuTaskProcess(String phantomjsPath) {
        super(phantomjsPath);
    }

    @Override
    public void process(AIMA aima, Task task) throws Exception{
        DesiredCapabilities caps = new DesiredCapabilities();
        List<String> args = new ArrayList<String>();
        args.add("--ignore-ssl-errors=yes");
        //启动phantomjs传递的命令行参数
        if (task.getArgs() != null) {
            args.addAll(task.getArgs());
        }
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, args.toArray(new String[args.size()]));
        //phantomjs启动后的参数
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.111 Safari/537.36");
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, phantomjsPath);
        caps.setJavascriptEnabled(true);
        caps.setCapability("takesScreenshot", true);
        PhantomJSDriver session = null;
        try {
            session = new PhantomJSDriver(caps);
            session.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
            session.manage().timeouts().pageLoadTimeout(5, TimeUnit.SECONDS);
            try {
                session.get(SOHU);
            } catch (Exception e) {
                throw new MachineException("网络超时，或者代理不可用", e);
            }
            String beginTitle = session.getTitle();
            if (beginTitle.isEmpty()) {
                throw new MachineException("找不到登陆头,网路超时或代理不可用");
            }
            log.debug("检测登陆头：" + beginTitle);
            log.debug(Thread.currentThread() + "填写邮箱名字");
            String email = task.getEmail();
            if (17 < email.length() || email.length() < 6) {
                throw new MachineException(Thread.currentThread() + "邮箱的长度应该在6-16个字符之间");
            } else if (!email.matches("\\w+")) {
                throw new MachineException(Thread.currentThread() + "邮箱名仅允许使用小写英文、数字或下划线");
            }
            WebElement emailElement = session.findElementByCssSelector("#email");
            WebElement passwordElementOne = session.findElementByCssSelector("#psw1");
            WebElement passwordElementAgain = session.findElementByCssSelector("#psw11");
            WebElement serviceAdmit = session.findElementByCssSelector("#email_reg > p:nth-child(8) > input");
            serviceAdmit.click();
            emailElement.sendKeys(email);
            String password = task.getPassword();
            if (17 < password.length() || password.length() < 6) {
                throw new MachineException(Thread.currentThread() + "密码的长度应该在6-16个字符之间");
            }
            passwordElementOne.sendKeys(password);
            Thread.sleep(1500);
            WebElement emailElementAlert = session.findElementByCssSelector("#email_reg > p:nth-child(2) > span:nth-child(4) em");
            String emailAlert = emailElementAlert.getAttribute("class");
            if (emailAlert.equals("success")) {
                log.debug(Thread.currentThread() + "邮箱ok,继续");
            } else {
                WebElement emailElementAlertInfo = session.findElementByCssSelector("#email_reg > p:nth-child(2) > span:nth-child(4)");
                throw new MachineException(Thread.currentThread() + "邮箱不合法：" + emailElementAlertInfo.getText());
            }
            passwordElementAgain.sendKeys(password);
            WebElement phoneElement = session.findElementByCssSelector("#email_reg input[name=mobile]");
//            String phone = aima.getPhone();
//            phoneElement.sendKeys(phone);
            phoneElement.sendKeys("18046049822");
            WebElement releaseElement = session.findElementByCssSelector("#email_reg a.mt5");
            releaseElement.click();
            log.debug("等待第一个验证码");
            Thread.sleep(1000);
            log.debug("截取第一个图片验证码");
            String firstPicturePath = "tmp/" + task.getEmail() + "-one.png";
            TakeFirstPicture(session, session.findElementByCssSelector("div.modal.verification.popsmsyzm"), firstPicturePath);
            Thread.sleep(1000);
            String result[] = UUAPI.easyDecaptcha(firstPicturePath, 3005);
            log.debug("第一个图片验证码codeID:" + result[0]);
            log.debug("第一个图片验证码Result:" + result[1]);
            log.debug("输入第一个图片验证码");
//            //TODO 获取图片验证码结果
            WebElement firstPictureVerify = session.findElementByCssSelector("body > div.modal.verification.popsmsyzm > div.vContext > input");
            firstPictureVerify.sendKeys(result[1]);
            WebElement firstPictureButton = session.findElementByCssSelector("body > div.modal.verification.popsmsyzm > a.blue_btn");
            int tryNum = 3;
            while (true) {
                log.debug("验证第一个图片验证码");
                FileUtils.copyFile(((TakesScreenshot) session).getScreenshotAs(OutputType.FILE), new File("tmp/sohu-first-before.png"));
                firstPictureButton.click();
                WebElement firstAlert = session.findElementByCssSelector("body > div.modal.verification.popsmsyzm > div.alert.alert-red");
                Thread.sleep(3000);
                log.debug("第一个验证码验证结果：" + firstAlert.getText());
                //验证成功
                if (!"验证码错误".equals(firstAlert.getText())) {
                    break;
                }
                break;
//                log.debug("更换第一个验证码，重试");
//                TakeFirstPicture(session, session.findElementByCssSelector("div.modal.verification.popsmsyzm"),firstPicturePath);
//                Thread.sleep(2000);
//                //TODO 获取图片验证码结果
//                result = UUAPI.easyDecaptcha(firstPicturePath, 1005);
//                log.debug("第一个图片验证码codeID:" + result[0]);
//                log.debug("第一个图片验证码Result:" + result[1]);
//                firstPictureVerify.clear();
//                firstPictureVerify.sendKeys(result[1]);
//                tryNum--;
            }
//            Thread.sleep(500);
//            String phoneCode = "123123";
//            String isPhoneRelease = releaseElement.getText();
//            if ("秒后可重新发送".equals(isPhoneRelease)) {
//                //TODO 获取手机验证码
//                log.debug("获取手机验证码");
//            } else {
//                throw new MachineException("错误");
//            }
//            WebElement phoneVerifyInput = session.findElementByCssSelector("#email_reg > p:nth-child(6) > input[type='text']");
//            phoneVerifyInput.sendKeys(phoneCode);
//
//            log.debug("截取第二个图片验证码");
//            WebElement secondPicture = session.findElementByCssSelector("#yzm_img");
//            log.debug("图片验证码：" + secondPicture.getTagName());
//            TakeScreenShot(session, secondPicture);
//            Thread.sleep(1000);
//            log.debug("输入第二个图片验证码");
//            String secondPictureCode = "asdas";
//            WebElement secondPictureVerify = session.findElementByCssSelector("#yzm");
//            secondPictureVerify.sendKeys(secondPictureCode);
//            log.debug("验证第二个图片验证码");
//            WebElement submit = session.findElementByCssSelector("#confirm1");
//            submit.click();
        } finally {
            try {
                Thread.sleep(1000);
                FileUtils.copyFile(((TakesScreenshot) session).getScreenshotAs(OutputType.FILE), new File("tmp/sohu-end-exception.png"));
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (session != null) {
                session.quit();
            }
        }
    }

    public static void TakeFirstPicture(PhantomJSDriver driver, WebElement element,String fileName) {
        File screen = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        BufferedImage img = null;
        try {
            img = ImageIO.read(screen);
        } catch (IOException e) {
            e.printStackTrace();
        }

        File f = new File(fileName);

        Point point = element.getLocation();
        int width = element.getSize().getWidth();
        int height = element.getSize().getHeight();
        BufferedImage dest = img.getSubimage(point.getX() + 435, point.getY() + 285, width/3 - 10, height/3 - 10);
        try {
            ImageIO.write(dest, "png", screen);
            FileUtils.copyFile(screen, f);
        } catch (IOException e) {
            e.printStackTrace();
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

        File f = new File("tmp/second.png");

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
        String pid = "1219";

        Config config = new Config(uid, pwd, pid);
        RegistryMachine registryMachine = new RegistryMachine();
        registryMachine.setConfig(config);
        registryMachine.thread(1);
        registryMachine.setTaskProcess(new SohuTaskProcess("dependency\\phantomjs\\phantomjs.exe"));
        registryMachine.addTask(
//                new Task("wasd1babaxq3", "2692194").setArgs(Arrays.asList("--proxy=127.0.0.1:7070", "--proxy-type=socks5")),
//                new Task("wasd123qxxc3", "2692194"),
//                new Task("azxas1asaz33", "2692194"),
//                new Task("azxas1asdxz33", "2692194"),
//                new Task("azxas1asdxz33", "2692194"),
//                new Task("azxas1asdxz33", "2692194"),
//                new Task("azxas1asdxz33", "2692194"),
                new Task("aazx123a1a1", "2692194")
        );
        boolean status = UUAPI.checkAPI();    //校验API，必须调用一次，校验失败，打码不成功

        if (!status) {
            System.out.print("API文件校验失败，无法使用打码服务");
            return;
        }

        registryMachine.run();

    }

}

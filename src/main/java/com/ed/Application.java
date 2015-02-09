package com.ed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by edwardsbean on 2015/2/8 0008.
 */
public class Application {
    private static Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        String uid = "2509003147";
        String pwd = "1314520";
        String pid = "1219";

        Config config = new Config(uid, pwd, pid);
        RegistryMachine registryMachine = new RegistryMachine();
        registryMachine.setConfig(config);
        registryMachine.run();

    }
}

package com.ed;


import java.io.IOException;

/**
 * Created by edwardsbean on 2015/2/8 0008.
 */
public class Application {

    public static void main(String[] args) throws IOException, InterruptedException {
        String uid = "2509003147";
        String pwd = "1314520";
        String pid = "1219";

        Config config = new Config(uid, pwd, pid);
        RegistryMachine registryMachine = new RegistryMachine();
        registryMachine.setConfig(config);
        registryMachine.thread(3);
        registryMachine.setTaskProcess(new SinaTaskProcess());
//        registryMachine.addTask(new Task("wasd1230q", "2692194"), new Task("wasd123qc", "2692194"), new Task("azxas1asd3", "2692194"));
        registryMachine.addTask(new Task("wasd123qc", "2692194"));
        registryMachine.run();

    }
}

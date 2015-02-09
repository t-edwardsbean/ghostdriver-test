package com.ed;

/**
 * Created by edwardsbean on 2015/2/9 0009.
 */
public class RegistryMachine {
    private Config config;
    private AIMA aima;

    public void setConfig(Config config) {
        this.config = config;
    }

    private void init() {
        this.aima = new AIMA(config.getUid(), config.getPwd(), config.getPid());
    }
    public void run() {
        init();
        MachineTask machineTask = new MachineTask(config.getEmail(), config.getPassword(), aima);
        try {
            machineTask.process();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package com;

public class CountUpdate {

    protected ServerLogic serverLogic;
    protected int updateCount = 0;

    public CountUpdate(ServerLogic serverLogic) {
        this.serverLogic = serverLogic;
    }

    public void update(double timeElapsed) throws Exception {
        updateCount++;
    }

    public void displayUpdateCount() {
        System.out.println(updateCount + " updates");
    }
}

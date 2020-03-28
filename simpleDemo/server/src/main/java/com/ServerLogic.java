package com;

public abstract class ServerLogic {

    protected int targetUPS = 100;
    protected double targetTime;
    protected double lastLoopTime = 0;
    protected double elapsedTime = 0;
    protected boolean mustStop = false;

    public ServerLogic() {
        this.targetTime = 1.0f / targetUPS;
    }

    public void stop() {
        this.mustStop = true;
    }

    public void run() {
        try {
            this.init();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        try {
            this.lastLoopTime = System.nanoTime() / 1000_000_000.0;
            while (!mustStop) {
                double time = System.nanoTime() / 1000_000_000.0;
                this.elapsedTime += time - this.lastLoopTime;
                if (this.elapsedTime >= this.targetTime) {
                    this.update(this.targetTime);
                    double timeLeft = this.elapsedTime - this.targetTime;
                    while (timeLeft >= targetTime) {
                        this.update(this.targetTime);
                        timeLeft -= targetTime;
                    }
                    elapsedTime = timeLeft;
                }
                this.lastLoopTime = time;

                try {
                    Thread.sleep(1);
                } catch (InterruptedException ie) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.destroy();
        }
    }


    protected abstract void init() throws Exception;

    protected abstract void update(double timeElapsed) throws Exception;

    protected abstract void destroy();
}

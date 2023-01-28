package work.lclpnet.provider;

import java.util.Random;

public class Loop {

    private boolean running = true;

    public void start() {
        long nextTick, sleep;

        while (running) {
            nextTick = System.currentTimeMillis() + 1000;

            tick();

            sleep = nextTick - System.currentTimeMillis();
            if (sleep < 0) System.err.printf("Warning: Tick took too long (%s ms)%n", -sleep + 1000);
            else if (sleep > 0) {
                try {
                    //noinspection BusyWait
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    running = false;
                    break;
                }
            }
        }
    }

    private final Random random = new Random();

    private void tick() {
        System.out.println("TICK");
        if (random.nextInt(10) == 0) {
            try {
                Thread.sleep(3203);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

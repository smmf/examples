package pt.ist.fenixframework.example.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

public class Increment {

    private static final Logger logger = LoggerFactory.getLogger(Increment.class);

    public static void main(final String[] args) {
        try {
            ensureCounter();

//            incrementMethod();

            Thread t1 = new Thread(new IncrementLoop());
            t1.start();

//            Thread t2 = new Thread(new IncrementLoop());
//            t2.start();
//
//            new Thread(new IncOther()).start();
//            new Thread(new IncOther()).start();
//
//            new Thread(new IncrementLoop()).start();
//            new Thread(new IncrementLoop()).start();
//            new Thread(new IncrementLoop()).start();
//            new Thread(new IncrementLoop()).start();

            new Thread(new MeasureThroughput()).start();

            t1.join();
//            t2.join();
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            FenixFramework.shutdown();
        }
    }

//    @Atomic
    @Atomic(mode = Atomic.TxMode.WRITE)
    private static void ensureCounter() {
        logger.debug("============ Ensure counter ============");
        Counter counter = FenixFramework.getDomainRoot().getCounter();
        if (counter == null) {
            logger.debug("Create counter");
            FenixFramework.getDomainRoot().setCounter(new Counter());
        }
    }

//    @Atomic
    @Atomic(mode = Atomic.TxMode.WRITE)
    private static void incCounter() {
        logger.debug("Increment tx");
        Counter counter = FenixFramework.getDomainRoot().getCounter();
        logger.info("start value=" + counter.getAndInc());
//        System.out.print(".");
//        for (int i = 0; i < 100; i++) {
//            new Counter();
//        }

        /* Add more stuff */
//        FenixFramework.getDomainRoot().addClock(new Counter());
    }

    /* this is the logic */
    public static void incrementMethod() {
        ensureCounter();
        while (true) {
            logger.debug("Increment loop");
            incCounter();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logger.warn("Interrupted.", e);
                break;
            }
        }
    }

    /* Runnable */
    public static class IncrementLoop implements Runnable {
        @Override
        public void run() {
            incrementMethod();
        }
    }

    /* Throughput thread */

    public static class MeasureThroughput implements Runnable {
        private static final double NANO_SEC_IN_SEC = 1000000000;

        int lastValue = 0;
        int lastValueOther = 0;
        long lastTime = 0;

        static void sleep(int millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                logger.warn("Interrupted.", e);
            }
        }

        @Override
//        @Atomic
//        @Atomic(mode = Atomic.TxMode.WRITE)
        public void run() {
            init();
            while (true) {
                checkRate();
            }
        }

        @Atomic
//        @Atomic(mode = Atomic.TxMode.WRITE)
        protected void init() {
            sleep(5000);

            Counter counter = FenixFramework.getDomainRoot().getCounter();
            if (counter != null) {
                this.lastValue = counter.getValue();
            }

            Counter otherCounter = FenixFramework.getDomainRoot().getOtherCounter();
            if (otherCounter != null) {
                this.lastValueOther = otherCounter.getValue();
            }

            this.lastTime = System.nanoTime();
        }

        @Atomic
//        @Atomic(mode = Atomic.TxMode.WRITE)
        protected void checkRate() {
            sleep(5000);

            /* get current values */

            int currentValue = 0;
            Counter counter = FenixFramework.getDomainRoot().getCounter();
            if (counter != null) {
                currentValue = counter.getValue();
            }

            int currentValueOther = 0;
            Counter otherCounter = FenixFramework.getDomainRoot().getOtherCounter();
            if (otherCounter != null) {
                currentValueOther = otherCounter.getValue();
                System.out.println("Other exists. Value is " + currentValueOther);
            } else {
                System.out.println("Other is NULL!!!");
            }

            /* get current time */
            long currentTime = System.nanoTime();

            /* compute inc rates */
            double incRate = 0;
            if (currentValue != 0) {
                incRate = (currentValue - this.lastValue) / ((currentTime - lastTime) / NANO_SEC_IN_SEC);
            }

            double incRateOther = 0;
            if (currentValueOther != 0) {
                incRateOther = (currentValueOther - this.lastValueOther) / ((currentTime - lastTime) / NANO_SEC_IN_SEC);
            }

            System.out.println("------> Inc rate: " + incRate + "/s ------> Inc rate other: " + incRateOther + "/s");

            /* Assign last values */
            this.lastValue = currentValue;
            this.lastValueOther = currentValueOther;
            this.lastTime = currentTime;
        }
    }

}

package pt.ist.fenixframework.example.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

public class IncrementOther {

    private static final Logger logger = LoggerFactory.getLogger(IncrementOther.class);

    public static void main(final String[] args) {
        try {
            Thread t1 = new Thread(new IncOther());
            t1.start();

            t1.join();
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            FenixFramework.shutdown();
        }
    }

    @Atomic(mode = Atomic.TxMode.WRITE)
    public static void ensureOtherCounter() {
        Counter otherCounter = FenixFramework.getDomainRoot().getOtherCounter();
        if (otherCounter == null) {
            logger.debug("Create otherCounter");
            FenixFramework.getDomainRoot().setOtherCounter(new Counter());
        }
    }

    @Atomic(mode = Atomic.TxMode.WRITE)
    public static void incOtherCounter() {
        logger.debug("Increment tx");
        Counter otherCounter = FenixFramework.getDomainRoot().getOtherCounter();
        logger.info("start value=" + otherCounter.getAndInc());
    }

    /* this is the logic */
    public static void incrementMethodOther() {
        ensureOtherCounter();
        while (true) {
            logger.debug("Increment other loop");
            incOtherCounter();
//        try {
//            Thread.sleep(50);
//        } catch (InterruptedException e) {
//            logger.warn("Interrupted.", e);
//            break;
//        }
        }
    }

    /* Runnable */
    public static class IncOther implements Runnable {
        @Override
        public void run() {
            incrementMethodOther();
        }

    }

}

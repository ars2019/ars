package org.apache.commons.dbcp;

import java.util.concurrent.CountDownLatch;

import edu.illinois.jacontebe.Helpers;
import edu.illinois.jacontebe.framework.Reporter;

/**
 * <p>
 * Bug URL: https://issues.apache.org/jira/browse/DBCP-271 <br>
 * Implemented Version: dbcp 1.2, JDK 1.6.0_33
 * </p>
 * <p>
 * This is an inconsistent synchronization. <br>
 * Refer to 271.html in description directory for more information.
 * </p>
 * 
 * @author Ziyi Lin
 * 
 */
public class Dbcp271 {

    @SuppressWarnings("deprecation")
    private AbandonedTrace test;

    // Use this latch to control to start all reading threads first and then
    // start writing thread.
    private CountDownLatch latch;

    // Use sync flag to switch between synchronized and unsynchronized version
    // of test.
    private final boolean sync;

    // Number of reading threads
    private final static int READS = 100;//100

    // Number of loops
    private final static int SIZE = 10;//10

    @SuppressWarnings("deprecation")
    public Dbcp271(boolean synch) {
        sync = synch;
        test = new AbandonedTrace();
        latch = new CountDownLatch(READS);
    }

    public void run() {
        Thread[] reads = new Thread[READS];
        for (int i = 0; i < READS; i++) {
            reads[i] = new Read();
        }
        Thread t2 = new Write();
        for (int i = 0; i < READS; i++) {
            reads[i].start();
        }
        t2.start();
        for (int i = 0; i < READS; i++) {
            try {
                reads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private class Read extends Thread {

        @SuppressWarnings("deprecation")
        public void run() {
            latch.countDown();
            long value;

            // Read the value of lastUsed. Quit the
            // loop when lastUsed reaches the value
            // set by write thread.
            while (true) {
                if (sync) {
                    synchronized (test) {
                        value = test.getLastUsed();
                    }
                } else {
                    value = test.getLastUsed();
                }
                if (value == SIZE - 1) {
                    break;
                }
            }
        }
    }

    private class Write extends Thread {

        public Write() {
            setName("Writer");
        }

        @SuppressWarnings("deprecation")
        public void run() {

            // Wait for all reading threads finishing starting
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (long i = 0; i < SIZE; i++) {
                // Change the value of lastUsed
                if (sync) {
                    synchronized (test) {
                        test.setLastUsed(i);
                    }
                } else {
                    test.setLastUsed(i);
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {

        Reporter.reportStart("dbcp271", 0, "inconsistent synchronization");

        // Run synchronized version first.
        Dbcp271 syncTest = new Dbcp271(true);
        long start = System.currentTimeMillis();
        syncTest.run();
        System.out.println("Proper synchronized version time consumed:"
                + (System.currentTimeMillis() - start) + " ms");
        System.out
                .println("Now executing not synchronized version. It might go into an endless loop:");

        // Start an endless loop monitor in case endless loop happens.
        Helpers.startEndlessLoopMonitor(10);
        // Run inconsistent synchronized version.
        Dbcp271 unsyncTest = new Dbcp271(false);
        start = System.currentTimeMillis();
        unsyncTest.run();
        System.out.println("Not synchronized version time consumed:"
                + (System.currentTimeMillis() - start) + " ms");
    }
}
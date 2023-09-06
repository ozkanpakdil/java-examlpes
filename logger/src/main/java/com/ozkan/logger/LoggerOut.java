package com.ozkan.logger;

import java.io.*;
import java.time.Instant;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Do not use in prod, this is just a playground code.
 */
public class LoggerOut implements Runnable {
    OutputStream genericOut;
    private final ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
    private AtomicBoolean enable = new AtomicBoolean(true);

    public LoggerOut(OutputStream genericOut) {
        this.genericOut = genericOut;
    }

    public LoggerOut() throws FileNotFoundException {
        this.genericOut = new FileOutputStream(new File("log.log"));
    }

    public void log(String whatEver) {
        queue.add(whatEver);
    }

    private void writeSome() {
        try {
            if (queue.size() > 0)
                genericOut.write((Instant.now() + "-" + queue.poll() + System.lineSeparator()).getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        while (this.enable.get()) {
            writeSome();
            try {
                genericOut.flush();
                Thread.sleep(10);
                if (queue.size() % 1000 == 0)
                    System.out.println("waiting size:" + queue.size());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void stop() {
        while (getQSize() > 0)// finish the queue
            writeSome();
        enable = new AtomicBoolean(false);
    }

    public int getQSize() {
        return queue.size();
    }
}

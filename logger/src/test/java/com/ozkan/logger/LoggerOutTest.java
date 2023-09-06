package com.ozkan.logger;


import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.stream.IntStream;

class LoggerOutTest {

    LoggerOut testee;

    @Test
    void log() throws Exception {
        testee=new LoggerOut();
        (new Thread(testee)).start();
        IntStream.range(0,100000)
                .parallel()
                .forEach(i->testee.log(String.valueOf(i)));
        testee.stop();
        System.out.println(testee.getQSize());
    }

    @Test
    void stop() {
    }
}
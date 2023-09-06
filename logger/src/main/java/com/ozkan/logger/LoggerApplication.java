package com.ozkan.logger;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Scanner;

public class LoggerApplication {

    public static void main(String[] args) throws Exception {
        LoggerOut loggerOut = new LoggerOut(new FileOutputStream(new File("del.log")));
        loggerOut.log("hello");
        Thread n = new Thread(loggerOut);
        n.start();
        loggerOut.log("hello");
        Scanner scan = new Scanner(System.in);

        while (true) {// application logic...
            System.out.print("sup?:");
            String next = scan.next();
            if (!next.isBlank()) {
                loggerOut.log(next);
                if (next.startsWith("exit")) {
                    loggerOut.stop();
                    System.exit(0);
                }
                Thread.sleep(10);
            }
        }
    }

}

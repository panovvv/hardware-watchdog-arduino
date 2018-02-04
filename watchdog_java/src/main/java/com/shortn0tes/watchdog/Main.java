package com.shortn0tes.watchdog;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOG = Logger.getLogger("Watchdog Controller");
    private static final int TIMEOUT = 30;

    public static void main(String[] args) {
        LOG.info("Trying to find watchdog... Connect your Arduino, please.");
        Watchdog watchdog = null;
        try {
            watchdog = Watchdog.findHardware("hello", "HELLO", TIMEOUT);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOG.severe(String.format("No watchdog watchdog found after %d second long search! Exiting...", TIMEOUT));
            System.exit(-1);
        }

        LOG.info(String.format("Found the watchdog on port %s", watchdog.getPort()));
        watchdog.start("ping", "pong");
    }
}

package com.shortn0tes.watchdog;

import com.fazecast.jSerialComm.SerialPort;
import com.shortn0tes.watchdog.model.SerialPortWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Watchdog {

    private static final Logger LOG = Logger.getLogger(Watchdog.class.getName());

    private static final int BAUD_RATE = 9600;
    private static final int DATA_BITS = 8;
    private static final int STOP_BITS = SerialPort.ONE_STOP_BIT;
    private static final int PARITY = SerialPort.NO_PARITY;

    private static final int MILLIS_TO_INIT_ARDUINO = 3000;
    private static final int SECONDS_TO_RECEIVE_HS_RESPONSE = 2;
    private static final int SECONDS_TO_RECEIVE_PONG = 10;
    private static final int MILLIS_BETWEEN_PINGS = 5000;

    private SerialPortWrapper port;

    private Watchdog() {

    }

    private Watchdog(SerialPortWrapper port) {
        this.port = port;
    }

    public SerialPortWrapper getPort() {
        return port;
    }

    public static Watchdog findHardware(String handshake, String response, int searchTimeoutSeconds)
            throws InterruptedException, ExecutionException, TimeoutException {

        FutureTask<SerialPortWrapper> scanAllSerialPorts = new FutureTask<>(() -> {
            List<SerialPortWrapper> oldPorts = new ArrayList<>();
            while (true) {
                List<SerialPortWrapper> newPorts = Arrays.stream(SerialPort.getCommPorts())
                        .map(SerialPortWrapper::new)
                        .collect(Collectors.toList());
                newPorts.removeAll(oldPorts);
                if (!newPorts.isEmpty()) {
                    SerialPortWrapper firstOne = newPorts.get(0);
                    LOG.info(String.format("Trying to find the watchdog on port %s...", firstOne));
                    SerialPort portToCheck = firstOne.get();
                    portToCheck.setComPortParameters(BAUD_RATE, DATA_BITS, STOP_BITS, PARITY);
                    portToCheck.openPort();
                    Thread.sleep(MILLIS_TO_INIT_ARDUINO);
                    clearUartBuffer(portToCheck);
                    if (portToCheck.writeBytes(handshake.getBytes(), handshake.getBytes().length) < 0) {
                        LOG.warning(String.format("Can't send handshake to port %s!", firstOne));
                        portToCheck.closePort();
                    } else {
                        LOG.info(String.format("Sent \"%s\" port %s...", handshake, firstOne));
                        if (listenForResponse(portToCheck, response, SECONDS_TO_RECEIVE_HS_RESPONSE)) {
                            return firstOne;
                        } else {
                            LOG.info(String.format("Looks like %s is not one...", firstOne));
                            portToCheck.closePort();
                        }
                    }
                    oldPorts.add(firstOne);
                }
            }
        });

        Executor executor = Executors.newSingleThreadScheduledExecutor();
        executor.execute(scanAllSerialPorts);
        return new Watchdog(scanAllSerialPorts.get(searchTimeoutSeconds, TimeUnit.SECONDS));
    }

    public void start(String ping, String pong) {
        final SerialPort watchdogPort = port.get();
        clearUartBuffer(watchdogPort);
        while (true) {
            if (watchdogPort.writeBytes(ping.getBytes(), ping.getBytes().length) < 0) {
                LOG.warning(String.format("Can't send ping to port %s!", port));
            } else {
                LOG.info("Ping!");
                if (!listenForResponse(watchdogPort, pong, SECONDS_TO_RECEIVE_PONG)) {
                    LOG.warning(String.format("[%s] not received! Check the connections,", pong));
                }
            }
            try {
                Thread.sleep(MILLIS_BETWEEN_PINGS);
            } catch (InterruptedException e) {
                LOG.warning("Can't perform delay");
                e.printStackTrace();
            }
        }
    }

    private static boolean listenForResponse(SerialPort port, String expectedResponse, int timeoutSeconds) {
        FutureTask<String> getResponseFromSerialPort = new FutureTask<>(() -> {
            String result = "";
            while (true) {
                if (port.bytesAvailable() == 0) {
                    Thread.sleep(20);
                } else {
                    byte[] readBuffer = new byte[port.bytesAvailable()];
                    port.readBytes(readBuffer, readBuffer.length);
                    String in = new String(readBuffer);
                    result += in;
                    LOG.info(String.format("Received [%s]", in));
                    if (result.contains(expectedResponse)) {
                        return result;
                    }
                }
            }
        });
        Executor executor = Executors.newSingleThreadScheduledExecutor();
        executor.execute(getResponseFromSerialPort);

        try {
            final String actualResponse = getResponseFromSerialPort.get(timeoutSeconds, TimeUnit.SECONDS);
            LOG.info(String.format("Received the expected [%s] in [%s]", expectedResponse, actualResponse));
            return true;
        } catch (TimeoutException e) {
            LOG.warning("Listening for response cancelled after timeout");
            return false;
        } catch (InterruptedException | ExecutionException e) {
            LOG.warning("Something went wrong while I was listening for response:");
            e.printStackTrace();
            return false;
        }
    }

    private static void clearUartBuffer(SerialPort serialPort) {
        byte[] buffer = new byte[1];
        while (serialPort.bytesAvailable() > 0) {
            serialPort.readBytes(buffer, 1);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Watchdog watchdog = (Watchdog) o;
        return Objects.equals(port, watchdog.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(port);
    }

    @Override
    public String toString() {
        return "Watchdog{ port=" + port + '}';
    }
}

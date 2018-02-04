package com.shortn0tes.watchdog.model;

import com.fazecast.jSerialComm.SerialPort;

import java.util.Objects;

/**
 * Wrapper class for serial port,
 * serves two purposes: adds equals()
 * and hashcode() so they could be used with
 * Collections and adds toString().
 * Extending SerialPort class would've made
 * more sense, but it's final :(
 */
public class SerialPortWrapper {
    private final SerialPort serialPort;

    public SerialPortWrapper(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    public SerialPort get() {
        return serialPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SerialPortWrapper that = (SerialPortWrapper) o;
        return Objects.equals(serialPort.getSystemPortName(), that.serialPort.getSystemPortName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(serialPort);
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", serialPort.getDescriptivePortName(), serialPort.getSystemPortName());
    }
}

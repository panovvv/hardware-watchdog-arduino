This repository contains the full source code for Arduino-based PC watchdog.

---

`watchdog_arduino_sketch` directory holds the sketch for Arduino board.
It's compatible with any board having at least one IO pin and one serial port, which is basically all of them :)
Just download [Arduino IDE](https://www.arduino.cc/en/Main/Software), open the sketch and upload it.

---

`watchdog_java` directory holds the PC-side code written in Java.
You're going to need to install [JRE](https://java.com/en/download/) to run it and [Maven](https://maven.apache.org/) to build it.

To build an executable jar, run
`mvn clean compile assembly:single` in _watchdog_java_.

The jar executable will be created in

_target/hardware-watchdog.jar_

You can then launch it:

`java -jar hardware-watchdog.jar`
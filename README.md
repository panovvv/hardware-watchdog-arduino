This repository contains the full source code for Arduino-based PC watchdog.

## Arduino part

__watchdog_arduino_sketch__ directory holds the sketch for Arduino board.
It's compatible with any board having at least one IO pin and one serial port, which is basically all of them :)
Just download [Arduino IDE](https://www.arduino.cc/en/Main/Software), open the sketch and upload it.

## PC part

__watchdog_java__ directory holds the PC-side code written in Java.
You're going to need to install [JRE](https://java.com/en/download/) to run it and [Maven](https://maven.apache.org/) to build it.

To build an executable jar, run

`mvn clean compile assembly:single`

in _watchdog_java_ directory.

The jar executable will be created in _target/hardware-watchdog.jar_

You can then launch it:

`java -jar hardware-watchdog.jar`

Then you'll have to add this line to your startup list to re-engage
your watchdog after restart:

#### Windows

Press __Win+R__, enter `shell:startup`. In the folder that you opened,
create a file __run_watchdog.bat__ with this line:

`java -jar yourjar`

where yourjar is the fully qualified file JAR name, e.g.
 __C:\data\hardware-watchdog-arduino\watchdog_java\target\hardware-watchdog.jar__

#### Linux

The matter of autostarting the applications in Linux is highly platform-dependent,
 so refer to your distribution's manuals e.g.
  [Arch wiki](https://wiki.archlinux.org/index.php/autostarting) for ArchLinux and so on.
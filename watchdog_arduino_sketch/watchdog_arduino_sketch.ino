//#define DEBUG

#define RESET_PIN 2
#define PING_TIMEOUT 10000 // in milliseconds
const String HANDSHAKE = "hello";
const String HANDSHAKE_RESPONSE = "HELLO";
const String PING = "ping";
const String PONG = "pong";

String handshakeBuf;
int handshakeBufPos = 0;

String pingBuf;
int pingBufPos = 0;

bool connected = false;
uint32_t timestamp;

void setup() {
  pinMode(RESET_PIN, OUTPUT);
  digitalWrite(RESET_PIN, 0);
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, 0);
  clearBuffers();

  Serial.begin(9600);
  while (!Serial) {
    ; // wait for serial port to connect. Needed for native USB port only
  }
}

void loop() {
  if (!connected) {
    /* Wait for a handshake. We won't
       reset the PC before the handshake has
       been established.
    */
    if (Serial.available() > 0) {
      handshakeBuf[handshakeBufPos] = Serial.read();
      if (handshakeBufPos == HANDSHAKE.length() - 1) {
        handshakeBufPos = 0;
      } else {
        handshakeBufPos++;
      }

#ifdef DEBUG
      Serial.print("handshakeBuf: [");
      Serial.print(handshakeBuf);
      Serial.println("]");
#endif

      if (bufferHasString(handshakeBuf, HANDSHAKE)) {
#ifdef DEBUG
        Serial.println("Detected handshake!");
#endif
        Serial.print(HANDSHAKE_RESPONSE);
        clearBuffers();
        connected = true;
        digitalWrite(LED_BUILTIN, 1);
        timestamp = millis();
      }
    }
  } else {
    /* Handshake has been performed.
       We wait for pings, and reset if no pings
       were detected within the PING_TIMEOUT milliseconds
    */
    if (Serial.available() > 0) {
      pingBuf[pingBufPos] = Serial.read();
      if (pingBufPos == PING.length() - 1) {
        pingBufPos = 0;
      } else {
        pingBufPos++;
      }

#ifdef DEBUG
      Serial.print("pingBuf: [");
      Serial.print(pingBuf);
      Serial.println("]");
#endif

      if (bufferHasString(pingBuf, PING)) {
#ifdef DEBUG
        Serial.print("Pinged ");
        Serial.print(millis() - timestamp);
        Serial.println(" millis after the previous");
#endif
        timestamp = millis();
        Serial.print(PONG);
        digitalWrite(LED_BUILTIN, 0);
        delay(100);
        digitalWrite(LED_BUILTIN, 1);
        clearPingBuffer();
      }
    }
    if (millis() - timestamp > PING_TIMEOUT) {
#ifdef DEBUG
      Serial.print("No ping detected for ");
      Serial.print(PING_TIMEOUT);
      Serial.println(" millis, restarting...");
#endif
      digitalWrite(RESET_PIN, 1);
      delay(1000);
      digitalWrite(RESET_PIN, 0);
      connected = false;
      digitalWrite(LED_BUILTIN, 0);
      clearBuffers();
    }
  }
}

bool bufferHasString(String buf, String what) {
  for (uint8_t i = 0; i < buf.length(); i++) {
    char leftmost = what.charAt(0);
    for (uint8_t i = 0; i < what.length() - 1; i++) {
      what.setCharAt(i, what.charAt(i + 1));
    }
    what.setCharAt(what.length() - 1, leftmost);
#ifdef DEBUG
    Serial.print("Comparing buffer [");
    Serial.print(buf);
    Serial.print("] to [");
    Serial.print(what);
    Serial.println("]");
#endif
    if (buf.equals(what)) {
#ifdef DEBUG
      Serial.println("Buffer is equal to this string!");
#endif
      return true;
    }
  }
#ifdef DEBUG
  Serial.println("No match found");
#endif
  return false;
}

void clearBuffers() {
  clearHandshakeBuffer();
  clearPingBuffer();
  clearInputBuffer();
}

void clearHandshakeBuffer() {
  handshakeBuf = "";
  for (uint8_t i = 0; i < HANDSHAKE.length(); i++) {
    handshakeBuf += " ";
  }
  handshakeBufPos = 0;
}

void clearPingBuffer() {
  pingBuf = "";
  for (uint8_t i = 0; i < PING.length(); i++) {
    pingBuf += " ";
  }
}

void clearInputBuffer() {
  while (Serial.available() > 0) {
    Serial.read();
  }
}

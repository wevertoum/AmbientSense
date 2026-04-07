/*
 * AmbientSense — Tinkercad / UNO: A0 TMP36 (°C), A1 luminosidade 0–100%, A2 umidade simulada.
 * Serial 9600 baud, uma linha JSON por amostra.
 */
static const char DEVICE_ID[] = "ambient-sense-01";
static const uint32_t INTERVAL_MS = 1000;

static const uint8_t PIN_TEMP = A0;
static const uint8_t PIN_LIGHT = A1;
static const uint8_t PIN_HUMIDITY = A2;

static float readTemperatureC() {
  int raw = analogRead(PIN_TEMP);
  float v = raw * (5.0f / 1024.0f);
  return (v - 0.5f) * 100.0f;
}

static int readLuminosityPercent() {
  int raw = analogRead(PIN_LIGHT);
  raw = constrain(raw, 0, 1023);
  return (int)map(raw, 0, 1023, 0, 100);
}

static float readHumidityPercent() {
  int raw = analogRead(PIN_HUMIDITY);
  raw = constrain(raw, 0, 1023);
  return (float)map(raw, 0, 1023, 0, 100);
}

static void emitJsonLine(uint32_t tsMs, float tempC, float humPct, int lumPct) {
  Serial.print(F("{\"timestamp\":"));
  Serial.print(tsMs);
  Serial.print(F(",\"temperature\":"));
  Serial.print(tempC, 2);
  Serial.print(F(",\"humidity\":"));
  Serial.print(humPct, 2);
  Serial.print(F(",\"luminosity\":"));
  Serial.print(lumPct);
  Serial.print(F(",\"deviceId\":\""));
  Serial.print(DEVICE_ID);
  Serial.println(F("\"}"));
}

void setup() {
  Serial.begin(9600);
  pinMode(PIN_TEMP, INPUT);
  pinMode(PIN_LIGHT, INPUT);
  pinMode(PIN_HUMIDITY, INPUT);
}

void loop() {
  static uint32_t lastTick = 0;
  uint32_t now = millis();

  if (now - lastTick < INTERVAL_MS) {
    return;
  }
  lastTick = now;

  float t = readTemperatureC();
  float h = readHumidityPercent();
  int lum = readLuminosityPercent();

  emitJsonLine(now, t, h, lum);
}

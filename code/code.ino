#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <Stepper.h>
#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>
#include <Keypad.h>

// Configuración de WiFi
const char* ssid = "Pension63";
const char* password = "LaPension2024.";
const String serverIP = "http://64.23.154.127:8080";
const String endpointEstado = serverIP + "/login/isAuthenticated";
const String endpointUsuario = serverIP + "/login/getLoggedUser";
const String endpointGetState = serverIP + "/components/getState";
const String endpointAddComponent = serverIP + "/components/addComponent";

// Configuración del motor paso a paso
const int stepsPerRevolution = 2048;  // Pasos por revolución para 28BYJ-48
// Definir los pines del motor en secuencia correcta IN1-IN2-IN3-IN4
Stepper motor(stepsPerRevolution, 16, 17, 18, 19);  // Orden corregido de pines
const int motorSpeed = 10;  // Velocidad en RPM, ajustada para mejor rendimiento

// LCD y otros componentes permanecen igual
LiquidCrystal_I2C lcd(0x27, 16, 2);

// Configuración del teclado matricial
const uint8_t ROWS = 4;
const uint8_t COLS = 4;
char keys[ROWS][COLS] = {
  { '1', '2', '3', 'A' },
  { '4', '5', '6', 'B' },
  { '7', '8', '9', 'C' },
  { '*', '0', '#', 'D' }
};
uint8_t colPins[COLS] = { 26, 25, 33, 32 };
uint8_t rowPins[ROWS] = { 13, 12, 14, 27 };
Keypad keypad = Keypad(makeKeymap(keys), rowPins, colPins, ROWS, COLS);

// Pines y estados
const int PIN_LED = 2;
bool stateLED = false;
bool stateMotor = false;
unsigned long lastCheckTime = 0;
const int checkInterval = 3000;

// Variables de control del motor
unsigned long lastMotorStep = 0;
const int stepDelay = 2;  // Delay entre pasos en millisegundos
int currentStep = 0;

String usuarioLogueado = "";
bool usuarioAutenticado = false;

void setup() {
  Serial.begin(115200);

  // Configuración WiFi
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.println("Conectando a WiFi...");
  }
  Serial.println("Conectado a WiFi");

  // Configuración del motor
  motor.setSpeed(motorSpeed);  // Establecer velocidad en RPM

  // Configuración del LED
  pinMode(PIN_LED, OUTPUT);

  // Configuración del LCD
  lcd.init();
  lcd.backlight();
  lcd.setCursor(0, 0);
  lcd.print("Esperando Login...");
}

void loop() {
  if (millis() - lastCheckTime > checkInterval) {
    verificarEstadoAutenticacion();
    if (usuarioAutenticado) {
      obtenerEstadoDelServidor();
    }
    lastCheckTime = millis();
    actualizarPantalla();
  }

  if (usuarioAutenticado) {
    // Manejar entrada del teclado
    char key = keypad.getKey();
    if (key) {
      switch (key) {
        case '1':
          enviarNuevoEstado(!stateLED, stateMotor);
          break;
        case '2':
          enviarNuevoEstado(stateLED, !stateMotor);
          break;
      }
    }

    // Control del LED
    digitalWrite(PIN_LED, stateLED ? HIGH : LOW);

    // Control mejorado del motor
    if (stateMotor) {
      if (millis() - lastMotorStep >= stepDelay) {
        motor.step(1);  // Dar un solo paso
        lastMotorStep = millis();
        currentStep++;
        
        // Opcional: mostrar información de depuración
        if (currentStep % 100 == 0) {  // Cada 100 pasos
          Serial.print("Pasos dados: ");
          Serial.println(currentStep);
        }
      }
    } else {
      currentStep = 0;  // Reiniciar contador cuando el motor está apagado
    }
  }
}

// Nueva función para enviar estados al servidor
void enviarNuevoEstado(bool nuevoStateLED, bool nuevoStateMotor) {
  if (WiFi.status() == WL_CONNECTED) {
    HTTPClient http;
    http.begin(endpointAddComponent);
    http.addHeader("Content-Type", "application/json");

    // Crear el JSON para enviar
    StaticJsonDocument<200> doc;
    doc["state_led"] = nuevoStateLED;
    doc["state_motor"] = nuevoStateMotor;
    
    String jsonString;
    serializeJson(doc, jsonString);

    int httpResponseCode = http.POST(jsonString);
    
    if (httpResponseCode == 200) {
      Serial.println("Estados actualizados correctamente");
      // Actualizar estados locales
      stateLED = nuevoStateLED;
      stateMotor = nuevoStateMotor;
      actualizarPantalla();
    } else {
      Serial.print("Error al actualizar estados: ");
      Serial.println(httpResponseCode);
    }
    http.end();
  }
}

// Obtener estado desde el servidor
void obtenerEstadoDelServidor() {
  if (WiFi.status() == WL_CONNECTED) {
    HTTPClient http;
    http.begin(endpointGetState);
    int httpResponseCode = http.GET();
    
    if (httpResponseCode == 200) {
      String payload = http.getString();
      StaticJsonDocument<200> doc;
      DeserializationError error = deserializeJson(doc, payload);
      
      if (!error) {
        stateLED = doc["state_led"].as<bool>();
        stateMotor = doc["state_Motor"].as<bool>();
        Serial.println("Estados actualizados desde el servidor");
      }
    } else {
      Serial.println("Error obteniendo estados del servidor");
    }
    http.end();
  }
}

// Actualizar la pantalla
void actualizarPantalla() {
  lcd.clear();
  if (usuarioAutenticado) {
    lcd.setCursor(0, 0);
    lcd.print("Usuario: ");
    lcd.setCursor(0, 1);
    lcd.print(usuarioLogueado);
    // Mostrar estados actuales
    lcd.setCursor(0, 2);
    lcd.print("LED: ");
    lcd.print(stateLED ? "ON" : "OFF");
    lcd.setCursor(0, 3);
    lcd.print("Motor: ");
    lcd.print(stateMotor ? "ON" : "OFF");
  } else {
    mostrarMensajeSesionCerrada();
  }
}

void mostrarMensajeSesionCerrada() {
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Sesion cerrada");
  lcd.setCursor(0, 1);
  lcd.print("Esperando Login");
}

// Verificar si el usuario está autenticado
void verificarEstadoAutenticacion() {
  if (WiFi.status() == WL_CONNECTED) {
    HTTPClient http;
    http.begin(endpointEstado);
    int httpResponseCode = http.GET();

    if (httpResponseCode == 200) {
      HTTPClient userHttp;
      userHttp.begin(endpointUsuario);
      int userResponseCode = userHttp.GET();

      if (userResponseCode == 200) {
        usuarioLogueado = userHttp.getString();
        usuarioAutenticado = true;
        Serial.println("Usuario logueado: " + usuarioLogueado);
      }
      userHttp.end();
    } else {
      usuarioAutenticado = false;
      usuarioLogueado = "";
      Serial.println("Sesion cerrada");
    }
    http.end();
  }
}
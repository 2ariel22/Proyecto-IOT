#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <Stepper.h>
#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>
#include <Keypad.h>


const char* ssid = "iPhone de ariel";
const char* password = "123456789";
const String serverIP = "http://64.23.154.127:8080";
//const String serverIP = "http://172.16.123.59:8080";
const String endpointEstado = serverIP + "/login/isAuthenticated";
const String endpointUsuario = serverIP + "/login/getLoggedUser";
const String endpointGetState = serverIP + "/components/getState";
const String endpointAddComponent = serverIP + "/components/addComponent";

// Configuración del motor paso a paso
int stepsPerRevolution = 200; // Cambia según tu motor
Stepper motor(stepsPerRevolution, 16, 17, 18, 19);
int motorSpeed = 100;  // Velocidad en RPM, ajustada para mejor rendimiento
int giro =1;

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
char mode = 'A';
int numText=0;
int modeB =0;

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
  
}

void loop() {
  
  while(mode == 'A'){
    if (millis() - lastCheckTime > checkInterval) {
    obtenerEstadoDelServidor();
    verificarEstadoAutenticacion();
    lastCheckTime = millis();
    
    }
    // Manejar entrada del teclado
    char key = keypad.getKey();
    if (key) {
      switch (key) {
        case '1':
          enviarNuevoEstado(!stateLED, stateMotor,motorSpeed,giro);
          break;
        case '2':
          enviarNuevoEstado(stateLED, !stateMotor,motorSpeed,giro);
          break;
        case 'B':
          Serial.println("change mode to B");
          mode = 'B';
          break;
      }
    }
    // Control del LED
    digitalWrite(PIN_LED, stateLED ? HIGH : LOW);
    if(stepsPerRevolution<0){
      stepsPerRevolution = stepsPerRevolution*(-1);
    }
    stepsPerRevolution =(stepsPerRevolution *(giro));
    if (stateMotor) {
       if (millis() - lastMotorStep >= stepDelay) {
        motor.step(stepsPerRevolution);  // Dar un solo paso
        lastMotorStep = millis();
        currentStep++;
        
        // Opcional: mostrar información de depuración
        if (currentStep % 100 == 0) {  // Cada 100 pasos
          Serial.print("Pasos dados: ");
          Serial.println(currentStep);
        }
      }
    } 
    
  }
   while(mode == 'B'){
    if (millis() - lastCheckTime > checkInterval) {
    textModeB();
    lastCheckTime = millis();
    
    }
    char key = keypad.getKey();
    if (key) {
      switch (key) {
        case '0':
          numText = 0;
          modeB= 0;
          break;
        case '1':
          numText = 1;
          if(modeB == 1){
            
            motorSpeed= motorSpeed+5;
             if (motorSpeed>100){
              motorSpeed =100;
            }
            enviarNuevoEstado(stateLED, stateMotor,motorSpeed,giro);
            motor.setSpeed(motorSpeed);
            lcd.clear();
            lcd.setCursor(0, 0);
            lcd.print("speed +5");
            lcd.setCursor(0, 1);
            lcd.print("speed: ");
            lcd.setCursor(8, 1);
            lcd.print(motorSpeed);
            delay(1000);
          }
          else{
            
            modeB =1;
          }
          
          break;
        case '2':
          numText =2;
          if(modeB == 1){
            motorSpeed= motorSpeed -5;
            if (motorSpeed<0){
              motorSpeed =0;
            }
            enviarNuevoEstado(stateLED, stateMotor,motorSpeed,giro);
            motor.setSpeed(motorSpeed);
            lcd.clear();
            lcd.setCursor(0, 0);
            lcd.print("speed -5");
            lcd.setCursor(0, 1);
            lcd.print("speed: ");
            lcd.setCursor(8, 1);
            lcd.print(motorSpeed);
            delay(1000);
            numText =1;
          }
          else{
            modeB =2;
            if(giro==1){
              giro = -1;
             
            }
            else{
              giro=1;
            }
            
          }
          
          break;
        case 'A':
          Serial.println("change mode to A");
          mode = 'A';
          break;
      }
    }
   }

}

void textModeB(){
    lcd.clear();
    if(numText ==0){
      lcd.setCursor(0, 0);
      lcd.print("1.change speed M");
  
      lcd.setCursor(0, 1);
      lcd.print("2.change giro");
    }
    else if(numText == 1){
      lcd.setCursor(0, 0);
      lcd.print("1.speed +5");
      lcd.setCursor(0, 1);
      lcd.print("2.speed -5");
    }
    else if(numText == 2){
      lcd.setCursor(0, 0);
      lcd.print("Giro Cambiado");
      delay(1500);
      numText=0;
    }
  
    delay(20);

}

// Nueva función para enviar estados al servidor
void enviarNuevoEstado(bool nuevoStateLED, bool nuevoStateMotor, int nuevoSpeed, int nuevoGiro) {
  if (WiFi.status() == WL_CONNECTED) {
    HTTPClient http;
    http.begin(endpointAddComponent);
    http.addHeader("Content-Type", "application/json");

    // Crear el JSON para enviar
    StaticJsonDocument<200> doc;
    doc["state_led"] = nuevoStateLED;
    doc["state_motor"] = nuevoStateMotor;
    doc["speed"]=nuevoSpeed;
    doc["giro"]=nuevoGiro;
    
    String jsonString;
    serializeJson(doc, jsonString);

    int httpResponseCode = http.POST(jsonString);
    
    if (httpResponseCode == 200) {
      Serial.println("Estados actualizados correctamente");
      // Actualizar estados locales
      stateLED = nuevoStateLED;
      stateMotor = nuevoStateMotor;
      
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
        motorSpeed = doc["speed"].as<int>();
        giro = doc["giro"].as<int>();
        Serial.println("Estados actualizados desde el servidor");
      }
    } else {
      Serial.println("Error obteniendo estados del servidor");
    }
    http.end();
    actualizarPantalla();
  }
}

// Actualizar la pantalla
void actualizarPantalla() {
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("User: ");
    lcd.setCursor(7, 0);
    lcd.print(usuarioLogueado);
    delay(20);
    Serial.println(usuarioLogueado);
    // Mostrar estados actuales
    lcd.setCursor(8, 1);
    lcd.print("L: ");
    lcd.setCursor(11, 1);
    lcd.print(stateLED ? "ON" : "OFF");
    Serial.println(stateLED ? "ON" : "OFF");
    lcd.setCursor(0, 1);
    lcd.print("M: ");
    lcd.setCursor(3, 1);
    lcd.print(stateMotor ? "ON" : "OFF");
    Serial.println(stateMotor ? "ON" : "OFF");
    delay(20);
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
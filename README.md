# Proyecto IoT - Sistema de Control Remoto

## 📋 Descripción del Proyecto

Este es un proyecto completo de Internet de las Cosas (IoT) que permite el control remoto de componentes electrónicos (LED y motor paso a paso) a través de una aplicación móvil Android y un dispositivo Arduino. El sistema incluye autenticación de usuarios, control por voz, y generación de reportes en PDF.

## 🏗️ Arquitectura del Sistema

El proyecto está compuesto por tres componentes principales:

1. **Backend API** (Spring Boot + MySQL)
2. **Aplicación Móvil Android** (Kotlin)
3. **Dispositivo Arduino** (C++)

## 🚀 Componentes del Sistema

### 1. Backend API (`/api`)
- **Framework**: Spring Boot 3.3.5
- **Base de datos**: MySQL
- **ORM**: Spring Data JPA
- **Migraciones**: Flyway
- **Puerto**: 8080

#### Endpoints principales:
- `POST /login` - Registro de usuarios
- `POST /login/authenticate` - Autenticación
- `GET /login/getLoggedUser` - Obtener usuario logueado
- `GET /login/isAuthenticated` - Verificar autenticación
- `POST /login/logout` - Cerrar sesión
- `POST /components/addComponent` - Agregar estado de componentes
- `GET /components/getState` - Obtener estado actual
- `GET /components/getAllComponents` - Obtener historial completo

### 2. Aplicación Móvil Android (`/AmiwaMateo`)
- **Lenguaje**: Kotlin
- **SDK mínimo**: 23 (Android 6.0)
- **SDK objetivo**: 34 (Android 14)

#### Características:
- ✅ Autenticación de usuarios
- 🎤 Control por voz (reconocimiento de voz)
- 📱 Interfaz intuitiva con indicadores visuales
- 📊 Generación de reportes PDF
- 🔄 Sincronización en tiempo real con Arduino

#### Comandos de voz soportados:
- "encender led" / "apagar led"
- "encender motor" / "apagar motor"
- "aumentar velocidad" / "bajar velocidad"
- "cambiar giro"

### 3. Dispositivo Arduino (`/code`)
- **Placa**: ESP32
- **Librerías**: WiFi, HTTPClient, ArduinoJson, LiquidCrystal_I2C, Stepper, Keypad

#### Componentes conectados:
- 🔴 LED (Pin 2)
- 🔄 Motor paso a paso (Pines 16, 17, 18, 19)
- 📺 LCD I2C (0x27)
- ⌨️ Teclado matricial 4x4
- 📶 Conexión WiFi

#### Funcionalidades:
- Control manual mediante teclado
- Sincronización automática con servidor
- Verificación de autenticación
- Control de velocidad y dirección del motor

## 🛠️ Instalación y Configuración

### Prerrequisitos
- Java 17+
- MySQL 8.0+
- Android Studio
- Arduino IDE
- Maven 3.6+

### 1. Configuración del Backend

```bash
cd api
# Configurar application.properties con tus credenciales de MySQL
# Editar: spring.datasource.url, username, password

# Ejecutar migraciones de base de datos
mvn flyway:migrate

# Compilar y ejecutar
mvn clean install
mvn spring-boot:run
```

### 2. Configuración de la Aplicación Android

```bash
cd AmiwaMateo
# Abrir en Android Studio
# Sincronizar proyecto
# Cambiar IP del servidor en MainActivity.kt y LoginActivity.kt
# Compilar y instalar en dispositivo
```

### 3. Configuración del Arduino

```bash
cd code
# Abrir code.ino en Arduino IDE
# Instalar librerías requeridas:
# - WiFi (incluida)
# - HTTPClient (incluida)
# - ArduinoJson
# - LiquidCrystal_I2C
# - Keypad

# Configurar credenciales WiFi y IP del servidor
# Subir código a ESP32
```

## 📊 Base de Datos

### Tabla `login`
```sql
CREATE TABLE login(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(250),
    user VARCHAR(100),
    password VARCHAR(300)
);
```

### Tabla `components`
```sql
CREATE TABLE components (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    state_led BOOLEAN,
    state_motor BOOLEAN,
    speed INT,
    giro INT,
    fecha DATETIME
);
```

## 🔧 Configuración de Red

### Cambiar IP del servidor:
1. **Arduino**: Modificar `serverIP` en `code.ino`
2. **Android**: Modificar URLs en `MainActivity.kt` y `LoginActivity.kt`

### Configuración WiFi Arduino:
```cpp
const char* ssid = "Tu_WiFi";
const char* password = "Tu_Contraseña";
```

## 📱 Uso de la Aplicación

### Flujo de trabajo:
1. **Registro/Login**: Crear cuenta o iniciar sesión
2. **Control Manual**: Botones para LED y motor
3. **Control por Voz**: Presionar micrófono y dar comandos
4. **Reportes**: Descargar historial en PDF
5. **Logout**: Cerrar sesión segura

### Comandos de voz disponibles:
- "encender led" - Activa el LED
- "apagar led" - Desactiva el LED
- "encender motor" - Activa el motor
- "apagar motor" - Desactiva el motor
- "aumentar velocidad" - Incrementa velocidad (+5)
- "bajar velocidad" - Decrementa velocidad (-5)
- "cambiar giro" - Cambia dirección del motor

## 🎛️ Control Manual (Arduino)

### Teclado matricial:
- **Tecla 1**: Alternar LED
- **Tecla 2**: Alternar motor
- **Tecla B**: Cambiar a modo configuración
- **Tecla A**: Volver al modo principal

### Modo configuración (Tecla B):
- **Tecla 1**: Aumentar velocidad (+5)
- **Tecla 2**: Disminuir velocidad (-5) o cambiar giro
- **Tecla 0**: Volver al menú principal

## 📈 Características Técnicas

### Backend:
- ✅ API REST con Spring Boot
- ✅ Autenticación de usuarios
- ✅ Base de datos MySQL con JPA
- ✅ Migraciones automáticas con Flyway
- ✅ CORS configurado para desarrollo

### Android:
- ✅ Material Design
- ✅ Reconocimiento de voz nativo
- ✅ Generación de PDFs con iText
- ✅ Corrutinas para operaciones asíncronas
- ✅ SharedPreferences para persistencia

### Arduino:
- ✅ Conexión WiFi automática
- ✅ Sincronización bidireccional
- ✅ Control de motor paso a paso
- ✅ Interfaz LCD con menús
- ✅ Teclado matricial para control manual

## 🔒 Seguridad

- Autenticación requerida para todas las operaciones
- Sesiones de usuario gestionadas en el backend
- Validación de credenciales
- Logout automático en la aplicación móvil

## 📝 Reportes

La aplicación genera reportes PDF que incluyen:
- Historial completo de estados
- Fechas y horas de cada cambio
- Estados de LED y motor
- Configuraciones de velocidad y giro
- Descarga automática en carpeta Downloads

## 🐛 Solución de Problemas

### Problemas comunes:
1. **Arduino no se conecta**: Verificar credenciales WiFi
2. **App no conecta al servidor**: Verificar IP y puerto
3. **Base de datos**: Verificar credenciales MySQL
4. **Reconocimiento de voz**: Verificar permisos de micrófono

### Logs útiles:
- **Arduino**: Monitor Serie (115200 baud)
- **Backend**: Logs de Spring Boot
- **Android**: Logcat en Android Studio

## 👥 Contribuidores

- **Ariel** - Desarrollo principal


## 📄 Licencia

Este proyecto es de uso educativo y de desarrollo personal.

## 🔄 Próximas Mejoras

- [ ] Notificaciones push
- [ ] Historial de comandos de voz
- [ ] Configuración remota de WiFi
- [ ] Dashboard web
- [ ] Múltiples dispositivos
- [ ] Programación de tareas
- [ ] Alertas por email/SMS

---

**Nota**: Asegúrate de cambiar las IPs y credenciales antes de usar en producción.

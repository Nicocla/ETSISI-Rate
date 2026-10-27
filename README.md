# ETSISI Rate

**ETSISI Rate** es una aplicación Android desarrollada como proyecto académico para consultar y valorar al profesorado de la Escuela Técnica Superior de Ingeniería de Sistemas Informáticos (ETSISI) de la Universidad Politécnica de Madrid (UPM).

El sistema permite consultar profesores, publicar y editar valoraciones, visualizar reseñas y estadísticas, y realizar operaciones de administración según el rol del usuario. Su ampliación estudia la adaptación de la arquitectura a servicios cloud de **AWS** y **Microsoft Azure**, así como la evaluación comparativa de su funcionamiento.

> **Estado del proyecto:** proyecto académico en evolución. La disponibilidad de los despliegues cloud y de los servicios externos puede cambiar. Este repositorio no garantiza una instancia pública permanentemente activa.

## Características

- Inicio de sesión mediante correo institucional y código de un solo uso (OTP).
- Autenticación mediante tokens JWT para el acceso a rutas protegidas.
- Listado y consulta de profesores.
- Publicación y edición de valoraciones docentes.
- Consulta de reseñas, valoración propia e historial del usuario.
- Estadísticas por criterios de evaluación y visualización mediante gráfico radar.
- Funciones de administración para gestionar profesores y reseñas.
- Mejoras de experiencia de usuario: mensajes de error más claros, pantallas adaptadas a modo claro/oscuro y migración parcial a Jetpack Compose.

## Tecnologías

| Componente | Tecnologías |
| --- | --- |
| Aplicación móvil | Android nativo, Kotlin, Jetpack Compose (parcial), Retrofit |
| API y autenticación | Node.js, Express, JWT, Nodemailer (OTP por correo) |
| Persistencia local | MySQL |
| Infraestructura estudiada | AWS (Elastic Beanstalk y servicios de bases de datos gestionadas), Azure (App Service y Azure SQL Database) |
| Documentación y control de versiones | Git, GitHub, LaTeX |

## Arquitectura

La solución sigue una arquitectura cliente-servidor en tres capas:

1. **Presentación:** aplicación Android desde la que los usuarios consultan información y envían valoraciones.
2. **Lógica de negocio:** API REST con Node.js y Express, responsable de autenticación, validaciones, permisos y operaciones sobre profesores y reseñas.
3. **Persistencia:** base de datos relacional que almacena los datos del sistema.

En desarrollo local, la app se comunica con el backend ejecutado en el equipo de desarrollo. En la ampliación cloud se analiza el despliegue de los servicios en infraestructura remota para permitir el acceso desde dispositivos físicos.

## Organización del repositorio

La estructura exacta puede variar según la versión publicada. **Comprueba los nombres de las carpetas antes de utilizar los comandos de instalación.**

```text
ETSISI-Rate/
├── UPM-Rate/                 # Aplicación Android
├── tfg-backend/              # Backend original 
├── BD UPM Rate.sql           # Script SQL 
├── README.md
└── .gitignore
```

La estructura modular del backend incluye, según la versión:

```text
src/
├── app.js
├── server.js
├── config/
├── controllers/
├── middleware/
├── routes/
└── utils/
```

> Elimina del árbol las carpetas que no existan en el repositorio y verifica la ubicación real de `app.js`, `server.js` y el script SQL.

## Requisitos para ejecución local

- Git.
- Node.js y npm (utilizar una versión compatible con el `package.json` del backend).
- MySQL y un cliente de administración, por ejemplo MySQL Workbench, para la versión local que utiliza MySQL.
- Android Studio y el SDK requerido por el proyecto Android.
- Credenciales de una cuenta de correo habilitada para el envío de OTP, si se desea probar la autenticación por correo.

## Puesta en marcha del backend local

1. Clona el repositorio y accede a la carpeta del backend que desees ejecutar:

   ```bash
   git clone URL_DEL_REPOSITORIO
   cd ETSISI-Rate/tfg-backend
   ```

2. Instala las dependencias:

   ```bash
   npm install
   ```

3. Crea el archivo `.env` en la carpeta del backend tomando como referencia `.env.example`, si está disponible. A continuación se muestra un **ejemplo orientativo**; los nombres de las variables deben coincidir con los usados por el código:

   ```dotenv
   PORT=3000
   DB_HOST=localhost
   DB_USER=root
   DB_PASSWORD=CAMBIAR_POR_CONTRASENA_LOCAL
   DB_NAME=etsisi_rate
   EMAIL_USER=CUENTA_DE_PRUEBAS
   EMAIL_PASS=CLAVE_DE_APLICACION
   JWT_SECRET=GENERAR_UN_SECRETO_LARGO_Y_ALEATORIO
   CORS_ORIGIN=ORIGEN_PERMITIDO
   ```

   No subas `.env` ni ninguna credencial al repositorio. No copies literalmente los valores de ejemplo.

4. Inicializa la base de datos de acuerdo con el script SQL incluido y ajusta la conexión del backend. Consulta los nombres de esquema y tablas del archivo antes de importarlo.

5. Inicia el servidor con el script definido en `package.json`. Por ejemplo, si existe `start`:

   ```bash
   npm start
   ```

   En la configuración local habitual, el servicio se ejecuta en `http://localhost:3000/`.

## Ejecución de la aplicación Android

1. Abre la carpeta del proyecto Android (`UPM-Rate/`, si mantiene ese nombre) en Android Studio.
2. Espera a que finalice la sincronización de Gradle e instala los componentes del SDK que solicite el proyecto.
3. Comprueba la URL base de Retrofit antes de ejecutar la app:

   - **Emulador Android y backend en el ordenador:** `http://10.0.2.2:3000/`.
   - **Dispositivo físico y backend local:** dirección IP o nombre accesible desde el móvil, con la red y el firewall configurados.
   - **Backend desplegado en cloud:** URL HTTPS real del servicio publicado.

4. Ejecuta la aplicación en un emulador o dispositivo compatible.

> `10.0.2.2` es una dirección especial del emulador Android; no sirve como dirección del backend en un teléfono físico. Para producción, utiliza HTTPS y configura de forma adecuada la seguridad de red de Android.

## Datos y valoraciones

El sistema emplea una base de datos relacional para gestionar profesores, usuarios, roles y valoraciones. Las estadísticas se calculan a partir de los datos almacenados.

Para respetar la privacidad de los participantes, no se deben publicar correos institucionales, tokens, códigos OTP, datos personales ni conjuntos de valoraciones reales que permitan identificar a usuarios. Utiliza únicamente datos de prueba o datos debidamente autorizados y anonimizados.

## Autenticación y permisos

El acceso se inicia con un código OTP enviado por correo electrónico. Tras la verificación, el backend emite un JWT para las operaciones que requieran autenticación.

Las peticiones protegidas utilizan una cabecera como esta:

```http
Authorization: Bearer TOKEN_JWT
```

Las funciones de administración se restringen a cuentas con los permisos correspondientes. Las credenciales, secretos JWT y contraseñas de base de datos se configuran mediante variables de entorno, nunca dentro del código público.

## Endpoints de referencia

Las siguientes rutas proceden de la versión documentada del proyecto. **Verifica los nombres y métodos en los archivos de `routes/` de la versión publicada**, porque pueden variar tras la modularización.

| Área | Método | Ruta |
| --- | --- | --- |
| Autenticación | POST | `/enviar_codigo` |
| Autenticación | POST | `/verificar_codigo` |
| Profesores | GET | `/profesores` |
| Profesores (admin) | POST | `/anadir_profesor` |
| Profesores (admin) | DELETE | `/borrar_profesor/:id` |
| Valoraciones | POST | `/insertar_valoracion` |
| Reseñas | GET | `/resenas/:id` |
| Reseñas (admin) | DELETE | `/borrar_resena` |
| Usuario | GET | `/mi_valoracion` |
| Usuario | GET | `/mis_resenas` |
| Estadísticas | GET | `/estadisticas/:id` |
| Estadísticas | GET | `/estadisticas_radar/:id` |

## Ampliación y evaluación en la nube

La ampliación del TFG estudia la migración de servicios a entornos cloud, manteniendo la misma lógica de aplicación y comparando opciones de despliegue en dos proveedores:

- **AWS:** evaluación del despliegue del backend con Elastic Beanstalk y de alternativas de bases de datos gestionadas.
- **Microsoft Azure:** evaluación del backend en App Service y del almacenamiento mediante Azure SQL Database.

El trabajo contempla pruebas funcionales y de rendimiento, incluyendo escenarios de concurrencia, tiempos de respuesta y tasas de éxito. Los resultados concretos y las conclusiones deben consultarse en la memoria del TFG; no se incluyen cifras aquí sin verificarlas.

La configuración local con MySQL y la configuración de Azure SQL Database no son intercambiables de forma automática: cada entorno necesita su controlador, parámetros de conexión y esquema compatibles.

## Mejoras de la aplicación

Durante las revisiones del proyecto se llevaron a cabo, entre otras, las siguientes mejoras:

- Sustitución de mensajes Toast en errores importantes por diálogos o mensajes persistentes.
- Migración parcial de interfaces Android a Jetpack Compose.
- Reducción de XML y ConstraintLayout en pantallas seleccionadas.
- Adaptación de la interfaz a los modos claro y oscuro.
- Modularización del backend en rutas, controladores, middlewares, configuración y utilidades.
- Revisión de los middlewares de Express y de la gestión de errores.
- Incorporación de JWT y protección de endpoints sensibles.
- Reorganización del repositorio y exclusión de archivos sensibles del control de versiones.

## Capturas y documentación

Es recomendable incluir capturas reales de las pantallas de inicio de sesión, listado de profesores, detalle con gráfico radar y administración. Para incorporarlas, guarda imágenes sin información personal en una carpeta como `docs/imagenes/` y enlázalas desde este README.

La memoria del TFG contiene la descripción académica de la arquitectura, el desarrollo y la evaluación del sistema. Si está publicada, añade aquí su enlace definitivo.

## Seguridad del repositorio

Antes de publicar cambios:

- Comprueba que `.env`, `node_modules/`, archivos de compilación, `local.properties` y otros archivos generados no estén versionados.
- No incluyas secretos en capturas, logs o ejemplos.
- Revisa los permisos de las rutas administrativas y la validación de los datos recibidos.
- No presupongas que un despliegue de pruebas está preparado para producción sin revisar HTTPS, CORS, secretos y acceso a la base de datos.

## Finalidad académica

Este repositorio documenta un trabajo académico y sirve como muestra del desarrollo de aplicaciones Android, APIs REST, bases de datos relacionales y despliegue y evaluación de infraestructuras cloud.

## Licencia

**Pendiente de definir.** La publicación del código en GitHub no concede por sí misma una licencia de reutilización. Si deseas permitir su uso o modificación, añade un archivo `LICENSE` con una licencia apropiada.

## Autor

**Nicolás Clavo Collado**

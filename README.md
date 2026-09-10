\# ETSISI Rate



ETSISI Rate es una aplicación Android desarrollada como parte de un Trabajo Fin de Grado para la valoración de profesorado universitario de la ETSISI.



El sistema permite a los alumnos consultar profesores, visualizar valoraciones, añadir reseñas y consultar estadísticas asociadas a diferentes criterios docentes. Además, incluye un panel de administración para la gestión de profesores y reseñas.



\## Tecnologías utilizadas



\- Android nativo con Kotlin

\- Jetpack Compose

\- Node.js

\- Express

\- MySQL

\- Retrofit

\- JWT para autenticación

\- Nodemailer para envío de códigos OTP



\## Estructura del proyecto



```text

ETSISI-Rate/

├─ UPM-Rate/              Aplicación Android

├─ tfg-backend/           Backend Node.js/Express

├─ BD UPM Rate.sql        Script de base de datos

├─ README.md

└─ .gitignore

```



\## Aplicación Android



La aplicación Android está desarrollada en Kotlin. Varias pantallas han sido migradas a Jetpack Compose para modernizar la interfaz y mejorar la mantenibilidad del código.



Funcionalidades principales de la app:



\- Inicio de sesión mediante correo institucional.

\- Verificación mediante código OTP.

\- Consulta de profesores.

\- Consulta de reseñas.

\- Inserción y edición de valoraciones.

\- Visualización de estadísticas.

\- Panel de administración.

\- Eliminación de profesores y reseñas por parte del administrador.

\- Consulta del historial de reseñas del usuario.



Para conectar la app Android con el backend local desde el emulador se utiliza:



```kotlin

http://10.0.2.2:3000/

```



\## Backend



El backend está desarrollado con Node.js y Express. Se ha reorganizado siguiendo una estructura modular para mejorar la mantenibilidad del proyecto.



Estructura principal del backend:



```text

tfg-backend/

├─ src/

│  ├─ app.js

│  ├─ server.js

│  ├─ config/

│  ├─ controllers/

│  ├─ middleware/

│  ├─ routes/

│  └─ utils/

├─ package.json

├─ package-lock.json

└─ .env.example

```



\## Instalación del backend



Entrar en la carpeta del backend:



```bash

cd tfg-backend

```



Instalar dependencias:



```bash

npm install

```



Crear un archivo `.env` tomando como referencia `.env.example`.



Ejemplo de configuración:



```env

PORT=3000



DB\_HOST=localhost

DB\_USER=root

DB\_PASSWORD=tu\_password

DB\_NAME=etsisi\_rate



EMAIL\_USER=tu\_correo@gmail.com

EMAIL\_PASS=tu\_password\_de\_aplicacion



JWT\_SECRET=pon\_aqui\_tu\_clave\_jwt



CORS\_ORIGIN=\*

```



Arrancar el servidor:



```bash

npm start

```



Por defecto, el servidor se ejecuta en:



```text

http://localhost:3000

```



\## Base de datos



El proyecto utiliza una base de datos MySQL. El script de creación de la base de datos se encuentra en:



```text

BD UPM Rate.sql

```



La base de datos almacena información sobre profesores y valoraciones realizadas por los alumnos.



\## Autenticación y seguridad



El sistema utiliza autenticación mediante código OTP enviado al correo institucional del usuario.



Una vez verificado el código, el backend genera un token JWT. Este token se utiliza para autorizar las peticiones a endpoints protegidos.



Las rutas sensibles del backend requieren la cabecera:



```text

Authorization: Bearer <token>

```



Además, las operaciones administrativas están restringidas a usuarios con permisos de administrador.



\## Endpoints principales



\### Autenticación



```text

POST /enviar\_codigo

POST /verificar\_codigo

```



\### Profesores



```text

GET /profesores

POST /anadir\_profesor

DELETE /borrar\_profesor/:id

```



\### Valoraciones y reseñas



```text

POST /insertar\_valoracion

GET /resenas/:id

DELETE /borrar\_resena

GET /mi\_valoracion

GET /mis\_resenas

```



\### Estadísticas



```text

GET /estadisticas/:id

GET /estadisticas\_radar/:id

```



\## Mejoras realizadas tras la revisión



Durante la revisión del proyecto se aplicaron varias mejoras:



\- Eliminación de mensajes Toast para errores importantes.

\- Sustitución por diálogos y mensajes persistentes en formularios.

\- Migración parcial de interfaces a Jetpack Compose.

\- Reducción del uso de XML y ConstraintLayout en pantallas principales.

\- Modularización del backend en rutas, controladores, middlewares, configuración y utilidades.

\- Mejora del uso de Express mediante middlewares específicos.

\- Incorporación de autenticación con token JWT.

\- Protección de endpoints sensibles.

\- Mejora de la estructura del repositorio en GitHub.



\## Uso académico



Este proyecto ha sido desarrollado con fines académicos como parte de un Trabajo Fin de Grado.



\## Autor



Nicolas Clavo Collado


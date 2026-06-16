require('dotenv').config();
// 1. Importar las librerías
const express = require('express');
const mysql = require('mysql2');
const cors = require('cors');
const bodyParser = require('body-parser');
const nodemailer = require('nodemailer'); 

// 2. Configurar el servidor
const app = express();
app.use(cors());
// Aumentamos el límite por si envían comentarios muy largos
app.use(bodyParser.json({ limit: '10mb' }));
app.use(bodyParser.urlencoded({ extended: true, limit: '10mb' }));

// ---------------- CONFIGURACIÓN DE CORREO (NODEMAILER) ----------------
const transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: process.env.EMAIL_USER,
        pass: process.env.EMAIL_PASS
    }
});

// Memoria temporal para guardar los códigos 
const codigosVerificacion = {};

// ---------------- CONFIGURACIÓN DE APP ----------------

// LISTA DE PALABRAS PROHIBIDAS (Censura) 🤬
const PALABROTAS = ["mierda", "basura", "estupido", "idiota", "cabron", "tonto", "puta", "joder"];

// Función para limpiar comentarios
function censurarComentario(texto) {
    if (!texto) return "";
    let textoLimpio = texto;
    PALABROTAS.forEach(palabra => {
        const regex = new RegExp(palabra, "gi");
        textoLimpio = textoLimpio.replace(regex, "****");
    });
    return textoLimpio;
}

// 3. Crear la conexión con tu Base de Datos MySQL
const db = mysql.createConnection({
    host: process.env.DB_HOST,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME
});

// 4. Probar que la conexión funciona
db.connect(err => {
    if (err) {
        console.error('❌ Error conectando a la Base de Datos:', err);
        return;
    }
    console.log('✅ Conectado a MySQL correctamente');
});

// ---------------- RUTAS (ENDPOINTS) ----------------

// Ruta de prueba
app.get('/', (req, res) => {
    res.send('¡Hola! El servidor del TFG está funcionando 🚀');
});

// --- RUTA NUEVA A: ENVIAR CÓDIGO AL CORREO ---
app.post('/enviar_codigo', (req, res) => {
    const { email } = req.body;

    // Validación de seguridad básica
    if (!email) {
        return res.status(400).send("Falta el correo");
    }

    // 👑 ATAJO SECRETO PARA EL ADMINISTRADOR 👑
    if (email === "admin@upm.es") {
        codigosVerificacion[email] = "1234"; 
        console.log(`\n👑 MODO ADMIN DETECTADO: El código es 1234 (No se envía email)\n`);
        return res.json({ mensaje: "Modo admin activado. Usa tu código maestro." });
    }

    // Validación para el resto de alumnos
    if (!email.endsWith("@alumnos.upm.es") && !email.endsWith("@upm.es")) {
        return res.status(400).send("Error: Usa un correo @alumnos.upm.es");
    }

    // Generar código aleatorio de 4 números
    const codigoGenerado = Math.floor(1000 + Math.random() * 9000).toString();
    
    // Guardarlo en memoria
    codigosVerificacion[email] = codigoGenerado;

    // ¡EL CHIVATO! Imprimirlo en la consola (cmd) para pruebas rápidas
    console.log(`\n======================================`);
    console.log(`🔑 CÓDIGO PARA ${email}: ${codigoGenerado}`);
    console.log(`======================================\n`);

    // Enviar el correo real
    const mailOptions = {
        from: process.env.EMAIL_USER, // <-- PON TU GMAIL AQUÍ TAMBIÉN
        to: email,
        subject: 'Tu código de acceso - ETSISI Rate',
        text: `¡Hola!\n\nTu código para iniciar sesión en ETSISI Rate es: ${codigoGenerado}\n\nEste código es de un solo uso.`
    };

    transporter.sendMail(mailOptions, (error, info) => {
        if (error) {
            console.error("⚠️ Aviso: No se pudo enviar el correo real (revisa credenciales), pero el código está en la consola.", error);
            return res.json({ mensaje: "El servidor de correo falló, pero lee la consola." });
        }
        res.json({ mensaje: "Correo enviado correctamente" });
    });
});

// --- RUTA NUEVA B: COMPROBAR CÓDIGO ---
app.post('/verificar_codigo', (req, res) => {
    const { email, codigo } = req.body;

    if (codigosVerificacion[email] && codigosVerificacion[email] === codigo) {
        delete codigosVerificacion[email]; // Borrar por seguridad
        console.log(`✅ Correo ${email} verificado con éxito.`);
        res.json({ verificado: true, mensaje: "Código correcto" });
    } else {
        console.log(`❌ Fallo de verificación para ${email}.`);
        res.status(400).json({ verificado: false, mensaje: "Código incorrecto o caducado" });
    }
});

// --- RUTA 1: OBTENER PROFESORES (GET) ---
app.get('/profesores', (req, res) => {
    const sql = `
        SELECT p.*, 
               COALESCE(AVG(v.puntuacion), 0) as media 
        FROM profesores p 
        LEFT JOIN valoraciones v ON p.id = v.profesor_id 
        GROUP BY p.id
    `;
    db.query(sql, (err, results) => {
        if (err) {
            console.error("❌ Error leyendo profesores:", err);
            return res.status(500).send(err);
        }
        res.json(results);
    });
});

// --- RUTA 2: GUARDAR VALORACIÓN DETALLADA (POST) ---
app.post('/insertar_valoracion', (req, res) => {
    const { 
        profesor_id, email_alumno, comentario,
        n1, n2, n3, n4, n5
    } = req.body;

    console.log("📥 Recibiendo valoración detallada:", req.body); 

    // Aquí dejamos pasar al admin también por si quiere comentar, o filtramos
    if (email_alumno !== "admin@upm.es" && !email_alumno.endsWith("@alumnos.upm.es") && !email_alumno.endsWith("@upm.es")) {
         return res.status(400).send("Error: Debes usar un correo @alumnos.upm.es");
    }

    if (!profesor_id) {
        return res.status(400).send("Falta el ID del profesor");
    }

    const val1 = parseFloat(n1) || 0;
    const val2 = parseFloat(n2) || 0;
    const val3 = parseFloat(n3) || 0;
    const val4 = parseFloat(n4) || 0;
    const val5 = parseFloat(n5) || 0;

    let promedio = 0;
    if (val1 > 0) {
        promedio = (val1 + val2 + val3 + val4 + val5) / 5;
    } else {
        promedio = parseFloat(req.body.puntuacion) || 0;
    }

    const comentarioLimpio = censurarComentario(comentario);

    const sql = `
        INSERT INTO valoraciones 
        (profesor_id, email_alumno, puntuacion, comentario, nota_horario, nota_material, nota_atencion, nota_tutorias, nota_guia)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE 
        puntuacion = VALUES(puntuacion),
        comentario = VALUES(comentario),
        nota_horario = VALUES(nota_horario),
        nota_material = VALUES(nota_material),
        nota_atencion = VALUES(nota_atencion),
        nota_tutorias = VALUES(nota_tutorias),
        nota_guia = VALUES(nota_guia)
    `;

    const valores = [profesor_id, email_alumno, promedio, comentarioLimpio, val1, val2, val3, val4, val5];

    db.query(sql, valores, (err, result) => {
        if (err) {
            console.error("❌ Error guardando valoración:", err);
            return res.status(500).send("Error en el servidor");
        }
        console.log(`✅ Valoración guardada (Media: ${promedio.toFixed(1)}) para Profe ID ${profesor_id}`);
        res.send("Valoración guardada correctamente");
    });
});

// --- RUTA 3: CREAR NUEVO PROFESOR (SOLO ADMIN) ---
app.post('/anadir_profesor', (req, res) => {
    const { nombre, departamento } = req.body;

    if (!nombre || !departamento) {
        return res.status(400).send("Faltan datos obligatorios");
    }

    const sql = "INSERT INTO profesores (nombre, departamento) VALUES (?, ?)";

    db.query(sql, [nombre, departamento], (err, resultado) => {
        if (err) {
            console.error("❌ Error creando profesor:", err);
            return res.status(500).json({ 
                mensaje: "Error al crear profesor" 
            });
        }

        console.log(`✅ Nuevo profesor creado en MySQL: ${nombre}`);

        res.json({ 
            mensaje: "Profesor añadido correctamente",
            id: resultado.insertId
        });
    });
});

// --- RUTA 4: BORRAR PROFESOR (DELETE) ---
app.delete('/borrar_profesor/:id', (req, res) => {
    const id = req.params.id;

    db.query('DELETE FROM valoraciones WHERE profesor_id = ?', [id], (err) => {
        if (err) {
            console.error("❌ Error borrando valoraciones:", err);
            return res.status(500).send("Error al borrar valoraciones");
        }
        
        db.query('DELETE FROM profesores WHERE id = ?', [id], (err, result) => {
            if (err) {
                console.error("❌ Error borrando profesor:", err);
                return res.status(500).send("Error al borrar profesor");
            }
            console.log(`🗑️ Profesor ID ${id} eliminado.`);
            res.send("Profesor eliminado correctamente");
        });
    });
});

// --- RUTA 5: OBTENER ESTADÍSTICAS (GRÁFICA) ---
app.get('/estadisticas/:id', (req, res) => {
    const id = req.params.id;
    const sql = `
        SELECT FLOOR(puntuacion) as puntuacion, COUNT(*) as cantidad 
        FROM valoraciones 
        WHERE profesor_id = ? 
        GROUP BY FLOOR(puntuacion)
    `;

    db.query(sql, [id], (err, results) => {
        if (err) {
            console.error("❌ Error estadísticas:", err);
            return res.status(500).send("Error al obtener estadísticas");
        }
        res.json(results);
    });
});

// --- RUTA 6: VER TODAS LAS RESEÑAS DE UN PROFESOR ---
app.get('/resenas/:id', (req, res) => {
    const id = req.params.id;
    const sql = "SELECT v.* FROM valoraciones v WHERE profesor_id = ?";
    
    db.query(sql, [id], (err, results) => {
        if (err) {
            console.error("❌ Error leyendo reseñas:", err);
            return res.status(500).send("Error del servidor");
        }
        res.json(results);
    });
});

// --- RUTA 7: BORRAR UNA RESEÑA ---
app.delete('/borrar_resena', (req, res) => {
    const { profesor_id, email_alumno } = req.query;

    const sql = "DELETE FROM valoraciones WHERE profesor_id = ? AND email_alumno = ?";
    
    db.query(sql, [profesor_id, email_alumno], (err, result) => {
        if (err) {
            console.error("❌ Error borrando reseña:", err);
            return res.status(500).send("Error al borrar");
        }
        console.log(`🗑️ Reseña de ${email_alumno} eliminada.`);
        res.send("Reseña eliminada");
    });
});

// --- RUTA 8: VER MI VALORACIÓN COMPLETA (PARA EDITAR) ---
app.get('/mi_valoracion', (req, res) => {
    const { profesor_id, email_alumno } = req.query;

    const sql = "SELECT * FROM valoraciones WHERE profesor_id = ? AND email_alumno = ?";
    
    db.query(sql, [profesor_id, email_alumno], (err, results) => {
        if (err) return res.status(500).send("Error");
        
        if (results.length > 0) {
            res.json(results[0]);
        } else {
            res.json(null); 
        }
    });
});

// --- RUTA 9: VER TODAS MIS RESEÑAS (HISTORIAL) ---
app.get('/mis_resenas', (req, res) => {
    const email = req.query.email; 
    console.log("🧐 Buscando reseñas para el email:", email); 

    const sql = `
        SELECT p.id as profesor_id, p.nombre as nombre_profesor, v.puntuacion, v.comentario 
        FROM valoraciones v
        JOIN profesores p ON v.profesor_id = p.id
        WHERE v.email_alumno = ?
    `;
    
    db.query(sql, [email], (err, results) => {
        if (err) {
            console.error("❌ Error buscando mis reseñas:", err);
            return res.status(500).send("Error del servidor");
        }
        res.json(results);
    });
});

// --- RUTA 10: ESTADÍSTICAS PARA RADAR (PROMEDIOS DETALLADOS) ---
app.get('/estadisticas_radar/:id', (req, res) => {
    const id = req.params.id;
    const sql = `
        SELECT 
            COALESCE(AVG(nota_horario), 0) as avg_horario,
            COALESCE(AVG(nota_material), 0) as avg_material,
            COALESCE(AVG(nota_atencion), 0) as avg_atencion,
            COALESCE(AVG(nota_tutorias), 0) as avg_tutorias,
            COALESCE(AVG(nota_guia), 0) as avg_guia
        FROM valoraciones 
        WHERE profesor_id = ?
    `;

    db.query(sql, [id], (err, results) => {
        if (err) {
            console.error("❌ Error estadísticas radar:", err);
            return res.status(500).send("Error servidor");
        }
        res.json(results[0]);
    });
});

// ---------------- ARRANCAR SERVIDOR ----------------
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`🚀 Servidor corriendo en http://localhost:${PORT}`);
});
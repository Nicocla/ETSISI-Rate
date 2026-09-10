const db = require('../config/db');
const { censurarComentario } = require('../utils/censura');

function insertarValoracion(req, res) {
    const {
        profesor_id,
        comentario,
        n1,
        n2,
        n3,
        n4,
        n5
    } = req.body;

    const email_alumno = req.usuario.email;

    console.log("Recibiendo valoración detallada:", req.body);

    if (!profesor_id) {
        return res.status(400).json({ mensaje: "Falta el ID del profesor" });
    }

    const val1 = parseFloat(n1) || 0;
    const val2 = parseFloat(n2) || 0;
    const val3 = parseFloat(n3) || 0;
    const val4 = parseFloat(n4) || 0;
    const val5 = parseFloat(n5) || 0;

    if (val1 === 0 || val2 === 0 || val3 === 0 || val4 === 0 || val5 === 0) {
        return res.status(400).json({
            mensaje: "Todas las notas son obligatorias"
        });
    }

    const promedio = (val1 + val2 + val3 + val4 + val5) / 5;
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

    const valores = [
        profesor_id,
        email_alumno,
        promedio,
        comentarioLimpio,
        val1,
        val2,
        val3,
        val4,
        val5
    ];

    db.query(sql, valores, (err) => {
        if (err) {
            console.error("Error guardando valoración:", err);
            return res.status(500).json({ mensaje: "Error al guardar valoración" });
        }

        console.log(`Valoración guardada. Media: ${promedio.toFixed(1)}. Profesor ID: ${profesor_id}`);
        res.json({ mensaje: "Valoración guardada correctamente" });
    });
}

function obtenerMiValoracion(req, res) {
    const { profesor_id } = req.query;
    const email_alumno = req.usuario.email;

    if (!profesor_id) {
        return res.status(400).json({ mensaje: "Falta el ID del profesor" });
    }

    const sql = "SELECT * FROM valoraciones WHERE profesor_id = ? AND email_alumno = ?";

    db.query(sql, [profesor_id, email_alumno], (err, results) => {
        if (err) {
            console.error("Error obteniendo mi valoración:", err);
            return res.status(500).json({ mensaje: "Error al obtener valoración" });
        }

        if (results.length > 0) {
            res.json(results[0]);
        } else {
            res.json(null);
        }
    });
}

module.exports = {
    insertarValoracion,
    obtenerMiValoracion
};
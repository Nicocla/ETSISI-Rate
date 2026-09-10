const db = require('../config/db');

function obtenerResenasProfesor(req, res) {
    const id = req.params.id;

    const sql = "SELECT v.* FROM valoraciones v WHERE profesor_id = ?";

    db.query(sql, [id], (err, results) => {
        if (err) {
            console.error("Error leyendo reseñas:", err);
            return res.status(500).json({ mensaje: "Error al obtener reseñas" });
        }

        res.json(results);
    });
}

function borrarResena(req, res) {
    const { profesor_id, email_alumno } = req.query;

    if (!profesor_id || !email_alumno) {
        return res.status(400).json({
            mensaje: "Faltan datos para borrar la reseña"
        });
    }

    const usuarioToken = req.usuario;
    const esAdmin = usuarioToken.esAdmin;
    const emailUsuario = usuarioToken.email;

    if (!esAdmin && email_alumno !== emailUsuario) {
        return res.status(403).json({
            mensaje: "No puedes borrar una reseña que no es tuya"
        });
    }

    const sql = "DELETE FROM valoraciones WHERE profesor_id = ? AND email_alumno = ?";

    db.query(sql, [profesor_id, email_alumno], (err) => {
        if (err) {
            console.error("Error borrando reseña:", err);
            return res.status(500).json({ mensaje: "Error al borrar reseña" });
        }

        console.log(`Reseña de ${email_alumno} eliminada.`);
        res.json({ mensaje: "Reseña eliminada" });
    });
}

function obtenerMisResenas(req, res) {
    const email = req.usuario.email;

    console.log("Buscando reseñas para el email:", email);

    const sql = `
        SELECT p.id as profesor_id, p.nombre as nombre_profesor, v.puntuacion, v.comentario 
        FROM valoraciones v
        JOIN profesores p ON v.profesor_id = p.id
        WHERE v.email_alumno = ?
    `;

    db.query(sql, [email], (err, results) => {
        if (err) {
            console.error("Error buscando mis reseñas:", err);
            return res.status(500).json({ mensaje: "Error al obtener mis reseñas" });
        }

        res.json(results);
    });
}

module.exports = {
    obtenerResenasProfesor,
    borrarResena,
    obtenerMisResenas
};
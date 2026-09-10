const db = require('../config/db');

function obtenerProfesores(req, res) {
    const sql = `
        SELECT p.*, 
               COALESCE(AVG(v.puntuacion), 0) as media 
        FROM profesores p 
        LEFT JOIN valoraciones v ON p.id = v.profesor_id 
        GROUP BY p.id
    `;

    db.query(sql, (err, results) => {
        if (err) {
            console.error("Error leyendo profesores:", err);
            return res.status(500).json({ mensaje: "Error al obtener profesores" });
        }

        res.json(results);
    });
}

function anadirProfesor(req, res) {
    const { nombre, departamento } = req.body;

    if (!nombre || !departamento) {
        return res.status(400).json({ mensaje: "Faltan datos obligatorios" });
    }

    const sql = "INSERT INTO profesores (nombre, departamento) VALUES (?, ?)";

    db.query(sql, [nombre, departamento], (err, resultado) => {
        if (err) {
            console.error("Error creando profesor:", err);
            return res.status(500).json({ mensaje: "Error al crear profesor" });
        }

        console.log(`Nuevo profesor creado en MySQL: ${nombre}`);

        res.json({
            mensaje: "Profesor añadido correctamente",
            id: resultado.insertId
        });
    });
}

function borrarProfesor(req, res) {
    const id = req.params.id;

    db.query('DELETE FROM valoraciones WHERE profesor_id = ?', [id], (err) => {
        if (err) {
            console.error("Error borrando valoraciones:", err);
            return res.status(500).json({ mensaje: "Error al borrar valoraciones" });
        }

        db.query('DELETE FROM profesores WHERE id = ?', [id], (err) => {
            if (err) {
                console.error("Error borrando profesor:", err);
                return res.status(500).json({ mensaje: "Error al borrar profesor" });
            }

            console.log(`Profesor ID ${id} eliminado.`);
            res.json({ mensaje: "Profesor eliminado correctamente" });
        });
    });
}

function obtenerEstadisticas(req, res) {
    const id = req.params.id;

    const sql = `
        SELECT FLOOR(puntuacion) as puntuacion, COUNT(*) as cantidad 
        FROM valoraciones 
        WHERE profesor_id = ? 
        GROUP BY FLOOR(puntuacion)
    `;

    db.query(sql, [id], (err, results) => {
        if (err) {
            console.error("Error estadísticas:", err);
            return res.status(500).json({ mensaje: "Error al obtener estadísticas" });
        }

        res.json(results);
    });
}

function obtenerEstadisticasRadar(req, res) {
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
            console.error("Error estadísticas radar:", err);
            return res.status(500).json({ mensaje: "Error al obtener estadísticas radar" });
        }

        res.json(results[0]);
    });
}

module.exports = {
    obtenerProfesores,
    anadirProfesor,
    borrarProfesor,
    obtenerEstadisticas,
    obtenerEstadisticasRadar
};
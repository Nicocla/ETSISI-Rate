const express = require('express');
const router = express.Router();

const {
    obtenerProfesores,
    anadirProfesor,
    borrarProfesor,
    obtenerEstadisticas,
    obtenerEstadisticasRadar
} = require('../controllers/profesores.controller');

const {
    verificarToken,
    soloAdmin
} = require('../middleware/auth.middleware');

router.get('/profesores', obtenerProfesores);

// Solo admin
router.post('/anadir_profesor', verificarToken, soloAdmin, anadirProfesor);
router.delete('/borrar_profesor/:id', verificarToken, soloAdmin, borrarProfesor);

// Públicas
router.get('/estadisticas/:id', obtenerEstadisticas);
router.get('/estadisticas_radar/:id', obtenerEstadisticasRadar);

module.exports = router;
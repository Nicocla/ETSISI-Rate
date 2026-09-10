const express = require('express');
const router = express.Router();

const {
    obtenerResenasProfesor,
    borrarResena,
    obtenerMisResenas
} = require('../controllers/resenas.controller');

const {
    verificarToken
} = require('../middleware/auth.middleware');

// Pública
router.get('/resenas/:id', obtenerResenasProfesor);

// Requieren sesión
router.delete('/borrar_resena', verificarToken, borrarResena);
router.get('/mis_resenas', verificarToken, obtenerMisResenas);

module.exports = router;
const express = require('express');
const router = express.Router();

const {
    insertarValoracion,
    obtenerMiValoracion
} = require('../controllers/valoraciones.controller');

const {
    verificarToken
} = require('../middleware/auth.middleware');

// Requieren sesión
router.post('/insertar_valoracion', verificarToken, insertarValoracion);
router.get('/mi_valoracion', verificarToken, obtenerMiValoracion);

module.exports = router;
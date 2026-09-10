const express = require('express');
const router = express.Router();

const {
    enviarCodigo,
    verificarCodigo
} = require('../controllers/auth.controller');

router.post('/enviar_codigo', enviarCodigo);
router.post('/verificar_codigo', verificarCodigo);

module.exports = router;
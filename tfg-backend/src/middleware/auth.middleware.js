const jwt = require('jsonwebtoken');

function verificarToken(req, res, next) {
    const authHeader = req.headers.authorization;

    if (!authHeader || !authHeader.startsWith("Bearer ")) {
        return res.status(401).json({
            mensaje: "Acceso denegado. Token no proporcionado."
        });
    }

    const token = authHeader.split(" ")[1];

    try {
        const usuario = jwt.verify(token, process.env.JWT_SECRET);
        req.usuario = usuario;
        next();
    } catch (error) {
        return res.status(401).json({
            mensaje: "Token inválido o caducado."
        });
    }
}

function soloAdmin(req, res, next) {
    if (!req.usuario || !req.usuario.esAdmin) {
        return res.status(403).json({
            mensaje: "Acceso reservado al administrador."
        });
    }

    next();
}

module.exports = {
    verificarToken,
    soloAdmin
};
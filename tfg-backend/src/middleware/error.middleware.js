function errorHandler(err, req, res, next) {
    console.error("Error interno:", err);

    res.status(500).json({
        mensaje: "Error interno del servidor"
    });
}

module.exports = errorHandler; 
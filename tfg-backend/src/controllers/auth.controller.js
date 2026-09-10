const jwt = require('jsonwebtoken');
const transporter = require('../utils/mailer');

const codigosVerificacion = {};

function generarToken(email) {
    const esAdmin = email === "admin" || email === "admin@upm.es" || email.includes("admin");

    return jwt.sign(
        {
            email: email,
            esAdmin: esAdmin
        },
        process.env.JWT_SECRET,
        {
            expiresIn: "2h"
        }
    );
}

function enviarCodigo(req, res) {
    const { email } = req.body;

    if (!email) {
        return res.status(400).json({ mensaje: "Falta el correo" });
    }

    if (email === "admin" || email === "admin@upm.es") {
        codigosVerificacion[email] = "1234";
        console.log("Modo admin detectado. Código: 1234");
        return res.json({ mensaje: "Modo admin activado" });
    }

    if (!email.endsWith("@alumnos.upm.es") && !email.endsWith("@upm.es")) {
        return res.status(400).json({ mensaje: "Usa un correo institucional de la UPM" });
    }

    const codigoGenerado = Math.floor(1000 + Math.random() * 9000).toString();
    codigosVerificacion[email] = codigoGenerado;

    console.log("======================================");
    console.log(`CÓDIGO PARA ${email}: ${codigoGenerado}`);
    console.log("======================================");

    const mailOptions = {
        from: process.env.EMAIL_USER,
        to: email,
        subject: 'Tu código de acceso - ETSISI Rate',
        text: `Hola.\n\nTu código para iniciar sesión en ETSISI Rate es: ${codigoGenerado}\n\nEste código es de un solo uso.`
    };

    transporter.sendMail(mailOptions, (error) => {
        if (error) {
            console.error("No se pudo enviar el correo real, pero el código está en consola:", error);
            return res.json({ mensaje: "El correo falló, pero el código está en consola" });
        }

        res.json({ mensaje: "Correo enviado correctamente" });
    });
}

function verificarCodigo(req, res) {
    const { email, codigo } = req.body;

    if (!email || !codigo) {
        return res.status(400).json({ mensaje: "Faltan datos" });
    }

    if (codigosVerificacion[email] && codigosVerificacion[email] === codigo) {
        delete codigosVerificacion[email];

        const esAdmin = email === "admin" || email === "admin@upm.es" || email.includes("admin");
        const token = generarToken(email);

        console.log(`Correo ${email} verificado con éxito.`);

        return res.json({
            verificado: true,
            mensaje: "Código correcto",
            token: token,
            email: email,
            esAdmin: esAdmin
        });
    }

    console.log(`Fallo de verificación para ${email}.`);

    return res.status(400).json({
        verificado: false,
        mensaje: "Código incorrecto o caducado"
    });
}

module.exports = {
    enviarCodigo,
    verificarCodigo
};
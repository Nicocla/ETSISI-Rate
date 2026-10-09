require('dotenv').config();

const mysql = require('mysql2');

const db = mysql.createPool({
    host: process.env.DB_HOST,
    port: Number(process.env.DB_PORT) || 3306,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,

    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0,

    enableKeepAlive: true,
    keepAliveInitialDelay: 0,

    ssl: process.env.DB_SSL === 'true'
        ? {
            rejectUnauthorized: false
        }
        : undefined
});

// Comprobación inicial de conexión
db.getConnection((err, connection) => {
    if (err) {
        console.error('Error conectando a la Base de Datos:', err);
        return;
    }

    console.log('Pool de MySQL conectado correctamente');
    connection.release();
});

module.exports = db;
const express = require('express');
const cors = require('cors');

const authRoutes = require('./routes/auth.routes');
const profesoresRoutes = require('./routes/profesores.routes');
const valoracionesRoutes = require('./routes/valoraciones.routes');
const resenasRoutes = require('./routes/resenas.routes');

const notFoundHandler = require('./middleware/notFound.middleware');
const errorHandler = require('./middleware/error.middleware');

const app = express();

app.use(cors({
    origin: process.env.CORS_ORIGIN || '*',
    methods: ['GET', 'POST', 'PUT', 'PATCH', 'DELETE'],
    allowedHeaders: ['Content-Type', 'Authorization']
}));

app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

app.get('/', (req, res) => {
    res.json({
        mensaje: 'Servidor del TFG funcionando'
    });
});

app.use('/', authRoutes);
app.use('/', profesoresRoutes);
app.use('/', valoracionesRoutes);
app.use('/', resenasRoutes);

app.use(notFoundHandler);
app.use(errorHandler);

module.exports = app;
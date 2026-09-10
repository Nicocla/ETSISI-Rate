const PALABROTAS = [
    "mierda",
    "basura",
    "estupido",
    "idiota",
    "cabron",
    "tonto",
    "puta",
    "joder"
];

function censurarComentario(texto) {
    if (!texto) return "";

    let textoLimpio = texto;

    PALABROTAS.forEach(palabra => {
        const regex = new RegExp(palabra, "gi");
        textoLimpio = textoLimpio.replace(regex, "****");
    });

    return textoLimpio;
}

module.exports = {
    censurarComentario
};
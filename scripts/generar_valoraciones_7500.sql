INSERT INTO valoraciones (
    profesor_id,
    email_alumno,
    puntuacion,
    comentario,
    nota_horario,
    nota_material,
    nota_atencion,
    nota_tutorias,
    nota_guia
)

WITH RECURSIVE
decenas AS (
    SELECT 0 AS n
    UNION ALL
    SELECT n + 1
    FROM decenas
    WHERE n < 74
),

unidades AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1
    FROM unidades
    WHERE n < 100
),

numeros AS (
    SELECT (d.n * 100) + u.n AS n
    FROM decenas d
    CROSS JOIN unidades u
),

datos AS (
    SELECT
        n,

        CASE
            /* Profesores con mayor número de valoraciones */
            WHEN MOD(n * 37, 100) < 6 THEN 1
            WHEN MOD(n * 37, 100) < 11 THEN 2
            WHEN MOD(n * 37, 100) < 16 THEN 3
            WHEN MOD(n * 37, 100) < 21 THEN 4
            WHEN MOD(n * 37, 100) < 25 THEN 5

            /* Resto de profesores */
            ELSE 17 + MOD(n * 19, 35)
        END AS profesor_id,

        ROUND(1 + MOD(n * 31, 91) / 10, 1) AS horario,
        ROUND(1 + MOD(n * 41, 91) / 10, 1) AS material,
        ROUND(1 + MOD(n * 47, 91) / 10, 1) AS atencion,
        ROUND(1 + MOD(n * 53, 91) / 10, 1) AS tutorias,
        ROUND(1 + MOD(n * 59, 91) / 10, 1) AS guia

    FROM numeros
)

SELECT
    profesor_id,

    CONCAT(
        ELT(
            1 + MOD(n * 7, 20),
            'alejandro','lucia','carlos','marta','david',
            'laura','pablo','elena','sergio','paula',
            'adrian','irene','alvaro','claudia','marcos',
            'silvia','daniel','patricia','hugo','beatriz'
        ),
        '.',
        ELT(
            1 + MOD(n * 11, 20),
            'garcia','martinez','lopez','sanchez','perez',
            'gomez','fernandez','moreno','jimenez','romero',
            'navarro','torres','ruiz','diaz','vega',
            'castro','ortega','molina','ramos','gil'
        ),

        /* Numeración nueva para evitar colisiones */
        LPAD(10000 + n, 5, '0'),

        '@alumnos.upm.es'
    ),

    ROUND(
        (horario + material + atencion + tutorias + guia) / 5,
        1
    ),

    CONCAT(
        CASE
            WHEN (horario + material + atencion + tutorias + guia) / 5 >= 8 THEN
                ELT(
                    1 + MOD(n, 5),
                    'Muy buena experiencia con el profesor. ',
                    'La experiencia general ha sido excelente. ',
                    'Estoy muy satisfecho con la asignatura. ',
                    'El profesor ha realizado un trabajo muy bueno. ',
                    'Valoracion muy positiva en terminos generales. '
                )

            WHEN (horario + material + atencion + tutorias + guia) / 5 >= 6 THEN
                ELT(
                    1 + MOD(n, 5),
                    'La experiencia ha sido bastante buena. ',
                    'En general estoy satisfecho con la asignatura. ',
                    'El profesor cumple correctamente con su labor. ',
                    'La valoracion general es positiva. ',
                    'Una experiencia correcta con algunos aspectos mejorables. '
                )

            WHEN (horario + material + atencion + tutorias + guia) / 5 >= 4 THEN
                ELT(
                    1 + MOD(n, 5),
                    'La experiencia ha sido irregular. ',
                    'Hay aspectos positivos y otros que deberian mejorar. ',
                    'La asignatura ha tenido resultados aceptables. ',
                    'Mi experiencia ha sido bastante intermedia. ',
                    'El profesor cumple, aunque existen varios puntos mejorables. '
                )

            ELSE
                ELT(
                    1 + MOD(n, 5),
                    'La experiencia no ha sido demasiado positiva. ',
                    'Hay bastantes aspectos que deberian mejorar. ',
                    'La asignatura me ha resultado poco satisfactoria. ',
                    'Considero que existen importantes puntos de mejora. ',
                    'Mi valoracion general de la experiencia es baja. '
                )
        END,

        CASE MOD(n, 5)

            WHEN 0 THEN
                CASE
                    WHEN horario >= 7 THEN
                        'El horario y la organizacion han funcionado especialmente bien. '
                    WHEN horario >= 5 THEN
                        'La organizacion de los horarios ha sido aceptable. '
                    ELSE
                        'La organizacion de los horarios podria mejorar bastante. '
                END

            WHEN 1 THEN
                CASE
                    WHEN material >= 7 THEN
                        'El material proporcionado resulta completo y util. '
                    WHEN material >= 5 THEN
                        'El material disponible es suficiente para seguir la asignatura. '
                    ELSE
                        'El material proporcionado resulta escaso o poco claro. '
                END

            WHEN 2 THEN
                CASE
                    WHEN atencion >= 7 THEN
                        'La atencion al alumnado ha sido uno de los puntos fuertes. '
                    WHEN atencion >= 5 THEN
                        'La atencion al alumno ha sido correcta. '
                    ELSE
                        'La atencion al alumnado deberia ser mas cercana y constante. '
                END

            WHEN 3 THEN
                CASE
                    WHEN tutorias >= 7 THEN
                        'Las tutorias han resultado accesibles y utiles. '
                    WHEN tutorias >= 5 THEN
                        'Las tutorias cumplen su funcion adecuadamente. '
                    ELSE
                        'Seria conveniente mejorar la disponibilidad y utilidad de las tutorias. '
                END

            ELSE
                CASE
                    WHEN guia >= 7 THEN
                        'La guia docente esta bien estructurada y se corresponde con el desarrollo del curso. '
                    WHEN guia >= 5 THEN
                        'La guia docente resulta razonablemente clara. '
                    ELSE
                        'La guia docente podria reflejar mejor el desarrollo real de la asignatura. '
                END
        END,

        ELT(
            1 + MOD(n * 13, 7),
            'En conjunto, repetiria con este profesor.',
            'Hay margen de mejora, pero la experiencia ha sido razonable.',
            'La valoracion refleja bastante bien mi experiencia durante el curso.',
            'Estos aspectos han condicionado mi opinion final de la asignatura.',
            'Espero que estos puntos se tengan en cuenta en proximos cursos.',
            'Mi valoracion final tiene en cuenta el conjunto de la asignatura.',
            'En general, estos han sido los aspectos que mas han influido en mi experiencia.'
        )
    ),

    horario,
    material,
    atencion,
    tutorias,
    guia

FROM datos;
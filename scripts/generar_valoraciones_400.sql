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

WITH RECURSIVE numeros AS (
    SELECT 1 AS n

    UNION ALL

    SELECT n + 1
    FROM numeros
    WHERE n < 400
),

datos AS (
    SELECT
        n,

        CASE
            WHEN MOD(n * 37, 100) < 6 THEN 1
            WHEN MOD(n * 37, 100) < 11 THEN 2
            WHEN MOD(n * 37, 100) < 16 THEN 3
            WHEN MOD(n * 37, 100) < 21 THEN 4
            WHEN MOD(n * 37, 100) < 25 THEN 5
            ELSE 17 + MOD(n * 19, 35)
        END AS profesor_id,

        ROUND(1 + MOD(n * 17, 91) / 10, 1) AS horario,
        ROUND(1 + MOD(n * 23, 91) / 10, 1) AS material,
        ROUND(1 + MOD(n * 29, 91) / 10, 1) AS atencion,
        ROUND(1 + MOD(n * 37, 91) / 10, 1) AS tutorias,
        ROUND(1 + MOD(n * 43, 91) / 10, 1) AS guia

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
        LPAD(6000 + n, 4, '0'),
        '@alumnos.upm.es'
    ) AS email_alumno,

    ROUND(
        (horario + material + atencion + tutorias + guia) / 5,
        1
    ) AS puntuacion,

    CONCAT(
        CASE
            WHEN (horario + material + atencion + tutorias + guia) / 5 >= 8 THEN
                ELT(
                    1 + MOD(n, 5),
                    'Muy buena experiencia con el profesor. ',
                    'La experiencia general ha sido excelente. ',
                    'Estoy muy satisfecho con la asignatura. ',
                    'El profesor ha realizado un trabajo muy bueno. ',
                    'Valoración muy positiva en términos generales. '
                )

            WHEN (horario + material + atencion + tutorias + guia) / 5 >= 6 THEN
                ELT(
                    1 + MOD(n, 5),
                    'La experiencia ha sido bastante buena. ',
                    'En general estoy satisfecho con la asignatura. ',
                    'El profesor cumple correctamente con su labor. ',
                    'La valoración general es positiva. ',
                    'Una experiencia correcta con algunos aspectos mejorables. '
                )

            WHEN (horario + material + atencion + tutorias + guia) / 5 >= 4 THEN
                ELT(
                    1 + MOD(n, 5),
                    'La experiencia ha sido irregular. ',
                    'Hay aspectos positivos y otros que deberían mejorar. ',
                    'La asignatura ha tenido resultados aceptables. ',
                    'Mi experiencia ha sido bastante intermedia. ',
                    'El profesor cumple, aunque existen varios puntos mejorables. '
                )

            ELSE
                ELT(
                    1 + MOD(n, 5),
                    'La experiencia no ha sido demasiado positiva. ',
                    'Hay bastantes aspectos que deberían mejorar. ',
                    'La asignatura me ha resultado poco satisfactoria. ',
                    'Considero que existen importantes puntos de mejora. ',
                    'Mi valoración general de la experiencia es baja. '
                )
        END,

        CASE MOD(n, 5)
            WHEN 0 THEN
                CASE
                    WHEN horario >= 7 THEN
                        'El horario y la organización han funcionado especialmente bien. '
                    WHEN horario >= 5 THEN
                        'La organización de los horarios ha sido aceptable. '
                    ELSE
                        'La organización de los horarios podría mejorar bastante. '
                END

            WHEN 1 THEN
                CASE
                    WHEN material >= 7 THEN
                        'El material proporcionado resulta completo y útil. '
                    WHEN material >= 5 THEN
                        'El material disponible es suficiente para seguir la asignatura. '
                    ELSE
                        'El material proporcionado resulta escaso o poco claro. '
                END

            WHEN 2 THEN
                CASE
                    WHEN atencion >= 7 THEN
                        'La atención al alumnado ha sido uno de los puntos fuertes. '
                    WHEN atencion >= 5 THEN
                        'La atención al alumno ha sido correcta. '
                    ELSE
                        'La atención al alumnado debería ser más cercana y constante. '
                END

            WHEN 3 THEN
                CASE
                    WHEN tutorias >= 7 THEN
                        'Las tutorías han resultado accesibles y útiles. '
                    WHEN tutorias >= 5 THEN
                        'Las tutorías cumplen su función adecuadamente. '
                    ELSE
                        'Sería conveniente mejorar la disponibilidad y utilidad de las tutorías. '
                END

            ELSE
                CASE
                    WHEN guia >= 7 THEN
                        'La guía docente está bien estructurada y se corresponde con el desarrollo del curso. '
                    WHEN guia >= 5 THEN
                        'La guía docente resulta razonablemente clara. '
                    ELSE
                        'La guía docente podría reflejar mejor el desarrollo real de la asignatura. '
                END
        END,

        ELT(
            1 + MOD(n * 13, 7),
            'En conjunto, repetiría con este profesor.',
            'Hay margen de mejora, pero la experiencia ha sido razonable.',
            'La valoración refleja bastante bien mi experiencia durante el curso.',
            'Estos aspectos han condicionado mi opinión final de la asignatura.',
            'Espero que estos puntos se tengan en cuenta en próximos cursos.',
            'Mi valoración final tiene en cuenta el conjunto de la asignatura.',
            'En general, estos han sido los aspectos que más han influido en mi experiencia.'
        )
    ) AS comentario,

    horario,
    material,
    atencion,
    tutorias,
    guia

FROM datos;
SET @inicio = 1;
SET @fin = 90;

INSERT INTO valoraciones (
    usuario_id,
    profesor_id,
    puntuacion,
    comentario,
    email_alumno,
    nota_horario,
    nota_material,
    nota_atencion,
    nota_tutorias,
    nota_guia
)

WITH
digitos AS (
    SELECT 0 AS d UNION ALL
    SELECT 1 UNION ALL
    SELECT 2 UNION ALL
    SELECT 3 UNION ALL
    SELECT 4 UNION ALL
    SELECT 5 UNION ALL
    SELECT 6 UNION ALL
    SELECT 7 UNION ALL
    SELECT 8 UNION ALL
    SELECT 9
),

numeros AS (
    SELECT
        u.d
        + d.d * 10
        + c.d * 100
        + m.d * 1000
        + 1 AS n
    FROM digitos u
    CROSS JOIN digitos d
    CROSS JOIN digitos c
    CROSS JOIN digitos m
),

profes AS (
    SELECT
        id,
        ROW_NUMBER() OVER (ORDER BY id) AS rn,
        COUNT(*) OVER () AS total
    FROM profesores
),

datos_base AS (
    SELECT
        n,

        ROUND(1 + MOD(n * 17, 91) / 10, 1) AS horario,
        ROUND(1 + MOD(n * 23, 91) / 10, 1) AS material,
        ROUND(1 + MOD(n * 29, 91) / 10, 1) AS atencion,
        ROUND(1 + MOD(n * 37, 91) / 10, 1) AS tutorias,
        ROUND(1 + MOD(n * 43, 91) / 10, 1) AS guia

    FROM numeros
    WHERE n BETWEEN @inicio AND @fin
),

datos AS (
    SELECT
        n,
        horario,
        material,
        atencion,
        tutorias,
        guia,

        ROUND(
            (
                horario +
                material +
                atencion +
                tutorias +
                guia
            ) / 5,
            2
        ) AS media,

        CONCAT(
            ELT(
                MOD(n * 7, 31) + 1,
                'alejandro',
                'alvaro',
                'andrea',
                'beatriz',
                'carlos',
                'claudia',
                'cristina',
                'daniel',
                'david',
                'elena',
                'fernando',
                'guillermo',
                'hugo',
                'irene',
                'ivan',
                'javier',
                'laura',
                'lucia',
                'marcos',
                'marta',
                'miguel',
                'natalia',
                'pablo',
                'patricia',
                'paula',
                'raul',
                'sara',
                'sergio',
                'silvia',
                'victor',
                'adrian'
            ),
            '.',
            ELT(
                MOD(n * 13, 37) + 1,
                'garcia',
                'gonzalez',
                'rodriguez',
                'fernandez',
                'lopez',
                'martinez',
                'sanchez',
                'perez',
                'gomez',
                'martin',
                'jimenez',
                'ruiz',
                'hernandez',
                'diaz',
                'moreno',
                'munoz',
                'alvarez',
                'romero',
                'alonso',
                'gutierrez',
                'navarro',
                'torres',
                'dominguez',
                'vazquez',
                'ramos',
                'gil',
                'ramirez',
                'serrano',
                'molina',
                'ortega',
                'delgado',
                'castro',
                'ortiz',
                'rubio',
                'marin',
                'sanz',
                'iglesias'
            ),
            LPAD(n, 4, '0'),
            '@alumnos.upm.es'
        ) AS email_generado

    FROM datos_base
)

SELECT
    NULL AS usuario_id,

    p.id AS profesor_id,

    d.media AS puntuacion,

    CONCAT(

        /* =========================
           PARTE 1: VALORACIÓN GENERAL
           ========================= */

        CASE
            WHEN d.media >= 8 THEN
                ELT(
                    MOD(d.n * 7, 15) + 1,
                    'La experiencia general con la asignatura ha sido muy positiva. ',
                    'Estoy muy satisfecho con el desarrollo de la asignatura. ',
                    'Mi valoración global del profesor es muy buena. ',
                    'La asignatura ha cumplido ampliamente mis expectativas. ',
                    'En general, la docencia ha sido excelente. ',
                    'El desarrollo del curso ha sido muy satisfactorio. ',
                    'He tenido una experiencia muy buena durante el curso. ',
                    'El profesor realiza un trabajo muy completo. ',
                    'La asignatura está muy bien planteada en términos generales. ',
                    'Mi impresión general ha sido muy positiva. ',
                    'El profesor ha conseguido que la asignatura resulte interesante. ',
                    'La organización general del curso me ha parecido muy buena. ',
                    'Considero que la calidad de la docencia ha sido alta. ',
                    'La experiencia durante el semestre ha sido muy satisfactoria. ',
                    'En términos generales estoy muy contento con la asignatura. '
                )

            WHEN d.media >= 6 THEN
                ELT(
                    MOD(d.n * 11, 15) + 1,
                    'La experiencia general ha sido positiva. ',
                    'En términos generales la asignatura está bien planteada. ',
                    'La valoración global del profesor es buena. ',
                    'La asignatura ha cumplido bastante bien mis expectativas. ',
                    'El desarrollo de las clases ha sido adecuado. ',
                    'En general estoy satisfecho con la docencia. ',
                    'La experiencia durante el curso ha sido buena. ',
                    'La asignatura funciona correctamente en la mayoría de aspectos. ',
                    'Mi impresión general es bastante buena. ',
                    'El profesor realiza un trabajo adecuado. ',
                    'La asignatura resulta interesante y está razonablemente organizada. ',
                    'El balance general del curso es positivo. ',
                    'La experiencia ha sido buena, aunque existen algunos aspectos mejorables. ',
                    'La docencia ha cumplido con lo que esperaba inicialmente. ',
                    'En conjunto, la asignatura me ha dejado una buena impresión. '
                )

            WHEN d.media >= 4 THEN
                ELT(
                    MOD(d.n * 13, 15) + 1,
                    'La experiencia ha tenido aspectos positivos y negativos. ',
                    'Mi valoración general de la asignatura es intermedia. ',
                    'La asignatura podría mejorar en varios aspectos. ',
                    'El desarrollo del curso ha sido algo irregular. ',
                    'Hay elementos que funcionan bien y otros que deberían revisarse. ',
                    'La experiencia general ha sido aceptable. ',
                    'La asignatura cumple, aunque tiene bastante margen de mejora. ',
                    'Mi impresión ha sido variable durante el curso. ',
                    'La docencia ha sido correcta, aunque mejorable. ',
                    'El resultado general ha sido razonable. ',
                    'La asignatura tiene puntos positivos, pero también algunas carencias. ',
                    'El curso ha sido aceptable sin llegar a destacar especialmente. ',
                    'La experiencia depende bastante del tema y del momento del curso. ',
                    'Hay aspectos bien planteados y otros que deberían reorganizarse. ',
                    'Mi valoración final se encuentra en un punto intermedio. '
                )

            ELSE
                ELT(
                    MOD(d.n * 17, 15) + 1,
                    'La experiencia general no ha sido demasiado positiva. ',
                    'La asignatura necesita mejorar varios aspectos importantes. ',
                    'Mi valoración general del curso es baja. ',
                    'El desarrollo de la asignatura no ha cumplido mis expectativas. ',
                    'He encontrado bastantes dificultades con la organización del curso. ',
                    'La experiencia durante la asignatura ha sido mejorable. ',
                    'Considero que hay varios aspectos que deberían revisarse. ',
                    'La asignatura presenta bastante margen de mejora. ',
                    'Mi impresión general no ha sido especialmente buena. ',
                    'La docencia podría estar mejor organizada. ',
                    'El curso me ha resultado más complicado de seguir de lo esperado. ',
                    'No estoy demasiado satisfecho con cómo se ha desarrollado la asignatura. ',
                    'La experiencia global ha quedado por debajo de mis expectativas. ',
                    'Considero necesario revisar distintos aspectos de la asignatura. ',
                    'El desarrollo del curso podría haber sido bastante más claro. '
                )
        END,


        /* =========================
           PARTE 2: ASPECTO CONCRETO
           ========================= */

        CASE MOD(d.n * 19, 5)

            /* HORARIO */
            WHEN 0 THEN
                CASE
                    WHEN d.horario >= 8 THEN
                        ELT(
                            MOD(d.n * 23, 5) + 1,
                            'La organización de las clases y los horarios ha sido muy buena. ',
                            'El ritmo de las sesiones me ha parecido adecuado y bien distribuido. ',
                            'La planificación temporal de la asignatura facilita bastante su seguimiento. ',
                            'Las clases están bien distribuidas y permiten seguir correctamente el temario. ',
                            'La organización del tiempo durante el curso ha sido uno de los puntos fuertes. '
                        )

                    WHEN d.horario >= 6 THEN
                        ELT(
                            MOD(d.n * 29, 5) + 1,
                            'La organización de las clases ha sido adecuada en términos generales. ',
                            'El ritmo de las sesiones es razonable, aunque algunos temas podrían distribuirse mejor. ',
                            'La planificación temporal funciona correctamente la mayor parte del curso. ',
                            'Los horarios y el ritmo son adecuados, aunque existe algún margen de mejora. ',
                            'La distribución de las clases me ha parecido bastante correcta. '
                        )

                    WHEN d.horario >= 4 THEN
                        ELT(
                            MOD(d.n * 31, 5) + 1,
                            'La organización de los horarios podría estar algo mejor ajustada. ',
                            'Algunas clases avanzan a un ritmo adecuado y otras resultan demasiado rápidas. ',
                            'La distribución temporal del contenido ha sido algo irregular. ',
                            'El ritmo de la asignatura podría equilibrarse mejor a lo largo del semestre. ',
                            'La planificación de las sesiones es aceptable, aunque mejorable. '
                        )

                    ELSE
                        ELT(
                            MOD(d.n * 37, 5) + 1,
                            'La organización y distribución de las clases debería mejorar bastante. ',
                            'El ritmo de algunas sesiones dificulta seguir correctamente los contenidos. ',
                            'La planificación temporal de la asignatura me ha parecido poco equilibrada. ',
                            'Sería conveniente reorganizar mejor los tiempos dedicados a cada tema. ',
                            'La distribución de las clases no facilita demasiado el seguimiento de la asignatura. '
                        )
                END


            /* MATERIAL */
            WHEN 1 THEN
                CASE
                    WHEN d.material >= 8 THEN
                        ELT(
                            MOD(d.n * 41, 5) + 1,
                            'El material proporcionado es completo y facilita mucho el estudio. ',
                            'Los apuntes y recursos disponibles resultan muy útiles para preparar la asignatura. ',
                            'La documentación proporcionada está bien organizada y resulta clara. ',
                            'Los materiales complementarios ayudan bastante a comprender los contenidos. ',
                            'Los recursos disponibles son uno de los aspectos que más valoro de la asignatura. '
                        )

                    WHEN d.material >= 6 THEN
                        ELT(
                            MOD(d.n * 43, 5) + 1,
                            'El material proporcionado resulta útil para estudiar. ',
                            'Los recursos disponibles son suficientes para seguir correctamente la asignatura. ',
                            'La documentación es adecuada, aunque algunos temas podrían desarrollarse algo más. ',
                            'Los apuntes cumplen bien su función en la mayoría de los contenidos. ',
                            'El material facilita el seguimiento general de las clases. '
                        )

                    WHEN d.material >= 4 THEN
                        ELT(
                            MOD(d.n * 47, 5) + 1,
                            'El material es suficiente, aunque algunos contenidos podrían estar mejor explicados. ',
                            'Hay recursos útiles, pero la documentación resulta irregular dependiendo del tema. ',
                            'Los apuntes podrían estar algo mejor organizados. ',
                            'Sería útil disponer de más ejemplos y material complementario. ',
                            'La documentación permite seguir la asignatura, aunque presenta algunas carencias. '
                        )

                    ELSE
                        ELT(
                            MOD(d.n * 53, 5) + 1,
                            'El material disponible resulta escaso o poco organizado. ',
                            'Sería necesario mejorar los apuntes y recursos proporcionados. ',
                            'La documentación no facilita demasiado el estudio de algunos contenidos. ',
                            'Faltan ejemplos y materiales adicionales para comprender ciertos temas. ',
                            'Los recursos disponibles deberían revisarse y estructurarse mejor. '
                        )
                END


            /* ATENCIÓN */
            WHEN 2 THEN
                CASE
                    WHEN d.atencion >= 8 THEN
                        ELT(
                            MOD(d.n * 59, 5) + 1,
                            'La atención al alumno y la resolución de dudas han sido excelentes. ',
                            'El profesor muestra mucha disponibilidad cuando surge alguna pregunta. ',
                            'Las dudas se resuelven con claridad y de forma bastante rápida. ',
                            'La comunicación con el profesor ha sido muy buena durante todo el curso. ',
                            'Destacaría especialmente la disposición para ayudar cuando aparece algún problema. '
                        )

                    WHEN d.atencion >= 6 THEN
                        ELT(
                            MOD(d.n * 61, 5) + 1,
                            'La atención a las dudas ha sido buena en términos generales. ',
                            'El profesor suele estar disponible cuando se necesita alguna aclaración. ',
                            'La comunicación con los alumnos funciona correctamente. ',
                            'Las dudas se resuelven de forma adecuada la mayoría de las veces. ',
                            'La atención recibida durante el curso ha sido bastante correcta. '
                        )

                    WHEN d.atencion >= 4 THEN
                        ELT(
                            MOD(d.n * 67, 5) + 1,
                            'La atención a las dudas es aceptable, aunque podría ser más constante. ',
                            'En algunas ocasiones cuesta obtener una respuesta rápida a determinadas preguntas. ',
                            'La comunicación con el profesor podría ser algo más fluida. ',
                            'La resolución de dudas funciona, aunque de manera algo irregular. ',
                            'La atención al alumno es correcta pero tiene margen de mejora. '
                        )

                    ELSE
                        ELT(
                            MOD(d.n * 71, 5) + 1,
                            'La atención a las dudas debería mejorar bastante. ',
                            'En ocasiones resulta complicado conseguir aclaraciones sobre determinados contenidos. ',
                            'La comunicación con los alumnos podría ser mucho más fluida. ',
                            'Sería conveniente ofrecer mayor disponibilidad para resolver preguntas. ',
                            'La atención al alumno es uno de los aspectos que considero más mejorables. '
                        )
                END


            /* TUTORÍAS */
            WHEN 3 THEN
                CASE
                    WHEN d.tutorias >= 8 THEN
                        ELT(
                            MOD(d.n * 73, 5) + 1,
                            'Las tutorías son muy útiles y permiten resolver dudas concretas con facilidad. ',
                            'La disponibilidad para tutorías ha sido excelente durante el curso. ',
                            'Las sesiones de tutoría ayudan bastante a comprender los temas más difíciles. ',
                            'El profesor aprovecha bien las tutorías para orientar al alumno. ',
                            'Las tutorías han resultado un recurso especialmente útil para preparar la asignatura. '
                        )

                    WHEN d.tutorias >= 6 THEN
                        ELT(
                            MOD(d.n * 79, 5) + 1,
                            'Las tutorías cumplen correctamente su función. ',
                            'La disponibilidad para tutorías ha sido adecuada durante el semestre. ',
                            'Las tutorías ayudan a aclarar las dudas más importantes. ',
                            'El funcionamiento de las tutorías me ha parecido bastante correcto. ',
                            'Las sesiones de tutoría resultan útiles cuando se aprovechan. '
                        )

                    WHEN d.tutorias >= 4 THEN
                        ELT(
                            MOD(d.n * 83, 5) + 1,
                            'Las tutorías cumplen su función, aunque podrían aprovecharse mejor. ',
                            'La disponibilidad para tutorías es razonable pero algo limitada. ',
                            'Las sesiones son útiles en algunos casos, aunque no siempre resultan suficientes. ',
                            'Sería positivo ampliar o mejorar la organización de las tutorías. ',
                            'El sistema de tutorías funciona de forma aceptable, aunque mejorable. '
                        )

                    ELSE
                        ELT(
                            MOD(d.n * 89, 5) + 1,
                            'Sería recomendable mejorar la disponibilidad y utilidad de las tutorías. ',
                            'Las tutorías han resultado difíciles de aprovechar durante el curso. ',
                            'La disponibilidad para resolver dudas fuera de clase es limitada. ',
                            'Creo que sería necesario reforzar el funcionamiento de las tutorías. ',
                            'Las tutorías son uno de los aspectos que deberían mejorarse. '
                        )
                END


            /* GUÍA DOCENTE */
            ELSE
                CASE
                    WHEN d.guia >= 8 THEN
                        ELT(
                            MOD(d.n * 97, 5) + 1,
                            'La planificación sigue muy bien lo establecido en la guía docente. ',
                            'La estructura de la asignatura es clara y coherente con la guía. ',
                            'Los contenidos y la evaluación están bien organizados desde el comienzo. ',
                            'La guía docente refleja correctamente cómo se desarrolla realmente la asignatura. ',
                            'La planificación de contenidos y actividades está muy bien definida. '
                        )

                    WHEN d.guia >= 6 THEN
                        ELT(
                            MOD(d.n * 101, 5) + 1,
                            'La organización respecto a la guía docente es adecuada. ',
                            'La planificación general coincide bastante bien con lo indicado inicialmente. ',
                            'La estructura de la asignatura resulta clara en la mayoría de los apartados. ',
                            'Los contenidos siguen de forma razonable lo establecido en la guía. ',
                            'La planificación académica se cumple correctamente en términos generales. '
                        )

                    WHEN d.guia >= 4 THEN
                        ELT(
                            MOD(d.n * 103, 5) + 1,
                            'La organización respecto a la guía docente es razonable, aunque mejorable. ',
                            'Algunos elementos del curso no coinciden completamente con la planificación inicial. ',
                            'La estructura general se entiende, aunque ciertos aspectos podrían aclararse mejor. ',
                            'La guía resulta útil, pero existen algunas diferencias con el desarrollo real del curso. ',
                            'La planificación es aceptable aunque podría ser más precisa. '
                        )

                    ELSE
                        ELT(
                            MOD(d.n * 107, 5) + 1,
                            'La planificación de la asignatura respecto a la guía podría ser bastante más clara. ',
                            'Existen diferencias importantes entre lo indicado inicialmente y el desarrollo real. ',
                            'La estructura del curso debería definirse mejor desde el comienzo. ',
                            'La guía docente no refleja suficientemente algunos aspectos de la asignatura. ',
                            'Sería conveniente mejorar la planificación y explicar mejor cómo se desarrollará el curso. '
                        )
                END
        END,


        /* =========================
           PARTE 3: CIERRE
           ========================= */

        ELT(
            MOD(d.n * 109, 20) + 1,

            'En conjunto, estoy satisfecho con la experiencia.',
            'En general, esa ha sido mi impresión durante el curso.',
            'Creo que con algunos cambios podría mejorar todavía más.',
            'El balance final coincide bastante con la nota que le daría.',
            'En conjunto, la experiencia ha sido razonable.',
            'Hay aspectos destacables y otros que todavía pueden mejorarse.',
            'La experiencia ha sido similar a lo que esperaba inicialmente.',
            'Considero que el resultado global ha sido coherente.',
            'La asignatura tiene una buena base sobre la que seguir trabajando.',
            'Mi valoración final refleja bastante bien la experiencia durante el semestre.',
            'El resultado global ha sido bastante equilibrado.',
            'La experiencia ha estado condicionada por algunos aspectos concretos.',
            'Creo que varios pequeños cambios podrían mejorar bastante la asignatura.',
            'El balance final es coherente con mi experiencia personal.',
            'En general, esa ha sido mi percepción a lo largo del curso.',
            'Hay margen para mejorar, pero también varios aspectos que funcionan correctamente.',
            'La impresión final depende bastante de los distintos elementos de la asignatura.',
            'Mi valoración resume de forma bastante fiel cómo ha ido el semestre.',
            'En términos generales, el curso ha tenido momentos mejores y peores.',
            'Esta valoración representa mi experiencia global con la asignatura.'
        )

    ) AS comentario,

    d.email_generado AS email_alumno,

    d.horario AS nota_horario,
    d.material AS nota_material,
    d.atencion AS nota_atencion,
    d.tutorias AS nota_tutorias,
    d.guia AS nota_guia

FROM datos d

JOIN profes p
    ON p.rn = MOD(d.n - 1, p.total) + 1

WHERE NOT EXISTS (
    SELECT 1
    FROM valoraciones v
    WHERE v.email_alumno = d.email_generado
)

ORDER BY d.n;
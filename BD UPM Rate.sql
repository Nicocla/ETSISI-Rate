CREATE DATABASE  IF NOT EXISTS `valoracion_profesores` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `valoracion_profesores`;
-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: valoracion_profesores
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `profesores`
--

DROP TABLE IF EXISTS `profesores`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `profesores` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) DEFAULT NULL,
  `departamento` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `profesores`
--

LOCK TABLES `profesores` WRITE;
/*!40000 ALTER TABLE `profesores` DISABLE KEYS */;
INSERT INTO `profesores` VALUES (1,'Daniel López Fernández','Sistemas Informáticos'),(2,'Aldo Gordillo Méndez','Sistemas Informáticos'),(3,'Javier Alegre Landaburu','Sistemas Informáticos'),(4,'Jorge Dueñas Lerín','Bases de Datos'),(5,'Jesús Mayor Márquez','Programación');
/*!40000 ALTER TABLE `profesores` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `password_hash` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios`
--

LOCK TABLES `usuarios` WRITE;
/*!40000 ALTER TABLE `usuarios` DISABLE KEYS */;
/*!40000 ALTER TABLE `usuarios` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `valoraciones`
--

DROP TABLE IF EXISTS `valoraciones`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `valoraciones` (
  `id` int NOT NULL AUTO_INCREMENT,
  `usuario_id` int DEFAULT NULL,
  `profesor_id` int DEFAULT NULL,
  `puntuacion` int DEFAULT NULL,
  `comentario` text,
  `email_alumno` varchar(100) NOT NULL,
  `nota_horario` float DEFAULT '0',
  `nota_material` float DEFAULT '0',
  `nota_atencion` float DEFAULT '0',
  `nota_tutorias` float DEFAULT '0',
  `nota_guia` float DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `voto_unico` (`profesor_id`,`email_alumno`),
  KEY `usuario_id` (`usuario_id`),
  CONSTRAINT `valoraciones_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `valoraciones_ibfk_2` FOREIGN KEY (`profesor_id`) REFERENCES `profesores` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `valoraciones`
--

LOCK TABLES `valoraciones` WRITE;
/*!40000 ALTER TABLE `valoraciones` DISABLE KEYS */;
INSERT INTO `valoraciones` VALUES (1,NULL,1,8,'Profesor ****, explica muy bien y las clases son fáciles de seguir.','nicolas.clavo.collado@alumnos.upm.es',8,10,6,8,10),(2,NULL,1,8,'Buen profesor, aunque algunas prácticas requieren bastante trabajo.','mario@alumnos.upm.es',8,8,8,8,8),(3,NULL,1,5,'Las explicaciones son correctas pero a veces falta más apoyo en las prácticas.','lucia@alumnos.upm.es',5,6,5,5,5),(4,NULL,2,8,'Explica de forma clara y responde bastante bien a las dudas.','sara@alumnos.upm.es',9,8,9,8,8),(5,NULL,2,6,'La asignatura es interesante pero algunas clases son difíciles de seguir.','nicolas.clavo.collado@alumnos.upm.es',6,6,6,7,5),(6,NULL,2,4,'Me costó seguir las clases y creo que el material podría mejorar bastante.','mario@alumnos.upm.es',4,3,4,4,3),(7,NULL,3,8,'Buen profesor, prepara las clases y domina la materia.','luis@alumnos.upm.es',8,8,8,7,8),(8,NULL,3,7,'Profesor correcto, aunque las explicaciones podrían ser más detalladas.','nicolas.clavo.collado@alumnos.upm.es',8,8,6,6,6),(9,NULL,3,2,'Las clases son complicadas de seguir y falta más organización del contenido.','mario@alumnos.upm.es',2,3,2,3,2),(10,NULL,4,10,'Excelente profesor, muy claro explicando y siempre dispuesto a ayudar.','nicolas.clavo.collado@alumnos.upm.es',10,10,9,10,9),(11,NULL,4,9,'Muy buen material y clases bien organizadas.','mario@alumnos.upm.es',9,9,8,8,9),(12,NULL,4,6,'Buen profesor aunque algunos temas avanzan demasiado rápido.','lucia@alumnos.upm.es',6,7,6,7,6),(13,NULL,5,9,'Explica muy bien y utiliza ejemplos prácticos útiles.','sara@alumnos.upm.es',9,9,9,8,9),(14,NULL,5,6,'La asignatura está bien planteada pero algunas partes son difíciles.','nicolas.clavo.collado@alumnos.upm.es',6,6,5,6,6),(15,NULL,5,3,'Me resultó complicado seguir el ritmo y faltaban más ejemplos.','mario@alumnos.upm.es',3,3,3,3,3);
/*!40000 ALTER TABLE `valoraciones` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-14 13:15:43

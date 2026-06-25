/*M!999999\- enable the sandbox mode */ 
-- MariaDB dump 10.19-12.2.2-MariaDB, for debian-linux-gnu (aarch64)
--
-- Host: localhost    Database: coba
-- ------------------------------------------------------
-- Server version	12.2.2-MariaDB-ubu2404

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*M!100616 SET @OLD_NOTE_VERBOSITY=@@NOTE_VERBOSITY, NOTE_VERBOSITY=0 */;

--
-- Table structure for table `kendaraan`
--

DROP TABLE IF EXISTS `kendaraan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `kendaraan` (
  `id_kendaraan` bigint(20) NOT NULL AUTO_INCREMENT,
  `merk` varchar(100) NOT NULL,
  `model` varchar(100) NOT NULL,
  `tahun` year(4) NOT NULL,
  `harga` decimal(15,2) NOT NULL,
  `status` varchar(50) NOT NULL,
  `foto` varchar(255) DEFAULT NULL,
  `warna` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id_kendaraan`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `kendaraan`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `kendaraan` WRITE;
/*!40000 ALTER TABLE `kendaraan` DISABLE KEYS */;
INSERT INTO `kendaraan` VALUES
(8,'esemka','aerox',2024,25000000.00,'Terjual','f2e6105c-7cc5-4dc4-928f-c4153d1405c3.png',NULL),
(9,'honda','viar',2000,399990.00,'Terjual','ec49ab92-7f5d-4a30-b277-026996bb210d.jpeg',NULL),
(12,'honda','viar',2023,3848888.00,'Terjual','6feed8bc-e385-4492-bf9b-b4d4f0acae3f.png','merah'),
(13,'honda','viar',2004,120000.00,'Tersedia','13846bbf-589a-408a-a73e-90e309849373.png','putih'),
(14,'honda','viar',2004,120000.00,'Dipesan','1131150e-a42a-43ed-add3-ee5fa957c3a2.png','merah');
/*!40000 ALTER TABLE `kendaraan` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `mobil`
--

DROP TABLE IF EXISTS `mobil`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `mobil` (
  `id_mobil` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_kendaraan` bigint(20) NOT NULL,
  `mesin_mobil` varchar(100) DEFAULT NULL,
  `jenis_mobil` varchar(100) DEFAULT NULL,
  `transmisi_mobil` varchar(50) DEFAULT NULL,
  `kapasitas_mobil` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_mobil`),
  KEY `fk_mobil_kendaraan` (`id_kendaraan`),
  CONSTRAINT `fk_mobil_kendaraan` FOREIGN KEY (`id_kendaraan`) REFERENCES `kendaraan` (`id_kendaraan`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mobil`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `mobil` WRITE;
/*!40000 ALTER TABLE `mobil` DISABLE KEYS */;
INSERT INTO `mobil` VALUES
(6,8,'-','-','-',0);
/*!40000 ALTER TABLE `mobil` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `motor`
--

DROP TABLE IF EXISTS `motor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `motor` (
  `id_motor` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_kendaraan` bigint(20) NOT NULL,
  `cc` int(11) DEFAULT NULL,
  `jenis_motor` varchar(100) DEFAULT NULL,
  `kapasitas_tangki` decimal(5,2) DEFAULT NULL,
  PRIMARY KEY (`id_motor`),
  KEY `fk_motor_kendaraan` (`id_kendaraan`),
  CONSTRAINT `fk_motor_kendaraan` FOREIGN KEY (`id_kendaraan`) REFERENCES `kendaraan` (`id_kendaraan`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `motor`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `motor` WRITE;
/*!40000 ALTER TABLE `motor` DISABLE KEYS */;
INSERT INTO `motor` VALUES
(3,9,0,'-',0.00),
(4,12,130,'Matic',2.20),
(5,13,150,'Sport',0.00),
(6,14,130,'Sport',3.00);
/*!40000 ALTER TABLE `motor` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `owner`
--

DROP TABLE IF EXISTS `owner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `owner` (
  `id_owner` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_user` bigint(20) NOT NULL,
  `kontak` varchar(20) DEFAULT NULL,
  `kota` varchar(100) DEFAULT NULL,
  `provinsi` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id_owner`),
  KEY `fk_owner_user` (`id_user`),
  CONSTRAINT `fk_owner_user` FOREIGN KEY (`id_user`) REFERENCES `user` (`id_user`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `owner`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `owner` WRITE;
/*!40000 ALTER TABLE `owner` DISABLE KEYS */;
INSERT INTO `owner` VALUES
(1,4,'0812-9999-8888','Tasikmalaya','Jawa Barat');
/*!40000 ALTER TABLE `owner` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `pembeli`
--

DROP TABLE IF EXISTS `pembeli`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `pembeli` (
  `id_pembeli` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_user` bigint(20) NOT NULL,
  `kontak` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id_pembeli`),
  KEY `fk_pembeli_user` (`id_user`),
  CONSTRAINT `fk_pembeli_user` FOREIGN KEY (`id_user`) REFERENCES `user` (`id_user`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pembeli`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `pembeli` WRITE;
/*!40000 ALTER TABLE `pembeli` DISABLE KEYS */;
INSERT INTO `pembeli` VALUES
(2,2,'0323103232'),
(3,3,'0323103232'),
(4,5,NULL),
(5,6,NULL),
(6,7,NULL),
(7,8,NULL),
(8,9,NULL),
(9,10,NULL),
(10,11,NULL),
(11,12,NULL),
(12,13,NULL);
/*!40000 ALTER TABLE `pembeli` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `penjualan`
--

DROP TABLE IF EXISTS `penjualan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `penjualan` (
  `id_penjualan` bigint(20) NOT NULL AUTO_INCREMENT,
  `tanggal` date NOT NULL,
  `status` varchar(50) NOT NULL,
  `id_pembeli` bigint(20) NOT NULL,
  `id_kendaraan` bigint(20) NOT NULL,
  `id_owner` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id_penjualan`),
  KEY `fk_penjualan_pembeli` (`id_pembeli`),
  KEY `fk_penjualan_kendaraan` (`id_kendaraan`),
  KEY `fk_penjualan_owner` (`id_owner`),
  CONSTRAINT `fk_penjualan_kendaraan` FOREIGN KEY (`id_kendaraan`) REFERENCES `kendaraan` (`id_kendaraan`),
  CONSTRAINT `fk_penjualan_owner` FOREIGN KEY (`id_owner`) REFERENCES `owner` (`id_owner`),
  CONSTRAINT `fk_penjualan_pembeli` FOREIGN KEY (`id_pembeli`) REFERENCES `pembeli` (`id_pembeli`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `penjualan`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `penjualan` WRITE;
/*!40000 ALTER TABLE `penjualan` DISABLE KEYS */;
INSERT INTO `penjualan` VALUES
(1,'2026-06-18','Ditolak',2,8,NULL),
(2,'2026-06-16','Selesai',2,8,NULL),
(3,'2026-06-20','Selesai',10,9,1),
(4,'2026-06-19','Selesai',3,12,NULL),
(5,'2026-06-25','Pending',3,13,NULL),
(6,'2026-06-25','Pending',11,14,1);
/*!40000 ALTER TABLE `penjualan` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `testdrive`
--

DROP TABLE IF EXISTS `testdrive`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `testdrive` (
  `id_testdrive` bigint(20) NOT NULL AUTO_INCREMENT,
  `tanggal` date NOT NULL,
  `jam` time NOT NULL,
  `status` varchar(50) NOT NULL,
  `catatan` text DEFAULT NULL,
  `id_pembeli` bigint(20) NOT NULL,
  `id_kendaraan` bigint(20) NOT NULL,
  `id_owner` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id_testdrive`),
  KEY `fk_testdrive_pembeli` (`id_pembeli`),
  KEY `fk_testdrive_kendaraan` (`id_kendaraan`),
  KEY `fk_testdrive_owner` (`id_owner`),
  CONSTRAINT `fk_testdrive_kendaraan` FOREIGN KEY (`id_kendaraan`) REFERENCES `kendaraan` (`id_kendaraan`),
  CONSTRAINT `fk_testdrive_owner` FOREIGN KEY (`id_owner`) REFERENCES `owner` (`id_owner`),
  CONSTRAINT `fk_testdrive_pembeli` FOREIGN KEY (`id_pembeli`) REFERENCES `pembeli` (`id_pembeli`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `testdrive`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `testdrive` WRITE;
/*!40000 ALTER TABLE `testdrive` DISABLE KEYS */;
INSERT INTO `testdrive` VALUES
(1,'2026-06-26','21:09:00','Selesai','lajdnwk',2,8,NULL),
(2,'2026-06-18','22:14:00','Ditolak','ljmk',2,8,NULL),
(3,'2026-06-26','13:00:00','Selesai','',10,9,1),
(4,'2026-06-26','09:00:00','Pending','hallo guys ',11,13,1);
/*!40000 ALTER TABLE `testdrive` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id_user` bigint(20) NOT NULL AUTO_INCREMENT,
  `nama` varchar(100) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id_user`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES
(2,'jopan','jopan@gmail.com','1234567890','Pembeli'),
(3,'jopan','nivea@hahaha.digital','1234567890','Pembeli'),
(4,'Admin Owner','owner@autoprime.id','12345','Owner'),
(5,'Abdad',NULL,'abdad123',NULL),
(6,'abdad1',NULL,'abdad123',NULL),
(7,'abdad2',NULL,'abdad123',NULL),
(8,'Abdad Mubaroq',NULL,'abdad123',NULL),
(9,'Abdad Mubaroq','example@gmail.com','123456',NULL),
(10,'Abdad1','hallo@gmail.com','12345678',NULL),
(11,'Abdad2','abdad.mubaroq@gmail.com','123456789',NULL),
(12,'abdad','abdad.mubaroq1@gmail.com','12345678','pembeli'),
(13,'Abdad3','abdad2@gmail.com','123456789','pembeli');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*M!100616 SET NOTE_VERBOSITY=@OLD_NOTE_VERBOSITY */;

-- Dump completed on 2026-06-25  3:11:18

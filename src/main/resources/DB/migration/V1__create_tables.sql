CREATE TABLE user (
    id_user BIGINT AUTO_INCREMENT PRIMARY KEY,
    nama VARCHAR(100) NOT NULL,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE owner (
    id_owner BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_user BIGINT NOT NULL,
    
    CONSTRAINT fk_owner_user
        FOREIGN KEY (id_user)
        REFERENCES user(id_user)
        ON DELETE CASCADE
);

CREATE TABLE pembeli (
    id_pembeli BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_user BIGINT NOT NULL,

    CONSTRAINT fk_pembeli_user
        FOREIGN KEY (id_user)
        REFERENCES user(id_user)
        ON DELETE CASCADE
);

CREATE TABLE kendaraan (
    id_kendaraan BIGINT AUTO_INCREMENT PRIMARY KEY,
    merk VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    tahun YEAR NOT NULL,
    harga DECIMAL(15,2) NOT NULL,
    status VARCHAR(50) NOT NULL
);

CREATE TABLE mobil (
    id_mobil BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_kendaraan BIGINT NOT NULL,
    mesin_mobil VARCHAR(100),
    jenis_mobil VARCHAR(100),
    transmisi_mobil VARCHAR(50),
    kapasitas_mobil INT,

    CONSTRAINT fk_mobil_kendaraan
        FOREIGN KEY (id_kendaraan)
        REFERENCES kendaraan(id_kendaraan)
        ON DELETE CASCADE
);

CREATE TABLE motor (
    id_motor BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_kendaraan BIGINT NOT NULL,
    cc INT,
    jenis_motor VARCHAR(100),
    kapasitas_tangki DECIMAL(5,2),

    CONSTRAINT fk_motor_kendaraan
        FOREIGN KEY (id_kendaraan)
        REFERENCES kendaraan(id_kendaraan)
        ON DELETE CASCADE
);

CREATE TABLE penjualan (
    id_penjualan BIGINT AUTO_INCREMENT PRIMARY KEY,
    tanggal DATE NOT NULL,
    status VARCHAR(50) NOT NULL,

    id_pembeli BIGINT NOT NULL,
    id_kendaraan BIGINT NOT NULL,
    id_owner BIGINT NOT NULL,

    CONSTRAINT fk_penjualan_pembeli
        FOREIGN KEY (id_pembeli)
        REFERENCES pembeli(id_pembeli),

    CONSTRAINT fk_penjualan_kendaraan
        FOREIGN KEY (id_kendaraan)
        REFERENCES kendaraan(id_kendaraan),

    CONSTRAINT fk_penjualan_owner
        FOREIGN KEY (id_owner)
        REFERENCES owner(id_owner)
);

CREATE TABLE testdrive (
    id_testdrive BIGINT AUTO_INCREMENT PRIMARY KEY,
    tanggal DATE NOT NULL,
    jam TIME NOT NULL,
    status VARCHAR(50) NOT NULL,

    id_pembeli BIGINT NOT NULL,
    id_kendaraan BIGINT NOT NULL,
    id_owner BIGINT NOT NULL,

    CONSTRAINT fk_testdrive_pembeli
        FOREIGN KEY (id_pembeli)
        REFERENCES pembeli(id_pembeli),

    CONSTRAINT fk_testdrive_kendaraan
        FOREIGN KEY (id_kendaraan)
        REFERENCES kendaraan(id_kendaraan),

    CONSTRAINT fk_testdrive_owner
        FOREIGN KEY (id_owner)
        REFERENCES owner(id_owner)
);
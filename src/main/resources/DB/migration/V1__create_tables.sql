CREATE TABLE users (
    id_user BIGINT AUTO_INCREMENT PRIMARY KEY,

    nama VARCHAR(100) NOT NULL,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
);

-- OWNER

CREATE TABLE owner (
    id_owner BIGINT AUTO_INCREMENT PRIMARY KEY,

    id_user BIGINT NOT NULL UNIQUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_owner_user
        FOREIGN KEY (id_user)
        REFERENCES users(id_user)
        ON DELETE CASCADE
);

-- PEMBELI

CREATE TABLE pembeli (
    id_pembeli BIGINT AUTO_INCREMENT PRIMARY KEY,

    id_user BIGINT NOT NULL UNIQUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_pembeli_user
        FOREIGN KEY (id_user)
        REFERENCES users(id_user)
        ON DELETE CASCADE
);

-- KENDARAAN

CREATE TABLE kendaraan (
    id_kendaraan BIGINT AUTO_INCREMENT PRIMARY KEY,

    id_owner BIGINT NOT NULL,

    merk VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    tahun YEAR NOT NULL,

    deskripsi TEXT,

    harga DECIMAL(15,2) NOT NULL,

    status ENUM(
        'tersedia',
        'booking',
        'terjual'
    ) NOT NULL DEFAULT 'tersedia',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_kendaraan_owner
        FOREIGN KEY (id_owner)
        REFERENCES owner(id_owner)
        ON DELETE CASCADE
);

-- FOTO KENDARAAN

CREATE TABLE foto_kendaraan (
    id_foto BIGINT AUTO_INCREMENT PRIMARY KEY,

    id_kendaraan BIGINT NOT NULL,

    foto VARCHAR(255) NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_foto_kendaraan
        FOREIGN KEY (id_kendaraan)
        REFERENCES kendaraan(id_kendaraan)
        ON DELETE CASCADE
);

-- MOBIL

CREATE TABLE mobil (
    id_mobil BIGINT AUTO_INCREMENT PRIMARY KEY,

    id_kendaraan BIGINT NOT NULL UNIQUE,

    mesin_mobil VARCHAR(100),
    jenis_mobil VARCHAR(100),
    transmisi_mobil ENUM(
        'manual',
        'matic'
    ),

    kapasitas_penumpang INT,

    bahan_bakar VARCHAR(50),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_mobil_kendaraan
        FOREIGN KEY (id_kendaraan)
        REFERENCES kendaraan(id_kendaraan)
        ON DELETE CASCADE
);

-- MOTOR

CREATE TABLE motor (
    id_motor BIGINT AUTO_INCREMENT PRIMARY KEY,

    id_kendaraan BIGINT NOT NULL UNIQUE,

    cc INT,

    jenis_motor VARCHAR(100),

    kapasitas_tangki DECIMAL(5,2),

    transmisi_motor ENUM(
        'manual',
        'matic'
    ),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_motor_kendaraan
        FOREIGN KEY (id_kendaraan)
        REFERENCES kendaraan(id_kendaraan)
        ON DELETE CASCADE
);

-- PENJUALAN

CREATE TABLE penjualan (
    id_penjualan BIGINT AUTO_INCREMENT PRIMARY KEY,

    id_pembeli BIGINT NOT NULL,
    id_kendaraan BIGINT NOT NULL,
    id_owner BIGINT NOT NULL,

    tanggal DATE NOT NULL,

    total_harga DECIMAL(15,2) NOT NULL,

    status ENUM(
        'pending',
        'dibayar',
        'selesai',
        'dibatalkan'
    ) NOT NULL DEFAULT 'pending',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

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

-- TEST DRIVE

CREATE TABLE testdrive (
    id_testdrive BIGINT AUTO_INCREMENT PRIMARY KEY,

    id_pembeli BIGINT NOT NULL,
    id_kendaraan BIGINT NOT NULL,
    id_owner BIGINT NOT NULL,

    tanggal DATE NOT NULL,
    jam TIME NOT NULL,

    catatan TEXT,

    status ENUM(
        'menunggu',
        'disetujui',
        'ditolak',
        'selesai'
    ) NOT NULL DEFAULT 'menunggu',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

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
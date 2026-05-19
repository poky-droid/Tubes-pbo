package com.example.demo.model;

public class Kendaraan {
    private int IdKendaraan;
    private String Merk;
    private int tahun ;
    private double harga;
    private String status;

    public Kendaraan( int IdKendaraan, String Merk, int tahun, double harga, String status) {
        this.IdKendaraan = IdKendaraan;
        this.Merk = Merk;
        this.tahun = tahun;
        this.harga = harga;
        this.status = status;

    }

        public int getIdKendaraan() {
            return IdKendaraan;
        }
        public String getMerk() {
            return Merk;
        }
        public int getTahun() {
            return tahun;
        }
        public double getHarga() {
            return harga;
        }
        public String getStatus() {
            return status;
        }

        public void setIdKendaraan(int IdKendaraan) {
            this.IdKendaraan = IdKendaraan;
        }
        public void setMerk(String Merk) {
            this.Merk = Merk;
        }
        public void setTahun(int tahun) {
            this.tahun = tahun;
        }
        public void setHarga(double harga) {
            this.harga = harga;
        }
        public void setStatus(String status) {
            this.status = status;
        }

    
}

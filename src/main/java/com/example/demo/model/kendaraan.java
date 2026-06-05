package com.example.demo.model;

public class kendaraan {
    private int IdKendaraan;
    private String Merk;
    private String model; // Tambahan wajib untuk sinkron dengan SQL
    private int tahun;
    private double harga;
    private String status;

    public kendaraan(int IdKendaraan, String Merk, String model, int tahun, double harga, String status) {
        this.IdKendaraan = IdKendaraan;
        this.Merk = Merk;
        this.model = model;
        this.tahun = tahun;
        this.harga = harga;
        this.status = status;
    }

    public int getIdKendaraan() {
        return IdKendaraan;
    }
    public void setIdKendaraan(int IdKendaraan) {
        this.IdKendaraan = IdKendaraan;
    }
    
    public String getMerk() {
        return Merk;
    }
    public void setMerk(String Merk) {
        this.Merk = Merk;
    }
    
    public String getModel() {
        return model;
    }
    public void setModel(String model) {
        this.model = model;
    }

    public int getTahun() {
        return tahun;
    }
    public void setTahun(int tahun) {
        this.tahun = tahun;
    }

    public double getHarga() {
        return harga;
    }
    public void setHarga(double harga) {
        this.harga = harga;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
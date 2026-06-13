package com.example.demo.model;

public class kendaraan {
    private long idKendaraan;  // bigint → long
    private String merk;
    private String model;
    private int tahun;
    private double harga;
    private String status;

    public kendaraan(long idKendaraan, String merk, String model, int tahun, double harga, String status) {
        this.idKendaraan = idKendaraan;
        this.merk = merk;
        this.model = model;
        this.tahun = tahun;
        this.harga = harga;
        this.status = status;
    }

    public long getIdKendaraan() { return idKendaraan; }
    public void setIdKendaraan(long idKendaraan) { this.idKendaraan = idKendaraan; }
    public String getMerk() { return merk; }
    public void setMerk(String merk) { this.merk = merk; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public int getTahun() { return tahun; }
    public void setTahun(int tahun) { this.tahun = tahun; }
    public double getHarga() { return harga; }
    public void setHarga(double harga) { this.harga = harga; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
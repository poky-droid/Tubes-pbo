package com.example.demo.model;

public class kendaraan {
    private Long idKendaraan;
    private String merk;
    private String model;
    private int tahun;
    private double harga;
    private String status;
    private String foto; // ← tambahkan field ini
    private String jenisKendaraan = ""; // defensive: template may reference this

    // Constructor dengan foto
    public kendaraan(Long idKendaraan, String merk, String model, int tahun, double harga, String status, String foto) {
        this.idKendaraan = idKendaraan;
        this.merk = merk;
        this.model = model;
        this.tahun = tahun;
        this.harga = harga;
        this.status = status;
        this.foto = foto; // ← tambahkan ini
    }

    // Getter yang sudah ada tetap sama, tambahkan getFoto()
    public Long getIdKendaraan() { return idKendaraan; }
    public String getMerk()      { return merk; }
    public String getModel()     { return model; }
    public int getTahun()        { return tahun; }
    public double getHarga()     { return harga; }
    public String getStatus()    { return status; }
    public String getFoto()      { return foto; } // ← tambahkan getter ini
    // Defensive getter: some templates reference "jenisKendaraan"
    public String getJenisKendaraan() { return jenisKendaraan; }
    public void setJenisKendaraan(String jenisKendaraan) { this.jenisKendaraan = jenisKendaraan; }
}
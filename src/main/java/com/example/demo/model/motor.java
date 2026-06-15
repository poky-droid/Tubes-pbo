package com.example.demo.model;

public class motor extends kendaraan {
    
    private int idMotor;
    private int cc;
    private String jenisMotor;
    private double kapasitasTangki;

    public motor(Long idKendaraan, String merk, String model, int tahun, double harga, String status, String foto,
                 int idMotor, int cc, String jenisMotor, double kapasitasTangki) {
        
        super(idKendaraan, merk, model, tahun, harga, status, foto);
        
        this.idMotor = idMotor;
        this.cc = cc;
        this.jenisMotor = jenisMotor;
        this.kapasitasTangki = kapasitasTangki;
    }

    public int getIdMotor()                     { return idMotor; }
    public void setIdMotor(int idMotor)         { this.idMotor = idMotor; }

    public int getCc()                          { return cc; }
    public void setCc(int cc)                   { this.cc = cc; }

    public String getJenisMotor()               { return jenisMotor; }
    public void setJenisMotor(String jenisMotor){ this.jenisMotor = jenisMotor; }

    public double getKapasitasTangki()                      { return kapasitasTangki; }
    public void setKapasitasTangki(double kapasitasTangki)  { this.kapasitasTangki = kapasitasTangki; }
}
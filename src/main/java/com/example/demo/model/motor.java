package com.example.demo.model;

public class Motor extends Kendaraan {
    
    private int idMotor;
    private int cc;
    private String jenisMotor;
    private double kapasitasTangki;

    public Motor(int IdKendaraan, String Merk, String model, int tahun, double harga, String status,
                 int idMotor, int cc, String jenisMotor, double kapasitasTangki) {
        
        super(IdKendaraan, Merk, model, tahun, harga, status);
        
        this.idMotor = idMotor;
        this.cc = cc;
        this.jenisMotor = jenisMotor;
        this.kapasitasTangki = kapasitasTangki;
    }

    public int getIdMotor() {
        return idMotor;
    }
    public void setIdMotor(int idMotor) {
        this.idMotor = idMotor;
    }

    public int getCc() {
        return cc;
    }
    public void setCc(int cc) {
        this.cc = cc;
    }

    public String getJenisMotor() {
        return jenisMotor;
    }
    public void setJenisMotor(String jenisMotor) {
        this.jenisMotor = jenisMotor;
    }

    public double getKapasitasTangki() {
        return kapasitasTangki;
    }
    public void setKapasitasTangki(double kapasitasTangki) {
        this.kapasitasTangki = kapasitasTangki;
    }
}
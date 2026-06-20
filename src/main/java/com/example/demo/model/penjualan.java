package com.example.demo.model;

import java.time.LocalDate;

public class penjualan {
    private Long idPenjualan;
    private LocalDate tanggal;
    private String status;
    private Long idPembeli;
    private Long idKendaraan;
    private Long idOwner;
    private Double totalHarga;

    public penjualan() {}

    public Long getIdPenjualan() { return idPenjualan; }
    public void setIdPenjualan(Long idPenjualan) { this.idPenjualan = idPenjualan; }

    public LocalDate getTanggal() { return tanggal; }
    public void setTanggal(LocalDate tanggal) { this.tanggal = tanggal; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getIdPembeli() { return idPembeli; }
    public void setIdPembeli(Long idPembeli) { this.idPembeli = idPembeli; }

    public Long getIdKendaraan() { return idKendaraan; }
    public void setIdKendaraan(Long idKendaraan) { this.idKendaraan = idKendaraan; }

    public Long getIdOwner() { return idOwner; }
    public void setIdOwner(Long idOwner) { this.idOwner = idOwner; }

    public Double getTotalHarga() { return totalHarga; }
    public void setTotalHarga(Double totalHarga) { this.totalHarga = totalHarga; }
}
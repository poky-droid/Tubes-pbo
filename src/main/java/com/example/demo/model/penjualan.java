package com.example.demo.model;

public class Penjualan {
    private int idPenjualan;
    private int tanggalPenjualan;
    private boolean statusPenjualan;

    public Penjualan(int idPenjualan, int tanggalPenjualan, boolean statusPenjualan) {
        this.idPenjualan = idPenjualan;
        this.tanggalPenjualan = tanggalPenjualan;
        this.statusPenjualan = statusPenjualan;
    }

    public int getIdPenjualan() {
        return idPenjualan;
    }
    public int getTanggalPenjualan() {
        return tanggalPenjualan;
    }
    public boolean isStatusPenjualan() {
        return statusPenjualan;
    }

        public void setIdPenjualan(int idPenjualan) {
            this.idPenjualan = idPenjualan;
        }
        public void setTanggalPenjualan(int tanggalPenjualan) {
            this.tanggalPenjualan = tanggalPenjualan;
        }
        public void setStatusPenjualan(boolean statusPenjualan) {
            this.statusPenjualan = statusPenjualan;
        }

        

}

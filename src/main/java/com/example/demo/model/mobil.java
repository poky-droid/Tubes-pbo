package com.example.demo.model;

public class mobil extends kendaraan {
    
    private int idMobil;
    private String mesinMobil;
    private String jenisMobil;
    private String transmisiMobil;
    private int kapasitasMobil;

    public mobil(Long idKendaraan, String merk, String model, int tahun, double harga, String status, String foto,
                 int idMobil, String mesinMobil, String jenisMobil, String transmisiMobil, int kapasitasMobil) {
        
        super(idKendaraan, merk, model, tahun, harga, status, foto);
        
        this.idMobil = idMobil;
        this.mesinMobil = mesinMobil;
        this.jenisMobil = jenisMobil;
        this.transmisiMobil = transmisiMobil;
        this.kapasitasMobil = kapasitasMobil;
    }

    public int getIdMobil()               { return idMobil; }
    public void setIdMobil(int idMobil)   { this.idMobil = idMobil; }

    public String getMesinMobil()                   { return mesinMobil; }
    public void setMesinMobil(String mesinMobil)    { this.mesinMobil = mesinMobil; }

    public String getJenisMobil()                   { return jenisMobil; }
    public void setJenisMobil(String jenisMobil)    { this.jenisMobil = jenisMobil; }

    public String getTransmisiMobil()                       { return transmisiMobil; }
    public void setTransmisiMobil(String transmisiMobil)    { this.transmisiMobil = transmisiMobil; }

    public int getKapasitasMobil()                      { return kapasitasMobil; }
    public void setKapasitasMobil(int kapasitasMobil)   { this.kapasitasMobil = kapasitasMobil; }
}
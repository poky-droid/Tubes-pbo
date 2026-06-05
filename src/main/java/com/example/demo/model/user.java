package com.example.demo.model;

public class User {

    private int idUser;
    private String nama;
    private String email;
    private String password;

    public User() {

    }

    public boolean login(String email, String password) {
        return true;
    
    }

    // Getter dan Setter
    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
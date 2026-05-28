package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@Controller
public class kendaraanController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 1. Menampilkan Halaman dan Membaca Data Kendaraan (Read)
   @GetMapping("/kendaraan")
    public String showKendaraan(Model model) {
        
        String sql = "SELECT k.*, " +
                     "m.transmisi_mobil, m.mesin_mobil, " +
                     "mo.cc, " +
                     "CASE WHEN m.id_kendaraan IS NOT NULL THEN 'mobil' " +
                     "     WHEN mo.id_kendaraan IS NOT NULL THEN 'motor' " +
                     "END as jenis " +
                     "FROM kendaraan k " +
                     "LEFT JOIN mobil m ON k.id_kendaraan = m.id_kendaraan " +
                     "LEFT JOIN motor mo ON k.id_kendaraan = mo.id_kendaraan " +
                     "ORDER BY k.id_kendaraan DESC";
        
        List<Map<String, Object>> daftarKendaraan = jdbcTemplate.queryForList(sql);
        
        // --- TAMBAHKAN LOGIKA PENGHITUNGAN DI SINI ---
        int totalStok = daftarKendaraan.size(); // Total semua kendaraan
        int totalMobil = 0;
        int totalMotor = 0;
        int totalTersedia = 0;

        for (Map<String, Object> k : daftarKendaraan) {
            String jenis = (String) k.get("jenis");
            String status = (String) k.get("status");

            if ("mobil".equals(jenis)) totalMobil++;
            if ("motor".equals(jenis)) totalMotor++;
            if ("Tersedia".equalsIgnoreCase(status)) totalTersedia++;
        }

        // Kirim variabel angka ke HTML
        model.addAttribute("totalStok", totalStok);
        model.addAttribute("totalMobil", totalMobil);
        model.addAttribute("totalMotor", totalMotor);
        model.addAttribute("totalTersedia", totalTersedia);
        // ---------------------------------------------
        
        model.addAttribute("listKendaraan", daftarKendaraan);
        
        return "kendaraan"; 
    }

    // 2. Memproses form Tambah Kendaraan (Create)
    @PostMapping("/kendaraan/tambah")
    public String tambahKendaraan(
            @RequestParam("jenisKendaraan") String jenisKendaraan,
            @RequestParam("merk") String merk,
            @RequestParam("model") String model,
            @RequestParam("tahun") int tahun,
            @RequestParam("harga") double harga,
            @RequestParam("status") String status
    ) {
        
        // --- Tahap A: Simpan data umum ke tabel 'kendaraan' ---
        String sqlKendaraan = "INSERT INTO kendaraan (merk, model, tahun, harga, status) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlKendaraan, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, merk);
            ps.setString(2, model);
            ps.setInt(3, tahun);
            ps.setDouble(4, harga);
            ps.setString(5, status);
            return ps;
        }, keyHolder);

        // --- Tahap B: Ambil ID Kendaraan yang baru dibuat ---
        Number newId = keyHolder.getKey();
        if (newId != null) {
            long idKendaraan = newId.longValue();

            // --- Tahap C: Simpan ke tabel spesifik (mobil/motor) ---
            if (jenisKendaraan.equalsIgnoreCase("Mobil")) {
                String sqlMobil = "INSERT INTO mobil (id_kendaraan, mesin_mobil, jenis_mobil, transmisi_mobil, kapasitas_mobil) VALUES (?, ?, ?, ?, ?)";
                jdbcTemplate.update(sqlMobil, idKendaraan, "-", "-", "-", 0);
                
            } else if (jenisKendaraan.equalsIgnoreCase("Motor")) {
                String sqlMotor = "INSERT INTO motor (id_kendaraan, cc, jenis_motor, kapasitas_tangki) VALUES (?, ?, ?, ?)";
                jdbcTemplate.update(sqlMotor, idKendaraan, 0, "-", 0.0);
            }
        }

        // --- Tahap D: Kembali ke halaman kelola kendaraan ---
        return "redirect:/kendaraan";
    }

    // 3. Menghapus Kendaraan (Delete)
    @GetMapping("/kendaraan/hapus/{id}")
    public String hapusKendaraan(@PathVariable("id") Long idKendaraan) {
        
        // Cukup hapus dari tabel kendaraan, tabel mobil/motor akan otomatis terhapus
        String sql = "DELETE FROM kendaraan WHERE id_kendaraan = ?";
        jdbcTemplate.update(sql, idKendaraan);
        
        // Refresh halaman
        return "redirect:/kendaraan";
    }
}
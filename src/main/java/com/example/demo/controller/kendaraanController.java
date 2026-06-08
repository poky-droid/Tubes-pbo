package com.example.demo.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class kendaraanController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Simpan foto di luar classpath agar bisa diakses langsung
    // Folder: <project_root>/uploads/kendaraan/
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "kendaraan" + File.separator;

    @GetMapping("/admin/kendaraan")
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

        int totalStok = daftarKendaraan.size();
        int totalMobil = 0, totalMotor = 0, totalTersedia = 0;

        for (Map<String, Object> k : daftarKendaraan) {
            String jenis = (String) k.get("jenis");
            String status = (String) k.get("status");
            if ("mobil".equals(jenis)) totalMobil++;
            if ("motor".equals(jenis)) totalMotor++;
            if ("Tersedia".equalsIgnoreCase(status)) totalTersedia++;
        }

        model.addAttribute("totalStok", totalStok);
        model.addAttribute("totalMobil", totalMobil);
        model.addAttribute("totalMotor", totalMotor);
        model.addAttribute("totalTersedia", totalTersedia);
        model.addAttribute("listKendaraan", daftarKendaraan);

        return "/admin/kendaraan";
    }

    @PostMapping("/admin/kendaraan/tambah")
    public String tambahKendaraan(
            @RequestParam("jenisKendaraan") String jenisKendaraan,
            @RequestParam("merk") String merk,
            @RequestParam("model") String model,
            @RequestParam("tahun") int tahun,
            @RequestParam("harga") double harga,
            @RequestParam("status") String status,
            @RequestParam(value = "warna", required = false) String warna,
            @RequestParam(value = "foto", required = false) MultipartFile foto
    ) {
        String namaFile = null;
        if (foto != null && !foto.isEmpty()) {
            try {
                File uploadFolder = new File(UPLOAD_DIR);
                if (!uploadFolder.exists()) uploadFolder.mkdirs();

                String ekstensi = "";
                String originalName = foto.getOriginalFilename();
                if (originalName != null && originalName.contains(".")) {
                    ekstensi = originalName.substring(originalName.lastIndexOf("."));
                }
                namaFile = UUID.randomUUID().toString() + ekstensi;
                Files.write(Paths.get(UPLOAD_DIR + namaFile), foto.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
                namaFile = null;
            }
        }

        String sqlKendaraan = "INSERT INTO kendaraan (merk, model, tahun, harga, status, foto) VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        final String fotoFinal = namaFile;

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlKendaraan, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, merk);
            ps.setString(2, model);
            ps.setInt(3, tahun);
            ps.setDouble(4, harga);
            ps.setString(5, status);
            ps.setString(6, fotoFinal);
            return ps;
        }, keyHolder);

        Number newId = keyHolder.getKey();
        if (newId != null) {
            long idKendaraan = newId.longValue();
            if (jenisKendaraan.equalsIgnoreCase("Mobil")) {
                jdbcTemplate.update("INSERT INTO mobil (id_kendaraan, mesin_mobil, jenis_mobil, transmisi_mobil, kapasitas_mobil) VALUES (?, ?, ?, ?, ?)",
                        idKendaraan, "-", "-", "-", 0);
            } else if (jenisKendaraan.equalsIgnoreCase("Motor")) {
                jdbcTemplate.update("INSERT INTO motor (id_kendaraan, cc, jenis_motor, kapasitas_tangki) VALUES (?, ?, ?, ?)",
                        idKendaraan, 0, "-", 0.0);
            }
        }

        return "redirect:/admin/kendaraan";
    }

    @GetMapping("/admin/kendaraan/hapus/{id}")
    public String hapusKendaraan(@PathVariable("id") Long idKendaraan) {
        try {
            String namaFoto = jdbcTemplate.queryForObject("SELECT foto FROM kendaraan WHERE id_kendaraan = ?", String.class, idKendaraan);
            if (namaFoto != null && !namaFoto.isEmpty()) {
                new File(UPLOAD_DIR + namaFoto).delete();
            }
        } catch (Exception e) { /* abaikan */ }

        jdbcTemplate.update("DELETE FROM kendaraan WHERE id_kendaraan = ?", idKendaraan);
        return "redirect:/admin/kendaraan";
    }
}
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
import org.springframework.http.HttpStatus;
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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
public class kendaraanController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "kendaraan" + File.separator;



    // ===== POST: submit pesanan =====
        @PostMapping("/buyer/pesan")
        public String pesanKendaraanSubmit(
                HttpSession session,
                @RequestParam("idKendaraan") Integer idKendaraan) {

            Integer idPembeli = (Integer) session.getAttribute("id_pembeli");
            if (idPembeli == null) return "redirect:/login?sessionExpired=true";

            try {
                Integer idOwner = null;
                try {
                    idOwner = jdbcTemplate.queryForObject(
                        "SELECT id_owner FROM owner LIMIT 1", Integer.class);
                } catch (Exception ex) {
                    System.err.println("Tidak ada data owner: " + ex.getMessage());
                }

                String cekStatus = jdbcTemplate.queryForObject(
                    "SELECT status FROM kendaraan WHERE id_kendaraan = ?",
                    String.class, idKendaraan);

                if (!"Tersedia".equalsIgnoreCase(cekStatus) && !"Test Drive".equalsIgnoreCase(cekStatus)) {
                    return "redirect:/?error=conflict";
                }

                jdbcTemplate.update(
                    "INSERT INTO penjualan (tanggal, status, id_pembeli, id_kendaraan, id_owner) " +
                    "VALUES (CURDATE(), 'Pending', ?, ?, ?)",
                    idPembeli, idKendaraan, idOwner);

                jdbcTemplate.update(
                    "UPDATE kendaraan SET status = 'Dipesan' WHERE id_kendaraan = ?",
                    idKendaraan);

            } catch (Exception e) {
                System.err.println("===== ERROR SUBMIT PESANAN =====");
                e.printStackTrace();
                return "redirect:/?error=gagal";
            }

            return "redirect:/buyer/pesanan?success=true";
        }
    

    @GetMapping("/buyer/kendaraan/{id}")
    public String detailKendaraan(@PathVariable Long id, Model model, HttpSession session) {

        // Session guard
        Integer idPembeli = (Integer) session.getAttribute("id_pembeli");
        if (idPembeli == null) return "redirect:/login?sessionExpired=true";

        String sql = """
            SELECT
                k.*,

                m.mesin_mobil,
                m.jenis_mobil,
                m.transmisi_mobil,
                m.kapasitas_mobil,

                mo.cc,
                mo.jenis_motor,
                mo.kapasitas_tangki,

                CASE
                    WHEN m.id_kendaraan IS NOT NULL THEN 'mobil'
                    WHEN mo.id_kendaraan IS NOT NULL THEN 'motor'
                END AS jenis

            FROM kendaraan k
            LEFT JOIN mobil m
                ON k.id_kendaraan = m.id_kendaraan
            LEFT JOIN motor mo
                ON k.id_kendaraan = mo.id_kendaraan
            WHERE k.id_kendaraan = ?
            """;

        List<Map<String, Object>> data = jdbcTemplate.queryForList(sql, id);

        if (data.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Kendaraan dengan ID " + id + " tidak ditemukan");
        }

        model.addAttribute("kendaraan", data.get(0));
        model.addAttribute("nama", session.getAttribute("nama"));

        return "buyer-kendaraan-detail";
    }

    public boolean isOwner(HttpSession session) {
        String role = (String) session.getAttribute("role");
        return role != null && role.equalsIgnoreCase("owner");
    }

   
    

    @GetMapping("/admin/kendaraan")
    public String showKendaraan(Model model, HttpSession session) {  // tambah HttpSession di sini
        String role = (String) session.getAttribute("role");
        System.out.println("=== DEBUG ROLE: " + role + " ===");
        if (!isOwner(session)) return "redirect:/login?accessDenied=true";

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

        model.addAttribute("nama", session.getAttribute("nama"));
        model.addAttribute("totalStok", totalStok);
        model.addAttribute("totalMobil", totalMobil);
        model.addAttribute("totalMotor", totalMotor);
        model.addAttribute("totalTersedia", totalTersedia);
        model.addAttribute("listKendaraan", daftarKendaraan);

        return "admin/kendaraan";  // hapus leading slash
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
            @RequestParam(value = "mesinMobil", required = false) String mesinMobil,
            @RequestParam(value = "jenisMobil", required = false) String jenisMobil,
            @RequestParam(value = "transmisiMobil", required = false) String transmisi,
            @RequestParam(value = "kapasitasMobil", required = false) Integer kapasitasMobil,
            @RequestParam(value = "cc", required = false) Integer cc,
            @RequestParam(value = "jenisMotor", required = false) String jenisMotor,
            @RequestParam(value = "kapasitasTangki", required = false) Double kapasitasTangki,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        if (!isOwner(session)) return "redirect:/login?accessDenied=true";

        // Validasi dulu sebelum insert apapun, supaya tidak ada row "yatim" di tabel kendaraan
        if (!jenisKendaraan.equalsIgnoreCase("Mobil") && !jenisKendaraan.equalsIgnoreCase("Motor")) {
            redirectAttributes.addFlashAttribute("error", "Jenis kendaraan tidak valid.");
            return "redirect:/admin/kendaraan";
        }

        // ── Upload foto, sekarang divalidasi juga di server (bukan cuma percaya accept="image/*" di HTML) ──
        String namaFile = null;
        if (foto != null && !foto.isEmpty()) {
            String contentType = foto.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                redirectAttributes.addFlashAttribute("error", "File harus berupa gambar.");
                return "redirect:/admin/kendaraan";
            }
            if (foto.getSize() > 5 * 1024 * 1024) {
                redirectAttributes.addFlashAttribute("error", "Ukuran foto maksimal 5MB.");
                return "redirect:/admin/kendaraan";
            }
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
                redirectAttributes.addFlashAttribute("error", "Gagal mengunggah foto, silakan coba lagi.");
                return "redirect:/admin/kendaraan";
            }
        }

        // ── Insert ke tabel induk kendaraan — sekarang termasuk warna ──
        String sqlKendaraan = "INSERT INTO kendaraan (merk, model, tahun, harga, status, warna, foto) VALUES (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        final String fotoFinal = namaFile;

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlKendaraan, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, merk);
            ps.setString(2, model);
            ps.setInt(3, tahun);
            ps.setDouble(4, harga);
            ps.setString(5, status);
            ps.setString(6, warna);
            ps.setString(7, fotoFinal);
            return ps;
        }, keyHolder);

        Number newId = keyHolder.getKey();
        if (newId == null) {
            redirectAttributes.addFlashAttribute("error", "Gagal menyimpan data kendaraan.");
            return "redirect:/admin/kendaraan";
        }
        long idKendaraan = newId.longValue();

        // ── Insert ke tabel spesifik — sekarang pakai nilai yang benar-benar dikirim user, bukan placeholder ──
        if (jenisKendaraan.equalsIgnoreCase("Mobil")) {
            jdbcTemplate.update(
                "INSERT INTO mobil (id_kendaraan, mesin_mobil, jenis_mobil, transmisi_mobil, kapasitas_mobil) VALUES (?, ?, ?, ?, ?)",
                idKendaraan,
                mesinMobil != null ? mesinMobil : "-",
                jenisMobil != null ? jenisMobil : "-",
                transmisi != null ? transmisi : "-",
                kapasitasMobil != null ? kapasitasMobil : 0);
        } else {
            jdbcTemplate.update(
                "INSERT INTO motor (id_kendaraan, cc, jenis_motor, kapasitas_tangki) VALUES (?, ?, ?, ?)",
                idKendaraan,
                cc != null ? cc : 0,
                jenisMotor != null ? jenisMotor : "-",
                kapasitasTangki != null ? kapasitasTangki : 0.0);
        }

        redirectAttributes.addFlashAttribute("success", "Kendaraan berhasil ditambahkan.");
        return "redirect:/admin/kendaraan";
    }

    @GetMapping("/admin/kendaraan/hapus/{id}")
    public String hapusKendaraan(@PathVariable("id") Long idKendaraan, HttpSession session) {
        if (!isOwner(session)) return "redirect:/login?accessDenied=true";

        try {
            String namaFoto = jdbcTemplate.queryForObject(
                "SELECT foto FROM kendaraan WHERE id_kendaraan = ?", String.class, idKendaraan);
            if (namaFoto != null && !namaFoto.isEmpty()) {
                new File(UPLOAD_DIR + namaFoto).delete();
            }
        } catch (Exception e) { /* abaikan */ }

        jdbcTemplate.update("DELETE FROM kendaraan WHERE id_kendaraan = ?", idKendaraan);
        return "redirect:/admin/kendaraan";
    }
}
package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;

@Controller
public class pembeliController extends BaseController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/admin/pembeli")
    public String showPembeli(Model model, HttpSession session) {
        if (!isOwner(session)) return "redirect:/login?accessDenied=true";

        try {
            // --- 1. LOGIKA KARTU STATISTIK PEMBELI ---
            Integer totalPembeli = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM pembeli", Integer.class);
            if (totalPembeli == null) totalPembeli = 0;

            Integer sudahBeli = jdbcTemplate.queryForObject("SELECT COUNT(DISTINCT id_pembeli) FROM penjualan WHERE status = 'Selesai'", Integer.class);
            if (sudahBeli == null) sudahBeli = 0;

            Integer pernahTestDrive = jdbcTemplate.queryForObject("SELECT COUNT(DISTINCT id_pembeli) FROM testdrive", Integer.class);
            if (pernahTestDrive == null) pernahTestDrive = 0;

            String sqlProspek = 
                "SELECT COUNT(DISTINCT p.id_pembeli) FROM pembeli p " +
                "LEFT JOIN penjualan pj ON p.id_pembeli = pj.id_pembeli AND pj.status = 'Pending' " +
                "LEFT JOIN testdrive td ON p.id_pembeli = td.id_pembeli AND td.status IN ('Aktif', 'Pending', 'Terkonfirmasi') " +
                "WHERE pj.id_penjualan IS NOT NULL OR td.id_testdrive IS NOT NULL";
            Integer prospekAktif = jdbcTemplate.queryForObject(sqlProspek, Integer.class);
            if (prospekAktif == null) prospekAktif = 0;


            // --- 2. AMBIL DATA TABEL PEMBELI ---
            // Menggunakan `user` (dengan backtick) agar aman dari reserved keyword MariaDB
            String sqlTabel = 
                "SELECT pb.id_pembeli, u.nama, u.username, u.email, pb.kontak, " +
                "(SELECT COUNT(*) FROM penjualan WHERE id_pembeli = pb.id_pembeli AND status = 'Selesai') as total_beli, " +
                "(SELECT COUNT(*) FROM testdrive WHERE id_pembeli = pb.id_pembeli) as total_td " +
                "FROM pembeli pb " +
                "JOIN `user` u ON pb.id_user = u.id_user " +
                "ORDER BY pb.id_pembeli DESC";
            List<Map<String, Object>> listPembeli = jdbcTemplate.queryForList(sqlTabel);

            // Mengirimkan data ke HTML
            model.addAttribute("totalPembeli", totalPembeli);
            model.addAttribute("sudahBeli", sudahBeli);
            model.addAttribute("pernahTestDrive", pernahTestDrive);
            model.addAttribute("prospekAktif", prospekAktif);
            model.addAttribute("listPembeli", listPembeli);

            return "/admin/pembeli"; 

        } catch (Exception e) {
            System.err.println("Error saat memuat halaman pembeli: " + e.getMessage());
            e.printStackTrace();
            return "/admin/pembeli"; 
        }
    }

    // --- 3. LOGIKA TAMBAH PEMBELI BARU ---
    @PostMapping("/admin/pembeli/tambah")
    public String tambahPembeli(
                HttpSession session,
            @RequestParam("nama") String nama,
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("kontak") String kontak,
            @RequestParam("password") String password) {
            if (!isOwner(session)) return "redirect:/login?accessDenied=true";
        
        try {
            // 1. Simpan ke tabel user (menggunakan backtick)
            String sqlUser = "INSERT INTO `user` (nama, username, email, password, role) VALUES (?, ?, ?, ?, 'Pembeli')";
            jdbcTemplate.update(sqlUser, nama, username, email, password);
            
            // 2. Ambil ID User berdasarkan username secara presisi
            String sqlGetId = "SELECT id_user FROM `user` WHERE username = ?";
            Integer idUser = jdbcTemplate.queryForObject(sqlGetId, Integer.class, username);
            
            // 3. Simpan ke tabel pembeli
            if (idUser != null) {
                String sqlPembeli = "INSERT INTO pembeli (id_user, kontak) VALUES (?, ?)";
                jdbcTemplate.update(sqlPembeli, idUser, kontak);
            }
            
        } catch (Exception e) {
            System.err.println("===== ERROR TAMBAH PEMBELI =====");
            System.err.println("Pesan Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        

        return "redirect:/admin/pembeli";
    }

    // --- 4. LOGIKA AMBIL DETAIL PEMBELI (AJAX) ---
    @GetMapping("/admin/pembeli/detail/{id}")
    @ResponseBody
    public Map<String, Object> getDetailPembeli(@PathVariable("id") Integer idPembeli, HttpSession session) {
         if (!isOwner(session)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Access Denied");
            return response;
        }
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. Ambil angka statistik
            Integer totalBeli = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM penjualan WHERE id_pembeli = ? AND status = 'Selesai'", Integer.class, idPembeli);
            Integer totalTd = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM testdrive WHERE id_pembeli = ?", Integer.class, idPembeli);

            response.put("totalBeli", totalBeli != null ? totalBeli : 0);
            response.put("totalTd", totalTd != null ? totalTd : 0);

            // 2. Ambil riwayat aktivitas (Gabungan dari tabel penjualan dan testdrive menggunakan UNION)
            String sqlAktivitas = 
                "SELECT * FROM (" +
                "  SELECT p.tanggal, k.merk, k.model, p.status, 'Beli' as jenis " +
                "  FROM penjualan p JOIN kendaraan k ON p.id_kendaraan = k.id_kendaraan " +
                "  WHERE p.id_pembeli = ? " +
                "  UNION ALL " +
                "  SELECT td.tanggal, k.merk, k.model, td.status, 'Test Drive' as jenis " +
                "  FROM testdrive td JOIN kendaraan k ON td.id_kendaraan = k.id_kendaraan " +
                "  WHERE td.id_pembeli = ? " +
                ") AS riwayat " +
                "ORDER BY tanggal DESC";
            
            List<Map<String, Object>> activities = jdbcTemplate.queryForList(sqlAktivitas, idPembeli, idPembeli);
            
            response.put("activities", activities);
            response.put("success", true);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

}
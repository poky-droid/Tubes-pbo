package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public class ownerController extends BaseController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/admin/profil")
    public String showProfil(Model model, @RequestParam(value = "error", required = false) String error, HttpSession session) {
        if (!isOwner(session)) return "redirect:/login?accessDenied=true";

        try {
            // 1. Ambil data Owner
            String sqlOwner = "SELECT u.id_user, o.id_owner, u.nama, u.email, o.kontak, o.kota, o.provinsi " +
                              "FROM `user` u JOIN owner o ON u.id_user = o.id_user LIMIT 1";
            List<Map<String, Object>> owners = jdbcTemplate.queryForList(sqlOwner);
            
            // Jika kosong, otomatis buat 1 akun default
            if (owners.isEmpty()) {
                jdbcTemplate.update("INSERT IGNORE INTO `user` (nama, email, password, role) VALUES ('Admin Owner', 'owner@autoprime.id', '12345', 'Owner')");
                Integer newUserId = jdbcTemplate.queryForObject("SELECT id_user FROM `user` WHERE nama = 'Admin Owner'", Integer.class);
                jdbcTemplate.update("INSERT INTO owner (id_user, kontak, kota, provinsi) VALUES (?, '0812-9999-8888', 'Tasikmalaya', 'Jawa Barat')", newUserId);
                owners = jdbcTemplate.queryForList(sqlOwner); // Ambil ulang data
            }
            
            model.addAttribute("owner", owners.get(0));

            // 2. Ambil Statistik Cepat
            Integer totalKendaraan = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM kendaraan", Integer.class);
            Integer totalPenjualan = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM penjualan WHERE status = 'Selesai'", Integer.class);
            Integer totalPembeli = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM pembeli", Integer.class);
            
            model.addAttribute("totalKendaraan", totalKendaraan != null ? totalKendaraan : 0);
            model.addAttribute("totalPenjualan", totalPenjualan != null ? totalPenjualan : 0);
            model.addAttribute("totalPembeli", totalPembeli != null ? totalPembeli : 0);

            // 3. Ambil Badge Notifikasi (Menunggu ACC)
            Integer pendingJual = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM penjualan WHERE status = 'Pending'", Integer.class);
            Integer pendingTD = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM testdrive WHERE status = 'Pending'", Integer.class);
            model.addAttribute("totalPending", (pendingJual != null ? pendingJual : 0) + (pendingTD != null ? pendingTD : 0));

            // 4. Riwayat Aktivitas Log (Campuran Penjualan & Test Drive)
            String sqlLog = 
                "SELECT 'Penjualan' as tipe, p.status, k.merk, k.model, p.tanggal as tgl_asli " +
                "FROM penjualan p JOIN kendaraan k ON p.id_kendaraan = k.id_kendaraan " +
                "UNION ALL " +
                "SELECT 'Test Drive' as tipe, td.status, k.merk, k.model, td.tanggal as tgl_asli " +
                "FROM testdrive td JOIN kendaraan k ON td.id_kendaraan = k.id_kendaraan " +
                "ORDER BY tgl_asli DESC LIMIT 4";
            model.addAttribute("logAktivitas", jdbcTemplate.queryForList(sqlLog));
            model.addAttribute("errorPass", error != null);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "admin/profil";
    }

    // --- FUNGSI UPDATE INFORMASI PROFIL ---
    @PostMapping("/admin/profil/update")
    public String updateProfil(
            HttpSession session,
            @RequestParam("idUser") Integer idUser,
            @RequestParam("idOwner") Integer idOwner,
            @RequestParam("nama") String nama,
            @RequestParam("email") String email,
            @RequestParam("kontak") String kontak,
            @RequestParam("kota") String kota,
            @RequestParam("provinsi") String provinsi) {
            if (!isOwner(session)) return "redirect:/login?accessDenied=true";
        
        try {
            jdbcTemplate.update("UPDATE `user` SET nama = ?, email = ? WHERE id_user = ?", nama, email, idUser);
            jdbcTemplate.update("UPDATE owner SET kontak = ?, kota = ?, provinsi = ? WHERE id_owner = ?", kontak, kota, provinsi, idOwner);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/admin/profil";
    }

    // --- FUNGSI GANTI PASSWORD ---
    @PostMapping("admin/profil/password")
    public String updatePassword(
            HttpSession session,
            @RequestParam("idUser") Integer idUser,
            @RequestParam("passwordLama") String passwordLama,
            @RequestParam("passwordBaru") String passwordBaru) {
            if (!isOwner(session)) return "redirect:/login?accessDenied=true";

        try {
            String passAsli = jdbcTemplate.queryForObject("SELECT password FROM `user` WHERE id_user = ?", String.class, idUser);
            if (passAsli != null && passAsli.equals(passwordLama)) {
                jdbcTemplate.update("UPDATE `user` SET password = ? WHERE id_user = ?", passwordBaru, idUser);
                return "redirect:/profil";
            } else {
                return "redirect:/profil?error=true"; // Password lama salah
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin/profil";
        }
    }
}
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
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;

@Controller
public class testdriveController  extends BaseController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    

    // Admin testdrive page (original)
    @GetMapping("/admin/testdrive")
    public String adminShowTestDrive(
            Model model, 
            HttpSession session,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "success", required = false) String success) {
            
         if (!isOwner(session)) return "redirect:/login?accessDenied=true";
         
         if (error != null && error.equals("conflict")) {
             model.addAttribute("errorMsg", "Jadwal yang dipilih sudah dibooking. Silakan pilih waktu lain.");
         }
         if (success != null) {
             model.addAttribute("successMsg", "Jadwal berhasil disimpan.");
         }
        
        try {
            // 1. Ambil data Daftar Jadwal Test Drive
            String sqlTabel = 
                "SELECT td.*, u.nama as pembeli, k.merk, k.model, k.tahun, k.foto, " +
                "CASE WHEN m.id_kendaraan IS NOT NULL THEN 'mobil' " +
                "     WHEN mo.id_kendaraan IS NOT NULL THEN 'motor' END as jenis " +
                "FROM testdrive td " +
                "JOIN pembeli pb ON td.id_pembeli = pb.id_pembeli " +
                "JOIN user u ON pb.id_user = u.id_user " +
                "JOIN kendaraan k ON td.id_kendaraan = k.id_kendaraan " +
                "LEFT JOIN mobil m ON k.id_kendaraan = m.id_kendaraan " +
                "LEFT JOIN motor mo ON k.id_kendaraan = mo.id_kendaraan " +
                "ORDER BY td.tanggal DESC, td.jam DESC";
            List<Map<String, Object>> listTestDrive = jdbcTemplate.queryForList(sqlTabel);
            
            // 2. Ambil data Dropdown 'Pembeli'
            String sqlPembeli = "SELECT pb.id_pembeli, u.nama FROM pembeli pb JOIN user u ON pb.id_user = u.id_user";
            List<Map<String, Object>> listPembeli = jdbcTemplate.queryForList(sqlPembeli);
            
            // 3. Ambil data Dropdown 'Kendaraan' (Hanya yang Tersedia)
            String sqlKendaraan = "SELECT id_kendaraan, merk, model FROM kendaraan WHERE status = 'Tersedia'";
            List<Map<String, Object>> listKendaraan = jdbcTemplate.queryForList(sqlKendaraan);

            // --- 4. LOGIKA KARTU STATISTIK TEST DRIVE ---
            
            // Aktif Hari Ini
            Integer aktifHariIni = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM testdrive WHERE status IN ('Aktif', 'Terkonfirmasi') AND tanggal = CURRENT_DATE", Integer.class);
            if (aktifHariIni == null) aktifHariIni = 0;

            // Selesai Bulan Ini
            Integer selesaiBulanIni = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM testdrive WHERE status = 'Selesai' AND MONTH(tanggal) = MONTH(CURRENT_DATE) AND YEAR(tanggal) = YEAR(CURRENT_DATE)", Integer.class);
            if (selesaiBulanIni == null) selesaiBulanIni = 0;

            // Menunggu Konfirmasi (Pending)
            Integer menungguKonfirmasi = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM testdrive WHERE status = 'Pending'", Integer.class);
            if (menungguKonfirmasi == null) menungguKonfirmasi = 0;

            // Dibatalkan / Ditolak
            Integer dibatalkan = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM testdrive WHERE status IN ('Dibatalkan', 'Ditolak')", Integer.class);
            if (dibatalkan == null) dibatalkan = 0;

            // Mengirimkan semua data ke HTML
            model.addAttribute("listTestDrive", listTestDrive);
            model.addAttribute("listPembeli", listPembeli);
            model.addAttribute("listKendaraan", listKendaraan);
            
            model.addAttribute("aktifHariIni", aktifHariIni);
            model.addAttribute("selesaiBulanIni", selesaiBulanIni);
            model.addAttribute("menungguKonfirmasi", menungguKonfirmasi);
            model.addAttribute("dibatalkan", dibatalkan);
            
        } catch (Exception e) {
            System.err.println("Error loading admin testdrive page: " + e.getMessage());
            e.printStackTrace();
        }

        return "/admin/testdrive"; 
    }

    // --- FUNGSI UNTUK MENANGKAP DATA TAMBAH JADWAL TEST DRIVE ---
    @PostMapping("/admin/testdrive/tambah")
    public String tambahTestDrive(
            HttpSession session,
            @RequestParam("idPembeli") Integer idPembeli,
            @RequestParam("idKendaraan") Integer idKendaraan,
            @RequestParam("tanggal") String tanggal,
            @RequestParam("jam") String jam,
            @RequestParam("status") String status,
            @RequestParam(value = "catatan", required = false) String catatan) {
            if (!isOwner(session)) return "redirect:/login?accessDenied=true";
        
        try {
            // Memasukkan data jadwal baru ke dalam tabel testdrive
            // Validasi & Insert menggunakan 1 query untuk mencegah Race Condition:
            // Hanya insert jika belum ada booking aktif (Pending/Aktif/Terkonfirmasi)
            // di kendaraan, tanggal, dan jam yang sama.
            String sql = 
                "INSERT INTO testdrive (id_pembeli, id_kendaraan, tanggal, jam, status, catatan) " +
                "SELECT ?, ?, ?, ?, ?, ? FROM DUAL " +
                "WHERE NOT EXISTS (" +
                "   SELECT 1 FROM testdrive " +
                "   WHERE id_kendaraan = ? AND tanggal = ? AND jam = ? " +
                "   AND status IN ('Pending', 'Aktif', 'Terkonfirmasi')" +
                ")";
            
            int rowsAffected = jdbcTemplate.update(sql, 
                idPembeli, idKendaraan, tanggal, jam, status, catatan,
                idKendaraan, tanggal, jam);

            if (rowsAffected == 0) {
                // Booking gagal karena jadwal sudah ada yang ambil
                return "redirect:/admin/testdrive?error=conflict";
            }
            
        } catch (Exception e) {
            System.err.println("===== ERROR TAMBAH TEST DRIVE =====");
            System.err.println("Pesan Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Setelah berhasil menyimpan, refresh halaman kembali ke menu test drive
        return "redirect:/admin/testdrive?success=true";
    }

    // --- FUNGSI UNTUK MENGUBAH STATUS TEST DRIVE (ACC / TOLAK / SELESAI) ---
    @PostMapping("/admin/testdrive/updateStatus")
    public String updateStatusTestDrive(
                HttpSession session,
            @RequestParam("idTestdrive") Integer idTestdrive,
            @RequestParam("status") String status) {
            if (!isOwner(session)) return "redirect:/login?accessDenied=true";
        
        try {
            // Melakukan update status di database berdasarkan ID Test Drive
            String sql = "UPDATE testdrive SET status = ? WHERE id_testdrive = ?";
            jdbcTemplate.update(sql, status, idTestdrive);
            
        } catch (Exception e) {
            System.err.println("===== ERROR UPDATE STATUS TEST DRIVE =====");
            e.printStackTrace();
        }
        
        // Refresh halaman kembali ke menu test drive
        return "redirect:/admin/testdrive";
    }




    // //buyer page (baru, khusus buyer)
    //     @GetMapping("/buyer/testdrive")
    //     public String buyerShowTestDrive() {
    //         return "buyer-testdrive";
    //      }


    // --- BUYER: SUBMIT JADWAL TEST DRIVE BARU ---
    @PostMapping("/buyer/testdrive")
    public String buyerSubmitTestDrive(
            HttpSession session,
            @RequestParam("idKendaraan") Integer idKendaraan,
            @RequestParam("tanggal") String tanggal,
            @RequestParam("jam") String jam,
            @RequestParam(value = "catatan", required = false) String catatan) {

        Integer idPembeli = (Integer) session.getAttribute("id_pembeli");
        if (idPembeli == null) return "redirect:/login?sessionExpired=true";

        try {
            // Ambil id_owner pertama yang ada (showroom owner)
            Integer idOwner = null;
            try {
                idOwner = jdbcTemplate.queryForObject(
                    "SELECT id_owner FROM owner LIMIT 1", Integer.class);
            } catch (Exception ex) {
                System.err.println("Tidak ada data owner: " + ex.getMessage());
            }

            // Validasi & Insert menggunakan 1 query untuk mencegah Race Condition:
            // Hanya insert jika belum ada booking aktif (Pending/Aktif/Terkonfirmasi)
            // di kendaraan, tanggal, dan jam yang sama.
            String sql = 
                "INSERT INTO testdrive (id_pembeli, id_kendaraan, tanggal, jam, status, catatan, id_owner) " +
                "SELECT ?, ?, ?, ?, 'Pending', ?, ? FROM DUAL " +
                "WHERE NOT EXISTS (" +
                "   SELECT 1 FROM testdrive " +
                "   WHERE id_kendaraan = ? AND tanggal = ? AND jam = ? " +
                "   AND status IN ('Pending', 'Aktif', 'Terkonfirmasi')" +
                ")";
            
            int rowsAffected = jdbcTemplate.update(sql, 
                idPembeli, idKendaraan, tanggal, jam, catatan, idOwner,
                idKendaraan, tanggal, jam);
               

            if (rowsAffected == 0) {
                // Booking gagal karena jadwal sudah ada yang ambil
                return "redirect:/buyer/testdrive?error=conflict&kendaraan=" + idKendaraan;
            }


            System.out.println("Jadwal test drive berhasil dibuat untuk pembeli " + idPembeli);

        } catch (Exception e) {
            System.err.println("===== ERROR SUBMIT TEST DRIVE BUYER =====");
            System.err.println("Pesan Error: " + e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/buyer/testdrive?success=true";
    }

    // --- API UNTUK MENGAMBIL SLOT JADWAL YANG SUDAH DIBOOKING ---
    @GetMapping("/api/testdrive/booked")
    @ResponseBody
    public List<Map<String, Object>> getBookedSlots(@RequestParam("idKendaraan") Integer idKendaraan) {
        String sql = "SELECT tanggal, jam FROM testdrive WHERE id_kendaraan = ? AND status IN ('Pending', 'Aktif', 'Terkonfirmasi')";
        return jdbcTemplate.queryForList(sql, idKendaraan);
    }

}
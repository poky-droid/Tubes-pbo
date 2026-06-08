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

@Controller
public class penjualanController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/admin/penjualan")
    public String showPenjualan(Model model) {
        // --- 1. LOGIKA STATISTIK (Menghitung Pendapatan dari Harga Kendaraan) ---
        String sqlPendapatan = "SELECT SUM(k.harga) FROM penjualan p JOIN kendaraan k ON p.id_kendaraan = k.id_kendaraan WHERE p.status = 'Selesai'";
        Double totalPendapatan = jdbcTemplate.queryForObject(sqlPendapatan, Double.class);
        if (totalPendapatan == null) totalPendapatan = 0.0;
        
        String totalPendapatanStr = "0";
        if (totalPendapatan >= 1000000000) {
            totalPendapatanStr = String.format("%.2f M", totalPendapatan / 1000000000.0).replace(",", ".");
        } else if (totalPendapatan >= 1000000) {
            totalPendapatanStr = String.format("%.0f Jt", totalPendapatan / 1000000.0).replace(",", ".");
        } else {
            totalPendapatanStr = String.format("%.0f", totalPendapatan);
        }

        Double revBulanIni = jdbcTemplate.queryForObject("SELECT SUM(k.harga) FROM penjualan p JOIN kendaraan k ON p.id_kendaraan = k.id_kendaraan WHERE p.status = 'Selesai' AND MONTH(p.tanggal) = MONTH(CURRENT_DATE) AND YEAR(p.tanggal) = YEAR(CURRENT_DATE)", Double.class);
        if (revBulanIni == null) revBulanIni = 0.0;
        Double revBulanLalu = jdbcTemplate.queryForObject("SELECT SUM(k.harga) FROM penjualan p JOIN kendaraan k ON p.id_kendaraan = k.id_kendaraan WHERE p.status = 'Selesai' AND MONTH(p.tanggal) = MONTH(CURRENT_DATE - INTERVAL 1 MONTH) AND YEAR(p.tanggal) = YEAR(CURRENT_DATE - INTERVAL 1 MONTH)", Double.class);
        if (revBulanLalu == null) revBulanLalu = 0.0;
        
        int pctPendapatan = 0;
        if (revBulanLalu > 0) {
            pctPendapatan = (int) Math.round(((revBulanIni - revBulanLalu) / revBulanLalu) * 100);
        } else if (revBulanIni > 0) {
            pctPendapatan = 100;
        }

        // --- 2. LOGIKA STATISTIK LAINNYA ---
        Integer totalSelesai = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM penjualan WHERE status = 'Selesai'", Integer.class);
        if (totalSelesai == null) totalSelesai = 0;
        Integer selesaiBulanIni = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM penjualan WHERE status = 'Selesai' AND MONTH(tanggal) = MONTH(CURRENT_DATE) AND YEAR(tanggal) = YEAR(CURRENT_DATE)", Integer.class);
        if (selesaiBulanIni == null) selesaiBulanIni = 0;

        Integer totalPending = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM penjualan WHERE status = 'Pending'", Integer.class);
        if (totalPending == null) totalPending = 0;
        Integer pendingHariIni = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM penjualan WHERE status = 'Pending' AND tanggal = CURRENT_DATE", Integer.class);
        if (pendingHariIni == null) pendingHariIni = 0;

        Integer totalDitolak = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM penjualan WHERE status = 'Ditolak'", Integer.class);
        if (totalDitolak == null) totalDitolak = 0;
        Integer ditolakHariIni = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM penjualan WHERE status = 'Ditolak' AND tanggal = CURRENT_DATE", Integer.class);
        if (ditolakHariIni == null) ditolakHariIni = 0;

        // --- 3. AMBIL DATA DROPDOWN ---
        // Menggunakan backtick untuk `user`
        String sqlPembeli = "SELECT pb.id_pembeli, u.nama FROM pembeli pb JOIN `user` u ON pb.id_user = u.id_user";
        List<Map<String, Object>> listPembeli = jdbcTemplate.queryForList(sqlPembeli);
        
        String sqlKendaraan = "SELECT id_kendaraan, merk, model, harga FROM kendaraan WHERE status = 'Tersedia'";
        List<Map<String, Object>> listKendaraan = jdbcTemplate.queryForList(sqlKendaraan);

        // --- 4. AMBIL DATA TABEL PENJUALAN ---
        String sqlTabel = 
            "SELECT pj.id_penjualan, u.nama as pembeli, k.id_kendaraan, k.merk, k.model, k.harga, pj.tanggal, pj.status " +
            "FROM penjualan pj " +
            "JOIN pembeli pb ON pj.id_pembeli = pb.id_pembeli " +
            "JOIN `user` u ON pb.id_user = u.id_user " +
            "JOIN kendaraan k ON pj.id_kendaraan = k.id_kendaraan " +
            "ORDER BY pj.id_penjualan DESC";
        List<Map<String, Object>> listPenjualan = jdbcTemplate.queryForList(sqlTabel);

        // --- 5. KIRIM DATA KE HTML ---
        model.addAttribute("totalPendapatanStr", totalPendapatanStr);
        model.addAttribute("pctPendapatan", Math.abs(pctPendapatan));
        model.addAttribute("isRevNaik", pctPendapatan >= 0);
        model.addAttribute("totalSelesai", totalSelesai);
        model.addAttribute("selesaiBulanIni", selesaiBulanIni);
        model.addAttribute("totalPending", totalPending);
        model.addAttribute("pendingHariIni", pendingHariIni);
        model.addAttribute("totalDitolak", totalDitolak);
        model.addAttribute("ditolakHariIni", ditolakHariIni);
        
        model.addAttribute("listPembeli", listPembeli);
        model.addAttribute("listKendaraan", listKendaraan);
        model.addAttribute("listPenjualan", listPenjualan);

        return "/admin/penjualan";
    }

    // --- FUNGSI TAMBAH TRANSAKSI (SESUAI STRUKTUR TABEL ASLI) ---
    @PostMapping("/admin/penjualan/tambah")
    public String tambahPenjualan(
            @RequestParam("idPembeli") Integer idPembeli,
            @RequestParam("idKendaraan") Integer idKendaraan,
            @RequestParam("tanggal") String tanggal,
            @RequestParam(value = "status", defaultValue = "Pending") String status) {
        
        try {
            // Hanya memasukkan id_pembeli, id_kendaraan, tanggal, status. (id_owner dibiarkan NULL)
            String sql = "INSERT INTO penjualan (id_pembeli, id_kendaraan, tanggal, status) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(sql, idPembeli, idKendaraan, tanggal, status);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return "redirect:/penjualan";
    }
    
    // --- FUNGSI UPDATE STATUS PENJUALAN (ACC / TOLAK) ---
    @PostMapping("/admin/penjualan/updateStatus")
    public String updateStatusPenjualan(
            @RequestParam("idPenjualan") Integer idPenjualan,
            @RequestParam("status") String status,
            @RequestParam("idKendaraan") Integer idKendaraan) {
        
        try {
            // Ubah status di tabel penjualan
            String sql = "UPDATE penjualan SET status = ? WHERE id_penjualan = ?";
            jdbcTemplate.update(sql, status, idPenjualan);
            
            // Jika di-ACC (Selesai), ubah mobil jadi 'Terjual'
            if ("Selesai".equalsIgnoreCase(status)) {
                jdbcTemplate.update("UPDATE kendaraan SET status = 'Terjual' WHERE id_kendaraan = ?", idKendaraan);
            } 
            // Jika ditolak, kembalikan mobil jadi 'Tersedia'
            else if ("Ditolak".equalsIgnoreCase(status)) {
                jdbcTemplate.update("UPDATE kendaraan SET status = 'Tersedia' WHERE id_kendaraan = ?", idKendaraan);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/admin/penjualan";
    }
}
package com.example.demo.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;

@Controller
public class laporanController extends BaseController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/admin/laporan")
    public String showLaporan(Model model, HttpSession session) {
         if (!isOwner(session)) return "redirect:/login?accessDenied=true";
        try {
            // --- 1. KARTU STATISTIK ATAS ---
            
            // A. Total Pendapatan
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

            // B. Unit Terjual
            Integer unitTerjual = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM penjualan WHERE status = 'Selesai'", Integer.class);
            if (unitTerjual == null) unitTerjual = 0;

            // C. Total Test Drive
            Integer totalTD = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM testdrive", Integer.class);
            if (totalTD == null) totalTD = 0;

            // D. Konversi TD -> Beli (%)
            int konversi = 0;
            if (totalTD > 0) {
                konversi = (int) Math.round(((double) unitTerjual / totalTD) * 100);
            }

            // --- 2. DATA GRAFIK PENDAPATAN BULANAN (Tahun Ini) ---
            List<Double> chartDataList = new ArrayList<>(Collections.nCopies(12, 0.0));
            String sqlChart = "SELECT MONTH(p.tanggal) as bulan, SUM(k.harga) as total " +
                              "FROM penjualan p JOIN kendaraan k ON p.id_kendaraan = k.id_kendaraan " +
                              "WHERE p.status = 'Selesai' AND YEAR(p.tanggal) = YEAR(CURRENT_DATE) " +
                              "GROUP BY MONTH(p.tanggal)";
            List<Map<String, Object>> chartRows = jdbcTemplate.queryForList(sqlChart);
            for(Map<String, Object> row : chartRows) {
                int bulan = ((Number) row.get("bulan")).intValue();
                double total = ((Number) row.get("total")).doubleValue();
                chartDataList.set(bulan - 1, total); // Set ke index array (Bulan 1 = index 0)
            }

            // --- 3. TOP 5 KENDARAAN TERLARIS ---
            String sqlTop = "SELECT k.merk, k.model, COUNT(p.id_penjualan) as terjual " +
                            "FROM penjualan p JOIN kendaraan k ON p.id_kendaraan = k.id_kendaraan " +
                            "WHERE p.status = 'Selesai' " +
                            "GROUP BY k.id_kendaraan " +
                            "ORDER BY terjual DESC LIMIT 5";
            List<Map<String, Object>> topKendaraan = jdbcTemplate.queryForList(sqlTop);
            
            // Hitung persentase untuk panjang warna emas (Progress Bar)
            long maxTerjual = 1;
            if (!topKendaraan.isEmpty()) {
                maxTerjual = ((Number) topKendaraan.get(0).get("terjual")).longValue();
            }
            for(Map<String, Object> k : topKendaraan) {
                long terjual = ((Number) k.get("terjual")).longValue();
                int pct = (int) Math.round(((double) terjual / maxTerjual) * 100);
                k.put("persentase", pct);
            }

            // --- 4. TABEL TRANSAKSI LENGKAP ---
            String sqlTabel = 
                "SELECT p.tanggal, p.id_penjualan, u.nama as pembeli, k.merk, k.model, " +
                "CASE WHEN m.id_kendaraan IS NOT NULL THEN 'Mobil' " +
                "     WHEN mo.id_kendaraan IS NOT NULL THEN 'Motor' ELSE '-' END as jenis, " +
                "k.harga, (k.harga * 0.1) as pajak, p.status " +
                "FROM penjualan p " +
                "JOIN pembeli pb ON p.id_pembeli = pb.id_pembeli " +
                "JOIN `user` u ON pb.id_user = u.id_user " +
                "JOIN kendaraan k ON p.id_kendaraan = k.id_kendaraan " +
                "LEFT JOIN mobil m ON k.id_kendaraan = m.id_kendaraan " +
                "LEFT JOIN motor mo ON k.id_kendaraan = mo.id_kendaraan " +
                "ORDER BY p.tanggal DESC";
            List<Map<String, Object>> listTransaksi = jdbcTemplate.queryForList(sqlTabel);

            // --- KIRIM SEMUA DATA KE HTML ---
            model.addAttribute("totalPendapatanStr", totalPendapatanStr);
            model.addAttribute("unitTerjual", unitTerjual);
            model.addAttribute("totalTD", totalTD);
            model.addAttribute("konversi", konversi);
            model.addAttribute("chartData", chartDataList); // Kirim List ke JS Chart
            model.addAttribute("topKendaraan", topKendaraan);
            model.addAttribute("listTransaksi", listTransaksi);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "admin/laporan";
    }
}
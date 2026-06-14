package com.example.demo.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;

@Controller
public class dashboardController extends BaseController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/admin/Dashboard")
    public String showDashboard(Model model, HttpSession session) {
        if (!isOwner(session)) return "redirect:/login?accessDenied=true";

        // --- LOGIKA AKTIVITAS TERBARU ---
        String sqlAktivitas =
            "SELECT 'Penjualan' as jenis, p.status, p.tanggal, k.merk, k.model, u.nama as aktor " +
            "FROM penjualan p " +
            "JOIN kendaraan k ON p.id_kendaraan = k.id_kendaraan " +
            "JOIN pembeli pb ON p.id_pembeli = pb.id_pembeli " +
            "JOIN `user` u ON pb.id_user = u.id_user " +
            "UNION ALL " +
            "SELECT 'Test Drive' as jenis, t.status, t.tanggal, k.merk, k.model, u.nama as aktor " +
            "FROM testdrive t " +
            "JOIN kendaraan k ON t.id_kendaraan = k.id_kendaraan " +
            "JOIN pembeli pb ON t.id_pembeli = pb.id_pembeli " +
            "JOIN `user` u ON pb.id_user = u.id_user " +
            "ORDER BY tanggal DESC LIMIT 5";

        List<Map<String, Object>> listAktivitas = jdbcTemplate.queryForList(sqlAktivitas);
        model.addAttribute("listAktivitas", listAktivitas);

        // --- LOGIKA GRAFIK PENJUALAN 6 BULAN TERAKHIR ---
        List<Map<String, Object>> grafikList = new ArrayList<>();
        LocalDate now = LocalDate.now();
        double maxPendapatan = 1.0;
        double totalPendapatan6Bulan = 0.0;

        String[] namaBulanIndo = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Ags", "Sep", "Okt", "Nov", "Des"};

        for (int i = 5; i >= 0; i--) {
            LocalDate targetMonth = now.minusMonths(i);
            int m = targetMonth.getMonthValue();
            int y = targetMonth.getYear();

            String sqlPendapatan = "SELECT SUM(k.harga) FROM penjualan p " +
                                   "JOIN kendaraan k ON p.id_kendaraan = k.id_kendaraan " +
                                   "WHERE p.status = 'Selesai' AND MONTH(p.tanggal) = ? AND YEAR(p.tanggal) = ?";

            Double totalBulanIni = jdbcTemplate.queryForObject(sqlPendapatan, Double.class, m, y);
            if (totalBulanIni == null) totalBulanIni = 0.0;

            totalPendapatan6Bulan += totalBulanIni;
            if (totalBulanIni > maxPendapatan) maxPendapatan = totalBulanIni;

            Map<String, Object> dataBulan = new HashMap<>();
            dataBulan.put("label", namaBulanIndo[m - 1]);
            dataBulan.put("total", totalBulanIni);
            dataBulan.put("isCurrent", i == 0);
            grafikList.add(dataBulan);
        }

        for (Map<String, Object> map : grafikList) {
            double total = (double) map.get("total");
            int height = (int) ((total / maxPendapatan) * 100);
            if (total > 0 && height < 5) height = 5;
            if (total == 0) height = 0;
            map.put("height", height);
        }

        model.addAttribute("totalPendapatan6Bulan", totalPendapatan6Bulan);
        model.addAttribute("grafikPenjualan", grafikList);

        // --- LOGIKA PERSENTASE PERTUMBUHAN (VS PERIODE LALU) ---
        double totalPendapatan6BulanLalu = 0.0;

        for (int i = 11; i >= 6; i--) {
            LocalDate targetMonth = now.minusMonths(i);
            int m = targetMonth.getMonthValue();
            int y = targetMonth.getYear();

            String sqlPendapatanLalu = "SELECT SUM(k.harga) FROM penjualan p " +
                                       "JOIN kendaraan k ON p.id_kendaraan = k.id_kendaraan " +
                                       "WHERE p.status = 'Selesai' AND MONTH(p.tanggal) = ? AND YEAR(p.tanggal) = ?";

            Double totalBulanIniLalu = jdbcTemplate.queryForObject(sqlPendapatanLalu, Double.class, m, y);
            if (totalBulanIniLalu == null) totalBulanIniLalu = 0.0;

            totalPendapatan6BulanLalu += totalBulanIniLalu;
        }

        double persentase = 0.0;
        if (totalPendapatan6BulanLalu > 0) {
            persentase = ((totalPendapatan6Bulan - totalPendapatan6BulanLalu) / totalPendapatan6BulanLalu) * 100;
        } else if (totalPendapatan6Bulan > 0) {
            persentase = 100.0;
        }

        model.addAttribute("persentasePertumbuhan", Math.round(Math.abs(persentase)));
        model.addAttribute("isPertumbuhanPositif", persentase >= 0);

        // --- KARTU STATISTIK ---
        Integer totalKendaraan = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM kendaraan", Integer.class);

        Integer penjualanBulanIni = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM penjualan WHERE MONTH(tanggal) = MONTH(CURRENT_DATE()) AND YEAR(tanggal) = YEAR(CURRENT_DATE())", Integer.class);

        Integer testDriveAktif = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM testdrive WHERE status = 'Aktif'", Integer.class);

        Integer permintaanPending = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM penjualan WHERE status = 'Pending'", Integer.class);

        // --- SUB-TEKS KARTU STATISTIK ---
        Integer kendaraanTersedia = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM kendaraan WHERE status = 'Tersedia'", Integer.class);
        if (kendaraanTersedia == null) kendaraanTersedia = 0;

        Integer penjualanBulanLalu = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM penjualan WHERE MONTH(tanggal) = MONTH(CURRENT_DATE - INTERVAL 1 MONTH) AND YEAR(tanggal) = YEAR(CURRENT_DATE - INTERVAL 1 MONTH)", Integer.class);
        if (penjualanBulanLalu == null) penjualanBulanLalu = 0;

        int persenPenjualan = 0;
        if (penjualanBulanLalu > 0) {
            persenPenjualan = (int) Math.round(((double)(penjualanBulanIni - penjualanBulanLalu) / penjualanBulanLalu) * 100);
        } else if (penjualanBulanIni != null && penjualanBulanIni > 0) {
            persenPenjualan = 100;
        }

        Integer testDriveHariIni = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM testdrive WHERE status = 'Aktif' AND tanggal = CURRENT_DATE", Integer.class);
        if (testDriveHariIni == null) testDriveHariIni = 0;

        Integer pendingKemarin = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM penjualan WHERE status = 'Pending' AND tanggal = CURRENT_DATE - INTERVAL 1 DAY", Integer.class);
        if (pendingKemarin == null) pendingKemarin = 0;

        Integer pendingHariIniStat = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM penjualan WHERE status = 'Pending' AND tanggal = CURRENT_DATE", Integer.class);
        if (pendingHariIniStat == null) pendingHariIniStat = 0;

        int selisihPending = pendingHariIniStat - pendingKemarin;

        model.addAttribute("kendaraanTersedia", kendaraanTersedia);
        model.addAttribute("persenPenjualan", Math.abs(persenPenjualan));
        model.addAttribute("isPenjualanNaik", persenPenjualan >= 0);
        model.addAttribute("testDriveHariIni", testDriveHariIni);
        model.addAttribute("selisihPending", Math.abs(selisihPending));
        model.addAttribute("isPendingNaik", selisihPending >= 0);

        model.addAttribute("totalKendaraan", totalKendaraan != null ? totalKendaraan : 0);
        model.addAttribute("penjualanBulanIni", penjualanBulanIni != null ? penjualanBulanIni : 0);
        model.addAttribute("testDriveAktif", testDriveAktif != null ? testDriveAktif : 0);
        model.addAttribute("permintaanPending", permintaanPending != null ? permintaanPending : 0);

        // --- STOK KENDARAAN UNGGULAN ---
        String sqlKendaraanUnggulan =
            "SELECT k.*, " +
            "CASE WHEN m.id_kendaraan IS NOT NULL THEN 'mobil' " +
            "     WHEN mo.id_kendaraan IS NOT NULL THEN 'motor' " +
            "END as jenis " +
            "FROM kendaraan k " +
            "LEFT JOIN mobil m ON k.id_kendaraan = m.id_kendaraan " +
            "LEFT JOIN motor mo ON k.id_kendaraan = mo.id_kendaraan " +
            "ORDER BY k.id_kendaraan DESC LIMIT 6";

        model.addAttribute("kendaraanUnggulan", jdbcTemplate.queryForList(sqlKendaraanUnggulan));

        // --- TRANSAKSI TERBARU ---
        String sqlTransaksi =
            "SELECT p.id_penjualan, u.nama as pembeli, k.merk, k.model, k.tahun, p.tanggal, k.harga, p.status " +
            "FROM penjualan p " +
            "JOIN pembeli pb ON p.id_pembeli = pb.id_pembeli " +
            "JOIN `user` u ON pb.id_user = u.id_user " +
            "JOIN kendaraan k ON p.id_kendaraan = k.id_kendaraan " +
            "ORDER BY p.tanggal DESC, p.id_penjualan DESC LIMIT 5";

        model.addAttribute("listTransaksi", jdbcTemplate.queryForList(sqlTransaksi));

        // --- KOMPOSISI STOK ---
        Integer mobilTersedia = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM kendaraan k JOIN mobil m ON k.id_kendaraan = m.id_kendaraan WHERE k.status = 'Tersedia'", Integer.class);
        if (mobilTersedia == null) mobilTersedia = 0;

        Integer motorTersedia = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM kendaraan k JOIN motor m ON k.id_kendaraan = m.id_kendaraan WHERE k.status = 'Tersedia'", Integer.class);
        if (motorTersedia == null) motorTersedia = 0;

        int pctMobil = 0, pctMotor = 0, pctTestDrive = 0, pctTerjual = 0;
        if (totalKendaraan != null && totalKendaraan > 0) {
            pctMobil    = (int) Math.round(((double) mobilTersedia   / totalKendaraan) * 100);
            pctMotor    = (int) Math.round(((double) motorTersedia   / totalKendaraan) * 100);
            pctTestDrive= (int) Math.round(((double) (testDriveAktif != null ? testDriveAktif : 0) / totalKendaraan) * 100);
            pctTerjual  = (int) Math.round(((double) (penjualanBulanIni != null ? penjualanBulanIni : 0) / totalKendaraan) * 100);
        }

        model.addAttribute("mobilTersedia", mobilTersedia);
        model.addAttribute("motorTersedia", motorTersedia);
        model.addAttribute("pctMobil", pctMobil);
        model.addAttribute("pctMotor", pctMotor);
        model.addAttribute("pctTestDrive", pctTestDrive);
        model.addAttribute("pctTerjual", pctTerjual);

        return "admin/Dasboard";
    }
}
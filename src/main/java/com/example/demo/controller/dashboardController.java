package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

@Controller
public class dashboardController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/Dashboard") 
    public String showDashboard(Model model) {

        // --- LOGIKA AKTIVITAS TERBARU ---
        // Menggabungkan data dari tabel penjualan dan testdrive, lalu mengurutkannya dari yang terbaru (Limit 5 data)
        String sqlAktivitas = 
            "SELECT 'Penjualan' as jenis, p.status, p.tanggal, k.merk, k.model, u.nama as aktor " +
            "FROM penjualan p " +
            "JOIN kendaraan k ON p.id_kendaraan = k.id_kendaraan " +
            "JOIN pembeli pb ON p.id_pembeli = pb.id_pembeli " +
            "JOIN user u ON pb.id_user = u.id_user " +
            "UNION ALL " +
            "SELECT 'Test Drive' as jenis, t.status, t.tanggal, k.merk, k.model, u.nama as aktor " +
            "FROM testdrive t " +
            "JOIN kendaraan k ON t.id_kendaraan = k.id_kendaraan " +
            "JOIN pembeli pb ON t.id_pembeli = pb.id_pembeli " +
            "JOIN user u ON pb.id_user = u.id_user " +
            "ORDER BY tanggal DESC LIMIT 5";

        List<Map<String, Object>> listAktivitas = jdbcTemplate.queryForList(sqlAktivitas);
        model.addAttribute("listAktivitas", listAktivitas);
        
        // --- LOGIKA GRAFIK PENJUALAN 6 BULAN TERAKHIR ---
        List<Map<String, Object>> grafikList = new ArrayList<>();
        LocalDate now = LocalDate.now();
        double maxPendapatan = 1.0; // Untuk mencari nilai tertinggi (mencegah bagi 0)
        double totalPendapatan6Bulan = 0.0;

        // Array nama bulan
        String[] namaBulanIndo = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Ags", "Sep", "Okt", "Nov", "Des"};

        // Loop mundur dari 5 bulan lalu sampai bulan ini (0)
        for (int i = 5; i >= 0; i--) {
            LocalDate targetMonth = now.minusMonths(i);
            int m = targetMonth.getMonthValue();
            int y = targetMonth.getYear();

            // Query total harga kendaraan yang status penjualannya 'Selesai' di bulan tersebut
            String sqlPendapatan = "SELECT SUM(k.harga) FROM penjualan p " +
                                   "JOIN kendaraan k ON p.id_kendaraan = k.id_kendaraan " +
                                   "WHERE p.status = 'Selesai' AND MONTH(p.tanggal) = ? AND YEAR(p.tanggal) = ?";
            
            Double totalBulanIni = jdbcTemplate.queryForObject(sqlPendapatan, Double.class, m, y);
            if (totalBulanIni == null) totalBulanIni = 0.0;

            totalPendapatan6Bulan += totalBulanIni;
            if (totalBulanIni > maxPendapatan) maxPendapatan = totalBulanIni;

            Map<String, Object> dataBulan = new HashMap<>();
            dataBulan.put("label", namaBulanIndo[m - 1]); // Ambil nama bulan
            dataBulan.put("total", totalBulanIni);
            dataBulan.put("isCurrent", i == 0); // Tandai jika ini adalah bulan sekarang (aktif)
            grafikList.add(dataBulan);
        }

        // Hitung persentase tinggi (height CSS) untuk masing-masing bulan
        for (Map<String, Object> map : grafikList) {
            double total = (double) map.get("total");
            int height = (int) ((total / maxPendapatan) * 100);
            
            // Minimal tinggi 5% agar batangnya tidak hilang jika ada penjualan sedikit
            if (total > 0 && height < 5) height = 5; 
            if (total == 0) height = 0;

            map.put("height", height);
        }

        model.addAttribute("totalPendapatan6Bulan", totalPendapatan6Bulan);
        model.addAttribute("grafikPenjualan", grafikList);

        // --- LOGIKA PERSENTASE PERTUMBUHAN (VS PERIODE LALU) ---
        double totalPendapatan6BulanLalu = 0.0;

        // Loop mundur dari 11 bulan lalu sampai 6 bulan lalu
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

        // Hitung persentasenya
        double persentase = 0.0;
        if (totalPendapatan6BulanLalu > 0) {
            persentase = ((totalPendapatan6Bulan - totalPendapatan6BulanLalu) / totalPendapatan6BulanLalu) * 100;
        } else if (totalPendapatan6Bulan > 0) {
            persentase = 100.0; // Jika periode lalu 0 tapi sekarang ada pendapatan, anggap naik 100%
        }

        // Mengirimkan nilai bulat absolut (menghilangkan minus) dan status naik/turun
        model.addAttribute("persentasePertumbuhan", Math.round(Math.abs(persentase)));
        model.addAttribute("isPertumbuhanPositif", persentase >= 0);

        // 1. Hitung Total Kendaraan
        String sqlTotalKendaraan = "SELECT COUNT(*) FROM kendaraan";
        Integer totalKendaraan = jdbcTemplate.queryForObject(sqlTotalKendaraan, Integer.class);

        // 2. Hitung Penjualan Bulan Ini
        // Menggunakan fungsi MONTH() dan YEAR() bawaan SQL untuk mencocokkan dengan bulan & tahun saat ini
        String sqlPenjualan = "SELECT COUNT(*) FROM penjualan WHERE MONTH(tanggal) = MONTH(CURRENT_DATE()) AND YEAR(tanggal) = YEAR(CURRENT_DATE())";
        Integer penjualanBulanIni = jdbcTemplate.queryForObject(sqlPenjualan, Integer.class);

        // 3. Hitung Test Drive Aktif
        // Asumsi: status di database disimpan dengan teks 'Aktif' atau 'Dijadwalkan'
        String sqlTestDrive = "SELECT COUNT(*) FROM testdrive WHERE status = 'Aktif'";
        Integer testDriveAktif = jdbcTemplate.queryForObject(sqlTestDrive, Integer.class);

        // 4. Hitung Permintaan Pending
        // Asumsi: status di tabel penjualan yang belum diproses disimpan dengan teks 'Pending'
        String sqlPending = "SELECT COUNT(*) FROM penjualan WHERE status = 'Pending'";
        Integer permintaanPending = jdbcTemplate.queryForObject(sqlPending, Integer.class);

        // Kirim data ke HTML (jika null/kosong, jadikan 0)
        model.addAttribute("totalKendaraan", totalKendaraan != null ? totalKendaraan : 0);
        model.addAttribute("penjualanBulanIni", penjualanBulanIni != null ? penjualanBulanIni : 0);
        model.addAttribute("testDriveAktif", testDriveAktif != null ? testDriveAktif : 0);
        model.addAttribute("permintaanPending", permintaanPending != null ? permintaanPending : 0);

        // --- LOGIKA STOK KENDARAAN UNGGULAN ---
        // Mengambil 6 kendaraan terbaru dari database beserta jenisnya (mobil/motor)
        String sqlKendaraanUnggulan = 
            "SELECT k.*, " +
            "CASE WHEN m.id_kendaraan IS NOT NULL THEN 'mobil' " +
            "     WHEN mo.id_kendaraan IS NOT NULL THEN 'motor' " +
            "END as jenis " +
            "FROM kendaraan k " +
            "LEFT JOIN mobil m ON k.id_kendaraan = m.id_kendaraan " +
            "LEFT JOIN motor mo ON k.id_kendaraan = mo.id_kendaraan " +
            "ORDER BY k.id_kendaraan DESC LIMIT 6";
        
        List<Map<String, Object>> kendaraanUnggulan = jdbcTemplate.queryForList(sqlKendaraanUnggulan);
        model.addAttribute("kendaraanUnggulan", kendaraanUnggulan);

        // --- LOGIKA TRANSAKSI PENJUALAN TERBARU ---
        // Mengambil 5 transaksi terakhir dari database
        String sqlTransaksi = 
            "SELECT p.id_penjualan, u.nama as pembeli, k.merk, k.model, k.tahun, p.tanggal, k.harga, p.status " +
            "FROM penjualan p " +
            "JOIN pembeli pb ON p.id_pembeli = pb.id_pembeli " +
            "JOIN user u ON pb.id_user = u.id_user " +
            "JOIN kendaraan k ON p.id_kendaraan = k.id_kendaraan " +
            "ORDER BY p.tanggal DESC, p.id_penjualan DESC LIMIT 5";
        
        List<Map<String, Object>> listTransaksi = jdbcTemplate.queryForList(sqlTransaksi);
        model.addAttribute("listTransaksi", listTransaksi);

        // --- LOGIKA KOMPOSISI STOK (PROGRESS BAR) ---
        // 1. Hitung Mobil Tersedia
        String sqlMobilTersedia = "SELECT COUNT(*) FROM kendaraan k JOIN mobil m ON k.id_kendaraan = m.id_kendaraan WHERE k.status = 'Tersedia'";
        Integer mobilTersedia = jdbcTemplate.queryForObject(sqlMobilTersedia, Integer.class);
        if (mobilTersedia == null) mobilTersedia = 0;

        // 2. Hitung Motor Tersedia
        String sqlMotorTersedia = "SELECT COUNT(*) FROM kendaraan k JOIN motor m ON k.id_kendaraan = m.id_kendaraan WHERE k.status = 'Tersedia'";
        Integer motorTersedia = jdbcTemplate.queryForObject(sqlMotorTersedia, Integer.class);
        if (motorTersedia == null) motorTersedia = 0;

        // 3. Hitung persentase panjang garis (width %) dibandingkan dengan Total Kendaraan
        // Cegah error pembagian dengan 0 (jika database masih kosong)
        int pctMobil = 0, pctMotor = 0, pctTestDrive = 0, pctTerjual = 0;
        
        if (totalKendaraan != null && totalKendaraan > 0) {
            pctMobil = (int) Math.round(((double) mobilTersedia / totalKendaraan) * 100);
            pctMotor = (int) Math.round(((double) motorTersedia / totalKendaraan) * 100);
            pctTestDrive = (int) Math.round(((double) testDriveAktif / totalKendaraan) * 100);
            pctTerjual = (int) Math.round(((double) penjualanBulanIni / totalKendaraan) * 100);
        }

        // Kirim angka total dan angka persentasenya ke HTML
        model.addAttribute("mobilTersedia", mobilTersedia);
        model.addAttribute("motorTersedia", motorTersedia);
        
        model.addAttribute("pctMobil", pctMobil);
        model.addAttribute("pctMotor", pctMotor);
        model.addAttribute("pctTestDrive", pctTestDrive);
        model.addAttribute("pctTerjual", pctTerjual);

        // Pastikan nama ini sesuai persis dengan nama file HTML-mu (Dasboard.html)
        return "Dasboard"; 
    }
}
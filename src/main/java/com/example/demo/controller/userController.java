package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.model.user;

import jakarta.servlet.http.HttpSession;

@Controller
public class userController extends user {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ── Halaman utama (katalog, bebas akses) ──────────────────────────────
    @RequestMapping(method = RequestMethod.GET, value = "/")
    public String home(Model model, HttpSession session) {
        // Jika sudah login, langsung ke buyer home
        if (session.getAttribute("id_pembeli") != null) {
            return "redirect:/buyer/home";
        }
        return "index";
    }

    // ── Halaman Login ─────────────────────────────────────────────────────
    @RequestMapping(method = RequestMethod.GET, value = "/login")
    public String loginPage(HttpSession session) {
        // Jika sudah login, tidak perlu ke halaman login
        if (session.getAttribute("id_pembeli") != null) {
            return "redirect:/buyer/home";
        }
        return "login";
    }

    // ── Logout ────────────────────────────────────────────────────────────
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?loggedOut=true";
    }

    // ── Proses LOGIN ──────────────────────────────────────────────────────
    @RequestMapping(method = RequestMethod.POST, value = "/loginForm")
    public String loginController(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            Model model,
            HttpSession session) {

        try {
            String sql = "SELECT id_user, nama, role FROM user WHERE username = ? AND password = ?";
            Map<String, Object> userData = jdbcTemplate.queryForMap(sql, username, password);

            String role = (String) userData.get("role");

            // ── Simpan data ke session ────────────────────────────
            session.setAttribute("id_user",  userData.get("id_user"));
            session.setAttribute("nama",     userData.get("nama"));
            session.setAttribute("username", username);
            session.setAttribute("role",     role);

            // Jika buyer, simpan juga id_pembeli
            if (!role.equalsIgnoreCase("owner") && !role.equalsIgnoreCase("admin")) {
                try {
                    Integer idPembeli = jdbcTemplate.queryForObject(
                        "SELECT id_pembeli FROM pembeli WHERE id_user = ?",
                        Integer.class, userData.get("id_user"));
                    session.setAttribute("id_pembeli", idPembeli);
                } catch (EmptyResultDataAccessException e) {
                    session.setAttribute("id_pembeli", null);
                }
                return "redirect:/buyer/home";
            }

            if (role.equalsIgnoreCase("owner") || role.equalsIgnoreCase("admin")) {
                return "redirect:/admin/Dashboard";
            }

            return "redirect:/buyer/home";

        } catch (EmptyResultDataAccessException e) {
            model.addAttribute("loginError", "Username atau password salah.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("loginError", "Terjadi kesalahan, coba lagi.");
            return "login";
        }
    }

    // ── Halaman Buyer Home ────────────────────────────────────────────────
    @RequestMapping(method = RequestMethod.GET, value = "/buyer/home")
    public String buyerHome(Model model, HttpSession session) {
        if (session.getAttribute("id_pembeli") == null) {
            return "redirect:/login?sessionExpired=true";
        }
        model.addAttribute("nama", session.getAttribute("nama"));
        return "index";
    }

    // ── Halaman Test Drive Pembeli ────────────────────────────────────────
    @GetMapping("/buyer/testdrive")
    public String buyerTestdrive(Model model, HttpSession session) {
        Integer idPembeli = (Integer) session.getAttribute("id_pembeli");
        if (idPembeli == null) return "redirect:/login?sessionExpired=true";

        try {
            model.addAttribute("nama", session.getAttribute("nama"));

            String sqlTestdrive =
                "SELECT td.id_testdrive, td.id_kendaraan, td.tanggal, td.status " +
                "FROM testdrive td " +
                "WHERE td.id_pembeli = ? ORDER BY td.tanggal DESC";
            List<Map<String, Object>> myTestdrives = jdbcTemplate.queryForList(sqlTestdrive, idPembeli);
            model.addAttribute("myTestdrives", myTestdrives);

            String sqlKendaraan = "SELECT id_kendaraan, merk, model FROM kendaraan";
            List<Map<String, Object>> kendaraanList = jdbcTemplate.queryForList(sqlKendaraan);
            Map<Integer, Map<String, Object>> kendaraanMap = new HashMap<>();
            for (Map<String, Object> k : kendaraanList) {
                kendaraanMap.put((Integer) k.get("id_kendaraan"), k);
            }
            model.addAttribute("kendaraanMap", kendaraanMap);

            return "buyer-testdrive";

        } catch (Exception e) {
            System.err.println("Error memuat halaman testdrive: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Terjadi kesalahan memuat data test drive");
            model.addAttribute("myTestdrives", new java.util.ArrayList<>());
            model.addAttribute("kendaraanMap", new HashMap<>());
            return "buyer-testdrive";
        }
    }

    // ── Halaman Pesanan Pembeli ───────────────────────────────────────────
    @GetMapping("/buyer/pesanan")
    public String buyerPesanan(Model model, HttpSession session) {
        Integer idPembeli = (Integer) session.getAttribute("id_pembeli");
        if (idPembeli == null) return "redirect:/login?sessionExpired=true";

        try {
            model.addAttribute("nama", session.getAttribute("nama"));

            String sqlPesanan =
                "SELECT p.id_penjualan, p.id_kendaraan, p.total_harga, " +
                "p.tanggal, p.status FROM penjualan p " +
                "WHERE p.id_pembeli = ? ORDER BY p.tanggal DESC";
            List<Map<String, Object>> pesanan = jdbcTemplate.queryForList(sqlPesanan, idPembeli);
            model.addAttribute("pesanan", pesanan);

            String sqlKendaraan = "SELECT id_kendaraan, merk, model, harga FROM kendaraan";
            List<Map<String, Object>> kendaraanList = jdbcTemplate.queryForList(sqlKendaraan);
            Map<Integer, Map<String, Object>> kendaraanMap = new HashMap<>();
            for (Map<String, Object> k : kendaraanList) {
                kendaraanMap.put((Integer) k.get("id_kendaraan"), k);
            }
            model.addAttribute("kendaraanMap", kendaraanMap);

            return "buyer-pesanan";

        } catch (Exception e) {
            System.err.println("Error memuat halaman pesanan: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Terjadi kesalahan memuat data pesanan");
            model.addAttribute("pesanan", new java.util.ArrayList<>());
            model.addAttribute("kendaraanMap", new HashMap<>());
            return "buyer-pesanan";
        }
    }

    // ── Halaman Profil Pembeli ────────────────────────────────────────────
    @GetMapping("/buyer/profil")
    public String buyerProfil(Model model, HttpSession session) {
        Integer idPembeli = (Integer) session.getAttribute("id_pembeli");
        if (idPembeli == null) return "redirect:/login?sessionExpired=true";

        try {
            String sqlUser =
                "SELECT u.id_user, u.nama, u.username, u.email, p.kontak " +
                "FROM user u " +
                "JOIN pembeli p ON u.id_user = p.id_user " +
                "WHERE p.id_pembeli = ?";
            Map<String, Object> userData;
            try {
                userData = jdbcTemplate.queryForMap(sqlUser, idPembeli);
            } catch (EmptyResultDataAccessException e) {
                userData = new HashMap<>();
                userData.put("id_user", null);
                userData.put("nama",    "Pengguna");
                userData.put("username","guest");
                userData.put("email",   "");
                userData.put("kontak",  "");
            }

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id_user",  userData.get("id_user"));
            userMap.put("nama",     userData.get("nama"));
            userMap.put("username", userData.get("username"));
            userMap.put("email",    userData.get("email"));
            userMap.put("kontak",   userData.get("kontak"));

            model.addAttribute("user", userMap);
            model.addAttribute("nama", userData.get("nama"));

            return "buyer-profil";

        } catch (Exception e) {
            System.err.println("Error memuat halaman profil: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("nama",     "Pengguna");
            userMap.put("username", "guest");
            model.addAttribute("user", userMap);
            model.addAttribute("nama", "Pengguna");
            model.addAttribute("error", "Terjadi kesalahan memuat data profil");
            return "buyer-profil";
        }
    }

    // ── Update Profil Pembeli ─────────────────────────────────────────────
    @PostMapping("/buyer/profil")
    public String updateBuyerProfil(
            @RequestParam("nama") String nama,
            Model model,
            HttpSession session) {

        Integer idPembeli = (Integer) session.getAttribute("id_pembeli");
        if (idPembeli == null) return "redirect:/login?sessionExpired=true";

        try {
            Integer idUser = jdbcTemplate.queryForObject(
                "SELECT id_user FROM pembeli WHERE id_pembeli = ?",
                Integer.class, idPembeli);

            if (idUser != null) {
                jdbcTemplate.update("UPDATE user SET nama = ? WHERE id_user = ?", nama, idUser);
                session.setAttribute("nama", nama);
                model.addAttribute("success", "Profil berhasil diperbarui!");
            }

            return "redirect:/buyer/profil";

        } catch (Exception e) {
            System.err.println("Error update profil: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Gagal memperbarui profil: " + e.getMessage());
            return "buyer-profil";
        }
    }

    // ── Proses REGISTER ───────────────────────────────────────────────────
    @RequestMapping(method = RequestMethod.POST, value = "/registerForm")
    public String registerController(
            @RequestParam("nama")            String nama,
            @RequestParam("username")        String username,
            @RequestParam("password")        String password,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {

        // 1. Password cocok?
        if (!password.equals(confirmPassword)) {
            model.addAttribute("registerError", "Password dan konfirmasi password tidak cocok.");
            model.addAttribute("showRegister", true);
            return "login";
        }

        // 2. Cek username sudah dipakai?
        try {
            Integer existCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user WHERE username = ?", Integer.class, username);
            if (existCount != null && existCount > 0) {
                model.addAttribute("registerError", "Username sudah digunakan, pilih yang lain.");
                model.addAttribute("showRegister", true);
                return "login";
            }
        } catch (Exception e) {
            model.addAttribute("registerError", "Terjadi kesalahan saat cek username.");
            model.addAttribute("showRegister", true);
            return "login";
        }

        // 3. Simpan ke tabel user
        try {
            jdbcTemplate.update(
                "INSERT INTO user (nama, username, password) VALUES (?, ?, ?)",
                nama, username, password);

            Long idUser = jdbcTemplate.queryForObject(
                "SELECT id_user FROM user WHERE username = ?", Long.class, username);

            if (idUser != null) {
                jdbcTemplate.update("INSERT INTO pembeli (id_user) VALUES (?)", idUser);
            }

            model.addAttribute("registerSuccess", "Akun berhasil dibuat! Silakan login.");
            return "login";

        } catch (Exception e) {
            model.addAttribute("registerError", "Gagal mendaftar: " + e.getMessage());
            model.addAttribute("showRegister", true);
            return "login";
        }
    }

    // ── Live-check username (AJAX dari form register) ─────────────────────
    @GetMapping("/checkUsername")
    @ResponseBody
    public Map<String, Boolean> checkUsername(@RequestParam("username") String username) {
        Map<String, Boolean> result = new HashMap<>();
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user WHERE username = ?", Integer.class, username);
            result.put("taken", count != null && count > 0);
        } catch (Exception e) {
            result.put("taken", false);
        }
        return result;
    }
}
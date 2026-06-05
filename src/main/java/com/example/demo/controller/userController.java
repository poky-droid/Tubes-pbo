package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.model.user;

@Controller
public class userController extends user {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ── Halaman utama (login) ─────────────────────────────────────────────
    @RequestMapping(method = RequestMethod.GET, value = "/")
    public String home() {
        return "index";
    }

    // ── Proses LOGIN ──────────────────────────────────────────────────────
    @RequestMapping(method = RequestMethod.POST, value = "/loginForm")
    public String loginController(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            Model model) {

        try {
            String sql = "SELECT COUNT(*) FROM user WHERE username = ? AND password = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username, password);

            if (count != null && count > 0) {
                return "redirect:/Dashboard";
            } else {
                model.addAttribute("loginError", "Username atau password salah.");
                return "index";
            }
        } catch (Exception e) {
            model.addAttribute("loginError", "Terjadi kesalahan, coba lagi.");
            return "index";
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
            return "index";
        }

        // 2. Cek username sudah dipakai?
        try {
            String cekSql = "SELECT COUNT(*) FROM user WHERE username = ?";
            Integer existCount = jdbcTemplate.queryForObject(cekSql, Integer.class, username);
            if (existCount != null && existCount > 0) {
                model.addAttribute("registerError", "Username sudah digunakan, pilih yang lain.");
                model.addAttribute("showRegister", true);
                return "index";
            }
        } catch (Exception e) {
            model.addAttribute("registerError", "Terjadi kesalahan saat cek username.");
            model.addAttribute("showRegister", true);
            return "index";
        }

        // 3. Simpan ke tabel user
        try {
            String insertUser = "INSERT INTO user (nama, username, password) VALUES (?, ?, ?)";
            jdbcTemplate.update(insertUser, nama, username, password);

            // Ambil id_user yang baru dibuat
            Long idUser = jdbcTemplate.queryForObject(
                "SELECT id_user FROM user WHERE username = ?", Long.class, username);

            // Daftarkan sebagai pembeli secara default
            if (idUser != null) {
                jdbcTemplate.update("INSERT INTO pembeli (id_user) VALUES (?)", idUser);
            }

            model.addAttribute("registerSuccess", "Akun berhasil dibuat! Silakan login.");
            return "index";

        } catch (Exception e) {
            model.addAttribute("registerError", "Gagal mendaftar: " + e.getMessage());
            model.addAttribute("showRegister", true);
            return "index";
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

    // // ── Routing halaman lain ──────────────────────────────────────────────
    // @RequestMapping(method = RequestMethod.GET, value = "/penjualan")
    // public String penjualan() { return "penjualan"; }

    // @RequestMapping(method = RequestMethod.GET, value = "/testdrive")
    // public String testdrive() { return "testdrive"; }

    // @RequestMapping(method = RequestMethod.GET, value = "/pembeli")
    // public String pembeli() { return "pembeli"; }

    // @RequestMapping(method = RequestMethod.GET, value = "/laporan")
    // public String laporan() { return "laporan"; }

    // @RequestMapping(method = RequestMethod.GET, value = "/profil")
    // public String profil() { return "profil"; }
}
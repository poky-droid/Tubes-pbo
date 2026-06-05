package com.example.demo.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.model.User;
@Controller
public class userController  extends User {
    @RequestMapping(method=RequestMethod.GET, value = "/")
    public String home() {
        return "index";
    }

    @RequestMapping(method=RequestMethod.GET, value = "/admin/Dashboard")
    public String dashboard() {
        return "admin/Dasboard";
    }

    @RequestMapping(method=RequestMethod.GET, value = "/admin/kendaraan")
    public String kendaraan() {
        return "admin/kendaraan";
    }

    @RequestMapping(method=RequestMethod.GET, value = "/admin/penjualan")
    public String penjualan() {
        return "admin/penjualan";
    }

    @RequestMapping(method=RequestMethod.GET, value = "/admin/testdrive")
    public String testdrive() {
        return "admin/testdrive";
    }

    @RequestMapping(method=RequestMethod.GET, value = "/admin/pembeli")
    public String pembeli() {
        return "admin/pembeli";
    }

    @RequestMapping(method=RequestMethod.GET, value = "/admin/laporan")
    public String laporan() {
        return "admin/laporan";
    }

    @RequestMapping(method=RequestMethod.GET, value = "/admin/profil")
    public String profil() {
        return "admin/profil";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/loginForm")
    public String loginController(
            @RequestParam("email") String email,
            @RequestParam("password") String password) {

        User user = new User();

        if (user.login(email, password)) {
            return "redirect:/admin/Dashboard";
        } else {
            return "index";
        }
    }
}


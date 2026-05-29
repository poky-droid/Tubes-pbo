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

    // @RequestMapping(method=RequestMethod.GET, value = "/Dashboard")
    // public String DashboardRooting() {
    //     return "Dasboard";
    // }
    // @RequestMapping( method=RequestMethod.GET, value = "/kendaraan")
    // public String kendaraan() {
    //     return "kendaraan";
    // }

    @RequestMapping( method=RequestMethod.GET, value = "/penjualan")
    public String penjualan() {
        return "penjualan";
    }

    @RequestMapping( method=RequestMethod.GET, value = "/testdrive")
    public String testdrive() {
        return "testdrive";
    }

    @RequestMapping( method=RequestMethod.GET, value = "/pembeli")
    public String pembeli() {
        return "pembeli";
    }

    @RequestMapping( method=RequestMethod.GET, value = "/laporan")
    public String laporan() {
        return "laporan";
    }

    @RequestMapping( method=RequestMethod.GET, value = "/profil")
    public String profil() {
        return "profil";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/loginForm")
    public String loginController(
            @RequestParam("email") String email,
            @RequestParam("password") String password) {

        User user = new User();

        if (user.login(email, password)) {
            return "Dasboard";
        } else {
            return "index";
        }
    }
}


package com.example.demo.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.model.user;
@Controller
public class userController  extends user {
    @RequestMapping(method=RequestMethod.GET, value = "/")
    public String home() {
        return "index";
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/login form")
    public String loginController(@RequestParam("email") String email, @RequestParam("password") String password) {
        if (login(email, password)) {
            return "home";
        } else {
            return "Login gagal!";
        }
    }
}


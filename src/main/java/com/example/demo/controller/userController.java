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
    
    @RequestMapping(method = RequestMethod.POST, value = "/loginForm")
    public String loginController(
            @RequestParam("email") String email,
            @RequestParam("password") String password) {

        User user = new User();

        if (user.login(email, password)) {
            return "home";
        } else {
            return "index";
        }
    }
}


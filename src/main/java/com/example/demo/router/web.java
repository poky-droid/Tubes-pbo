package com.example.demo.router;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class web {

    @RequestMapping(method = RequestMethod.GET, value = "/")
    public String webRoot(){
        return "index";
    }

}

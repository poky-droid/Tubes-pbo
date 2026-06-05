package com.example.demo; // Sesuaikan dengan nama package utamamu!

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class webconfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Mengarahkan URL "/uploads/**" ke folder fisik "uploads" di komputermu
        Path uploadDir = Paths.get("/uploads/kendaraan"); // Pastikan nama folder ini sama dengan folder tempat fotomu tersimpan
        String uploadPath = uploadDir.toFile().getAbsolutePath();

        registry.addResourceHandler("/uploads/kendaraan")
                .addResourceLocations("file:/" + uploadPath + "/");
    }
}
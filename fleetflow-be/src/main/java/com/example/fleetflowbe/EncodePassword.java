package com.example.fleetflowbe;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class EncodePassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "password123";
        String hash = encoder.encode(password);

        System.out.println(hash);
    }
}

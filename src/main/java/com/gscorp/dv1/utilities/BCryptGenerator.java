package com.gscorp.dv1.utilities;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String plainPassword = "PasswordsTests"; // Cambia por la contraseña que quieras
        String hashed = encoder.encode(plainPassword);
        System.out.println(hashed);
    }

}

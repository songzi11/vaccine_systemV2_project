package com.tjut.edu.vaccine;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
class VaccineApplicationTests {
    @Test
    void contextLoads() {
    }

    @Test
    void generatePasswordHash() {
        String hash = new BCryptPasswordEncoder().encode("123456");
        System.out.println("BCrypt hash for '123456': " + hash);
    }
}

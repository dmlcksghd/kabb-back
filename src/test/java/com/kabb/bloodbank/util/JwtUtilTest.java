package com.kabb.bloodbank.util;

import com.kabb.bloodbank.config.JwtProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    @Test
    void generateAndParseToken() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("my-jwt-secret-key-my-jwt-secret-key");
        properties.setAccessTokenExpiration(3600000);

        JwtUtil jwtUtil = new JwtUtil(properties);

        String token = jwtUtil.generateToken(1L, "test@example.com", "USER");

        assertTrue(jwtUtil.validateToken(token));
        assertEquals(1L, jwtUtil.getUserIdFromToken(token));
        assertEquals("test@example.com", jwtUtil.getEmailFromToken(token));
        assertEquals("USER", jwtUtil.getRoleFromToken(token));
    }
}

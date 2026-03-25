package com.astrourl.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Genera códigos cortos tipo bit.ly: alfanuméricos impredecibles, longitud fija.
 */
@Component
public class ShortCodeGenerator {

    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static final int MIN_LENGTH = 4;
    public static final int MAX_LENGTH = 8;

    private final SecureRandom random = new SecureRandom();
    private final int length;

    public ShortCodeGenerator(@Value("${app.shortcode.length:5}") int length) {
        // Si APP_SHORTCODE_LENGTH u otra config llega mal (p. ej. 1), clamp: nunca 1 carácter tipo "3".
        this.length = Math.clamp(length, MIN_LENGTH, MAX_LENGTH);
    }

    public String next() {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}

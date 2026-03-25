package com.astrourl.api.common;

public class Base62Converter {
    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = ALPHABET.length();

    public static String encode(long input) {
        if (input < 0) {
            throw new IllegalArgumentException("El valor a codificar no puede ser negativo.");
        }
        if (input == 0) {
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        while (input > 0) {
            sb.append(ALPHABET.charAt((int) (input % BASE)));
            input /= BASE;
        }
        return sb.reverse().toString();
    }

    public static long decode(String str) {
        if (str == null || str.isBlank()) {
            throw new IllegalArgumentException("El código no puede estar vacío.");
        }
        long result = 0;
        for (int i = 0; i < str.length(); i++) {
            int index = ALPHABET.indexOf(str.charAt(i));
            if (index < 0) {
                throw new IllegalArgumentException("El código contiene caracteres inválidos.");
            }
            result = result * BASE + index;
        }
        return result;
    }
}
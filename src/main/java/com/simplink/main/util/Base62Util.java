package com.simplink.main.util;

import org.springframework.stereotype.Component;

@Component
public class Base62Util {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int BASE = ALPHABET.length();

    public String encode(long id) {
        if (id == 0) return String.valueOf(ALPHABET.charAt(0));
        StringBuilder shortCode = new StringBuilder();
        while (id > 0) {
            shortCode.append(ALPHABET.charAt((int) (id % BASE)));
            id /= BASE;
        }
        return shortCode.reverse().toString();
    }

    public long decode(String shortCode) {
        long id = 0;
        for (int i = 0; i < shortCode.length(); i++) {
            id = id * BASE + ALPHABET.indexOf(shortCode.charAt(i));
        }
        return id;
    }
}
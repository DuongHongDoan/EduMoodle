package com.example.edumoodle.Configuration;

import java.security.SecureRandom;
import java.util.Base64;
import org.apache.commons.codec.digest.Crypt;

public class HashPassword {

    public static String hashPassword(String password) {
        // Tạo salt ngẫu nhiên
        byte[] saltBytes = new byte[16];
        new SecureRandom().nextBytes(saltBytes);
        String salt = Base64.getEncoder().encodeToString(saltBytes);

        // Định dạng salt cho SHA-512 với 10,000 rounds
        String cryptSalt = String.format("$6$rounds=10000$%s$", salt);

        // Băm mật khẩu với salt
        return Crypt.crypt(password, cryptSalt);
    }
}

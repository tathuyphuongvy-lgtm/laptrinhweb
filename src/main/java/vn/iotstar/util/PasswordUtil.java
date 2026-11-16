package vn.iotstar.util;

import java.nio.charset.StandardCharsets;
import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    public static boolean isValidNewPassword(String password) {
        return password != null && !password.isBlank() && password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    // Mã hóa mật khẩu thuần sang chuỗi Hash BCrypt
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    // Kiểm tra mật khẩu nhập vào có khớp với chuỗi Hash trong DB không
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null || hashedPassword.length() != 60
                || !hashedPassword.startsWith("$2a$")) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

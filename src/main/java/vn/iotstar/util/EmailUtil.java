package vn.iotstar.util;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Logger;

public final class EmailUtil {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Logger LOGGER = Logger.getLogger(EmailUtil.class.getName());

    private EmailUtil() { }

    public static String generateOTP() {
        return String.format(Locale.ROOT, "%06d", RANDOM.nextInt(1_000_000));
    }

    public static boolean isValidAddress(String email) {
        try {
            InternetAddress address = new InternetAddress(email, true);
            address.validate();
            return !address.isGroup() && email.equals(address.getAddress());
        } catch (AddressException e) {
            return false;
        }
    }

    public static boolean sendOtpEmail(String email, String subject, String code) {
        String host = setting("smtp.host", "SMTP_HOST", "smtp.gmail.com");
        String port = setting("smtp.port", "SMTP_PORT", "587");
        String username = setting("smtp.username", "SMTP_USERNAME", "");
        String password = setting("smtp.password", "SMTP_PASSWORD", "");
        String sender = setting("smtp.from", "SMTP_FROM", username);
        boolean auth = Boolean.parseBoolean(setting("smtp.auth", "SMTP_AUTH", "true"));
        boolean startTls = Boolean.parseBoolean(setting("smtp.starttls", "SMTP_STARTTLS", "true"));
        if (sender.isBlank() || (auth && (username.isBlank() || password.isBlank()))) {
            LOGGER.warning("SMTP chưa có đủ thông tin cấu hình.");
            return false;
        }

        Properties props = new Properties();
        props.setProperty("mail.smtp.host", host);
        props.setProperty("mail.smtp.port", port);
        props.setProperty("mail.smtp.auth", Boolean.toString(auth));
        props.setProperty("mail.smtp.starttls.enable", Boolean.toString(startTls));
        props.setProperty("mail.smtp.starttls.required", Boolean.toString(startTls));
        props.setProperty("mail.smtp.ssl.checkserveridentity", "true");
        props.setProperty("mail.smtp.connectiontimeout", "5000");
        props.setProperty("mail.smtp.timeout", "5000");
        props.setProperty("mail.smtp.writetimeout", "5000");

        Session session = Session.getInstance(props);
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender, true));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(email, true));
            message.setSubject(subject, "UTF-8");
            message.setText("Mã OTP: " + code + "\nMã có hiệu lực trong 5 phút.", "UTF-8");
            if (auth) {
                Transport.send(message, username, password);
            } else {
                Transport.send(message);
            }
            return true;
        } catch (MessagingException e) {
            LOGGER.warning("Gửi OTP qua SMTP thất bại (" + e.getClass().getSimpleName() + ").");
            return false;
        }
    }

    private static String setting(String key, String environmentVariable, String fallback) {
        return System.getProperty(key, System.getenv().getOrDefault(environmentVariable, fallback));
    }
}

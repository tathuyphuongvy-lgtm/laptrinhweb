package vn.iotstar.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "Users")
public class User implements Serializable {
    public enum OtpPurpose { ACTIVATION, PASSWORD_RESET }

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String fullname;

    // Trạng thái kích hoạt tài khoản (false = chưa kích hoạt, true = đã kích hoạt)
    @Column(nullable = false)
    private boolean active = false;

    // Mã OTP 6 chữ số
    @Column(name = "otp_code", length = 6)
    private String otpCode;

    // Thời gian hết hạn của OTP
    @Column(name = "otp_expiry")
    private LocalDateTime otpExpiry;

    @Enumerated(EnumType.STRING)
    @Column(name = "otp_purpose", length = 16)
    private OtpPurpose otpPurpose;

    @Column(name = "otp_attempts")
    private Integer otpAttempts;

    public User() {}

    // Getters và Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
    
    public LocalDateTime getOtpExpiry() { return otpExpiry; }
    public void setOtpExpiry(LocalDateTime otpExpiry) { this.otpExpiry = otpExpiry; }

    public OtpPurpose getOtpPurpose() { return otpPurpose; }
    public void setOtpPurpose(OtpPurpose otpPurpose) { this.otpPurpose = otpPurpose; }

    public int getOtpAttempts() { return otpAttempts == null ? 0 : otpAttempts; }
    public void setOtpAttempts(int otpAttempts) { this.otpAttempts = otpAttempts; }
}

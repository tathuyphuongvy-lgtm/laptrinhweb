package vn.iotstar.service;

import vn.iotstar.dao.impl.UserDao;
import vn.iotstar.entity.User;
import vn.iotstar.entity.User.OtpPurpose;
import vn.iotstar.util.EmailUtil;

public final class OtpService {
    private static final UserDao USERS = new UserDao();

    private OtpService() { }

    /** Trả về thông báo lỗi hoặc null khi SMTP đã nhận thư. */
    public static String send(User user, OtpPurpose purpose) {
        String code = EmailUtil.generateOTP();
        if (!USERS.issueOtp(user.getId(), purpose, code)) {
            return "Vui lòng chờ 30 giây trước khi yêu cầu mã mới.";
        }
        String subject = purpose == OtpPurpose.ACTIVATION
                ? "Kích hoạt tài khoản - Mã OTP" : "Khôi phục mật khẩu - Mã OTP";
        return EmailUtil.sendOtpEmail(user.getEmail(), subject, code)
                ? null : "Không gửi được email OTP. Vui lòng thử lại sau.";
    }
}

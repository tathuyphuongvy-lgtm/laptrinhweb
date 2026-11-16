package vn.iotstar.dao;

import vn.iotstar.entity.User;
import vn.iotstar.entity.User.OtpPurpose;

public interface IUserDao {
    void insert(User user);
    User findByUsername(String username);
    User findByEmail(String email);
    boolean issueOtp(int userId, OtpPurpose purpose, String code);
    boolean consumeOtp(int userId, OtpPurpose purpose, String code, String newPasswordHash);
}

package vn.iotstar.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Objects;
import vn.iotstar.dao.IUserDao;
import vn.iotstar.dao.impl.UserDao;
import vn.iotstar.entity.User;
import vn.iotstar.entity.User.OtpPurpose;
import vn.iotstar.service.OtpService;
import vn.iotstar.util.PasswordUtil;

public class ForgotPasswordController extends HttpServlet {
    private final IUserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/account/forgot-password.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if ("sendOtp".equals(action)) {
            sendOtp(req, resp);
        } else if ("resetPassword".equals(action)) {
            resetPassword(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void sendOtp(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        session.removeAttribute("resetUserId");
        String email = Objects.toString(req.getParameter("email"), "").strip();
        User user = email.isEmpty() || email.length() > 255 ? null : userDao.findByEmail(email);
        String error = null;
        if (user != null && user.isActive()) {
            session.setAttribute("resetUserId", user.getId());
            error = OtpService.send(user, OtpPurpose.PASSWORD_RESET);
        }
        if (error == null) {
            req.setAttribute("message", "Nếu email thuộc tài khoản đã kích hoạt, bạn sẽ nhận được mã OTP.");
        } else {
            req.setAttribute("error", error);
        }
        req.setAttribute("step", "reset");
        doGet(req, resp);
    }

    private void resetPassword(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        Integer userId = (Integer) session.getAttribute("resetUserId");
        String code = req.getParameter("otp");
        String password = req.getParameter("newPassword");

        if (!PasswordUtil.isValidNewPassword(password)) {
            req.setAttribute("error", "Mật khẩu phải có từ 1 đến 72 byte UTF-8.");
        } else if (userId == null || code == null || !code.matches("[0-9]{6}")
                || !userDao.consumeOtp(userId, OtpPurpose.PASSWORD_RESET, code, PasswordUtil.hashPassword(password))) {
            req.setAttribute("error", "Mã OTP không hợp lệ, đã hết hạn hoặc đã hết lượt thử.");
        } else {
            session.removeAttribute("resetUserId");
            req.setAttribute("message", "Đặt lại mật khẩu thành công. Hãy đăng nhập.");
            req.getRequestDispatcher("/WEB-INF/views/account/login.jsp").forward(req, resp);
            return;
        }
        req.setAttribute("step", "reset");
        doGet(req, resp);
    }
}

package vn.iotstar.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import vn.iotstar.dao.IUserDao;
import vn.iotstar.dao.impl.UserDao;
import vn.iotstar.entity.User;
import vn.iotstar.entity.User.OtpPurpose;
import vn.iotstar.service.OtpService;

import java.io.IOException;

public class VerifyOtpController extends HttpServlet {
    private final IUserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        if (session.getAttribute("pendingEmail") == null) {
            resp.sendRedirect(req.getContextPath() + "/register");
            return;
        }
        req.setAttribute("error", session.getAttribute("activationError"));
        session.removeAttribute("activationError");
        req.getRequestDispatcher("/WEB-INF/views/account/verify-otp.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        String email = (String) session.getAttribute("pendingEmail");
        String inputOtp = req.getParameter("otp");

        if (email == null) {
            resp.sendRedirect(req.getContextPath() + "/register");
            return;
        }

        User user = userDao.findByEmail(email);
        if (user == null || user.isActive()) {
            session.removeAttribute("pendingEmail");
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        if ("resend".equals(req.getParameter("action"))) {
            String error = OtpService.send(user, OtpPurpose.ACTIVATION);
            if (error == null) {
                req.setAttribute("message", "Đã gửi mã kích hoạt mới.");
            } else {
                req.setAttribute("error", error);
            }
            req.getRequestDispatcher("/WEB-INF/views/account/verify-otp.jsp").forward(req, resp);
            return;
        }
        if (userDao.consumeOtp(user.getId(), OtpPurpose.ACTIVATION, inputOtp, null)) {
            session.removeAttribute("pendingEmail");
            req.setAttribute("message", "Kích hoạt tài khoản thành công! Vui lòng đăng nhập.");
            req.getRequestDispatcher("/WEB-INF/views/account/login.jsp").forward(req, resp);
        } else {
            req.setAttribute("error", "Mã OTP không hợp lệ, đã hết hạn hoặc đã hết lượt thử.");
            req.getRequestDispatcher("/WEB-INF/views/account/verify-otp.jsp").forward(req, resp);
        }
    }
}

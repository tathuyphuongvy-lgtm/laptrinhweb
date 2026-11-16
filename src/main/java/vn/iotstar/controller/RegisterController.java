package vn.iotstar.controller;

import jakarta.persistence.PersistenceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import vn.iotstar.dao.IUserDao;
import vn.iotstar.dao.impl.UserDao;
import vn.iotstar.entity.User;
import vn.iotstar.entity.User.OtpPurpose;
import vn.iotstar.service.OtpService;
import vn.iotstar.util.EmailUtil;
import vn.iotstar.util.PasswordUtil;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

public class RegisterController extends HttpServlet {
    private final IUserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/account/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = Objects.toString(req.getParameter("username"), "").strip();
        String email = Objects.toString(req.getParameter("email"), "").strip().toLowerCase(Locale.ROOT);
        String password = req.getParameter("password");
        String fullname = Objects.toString(req.getParameter("fullname"), "").strip();

        if (username.isEmpty() || username.length() > 255 || fullname.isEmpty() || fullname.length() > 255
                || email.isEmpty() || email.length() > 255 || !EmailUtil.isValidAddress(email)
                || !PasswordUtil.isValidNewPassword(password)) {
            showError(req, resp, "Kiểm tra thông tin đăng ký. Mật khẩu phải có từ 1 đến 72 byte UTF-8.");
            return;
        }
        if (alreadyRegistered(username, email)) {
            showError(req, resp, "Tên đăng nhập hoặc email đã tồn tại.");
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullname(fullname);
        user.setPassword(PasswordUtil.hashPassword(password));
        try {
            userDao.insert(user);
        } catch (PersistenceException e) {
            if (!alreadyRegistered(username, email)) {
                throw e;
            }
            showError(req, resp, "Tên đăng nhập hoặc email đã tồn tại.");
            return;
        }

        HttpSession session = req.getSession();
        session.setAttribute("pendingEmail", email);
        session.setAttribute("activationError", OtpService.send(user, OtpPurpose.ACTIVATION));
        resp.sendRedirect(req.getContextPath() + "/verify-otp");
    }

    private boolean alreadyRegistered(String username, String email) {
        return userDao.findByUsername(username) != null || userDao.findByEmail(email) != null;
    }

    private void showError(HttpServletRequest req, HttpServletResponse resp, String error)
            throws ServletException, IOException {
        req.setAttribute("error", error);
        doGet(req, resp);
    }
}

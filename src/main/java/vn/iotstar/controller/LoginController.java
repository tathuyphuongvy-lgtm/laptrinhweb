package vn.iotstar.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import vn.iotstar.dao.IUserDao;
import vn.iotstar.dao.impl.UserDao;
import vn.iotstar.entity.User;
import vn.iotstar.util.PasswordUtil;

import java.io.IOException;
import java.util.Objects;

public class LoginController extends HttpServlet {
    private final IUserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/account/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = Objects.toString(req.getParameter("username"), "").strip();
        String password = req.getParameter("password");

        User user = userDao.findByUsername(username);

        if (user == null || !PasswordUtil.checkPassword(password, user.getPassword())) {
            req.setAttribute("error", "Sai tên đăng nhập hoặc mật khẩu!");
            doGet(req, resp);
            return;
        }

        if (!user.isActive()) {
            req.getSession().setAttribute("pendingEmail", user.getEmail());
            req.getSession().removeAttribute("activationError");
            req.setAttribute("error", "Tài khoản chưa được kích hoạt. Vui lòng nhập mã OTP!");
            req.getRequestDispatcher("/WEB-INF/views/account/verify-otp.jsp").forward(req, resp);
            return;
        }

        HttpSession session = req.getSession();
        req.changeSessionId();
        session.removeAttribute("csrf");
        session.removeAttribute("pendingEmail");
        session.removeAttribute("activationError");
        session.removeAttribute("resetUserId");
        session.setAttribute("account", user.getUsername());
        resp.sendRedirect(req.getContextPath() + "/admin/categories");
    }
}

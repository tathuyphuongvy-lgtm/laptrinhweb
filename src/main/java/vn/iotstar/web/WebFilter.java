package vn.iotstar.web;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.UUID;
public class WebFilter implements Filter {
    @Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8"); response.setCharacterEncoding("UTF-8");
        HttpServletRequest req = (HttpServletRequest)request;
        HttpServletResponse resp = (HttpServletResponse)response;
        resp.setHeader("X-Content-Type-Options", "nosniff");
        String path = req.getServletPath();
        if (path.startsWith("/assets/")) {
            chain.doFilter(req, resp);
            return;
        }
        resp.setHeader("Cache-Control", "no-store");
        HttpSession session = req.getSession();
        if ((path.startsWith("/admin/") || "/image".equals(path)) && session.getAttribute("account") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        if (session.getAttribute("csrf") == null) session.setAttribute("csrf", UUID.randomUUID().toString());
        if ("POST".equals(req.getMethod())) {
            String token;
            try { token = req.getParameter("csrf"); }
            catch (IllegalStateException e) { resp.sendError(400, "Tệp quá lớn, giới hạn ảnh 2 MB"); return; }
            if (!session.getAttribute("csrf").equals(token)) {
                resp.sendError(403, "Biểu mẫu hết hạn, hãy tải lại trang"); return;
            }
        }
        chain.doFilter(req, resp);
    }
}

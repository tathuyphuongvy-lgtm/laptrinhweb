package vn.iotstar.controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import vn.iotstar.entity.Category;
import vn.iotstar.service.ICategoryService;
import vn.iotstar.service.impl.CategoryServiceImpl;
import vn.iotstar.web.ImageStorage;

public class CategoryController extends HttpServlet {
    private final ICategoryService service = new CategoryServiceImpl();
    private static final int PAGE_SIZE = 5;
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String route = req.getServletPath();
        try {
            switch (route) {
                case "/admin/categories" -> list(req, resp);
                case "/admin/category/add" -> { req.setAttribute("cate", new Category()); form(req, resp, false); }
                case "/admin/category/edit" -> {
                    Category c = service.findById(id(req));
                    if (c == null) { resp.sendError(404); return; }
                    req.setAttribute("cate", c); form(req, resp, true);
                }
                default -> resp.sendError(405);
            }
        } catch (IllegalArgumentException e) { resp.sendError(400, e.getMessage()); }
    }
    private void list(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String q = req.getParameter("q") == null ? "" : req.getParameter("q").trim();
        int count = service.count(q);
        req.setAttribute("allCount", service.count());
        req.setAttribute("activeCount", service.countByStatus(1));
        req.setAttribute("lockedCount", service.countByStatus(0));
        int totalPages = Math.max(1, (count + PAGE_SIZE - 1) / PAGE_SIZE);
        int page = 1;
        try { page = Integer.parseInt(req.getParameter("page")); } catch (NumberFormatException ignored) { }
        page = Math.max(1, Math.min(page, totalPages));
        req.setAttribute("listcate", service.searchByName(q, page - 1, PAGE_SIZE));
        req.setAttribute("q", q); req.setAttribute("page", page);
        req.setAttribute("totalPages", totalPages); req.setAttribute("total", count);
        req.setAttribute("offset", (page - 1) * PAGE_SIZE);
        Object flash = req.getSession().getAttribute("flash");
        req.setAttribute("flash", flash); req.getSession().removeAttribute("flash");
        req.getRequestDispatcher("/WEB-INF/views/admin/category-list.jsp").forward(req, resp);
    }
    private static int id(HttpServletRequest req) {
        String raw = req.getParameter("categoryid");
        if (raw == null) raw = req.getParameter("id");
        try { int id = Integer.parseInt(raw); if (id < 1) throw new NumberFormatException(); return id; }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Mã danh mục không hợp lệ"); }
    }
    private void form(HttpServletRequest req, HttpServletResponse resp, boolean edit) throws ServletException, IOException {
        req.setAttribute("edit", edit);
        req.getRequestDispatcher("/WEB-INF/views/admin/" + (edit ? "category-edit.jsp" : "category-add.jsp")).forward(req, resp);
    }
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String route = req.getServletPath();
        if (route.equals("/admin/category/delete")) {
            try {
                Category old = service.findById(id(req));
                if (old == null) { resp.sendError(404); return; }
                service.delete(old.getCategoryid()); cleanup(old.getImages());
                req.getSession().setAttribute("flash", "Đã xóa danh mục");
            } catch (IllegalArgumentException e) { req.getSession().setAttribute("flash", e.getMessage()); }
            catch (RuntimeException e) { log("Xóa danh mục thất bại", e); req.getSession().setAttribute("flash", "Không xóa được danh mục, hãy kiểm tra dữ liệu liên quan"); }
            redirect(req, resp); return;
        }
        boolean edit = route.equals("/admin/category/update");
        if (!edit && !route.equals("/admin/category/insert")) { resp.sendError(405); return; }
        Category c = new Category();
        String oldImage = null, newImage = null;
        try {
            if (edit) {
                Category old = service.findById(id(req));
                if (old == null) { resp.sendError(404); return; }
                c.setCategoryid(old.getCategoryid()); oldImage = old.getImages();
            }
            c.setCategoryname(req.getParameter("categoryname"));
            String status = req.getParameter("status");
            if (!"0".equals(status) && !"1".equals(status)) throw new IllegalArgumentException("Vui lòng chọn trạng thái");
            c.setStatus(Integer.parseInt(status));
            c.setImages(oldImage);
            Part part = req.getPart("images1");
            if (part != null && part.getSize() > 0) {
                newImage = ImageStorage.save(part); c.setImages(newImage);
            } else if ("1".equals(req.getParameter("removeImage"))) c.setImages(null);
            else {
                String url = ImageStorage.validUrl(req.getParameter("images"));
                if (url != null) c.setImages(url);
            }
            if (edit) service.update(c); else service.insert(c);
        } catch (IllegalArgumentException | IllegalStateException e) {
            if (newImage != null) cleanup(newImage);
            c.setImages(oldImage); req.setAttribute("cate", c);
            req.setAttribute("error", e instanceof IllegalStateException ? "Tệp quá lớn hoặc không xử lý được, giới hạn ảnh 2 MB" : e.getMessage());
            resp.setStatus(400); form(req, resp, edit); return;
        } catch (RuntimeException e) {
            if (newImage != null) cleanup(newImage);
            log("Lưu danh mục thất bại", e);
            c.setImages(oldImage); req.setAttribute("cate", c); req.setAttribute("error", "Không lưu được dữ liệu. Kiểm tra tên trùng hoặc kết nối cơ sở dữ liệu.");
            resp.setStatus(400); form(req, resp, edit); return;
        } catch (IOException | ServletException e) {
            throw e;
        }
        if (oldImage != null && !oldImage.equals(c.getImages())) cleanup(oldImage);
        req.getSession().setAttribute("flash", edit ? "Đã cập nhật danh mục" : "Đã thêm danh mục");
        redirect(req, resp);
    }
    private void cleanup(String image) { try { ImageStorage.delete(image); } catch (IOException e) { log("Không dọn được ảnh cũ", e); } }
    private static void redirect(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/admin/categories");
    }
}

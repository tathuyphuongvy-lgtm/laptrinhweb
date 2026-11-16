package vn.iotstar.service.impl;
import java.util.List;
import vn.iotstar.dao.ICategoryDao;
import vn.iotstar.dao.impl.CategoryDao;
import vn.iotstar.entity.Category;
import vn.iotstar.service.ICategoryService;
public class CategoryServiceImpl implements ICategoryService {
    private final ICategoryDao dao = new CategoryDao();
    private void validate(Category c) {
        String name = c.getCategoryname() == null ? "" : c.getCategoryname().trim();
        if (name.isEmpty() || name.length() > 50) throw new IllegalArgumentException("Tên danh mục cần từ 1 đến 50 ký tự");
        if (c.getStatus() != 0 && c.getStatus() != 1) throw new IllegalArgumentException("Trạng thái không hợp lệ");
        if (c.getImages() != null && c.getImages().length() > 500) throw new IllegalArgumentException("Link ảnh tối đa 500 ký tự");
        Category existing = dao.findByCategoryname(name);
        if (existing != null && existing.getCategoryid() != c.getCategoryid()) throw new IllegalArgumentException("Tên danh mục đã tồn tại");
        c.setCategoryname(name);
    }
    public void insert(Category c) { validate(c); dao.insert(c); }
    public void update(Category c) { validate(c); dao.update(c); }
    public void delete(int id) { dao.delete(id); }
    public Category findById(int id) { return dao.findById(id); }
    public Category findByCategoryname(String name) { return dao.findByCategoryname(name); }
    public List<Category> findAll() { return dao.findAll(); }
    public List<Category> findAll(int page, int size) { return dao.findAll(page, size); }
    public List<Category> searchByName(String name) { return dao.searchByName(name); }
    public List<Category> searchByName(String name, int page, int size) { return dao.searchByName(name, page, size); }
    public int countByStatus(int status) { return dao.countByStatus(status); }
    public int count() { return dao.count(); }
    public int count(String name) { return dao.count(name); }
}

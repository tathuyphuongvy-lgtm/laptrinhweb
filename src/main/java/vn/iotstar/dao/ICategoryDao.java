package vn.iotstar.dao;
import java.util.List;
import vn.iotstar.entity.Category;
public interface ICategoryDao {
    void insert(Category category);
    void update(Category category);
    void delete(int id);
    Category findById(int id);
    Category findByCategoryname(String name);
    List<Category> findAll();
    List<Category> findAll(int page, int pagesize);
    List<Category> searchByName(String name);
    List<Category> searchByName(String name, int page, int pagesize);
    int count();
    int countByStatus(int status);
    int count(String name);
}

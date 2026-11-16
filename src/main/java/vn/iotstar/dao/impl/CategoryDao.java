package vn.iotstar.dao.impl;

import jakarta.persistence.*;
import java.util.List;
import java.util.function.Function;
import vn.iotstar.config.JpaConfig;
import vn.iotstar.dao.ICategoryDao;
import vn.iotstar.entity.Category;

public class CategoryDao implements ICategoryDao {
    private <T> T read(Function<EntityManager, T> action) {
        EntityManager em = JpaConfig.getEntityManager();
        try { return action.apply(em); } finally { em.close(); }
    }
    private void write(java.util.function.Consumer<EntityManager> action) {
        EntityManager em = JpaConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try { tx.begin(); action.accept(em); tx.commit(); }
        catch (RuntimeException ex) { if (tx.isActive()) tx.rollback(); throw ex; }
        finally { em.close(); }
    }
    @Override public void insert(Category c) { write(em -> em.persist(c)); }
    @Override public void update(Category c) {
        write(em -> {
            if (em.find(Category.class, c.getCategoryid()) == null)
                throw new IllegalArgumentException("Không tìm thấy danh mục");
            em.merge(c);
        });
    }
    @Override public void delete(int id) {
        write(em -> {
            Category c = em.find(Category.class, id);
            if (c == null) throw new IllegalArgumentException("Không tìm thấy danh mục");
            long videos = em.createQuery("SELECT COUNT(v) FROM Video v WHERE v.category.categoryid = :id", Long.class)
                .setParameter("id", id).getSingleResult();
            if (videos > 0) throw new IllegalArgumentException("Danh mục đang chứa video, hãy chuyển hoặc xóa video trước");
            em.remove(c);
        });
    }
    @Override public Category findById(int id) { return read(em -> em.find(Category.class, id)); }
    @Override public Category findByCategoryname(String name) {
        return read(em -> em.createQuery("SELECT c FROM Category c WHERE LOWER(c.categoryname) = LOWER(:name)", Category.class)
            .setParameter("name", name).setMaxResults(1).getResultStream().findFirst().orElse(null));
    }
    @Override public List<Category> findAll() {
        return read(em -> em.createNamedQuery("Category.findAll", Category.class).getResultList());
    }
    @Override public List<Category> findAll(int page, int size) {
        checkPage(page, size);
        return read(em -> em.createNamedQuery("Category.findAll", Category.class)
            .setFirstResult(page * size).setMaxResults(size).getResultList());
    }
    private static String pattern(String name) {
        return "%" + name.toLowerCase(java.util.Locale.ROOT).replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
    }
    private static void checkPage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("Phân trang không hợp lệ");
    }
    @Override public List<Category> searchByName(String name) {
        return read(em -> em.createQuery("SELECT c FROM Category c WHERE LOWER(c.categoryname) LIKE :name ESCAPE '!' ORDER BY c.categoryid DESC", Category.class)
            .setParameter("name", pattern(name)).getResultList());
    }
    @Override public List<Category> searchByName(String name, int page, int size) {
        checkPage(page, size);
        return read(em -> em.createQuery("SELECT c FROM Category c WHERE LOWER(c.categoryname) LIKE :name ESCAPE '!' ORDER BY c.categoryid DESC", Category.class)
            .setParameter("name", pattern(name)).setFirstResult(page * size).setMaxResults(size).getResultList());
    }
    @Override public int countByStatus(int status) {
        return read(em -> em.createQuery("SELECT COUNT(c) FROM Category c WHERE c.status = :status", Long.class)
            .setParameter("status", status).getSingleResult().intValue());
    }
    @Override public int count() { return read(em -> em.createQuery("SELECT COUNT(c) FROM Category c", Long.class).getSingleResult().intValue()); }
    @Override public int count(String name) {
        return read(em -> em.createQuery("SELECT COUNT(c) FROM Category c WHERE LOWER(c.categoryname) LIKE :name ESCAPE '!'", Long.class)
            .setParameter("name", pattern(name)).getSingleResult().intValue());
    }
}

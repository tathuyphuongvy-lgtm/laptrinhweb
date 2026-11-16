package vn.iotstar.config;
import jakarta.persistence.*;
import vn.iotstar.entity.*;
import java.util.UUID;
/** Test tay tương ứng tài liệu cấu hình: persist Category và Video trong cùng transaction. */
public class JpaSmokeTest {
    public static void main(String[] args) {
        EntityManager em = JpaConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            Category c = new Category(); c.setCategoryname("JPA Test " + UUID.randomUUID().toString().substring(0, 8)); c.setStatus(1);
            Video v = new Video(); v.setVideoId("test-" + UUID.randomUUID()); v.setTitle("Test quan hệ JPA"); c.addVideo(v);
            tx.begin(); em.persist(c); em.persist(v); tx.commit();
            em.clear();
            Video loaded = em.find(Video.class, v.getVideoId());
            if (loaded == null || loaded.getCategory().getCategoryid() != c.getCategoryid()) throw new IllegalStateException("Sai quan hệ JPA");
            System.out.println("JPA TEST OK: Category #" + c.getCategoryid() + " và Video " + v.getVideoId());
            tx.begin(); em.remove(loaded); em.remove(em.find(Category.class, c.getCategoryid())); tx.commit();
            System.out.println("Đã dọn dữ liệu kiểm thử.");
        } catch (RuntimeException e) { if (tx.isActive()) tx.rollback(); throw e; }
        finally { em.close(); JpaConfig.close(); }
    }
}

package vn.iotstar;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import jakarta.persistence.*;
import vn.iotstar.config.JpaConfig;
import vn.iotstar.dao.impl.CategoryDao;
import vn.iotstar.entity.*;
import vn.iotstar.service.impl.CategoryServiceImpl;

class JpaIntegrationTest {
    final CategoryDao dao = new CategoryDao();
    final CategoryServiceImpl service = new CategoryServiceImpl();
    @BeforeEach void clean() {
        EntityManager em = JpaConfig.getEntityManager();
        try { em.getTransaction().begin(); em.createQuery("DELETE FROM Video").executeUpdate(); em.createQuery("DELETE FROM Category").executeUpdate(); em.getTransaction().commit(); }
        finally { em.close(); }
    }
    Category insert(String name) {
        Category c = new Category(); c.setCategoryname(name); c.setStatus(1); service.insert(c); return c;
    }
    @Test void crudPersistsAcrossEntityManagers() {
        Category c = insert("Áo thun"); assertTrue(c.getCategoryid() > 0);
        assertEquals("Áo thun", dao.findById(c.getCategoryid()).getCategoryname());
        c.setCategoryname("Áo sơ mi"); c.setStatus(0); c.setImages("https://example.com/a.png"); service.update(c);
        Category reloaded = dao.findById(c.getCategoryid()); assertEquals("Áo sơ mi", reloaded.getCategoryname()); assertEquals(0, reloaded.getStatus());
        assertEquals("https://example.com/a.png", reloaded.getImages());
        service.delete(c.getCategoryid()); assertNull(dao.findById(c.getCategoryid()));
    }
    @Test void searchPaginationAndCount() {
        for (int i=0;i<7;i++) insert("Áo sơ mi " + i);
        insert("Quần");
        assertEquals(8, dao.count()); assertEquals(7, dao.count("áo sơ mi"));
        assertEquals(7, dao.searchByName("ÁO SƠ MI").size());
        assertEquals(5, dao.findAll(0, 5).size()); assertEquals(3, dao.findAll(1, 5).size());
        assertEquals(2, dao.searchByName("áo sơ mi", 1, 5).size());
        assertNotEquals(dao.findAll(0, 5).get(0).getCategoryid(), dao.findAll(1, 5).get(0).getCategoryid());
    }
    @Test void nameLookupAndDuplicates() {
        Category c=insert("  Áo sơ mi  "); assertEquals("Áo sơ mi", c.getCategoryname());
        assertEquals(c.getCategoryid(), dao.findByCategoryname("áo sơ mi").getCategoryid());
        assertNull(dao.findByCategoryname("Không tồn tại"));
        assertThrows(IllegalArgumentException.class, () -> insert("ÁO SƠ MI"));
        service.update(c); assertEquals(1, dao.count());
    }
    @Test void literalWildcardsAreEscaped() {
        insert("100% phụ kiện"); insert("Quần_A"); insert("QuầnXA");
        assertEquals(1, dao.searchByName("%").size()); assertEquals(1, dao.searchByName("_").size());
    }
    @Test void blankNameAndInvalidStatusRejected() {
        assertThrows(IllegalArgumentException.class, () -> insert("   "));
        Category c = new Category(); c.setCategoryname("Test"); c.setStatus(9);
        assertThrows(IllegalArgumentException.class, () -> service.insert(c)); assertEquals(0, dao.count());
    }
    @Test void failedTransactionRollsBackAndDaoStillWorks() {
        Category invalid = new Category(); invalid.setCategoryname(" ");
        assertThrows(RuntimeException.class, () -> dao.insert(invalid)); assertEquals(0, dao.count());
        insert("Valid"); assertEquals(1, dao.count());
    }
    @Test void categoryVideoRelationshipAndForeignKey() {
        EntityManager em=JpaConfig.getEntityManager(); int id;
        try {
            Category c=new Category(); c.setCategoryname("Video category");
            Video v=new Video(); v.setVideoId("v01"); v.setTitle("Test JPA"); c.addVideo(v);
            em.getTransaction().begin(); em.persist(c); em.persist(v); em.getTransaction().commit(); id=c.getCategoryid(); em.clear();
            assertEquals(id, em.find(Video.class,"v01").getCategory().getCategoryid());
            assertEquals(1, em.find(Category.class,id).getVideos().size());
        } finally { em.close(); }
        assertThrows(IllegalArgumentException.class, () -> service.delete(id)); assertNotNull(dao.findById(id));
    }
    @Test void statusCountsComeFromDatabase() {
        insert("Active 1"); insert("Active 2");
        Category locked = insert("Locked"); locked.setStatus(0); service.update(locked);
        assertEquals(3, service.count()); assertEquals(2, service.countByStatus(1)); assertEquals(1, service.countByStatus(0));
    }
    @Test void nonexistentUpdateAndDeleteRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.delete(Integer.MAX_VALUE));
        Category c=new Category(); c.setCategoryid(Integer.MAX_VALUE); c.setCategoryname("Missing");
        assertThrows(IllegalArgumentException.class, () -> service.update(c)); assertEquals(0, dao.count());
    }
}

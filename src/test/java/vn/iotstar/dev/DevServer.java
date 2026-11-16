package vn.iotstar.dev;
import java.nio.file.*;
import org.apache.catalina.*;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.*;

/** Tomcat nhúng chỉ dùng khi phát triển/test, không được đóng vào WAR. */
public class DevServer {
    public static Tomcat start(int port, Path base) throws Exception {
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(Files.createDirectories(base).toAbsolutePath().toString());
        tomcat.setPort(port); tomcat.getConnector();
        Context ctx = tomcat.addWebapp("/jpa-category-v2", Path.of("src/main/webapp").toAbsolutePath().toString());
        ctx.setParentClassLoader(DevServer.class.getClassLoader());
        org.apache.catalina.loader.WebappLoader loader = new org.apache.catalina.loader.WebappLoader();
        loader.setDelegate(true); ctx.setLoader(loader);
        StandardRoot resources = new StandardRoot(ctx);
        resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes", Path.of("target/classes").toAbsolutePath().toString(), "/"));
        ctx.setResources(resources);
        tomcat.start();
        if (!ctx.getState().isAvailable()) throw new IllegalStateException("Ứng dụng không khởi động được, xem log bên trên");
        return tomcat;
    }
    public static void main(String[] args) throws Exception {
        if (System.getProperty("app.db") == null && System.getenv("APP_DB") == null) System.setProperty("app.db", "h2");
        Tomcat server = start(Integer.getInteger("http.port", 8080), Path.of("target/tomcat-dev"));
        if ("h2".equals(System.getProperty("app.db")) && Boolean.getBoolean("demo.seed")) {
            var service = new vn.iotstar.service.impl.CategoryServiceImpl();
            if (service.count() == 0) {
                String[] names = {"Áo thun", "Áo sơ mi", "Áo khoác", "Phụ kiện", "Quần dài", "Váy đầm"};
                for (int i = 0; i < names.length; i++) {
                    var c = new vn.iotstar.entity.Category(); c.setCategoryname(names[i]); c.setStatus(i == 3 ? 0 : 1); service.insert(c);
                }
            }
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> { try { server.stop(); server.destroy(); } catch (Exception ignored) { } }));
        System.out.println("Mở http://localhost:" + server.getConnector().getLocalPort() + "/jpa-category-v2/admin/categories");
        server.getServer().await();
    }
}

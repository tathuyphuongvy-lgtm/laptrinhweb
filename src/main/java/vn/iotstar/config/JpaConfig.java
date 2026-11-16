package vn.iotstar.config;

import jakarta.persistence.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Một factory cho ứng dụng; mỗi thao tác DAO dùng một EntityManager riêng. */
public final class JpaConfig {
    private static EntityManagerFactory factory;
    private JpaConfig() { }
    public static synchronized EntityManagerFactory getFactory() {
        if (factory == null || !factory.isOpen()) {
            Properties local = new Properties();
            Path path = Path.of(System.getProperty("db.config", "config/db.properties"));
            if (Files.isRegularFile(path)) {
                try (Reader in = Files.newBufferedReader(path)) { local.load(in); }
                catch (IOException e) { throw new IllegalStateException("Không đọc được cấu hình DB", e); }
            }
            Map<String, Object> props = new HashMap<>();
            if ("h2".equals(setting(local, "app.db", "APP_DB", "mysql"))) {
                Properties h2 = local.getProperty("db.url", "").startsWith("jdbc:h2:") ? local : new Properties();
                props.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");
                props.put("jakarta.persistence.jdbc.url", setting(h2, "db.url", "DB_URL", "jdbc:h2:file:./data/fashion_jpa;MODE=MySQL"));
                props.put("jakarta.persistence.jdbc.user", setting(h2, "db.user", "DB_USER", "sa"));
                props.put("jakarta.persistence.jdbc.password", setting(h2, "db.password", "DB_PASSWORD", ""));
                props.put("hibernate.hbm2ddl.auto", setting(h2, "db.schema", "DB_SCHEMA", "update"));
            } else {
                copy(props, local, "db.url", "DB_URL", "jakarta.persistence.jdbc.url");
                copy(props, local, "db.user", "DB_USER", "jakarta.persistence.jdbc.user");
                copy(props, local, "db.password", "DB_PASSWORD", "jakarta.persistence.jdbc.password");
                props.put("hibernate.hbm2ddl.auto", setting(local, "db.schema", "DB_SCHEMA", "update"));
            }
            factory = Persistence.createEntityManagerFactory("jpa-hibernate-mysql", props);
        }
        return factory;
    }
    private static String setting(Properties p, String key, String env, String fallback) {
        String s = System.getProperty(key);
        if (s == null) s = System.getenv(env);
        if (s == null) s = p.getProperty(key);
        return s == null ? fallback : s;
    }
    private static void copy(Map<String, Object> out, Properties p, String key, String env, String target) {
        String value = setting(p, key, env, null);
        if (value != null) out.put(target, value);
    }
    public static EntityManager getEntityManager() { return getFactory().createEntityManager(); }
    public static synchronized void close() {
        if (factory != null && factory.isOpen()) factory.close();
        factory = null;
    }
}

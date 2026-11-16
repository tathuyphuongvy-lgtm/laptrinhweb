package vn.iotstar.config;
import jakarta.servlet.*;
public class AppListener implements ServletContextListener {
    @Override public void contextInitialized(ServletContextEvent e) { JpaConfig.getFactory(); }
    @Override public void contextDestroyed(ServletContextEvent e) { JpaConfig.close(); }
}

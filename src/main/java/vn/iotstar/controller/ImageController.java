package vn.iotstar.controller;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.file.*;
import vn.iotstar.web.ImageStorage;
public class ImageController extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = req.getParameter("fname");
        if (!ImageStorage.isLocal(name)) { resp.sendError(404); return; }
        Path file = ImageStorage.directory().resolve(name);
        if (!Files.isRegularFile(file)) { resp.sendError(404); return; }
        String ext = name.substring(name.lastIndexOf('.') + 1);
        resp.setContentType("image/" + (ext.equals("jpg") ? "jpeg" : ext));
        resp.setContentLengthLong(Files.size(file));
        Files.copy(file, resp.getOutputStream());
    }
}

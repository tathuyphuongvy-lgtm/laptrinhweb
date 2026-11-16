package vn.iotstar.web;
import jakarta.servlet.http.Part;
import java.io.*;
import java.nio.file.*;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/** Tên file do server sinh; chỉ nhận PNG/JPEG/GIF/WebP, giới hạn 2 MB. */
public final class ImageStorage {
    private static final Pattern LOCAL = Pattern.compile("[a-f0-9-]{36}[.](png|jpg|gif|webp)");
    private ImageStorage() { }
    public static Path directory() throws IOException {
        String dir = System.getProperty("upload.dir");
        if (dir == null || dir.isBlank()) dir = System.getenv("UPLOAD_DIR");
        if (dir == null || dir.isBlank()) dir = Path.of(System.getProperty("user.home"), "jpa-category-v2-uploads").toString();
        return Files.createDirectories(Path.of(dir));
    }
    public static boolean isLocal(String name) { return name != null && LOCAL.matcher(name).matches(); }
    public static String save(Part part) throws IOException {
        if (part.getSize() > 2 * 1024 * 1024) throw new IllegalArgumentException("Ảnh tối đa 2 MB");
        byte[] bytes;
        try (InputStream in = part.getInputStream()) { bytes = in.readNBytes(2 * 1024 * 1024 + 1); }
        if (bytes.length > 2 * 1024 * 1024) throw new IllegalArgumentException("Ảnh tối đa 2 MB");
        String ext;
        if (bytes.length > 8 && bytes[0] == (byte)0x89 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G') ext = "png";
        else if (bytes.length > 3 && bytes[0] == (byte)0xff && bytes[1] == (byte)0xd8 && bytes[2] == (byte)0xff) ext = "jpg";
        else if (bytes.length > 6 && new String(bytes, 0, 6, java.nio.charset.StandardCharsets.US_ASCII).matches("GIF8[79]a")) ext = "gif";
        else if (bytes.length > 12 && new String(bytes, 0, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("RIFF") && new String(bytes, 8, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("WEBP")) ext = "webp";
        else throw new IllegalArgumentException("Chỉ nhận ảnh PNG, JPG, GIF hoặc WebP");
        if (!ext.equals("webp") && ImageIO.read(new ByteArrayInputStream(bytes)) == null)
            throw new IllegalArgumentException("Nội dung ảnh không hợp lệ");
        String name = UUID.randomUUID() + "." + ext;
        Files.write(directory().resolve(name), bytes, StandardOpenOption.CREATE_NEW);
        return name;
    }
    public static void delete(String name) throws IOException {
        if (isLocal(name)) Files.deleteIfExists(directory().resolve(name));
    }
    public static String validUrl(String value) {
        if (value == null || value.isBlank()) return null;
        String url = value.trim();
        try {
            java.net.URI uri = java.net.URI.create(url);
            if (url.length() > 500 || !("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme())) || uri.getHost() == null)
                throw new IllegalArgumentException();
            return uri.getScheme().toLowerCase(java.util.Locale.ROOT) + url.substring(url.indexOf(':'));
        } catch (IllegalArgumentException e) { throw new IllegalArgumentException("Link ảnh phải bắt đầu bằng http:// hoặc https:// và tối đa 500 ký tự"); }
    }
}

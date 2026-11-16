package vn.iotstar;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.*;
import java.io.*;
import org.apache.catalina.startup.Tomcat;
import vn.iotstar.dev.DevServer;
import vn.iotstar.config.JpaConfig;
import vn.iotstar.dao.impl.CategoryDao;
import vn.iotstar.dao.impl.UserDao;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.User;
import vn.iotstar.util.PasswordUtil;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WebIntegrationTest {
    Tomcat server; HttpClient client; String base; Path images; String csrf;
    final CategoryDao dao=new CategoryDao();
    @BeforeAll void start() throws Exception {
        images=Files.createTempDirectory("jpa-images-"); System.setProperty("upload.dir", images.toString());
        server=DevServer.start(0, Files.createTempDirectory("jpa-tomcat-"));
        base="http://localhost:"+server.getConnector().getLocalPort()+"/jpa-category-v2";
        client = newClient();
        User account = new User();
        account.setUsername("category-tester");
        account.setEmail("category-tester@example.test");
        account.setPassword(PasswordUtil.hashPassword("category-test-password"));
        account.setActive(true);
        new UserDao().insert(account);
        assertRedirect(post("/login", Map.of("csrf", csrf(get("/login")), "username", account.getUsername(),
                "password", "category-test-password")), "/admin/categories");
        csrf = csrf(get("/admin/category/add"));
    }
    @AfterAll void stop() throws Exception { if(server!=null){server.stop();server.destroy();} System.clearProperty("upload.dir"); JpaConfig.close(); }
    @BeforeEach void clean() throws Exception {
        var em=JpaConfig.getEntityManager();
        try {em.getTransaction().begin();em.createQuery("DELETE FROM Video").executeUpdate();em.createQuery("DELETE FROM Category").executeUpdate();em.getTransaction().commit();}finally{em.close();}
        try(var paths=Files.list(images)){for(Path p:paths.toList())Files.delete(p);}
    }
    private static HttpClient newClient() {
        return HttpClient.newBuilder().cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL)).build();
    }
    HttpResponse<String> get(String path) throws Exception { return get(client, path); }
    HttpResponse<String> get(HttpClient browser, String path) throws Exception {
        return browser.send(HttpRequest.newBuilder(URI.create(base + path)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }
    HttpResponse<String> post(String path, Map<String,String> fields) throws Exception {
        return post(client, path, fields);
    }
    HttpResponse<String> post(HttpClient browser, String path, Map<String,String> fields) throws Exception {
        String body=fields.entrySet().stream().map(e->URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)+"="+URLEncoder.encode(e.getValue(),StandardCharsets.UTF_8)).collect(java.util.stream.Collectors.joining("&"));
        return browser.send(HttpRequest.newBuilder(URI.create(base+path)).header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
    }
    HttpResponse<String> multipart(String path, Map<String,String> fields, byte[] file) throws Exception {
        String boundary="----JpaBoundary"+UUID.randomUUID();
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        for(var e:fields.entrySet())out.write(("--"+boundary+"\r\nContent-Disposition: form-data; name=\""+e.getKey()+"\"\r\n\r\n"+e.getValue()+"\r\n").getBytes(StandardCharsets.UTF_8));
        if(file!=null){out.write(("--"+boundary+"\r\nContent-Disposition: form-data; name=\"images1\"; filename=\"test.png\"\r\nContent-Type: image/png\r\n\r\n").getBytes(StandardCharsets.UTF_8));out.write(file);out.write("\r\n".getBytes());}
        out.write(("--"+boundary+"--\r\n").getBytes());
        return client.send(HttpRequest.newBuilder(URI.create(base+path)).header("Content-Type","multipart/form-data; boundary="+boundary).POST(HttpRequest.BodyPublishers.ofByteArray(out.toByteArray())).build(),HttpResponse.BodyHandlers.ofString());
    }
    Map<String,String> fields(String name){Map<String,String> f=new HashMap<>();f.put("csrf",csrf);f.put("categoryname",name);f.put("status","1");return f;}
    byte[] png(){return Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jRZkAAAAASUVORK5CYII=");}
    @Test void jspPagesAndUtf8Crud() throws Exception {
        var response=multipart("/admin/category/insert",fields("Áo thun"),null); assertEquals(302,response.statusCode(),response.body());
        Category c=dao.findAll().get(0); assertEquals("Áo thun",c.getCategoryname());
        var page=get("/admin/categories");assertEquals(200,page.statusCode());assertTrue(page.body().contains("Áo thun"));assertTrue(page.body().contains("Danh mục quần áo"));assertTrue(page.body().contains("MÂY Fashion"));
        var edit=get("/admin/category/edit?id="+c.getCategoryid());assertEquals(200,edit.statusCode(),edit.body());
        var f=fields("Phụ kiện");f.put("categoryid",""+c.getCategoryid());f.put("status","0");
        assertEquals(302,multipart("/admin/category/update",f,null).statusCode());assertEquals(0,dao.findById(c.getCategoryid()).getStatus());
        assertEquals(302,post("/admin/category/delete",Map.of("csrf",csrf,"categoryid",""+c.getCategoryid())).statusCode());assertEquals(0,dao.count());
    }
    @Test void uploadServeRetainReplaceAndDeleteImage() throws Exception {
        var result=multipart("/admin/category/insert",fields("Ảnh"),png());assertEquals(302,result.statusCode(),result.body());
        Category c=dao.findAll().get(0);String image=c.getImages();assertTrue(Files.exists(images.resolve(image)));
        HttpResponse<byte[]> served=client.send(HttpRequest.newBuilder(URI.create(base+"/image?fname="+image)).GET().build(),HttpResponse.BodyHandlers.ofByteArray());
        assertEquals(200,served.statusCode());assertArrayEquals(png(),served.body());
        var f=fields("Ảnh mới");f.put("categoryid",""+c.getCategoryid());
        assertEquals(302,multipart("/admin/category/update",f,null).statusCode());assertEquals(image,dao.findById(c.getCategoryid()).getImages());
        f.put("images","https://example.com/image.png");assertEquals(302,multipart("/admin/category/update",f,null).statusCode());assertFalse(Files.exists(images.resolve(image)));
        assertEquals(302,multipart("/admin/category/update",f,png()).statusCode());String lastImage=dao.findById(c.getCategoryid()).getImages();
        assertEquals(302,post("/admin/category/delete",Map.of("csrf",csrf,"categoryid",""+c.getCategoryid())).statusCode());assertFalse(Files.exists(images.resolve(lastImage)));
    }
    @Test void invalidAndDuplicateRequestsDoNotLeaveFiles() throws Exception {
        assertEquals(302,multipart("/admin/category/insert",fields("Áo sơ mi"),null).statusCode());
        assertEquals(400,multipart("/admin/category/insert",fields("Áo sơ mi"),png()).statusCode());
        try(var paths=Files.list(images)){assertEquals(0,paths.count());}
        assertEquals(400,multipart("/admin/category/insert",fields("Fake"),"not an image".getBytes()).statusCode());assertEquals(1,dao.count());
        var f=fields("Bad URL");f.put("images","javascript:alert(1)");assertEquals(400,multipart("/admin/category/insert",f,null).statusCode());
        assertEquals(400,multipart("/admin/category/insert",fields("Large"),new byte[2097153]).statusCode());assertEquals(1,dao.count());
    }
    @Test void utf8PagesAndLocalVietnameseFonts() throws Exception {
        var page = get("/admin/category/add");
        assertEquals(200, page.statusCode());
        assertTrue(page.headers().firstValue("content-type").orElse("").toLowerCase().contains("charset=utf-8"));
        assertTrue(page.body().contains("Thông tin danh mục"));
        assertTrue(page.body().contains("Ảnh đại diện"));
        assertTrue(page.body().contains("MÂY Fashion"));
        assertFalse(page.body().contains("\uFFFD"));
        var css = get("/assets/style.css?v=fontfix2");
        assertEquals(200, css.statusCode()); assertTrue(css.body().contains("@font-face"));
        for (String weight : List.of("Regular", "Bold")) {
            var font = client.send(HttpRequest.newBuilder(URI.create(base + "/assets/fonts/NotoSans-" + weight + ".ttf")).GET().build(), HttpResponse.BodyHandlers.ofByteArray());
            assertEquals(200, font.statusCode()); assertTrue(font.body().length > 10000);
            assertEquals(0, font.body()[0]); assertEquals(1, font.body()[1]);
        }
    }
    @Test void csrfAndIdsAndGetDelete() throws Exception {
        assertEquals(403,post("/admin/category/delete",Map.of("categoryid","1")).statusCode());
        assertEquals(400,get("/admin/category/edit?id=abc").statusCode());assertEquals(404,get("/admin/category/edit?id=999999").statusCode());
        assertEquals(405,get("/admin/category/delete?id=1").statusCode());assertEquals(404,get("/image?fname=../pom.xml").statusCode());
    }
    @Test void listEscapesHtmlAndSupportsSearchPagination() throws Exception {
        for(int i=0;i<7;i++)assertEquals(302,multipart("/admin/category/insert",fields("Áo sơ mi "+i),null).statusCode());
        assertEquals(302,multipart("/admin/category/insert",fields("<script>alert(1)</script>"),null).statusCode());
        var html=get("/admin/categories").body();assertTrue(html.contains("&lt;script&gt;"));assertFalse(html.contains("<script>alert(1)</script>"));
        assertTrue(get("/admin/categories?q=" + URLEncoder.encode("Áo sơ mi", StandardCharsets.UTF_8) + "&page=2").body().contains("Trang 2 / 2"));
        assertTrue(get("/admin/categories?q=KhongCo").body().contains("Chưa có danh mục phù hợp"));
    }

    @Test void accountActivationLoginLogoutAndPasswordResetWithSmtp() throws Exception {
        HttpClient browser = newClient();
        String email = "otp-recipient@example.test";
        String username = "otp-tester";
        String originalPassword = "initial-test-password";
        String newPassword = "new-test-password";
        UserDao users = new UserDao();

        try (TestMailbox mailbox = new TestMailbox()) {
            Map<String, String> smtp = Map.of("smtp.host", "127.0.0.1", "smtp.port", "" + mailbox.port(),
                    "smtp.auth", "false", "smtp.starttls", "false", "smtp.from", "fashion@example.test");
            Map<String, String> previous = new HashMap<>();
            smtp.forEach((key, value) -> previous.put(key, System.setProperty(key, value)));
            try {
                assertRedirect(get(browser, "/admin/categories"), "/login");
                String token = csrf(get(browser, "/register"));
                assertRedirect(post(browser, "/register", Map.of("csrf", token, "username", username,
                        "email", email, "fullname", "Người dùng thử", "password", originalPassword)), "/verify-otp");
                User pending = users.findByUsername(username);
                assertFalse(pending.isActive());
                assertTrue(PasswordUtil.checkPassword(originalPassword, pending.getPassword()));
                assertFalse(originalPassword.equals(pending.getPassword()));
                String expiredCode = mailbox.readOtp(email, "Kích hoạt tài khoản");

                assertTrue(login(browser, username, originalPassword).body().contains("Tài khoản chưa được kích hoạt"));
                var response = post(browser, "/forgot-password", Map.of("csrf", token, "action", "sendOtp", "email", email));
                assertEquals(200, response.statusCode());
                assertTrue(mailbox.messages.isEmpty());
                expireOtp(pending.getId());
                assertTrue(post(browser, "/verify-otp", Map.of("csrf", token, "otp", expiredCode))
                        .body().contains("Mã OTP không hợp lệ"));
                assertFalse(users.findByUsername(username).isActive());
                assertTrue(post(browser, "/verify-otp", Map.of("csrf", token, "action", "resend"))
                        .body().contains("Đã gửi mã kích hoạt mới"));
                String activationCode = mailbox.readOtp(email, "Kích hoạt tài khoản");
                assertTrue(post(browser, "/verify-otp", Map.of("csrf", token, "otp", wrongCode(activationCode)))
                        .body().contains("Mã OTP không hợp lệ"));
                assertTrue(post(browser, "/verify-otp", Map.of("csrf", token, "otp", activationCode))
                        .body().contains("Kích hoạt tài khoản thành công"));
                assertTrue(users.findByUsername(username).isActive());
                assertNull(users.findByUsername(username).getOtpCode());

                assertTrue(login(browser, username, "wrong-password").body().contains("Sai tên đăng nhập"));
                assertRedirect(login(browser, username, originalPassword), "/admin/categories");
                var categories = get(browser, "/admin/categories");
                assertEquals(200, categories.statusCode());
                assertTrue(categories.body().contains("Đăng xuất"));
                assertEquals(405, get(browser, "/logout").statusCode());
                assertRedirect(post(browser, "/logout", Map.of("csrf", csrf(categories))), "/login");
                assertRedirect(get(browser, "/admin/categories"), "/login");

                token = csrf(get(browser, "/forgot-password"));
                var reset = post(browser, "/forgot-password", Map.of("csrf", token, "action", "sendOtp", "email", email));
                assertTrue(reset.body().contains("name=\"newPassword\""));
                String resetCode = mailbox.readOtp(email, "Khôi phục mật khẩu");
                assertTrue(post(browser, "/forgot-password", Map.of("csrf", token, "action", "resetPassword",
                        "otp", wrongCode(resetCode), "newPassword", newPassword)).body().contains("Mã OTP không hợp lệ"));
                Map<String, String> resetFields = Map.of("csrf", token, "action", "resetPassword", "otp", resetCode,
                        "newPassword", newPassword);
                assertTrue(post(browser, "/forgot-password", resetFields).body().contains("Đặt lại mật khẩu thành công"));
                assertTrue(post(browser, "/forgot-password", resetFields).body().contains("Mã OTP không hợp lệ"));
                assertTrue(login(browser, username, originalPassword).body().contains("Sai tên đăng nhập"));
                assertRedirect(login(browser, username, newPassword), "/admin/categories");

                System.setProperty("smtp.from", "");
                HttpClient retryBrowser = newClient();
                String retryEmail = "no-smtp@example.test";
                String retryToken = csrf(get(retryBrowser, "/register"));
                assertRedirect(post(retryBrowser, "/register", Map.of("csrf", retryToken, "username", "retry-tester",
                        "email", retryEmail, "fullname", "Người thử gửi lại", "password", originalPassword)), "/verify-otp");
                assertTrue(get(retryBrowser, "/verify-otp").body().contains("Không gửi được email OTP"));
                assertTrue(mailbox.messages.isEmpty());
                User retryAccount = users.findByEmail(retryEmail);
                assertFalse(retryAccount.isActive());
                expireOtp(retryAccount.getId());
                System.setProperty("smtp.from", smtp.get("smtp.from"));
                assertTrue(post(retryBrowser, "/verify-otp", Map.of("csrf", retryToken, "action", "resend"))
                        .body().contains("Đã gửi mã kích hoạt mới"));
                mailbox.readOtp(retryEmail, "Kích hoạt tài khoản");
            } finally {
                previous.forEach((key, value) -> {
                    if (value == null) System.clearProperty(key);
                    else System.setProperty(key, value);
                });
            }
        }
    }

    private HttpResponse<String> login(HttpClient browser, String username, String password) throws Exception {
        String token = csrf(get(browser, "/login"));
        return post(browser, "/login", Map.of("csrf", token, "username", username, "password", password));
    }

    private String csrf(HttpResponse<String> page) {
        assertEquals(200, page.statusCode(), page.body());
        Matcher matcher = Pattern.compile("name=\"csrf\" value=\"([^\"]+)\"").matcher(page.body());
        assertTrue(matcher.find(), "Expected a CSRF field");
        return matcher.group(1);
    }

    private void assertRedirect(HttpResponse<String> response, String destination) {
        assertEquals(302, response.statusCode(), response.body());
        String location = response.headers().firstValue("Location").orElseThrow();
        assertEquals("/jpa-category-v2" + destination, URI.create(location).getPath());
    }

    private void expireOtp(int userId) {
        var em = JpaConfig.getEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("update User u set u.otpExpiry = :expiry where u.id = :id")
                    .setParameter("expiry", LocalDateTime.now().minusMinutes(1))
                    .setParameter("id", userId).executeUpdate();
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    private String wrongCode(String code) {
        return code.equals("000000") ? "000001" : "000000";
    }

    private static final class TestMailbox implements AutoCloseable {
        private final ServerSocket server = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"));
        private final LinkedBlockingQueue<byte[]> messages = new LinkedBlockingQueue<>();
        private final Thread worker = new Thread(this::receive, "test-smtp");

        TestMailbox() throws IOException {
            worker.setDaemon(true);
            worker.start();
        }

        int port() { return server.getLocalPort(); }

        String readOtp(String email, String subject) throws Exception {
            byte[] raw = messages.poll(5, TimeUnit.SECONDS);
            assertNotNull(raw, "Expected an OTP message from local SMTP");
            MimeMessage message = new MimeMessage(Session.getInstance(new Properties()), new ByteArrayInputStream(raw));
            assertEquals(email, message.getRecipients(Message.RecipientType.TO)[0].toString());
            assertTrue(message.getSubject().startsWith(subject));
            Matcher code = Pattern.compile("Mã OTP: ([0-9]{6})").matcher(message.getContent().toString());
            assertTrue(code.find(), "Expected a 6-digit OTP in the email body");
            return code.group(1);
        }

        private void receive() {
            while (!server.isClosed()) {
                try (Socket connection = server.accept()) {
                    connection.setSoTimeout(5000);
                    var in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.ISO_8859_1));
                    var out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.US_ASCII));
                    reply(out, "220 localhost");
                    String line;
                    while ((line = in.readLine()) != null) {
                        if (line.equals("QUIT")) {
                            reply(out, "221 Bye");
                            break;
                        }
                        if (line.equals("DATA")) {
                            reply(out, "354 End message with a dot");
                            StringBuilder data = new StringBuilder();
                            while ((line = in.readLine()) != null && !line.equals(".")) {
                                data.append(line).append("\r\n");
                            }
                            messages.add(data.toString().getBytes(StandardCharsets.ISO_8859_1));
                        }
                        reply(out, "250 OK");
                    }
                } catch (IOException e) {
                    if (!server.isClosed()) throw new UncheckedIOException(e);
                }
            }
        }

        private void reply(BufferedWriter out, String line) throws IOException {
            out.write(line + "\r\n");
            out.flush();
        }

        @Override public void close() throws Exception {
            server.close();
            worker.join(5000);
        }
    }
}

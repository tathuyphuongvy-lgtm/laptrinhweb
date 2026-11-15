# Bài tập 01 ngày 09/11/2026 — CRUD Category với JPA


Project Java Web thực hiện CRUD Category và cấu hình/test JPA theo `link1.txt`, `link2.txt` của bài tập. Chủ đề cửa hàng quần áo **MÂY Fashion**: giao diện màu kem và nâu, minh họa trang phục, lưới bộ sưu tập, thống kê và form kèm ảnh xem trước.

## Chủ đề

Ứng dụng quản lý danh mục của cửa hàng quần áo theo yêu cầu CRUD JPA. Minh họa trang phục là SVG cục bộ, không cần tải ảnh từ bên ngoài. Chưa có giỏ hàng, thanh toán hoặc quản lý sản phẩm vì các chức năng này nằm ngoài hai tài liệu bài tập. Project không gắn tên hoặc MSSV của người dùng.

## Chức năng

- Thêm, xem danh sách, sửa, xóa Category.
- Tên, ảnh từ URL hoặc upload, trạng thái Hoạt động/Khóa.
- Kiểm tra tên rỗng/trùng, trạng thái, file ảnh và giới hạn 2 MB.
- Tìm kiếm tên, phân trang 5 mục, thống kê tổng/đang hoạt động/đang khóa từ database.
- Xem trước tên, trạng thái và ảnh khi nhập form; xem trước chưa lưu vào database.
- Quan hệ Category–Video bằng `@OneToMany` và `@ManyToOne`; chặn xóa danh mục có video.
- Test cấu hình JPA, transaction, rollback, đọc lại dữ liệu và quan hệ.

## Yêu cầu và kiến trúc

JDK **21**, Maven Wrapper 3.9.9, Tomcat **11**, MySQL **8.x**. Chạy demo dùng H2 và Tomcat nhúng nên không cần cài riêng MySQL/Tomcat. Lần đầu cần Internet để Maven tải thư viện.

Luồng xử lý: **Servlet/JSP → Service → DAO → JPA/Hibernate → Database**.

Hibernate ORM 6.6.59.Final / Jakarta Persistence 3.1, Servlet 6.1 và JSTL 3 được chọn thành bộ tương thích. Tài liệu mẫu trộn MySQL/SQL Server và có một số tên chưa đồng nhất (`category`/`categories`, `catename`/`categoryname`); project dùng MySQL, thống nhất tên thuộc tính, đóng EntityManager và rollback khi lỗi. Không dùng Spring Data JPA để thay thế DAO trong bài.

Phần giao diện chỉ quản lý Category; Video dùng để cấu hình quan hệ và kiểm thử theo tài liệu. Hai tài liệu được cung cấp không có yêu cầu login/register. Nếu bài 01 trên lớp có yêu cầu riêng khác, cần đối chiếu thêm.

## Font chữ và tiếng Việt

Font Noto Sans hỗ trợ tiếng Việt được đóng gói trong `assets/fonts`, tải trực tiếp từ ứng dụng, không cần Google Fonts hay cài font trên máy. Biểu tượng giao diện dùng SVG; các trang JSP và request/response dùng UTF-8. License font kèm trong thư mục fonts.

Khi thay bản cũ: dừng terminal bằng Ctrl+C, giải nén bản sửa vào thư mục mới rồi chạy lại. Trình duyệt nhấn Ctrl+F5. Nếu deploy WAR, dừng Tomcat, thay WAR và thư mục deploy cũ của ứng dụng để tránh dùng JSP đã cache. Không xóa database hoặc thư mục ảnh upload.

## Chạy nhanh trên Windows

1. Giải nén toàn bộ gói, mở thư mục **jpa-category-v2** có `pom.xml`.
2. Nhấp đúp **CHAY_DEMO.bat**. Script tự chuyển về đúng thư mục, không cần gõ đường dẫn từ `C:\Users\PC`.
3. Đợi dòng `Mở http://localhost:8080/...`.
4. Mở **http://localhost:8080/jpa-category-v2/admin/categories**.
5. Giữ terminal mở. Dừng bằng **Ctrl+C**.

Demo dùng H2 file `data/fashion_jpa`, tự tạo 6 danh mục quần áo nếu database trống: Áo thun, Áo sơ mi, Áo khoác, Phụ kiện, Quần dài và Váy đầm. Tên, ảnh và trạng thái có thể sửa/xóa bình thường; dữ liệu được giữ giữa các lần chạy. Nếu xóa hết danh mục, lần chạy demo tiếp theo sẽ tạo lại dữ liệu mẫu. Không chạy bài cũ đồng thời trên cổng 8080.

Trong VS Code: **File → Open Folder → jpa-category-v2 → Terminal → New Terminal**, chạy:

```powershell
.\CHAY_DEMO.bat
```

Lệnh tương đương:

```powershell
.\mvnw.cmd -U test-compile exec:java "-Dexec.mainClass=vn.iotstar.dev.DevServer" "-Dexec.classpathScope=test" "-Dapp.db=h2" "-Ddemo.seed=true"
```

Nếu 8080 bận, dừng ứng dụng cũ hoặc thêm `"-Dhttp.port=8081"` vào lệnh tương đương, rồi mở URL cổng 8081. Linux/macOS thay `.\mvnw.cmd` bằng `./mvnw` và chạy `chmod +x mvnw` nếu cần.

## Chạy MySQL để trình bày bài

1. MySQL Workbench: chạy `database/01_create_database.sql`. Database riêng **fashion_jpa**, không sử dụng database bài cũ.
2. Copy `config/db.properties.example` thành `config/db.properties`.
3. Sửa `db.user`, `db.password` và `db.url` cho đúng máy của bạn.
4. Chạy **CHAY_MYSQL.bat**. Nếu thiếu file cấu hình, script sẽ tạo bản mẫu và yêu cầu bạn sửa trước khi chạy lại.
5. Mở **http://localhost:8080/jpa-category-v2/admin/categories**.

Hibernate tự tạo bảng `categories`, `videos`, unique constraint và foreign key. `hibernate.hbm2ddl.auto=update` giữ dữ liệu hiện có. Sau lần khởi động đầu tiên, có thể chạy `database/02_sample_data.sql` để thêm dữ liệu minh họa.

Mật khẩu thật chỉ ở `config/db.properties`, không commit Git. Thứ tự cấu hình: Java system properties → biến môi trường → `config/db.properties` → mặc định persistence.xml. Thư mục upload mặc định `jpa-category-v2-uploads` trong home của người chạy Java; có thể đặt biến `UPLOAD_DIR` hoặc `-Dupload.dir`.

### Deploy WAR lên Tomcat ngoài

- Bản đã build: `dist/jpa-category-v2.war`. Hoặc build lại bằng `.\mvnw.cmd clean package` và dùng `target/jpa-category-v2.war`.
- Dùng JDK 21 + Tomcat 11. Copy `config/setenv.bat.example` vào `Tomcat/bin/setenv.bat`, sửa `JAVA_HOME` đúng thư mục JDK, `DB_USER`, `DB_PASSWORD` và URL DB.
- Tomcat ngoài không tự tìm file cấu hình trong thư mục source; dùng setenv như trên, hoặc trỏ đường dẫn tuyệt đối bằng `-Ddb.config` trong `CATALINA_OPTS`.
- Dừng Tomcat, copy WAR vào `Tomcat/webapps`, chạy `Tomcat/bin/catalina.bat run`.
- Mở URL `/jpa-category-v2/admin/categories`. Nếu deploy lại, dừng Tomcat và xóa WAR cùng thư mục deploy `webapps/jpa-category-v2` cũ trước khi copy bản mới. Ảnh upload được lưu ngoài thư mục deploy.

## Cấu hình và test JPA

File `src/main/resources/META-INF/persistence.xml` khai báo persistence unit `jpa-hibernate-mysql`. `JpaConfig` dùng một EntityManagerFactory cho ứng dụng; DAO cấp và đóng một EntityManager cho mỗi thao tác.

Chạy test tự động không cần MySQL:

```powershell
.\mvnw.cmd clean verify
```

Hoặc nhấp đúp `KIEM_TRA_BAI.bat`. Các test dùng H2 in-memory riêng, không xóa dữ liệu demo hay MySQL. Báo cáo trong `target/surefire-reports`.

Test MySQL theo tài liệu sau khi tạo/sửa `config/db.properties`:

```powershell
.\mvnw.cmd compile exec:java "-Dexec.mainClass=vn.iotstar.config.JpaSmokeTest"
```

Chương trình persist Category + Video trong một transaction, xóa persistence context, đọc lại quan hệ, sau đó dọn dữ liệu test. Thành công khi có dòng `JPA TEST OK` và `Đã dọn dữ liệu kiểm thử`.

Trước khi nộp, kiểm tra thêm/sửa/xóa, tìm tên, tạo trên 5 mục để thử phân trang, đổi ảnh và trạng thái. Thử tên rỗng/trùng, file không phải ảnh và ảnh >2 MB. Trong Workbench kiểm tra `USE fashion_jpa; SHOW TABLES; SELECT * FROM categories; SELECT * FROM videos;`. Chụp minh chứng nếu giảng viên yêu cầu.

## Đối chiếu yêu cầu

| Yêu cầu trong tài liệu | File triển khai |
| --- | --- |
| Cấu hình và test JPA | `config/JpaConfig.java`, `config/JpaSmokeTest.java`, `META-INF/persistence.xml` |
| Category, Video, quan hệ JPA | `entity/Category.java`, `entity/Video.java` |
| DAO + interface | `dao/ICategoryDao.java`, `dao/impl/CategoryDao.java` |
| Service + interface | `service/ICategoryService.java`, `service/impl/CategoryServiceImpl.java` |
| Controller nhận request | `controller/CategoryController.java` |
| List/add/edit bằng JSP | `src/main/webapp/WEB-INF/views/admin/` |
| Upload, đọc và dọn ảnh | `web/ImageStorage.java`, `controller/ImageController.java` |
| Giao diện mới | `assets/style.css`, `assets/app.js`, các JSP/JSPF |
| Kiểm thử | `src/test/java/vn/iotstar/` |

Các đường dẫn Java ở bảng trên tương đối với `src/main/java/vn/iotstar/`.

## Đưa source lên GitHub và nộp

Xem **HUONG_DAN_GITHUB.md**. Gói này đã có source và WAR; chưa upload GitHub hoặc nộp UTExLMS thay bạn. Không đưa `target`, `dist`, `data`, mật khẩu thật hoặc chỉ một file ZIP lên repository. `.gitignore` đã bỏ qua các mục cần thiết khi dùng Git.

## Khi có lỗi

| Lỗi | Xử lý |
| --- | --- |
| `JAVA_HOME`, `javac` | Cài JDK 21 đầy đủ và kiểm tra `.\mvnw.cmd -v` |
| Không tìm thấy đường dẫn script | Nhấp đúp `CHAY_DEMO.bat` từ thư mục đã giải nén, không chạy bên trong ZIP |
| Maven `Connect timed out` | Kiểm tra mạng, chạy lại lệnh có `-U`; thử mạng khác nếu cần. `-U` không sửa được mạng mất kết nối |
| `Access denied for user` | Kiểm tra tài khoản MySQL, sửa cấu hình rồi khởi động lại |
| `Unknown database` | Chạy `01_create_database.sql` |
| 404 | Kiểm tra đúng context `/jpa-category-v2`, Tomcat đã start thành công |
| 403 khi gửi form | Reload form để lấy session/token mới |
| Ảnh link không hiển thị | Dùng URL ảnh trực tiếp và nguồn cho phép nhúng; thử upload từ máy |

Ứng dụng dành cho thực hành cục bộ, chưa có đăng nhập quản trị. H2 test không thay thế kiểm chứng trực tiếp trên MySQL.

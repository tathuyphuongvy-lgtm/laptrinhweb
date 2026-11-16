<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!doctype html>
<html lang="vi">
<head>
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Đăng nhập | MÂY Fashion</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
</head>
<body>
<main class="account">
    <h1>Đăng nhập</h1>
    <%@ include file="feedback.jspf" %>
    <form action="${pageContext.request.contextPath}/login" method="post">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
        <label>Tên đăng nhập <input type="text" name="username" autocomplete="username" required></label>
        <label>Mật khẩu <input type="password" name="password" autocomplete="current-password" required></label>
        <button type="submit">Đăng nhập</button>
    </form>
    <div class="account-links">
        <a href="${pageContext.request.contextPath}/register">Đăng ký tài khoản</a>
        <a href="${pageContext.request.contextPath}/forgot-password">Quên mật khẩu?</a>
    </div>
</main>
</body>
</html>

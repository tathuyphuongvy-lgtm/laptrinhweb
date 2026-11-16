<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!doctype html>
<html lang="vi">
<head>
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Đăng ký | MÂY Fashion</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
</head>
<body>
<main class="account">
    <h1>Đăng ký tài khoản</h1>
    <%@ include file="feedback.jspf" %>
    <form action="${pageContext.request.contextPath}/register" method="post">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
        <label>Tên đăng nhập <input type="text" name="username" maxlength="255" autocomplete="username" required></label>
        <label>Email <input type="email" name="email" maxlength="255" autocomplete="email" required></label>
        <label>Mật khẩu <input type="password" name="password" autocomplete="new-password" required></label>
        <label>Họ và tên <input type="text" name="fullname" maxlength="255" autocomplete="name" required></label>
        <button type="submit">Đăng ký</button>
    </form>
    <div class="account-links"><a href="${pageContext.request.contextPath}/login">Đăng nhập</a></div>
</main>
</body>
</html>

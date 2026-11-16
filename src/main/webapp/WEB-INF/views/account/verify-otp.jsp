<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!doctype html>
<html lang="vi">
<head>
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Kích hoạt tài khoản | MÂY Fashion</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
</head>
<body>
<main class="account">
    <h1>Kích hoạt tài khoản</h1>
    <%@ include file="feedback.jspf" %>
    <p>Nhập mã OTP trong email gửi đến <strong><c:out value="${sessionScope.pendingEmail}"/></strong>. Mã có hiệu lực 5 phút.</p>
    <form action="${pageContext.request.contextPath}/verify-otp" method="post">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
        <label>Mã OTP <input type="text" name="otp" minlength="6" maxlength="6" pattern="[0-9]{6}" inputmode="numeric" autocomplete="one-time-code" required></label>
        <button type="submit">Xác nhận</button>
    </form>
    <form action="${pageContext.request.contextPath}/verify-otp" method="post">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
        <button class="secondary" type="submit" name="action" value="resend">Gửi lại mã</button>
    </form>
    <div class="account-links"><a href="${pageContext.request.contextPath}/login">Quay lại đăng nhập</a></div>
</main>
</body>
</html>
